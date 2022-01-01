/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.filescanner.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import de.carne.filescanner.engine.format.AttributeBindMode;
import de.carne.filescanner.engine.format.AttributeSpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.input.InputDecodeCache;
import de.carne.filescanner.engine.util.HexFormat;
import de.carne.util.Check;
import de.carne.util.Strings;
import de.carne.util.logging.Log;

/**
 * Input data processor class used during result decoding.
 */
public class FileScannerResultDecodeContext extends FileScannerResultInputContext {

	private static final Log LOG = new Log();

	private final FileScanner fileScanner;
	private final LinkedList<Scope> decodeStack = new LinkedList<>();
	private final List<FileScannerResultBuilder> pendingInputResults = new LinkedList<>();

	FileScannerResultDecodeContext(FileScanner fileScanner, FileScannerResultBuilder parent,
			FileScannerInputRange inputRange, long position) {
		super(inputRange, position);
		this.fileScanner = fileScanner;
		this.decodeStack.push(new Scope(parent));
	}

	/**
	 * Decodes a {@linkplain CompositeSpec}.
	 *
	 * @param formatSpec a {@linkplain CompositeSpec} to decode.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 */
	public FileScannerResult decodeComposite(CompositeSpec formatSpec) throws IOException {
		return decodeComposite(formatSpec, 0, false);
	}

	/**
	 * Decodes a relocated {@linkplain CompositeSpec}.
	 *
	 * @param formatSpec the {@linkplain CompositeSpec} to decode.
	 * @param decodePosition the position to start decoding at.
	 * @param decodeLevel the level to put the decode result.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 */
	public FileScannerResult decodeComposite(CompositeSpec formatSpec, long decodePosition, int decodeLevel)
			throws IOException {
		long currentPosition = position();
		FileScannerResult decodeResult;

		LOG.debug("Decode relocated at {0}:{1}...", HexFormat.formatLong(decodePosition), decodeLevel);

		setPosition(decodePosition);
		try {
			decodeResult = decodeComposite(formatSpec, decodeLevel,
					decodePosition != currentPosition || decodeLevel != 0);
		} finally {
			setPosition(currentPosition);
		}
		return decodeResult;
	}

	private FileScannerResult decodeComposite(CompositeSpec formatSpec, int decodeLevel, boolean relocated)
			throws IOException {
		LOG.debug("Decoding composite spec {0}...", formatSpec);

		boolean isRootSpec = this.decodeStack.size() == 1;
		boolean isResultSpec = formatSpec.isResult();

		if (isRootSpec && !isResultSpec) {
			throw new IllegalArgumentException("Root format spec must be a result spec");
		}

		byteOrder(formatSpec.byteOrder());

		FileScannerResultBuilder decodeResult = Objects.requireNonNull(this.decodeStack.peek()).builder();

		if (isResultSpec && !(formatSpec instanceof EncodedInputSpec)) {
			long position = position();
			FileScannerResultBuilder formatSpecResult = FileScannerResultBuilder.formatResult(
					getDecodeParent(decodeResult, decodeLevel), formatSpec, relocated, inputRange(), position);

			this.decodeStack.push(new Scope(formatSpecResult));
			if (isRootSpec) {
				bindResultValue(formatSpec, FileScannerResultContextValueSpecs.FORMAT_POSITION, position);
			}
			bindContextValue(FileScannerResultContextValueSpecs.RESULT_POSITION, position);
			try {
				runV(() -> {
					formatSpec.decodeComposite(this);
					formatSpecResult.resolveExportHandlers(formatSpec.exportHandlers());
					if (!isRootSpec) {
						formatSpecResult.updateAndCommit(position(), false);
					}
				});
				if (isRootSpec) {
					commit(formatSpecResult);
				}
			} finally {
				decodeResult = this.decodeStack.pop().builder();
			}
		} else {
			formatSpec.decodeComposite(this);
		}
		return decodeResult;
	}

	private FileScannerResultBuilder getDecodeParent(FileScannerResultBuilder builder, int decodeLevel) {
		FileScannerResultBuilder decodeParent = builder;
		int remaining = decodeLevel;

		while (remaining > 0) {
			decodeParent = decodeParent.parent();
			remaining--;
		}
		return decodeParent;
	}

	/**
	 * Decodes an {@linkplain EncodedInputSpec}.
	 *
	 * @param encodedInputSpec the {@linkplain EncodedInputSpec} to decode.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 */
	public FileScannerResult decodeEncodedInputs(EncodedInputSpec encodedInputSpec) throws IOException {
		LOG.debug("Decoding encoded input spec {0}...", encodedInputSpec);

		long decodeStart = position();
		FileScannerResultBuilder decodeResult = FileScannerResultBuilder.encodedInputResult(
				Objects.requireNonNull(this.decodeStack.peek()).builder(), encodedInputSpec, inputRange(), decodeStart,
				decodeStart);
		InputDecodeCache.DecodeResult decoded = this.fileScanner.decodeInputs(
				encodedInputSpec.decodedInputMapper().get(), encodedInputSpec.inputDecoderTable().get(), inputRange(),
				decodeStart);
		long commitPosition = decodeStart + decoded.encodedSize();

		setPosition(commitPosition);

		for (FileScannerInput decodedInput : decoded.decodedInputs()) {
			if (decodedInput.size() > 0) {
				FileScannerResultBuilder decodedInputResult = Objects.requireNonNull(
						FileScannerResultBuilder.inputResult(decodeResult, decodedInput).updateAndCommit(-1, false));

				this.pendingInputResults.add(decodedInputResult);
			}
		}
		decodeResult.updateAndCommit(commitPosition, false);
		return decodeResult;
	}

	/**
	 * Binds an attribute value in {@linkplain AttributeBindMode#CONTEXT} mode.
	 *
	 * @param <T> the actual value type.
	 * @param valueSpec the attribute to bind to.
	 * @param value the attribute value to bind.
	 * @return the bound attribute value.
	 */
	public <T> T bindContextValue(AttributeSpec<T> valueSpec, @NonNull T value) {
		LOG.debug("Binding context attribute {0} = ''{1}''", valueSpec, Strings.encode(Objects.toString(value)));

		Objects.requireNonNull(this.decodeStack.peek()).contextValues().put(valueSpec, value);
		return value;
	}

	/**
	 * Binds an attribute value in {@linkplain AttributeBindMode#RESULT} mode.
	 *
	 * @param <T> the actual value type.
	 * @param scope the bind scope to use.
	 * @param valueSpec the attribute to bind to.
	 * @param value the attribute value to bind.
	 * @return the bound attribute value.
	 */
	public <T> T bindResultValue(CompositeSpec scope, AttributeSpec<T> valueSpec, @NonNull T value) {
		LOG.debug("Binding result attribute {0}:{1} = ''{2}''", scope, valueSpec,
				Strings.encode(Objects.toString(value)));

		Objects.requireNonNull(this.decodeStack.peek()).builder().bindResultValue(scope, valueSpec, value);
		return value;
	}

	/**
	 * Binds a decoded value to a given context position.
	 *
	 * @param <T> the actual value type.
	 * @param valueSpec the decode result to bind to.
	 * @param value the decoded value to bind.
	 * @return the bound attribute value.
	 */
	public <T> T bindDecodedValue(FileScannerResultContextValueSpec<T> valueSpec, @NonNull T value) {
		LOG.debug("Binding decoded value {0} = ''{1}''", valueSpec, Strings.encode(Objects.toString(value)));

		Objects.requireNonNull(this.decodeStack.peek()).builder().bindDecodedValue(valueSpec, value);
		return value;
	}

	@Override
	public <T> T getValue(FileScannerResultContextValueSpec<T> valueSpec) {
		LOG.debug("Resolving context value {0}", valueSpec);

		Scope result = Objects.requireNonNull(this.decodeStack.peek());
		Object contextValue = result.contextValues().get(valueSpec);
		T value = (contextValue != null ? Check.isInstanceOf(contextValue, valueSpec.type())
				: result.builder().getValue(valueSpec, false));

		LOG.debug("Resolved context value {0} = ''{1}''", valueSpec, Strings.encode(Objects.toString(value)));

		return value;
	}

	/**
	 * Commits the already decoded {@linkplain FileScannerResult} instances and makes them visible to the
	 * {@linkplain FileScanner} user.
	 */
	public void commit() {
		commit(Objects.requireNonNull(this.decodeStack.peek()).builder());
	}

	private void commit(FileScannerResultBuilder result) {
		FileScannerResultBuilder commitResult = result.updateAndCommit(position(), true);

		if (commitResult != null) {
			this.fileScanner.onScanResultCommit(commitResult);
			this.fileScanner.queueInputResults(this.pendingInputResults);
			this.pendingInputResults.clear();
		}
	}

	private static final class Scope {

		private final FileScannerResultBuilder builder;
		private final Map<Object, Object> contextValues = new HashMap<>();

		Scope(FileScannerResultBuilder builder) {
			this.builder = builder;
		}

		public FileScannerResultBuilder builder() {
			return this.builder;
		}

		public Map<Object, Object> contextValues() {
			return this.contextValues;
		}

		@Override
		public String toString() {
			return this.builder.toString();
		}

	}

}
