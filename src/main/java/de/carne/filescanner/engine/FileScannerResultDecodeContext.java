/*
 * Copyright (c) 2007-2019 Holger de Carne and contributors, All Rights Reserved.
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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.spec.AttributeBindMode;
import de.carne.filescanner.engine.format.spec.AttributeSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpec;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.input.InputDecodeCache;
import de.carne.util.Strings;

/**
 * Input data processor class used during result decoding.
 */
public class FileScannerResultDecodeContext extends FileScannerResultInputContext {

	private static final Log LOG = new Log();

	private final FileScanner fileScanner;
	private final Deque<Scope> decodeStack = new LinkedList<>();
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
		return decodeComposite(formatSpec, false);
	}

	/**
	 * Decodes a relocated {@linkplain CompositeSpec}.
	 *
	 * @param formatSpec the {@linkplain CompositeSpec} to decode.
	 * @param decodePosition the position to start decoding at.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 */
	public FileScannerResult decodeComposite(CompositeSpec formatSpec, long decodePosition) throws IOException {
		long currentPosition = position();
		FileScannerResult decodeResult;

		if (decodePosition != currentPosition) {
			LOG.debug("Decode relocated at {0}...", HexFormat.formatLong(decodePosition));

			setPosition(decodePosition);
			try {
				decodeResult = decodeComposite(formatSpec, true);
			} finally {
				setPosition(currentPosition);
			}
		} else {
			decodeResult = decodeComposite(formatSpec, false);
		}
		return decodeResult;
	}

	private FileScannerResult decodeComposite(CompositeSpec formatSpec, boolean relocated) throws IOException {
		LOG.debug("Decoding composite spec ''{0}''...", formatSpec);

		boolean isRootSpec = this.decodeStack.size() == 1;
		boolean isResultSpec = formatSpec.isResult();

		if (isRootSpec && !isResultSpec) {
			throw new IllegalArgumentException("Root format spec must be a result spec");
		}

		byteOrder(formatSpec.byteOrder());

		FileScannerResultBuilder decodeResult = this.decodeStack.peek().builder();

		if (isResultSpec) {
			FileScannerResultBuilder formatSpecResult = FileScannerResultBuilder.formatResult(decodeResult, formatSpec,
					relocated, inputRange(), position());

			this.decodeStack.push(new Scope(formatSpecResult));
			try {
				run(() -> {
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

	/**
	 * Decodes an {@linkplain EncodedInputSpec}.
	 *
	 * @param encodedInputSpec the {@linkplain EncodedInputSpec} to decode.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 */
	public FileScannerResult decodeEncodedInput(EncodedInputSpec encodedInputSpec) throws IOException {
		LOG.debug("Decoding encoded input spec ''{0}''...", encodedInputSpec);

		long decodeStart = position();
		long encodedInputSize = encodedInputSpec.encodedInputSize().get();
		long decodeEnd;
		FileScannerResultBuilder decodeResult;

		if (encodedInputSize >= 0) {
			decodeEnd = decodeStart + encodedInputSize;
			decodeResult = FileScannerResultBuilder.encodedInputResult(this.decodeStack.peek().builder(),
					encodedInputSpec, inputRange(), decodeStart, decodeEnd);
		} else {
			decodeEnd = inputRange().end();
			decodeResult = FileScannerResultBuilder.encodedInputResult(this.decodeStack.peek().builder(),
					encodedInputSpec, inputRange(), decodeStart, decodeStart);
		}

		InputDecodeCache.Decoded decoded = this.fileScanner.decodeInput(encodedInputSpec.decodedInputName().get(),
				encodedInputSpec.inputDecoder().get(), inputRange(), decodeStart, decodeEnd);
		long commitPosition = Math.max(encodedInputSize, decodeStart + decoded.encodedSize());

		setPosition(commitPosition);

		FileScannerInput decodedInput = decoded.decodedInput();

		if (decodedInput.size() > 0) {
			FileScannerResultBuilder decodedInputResult = Objects.requireNonNull(
					FileScannerResultBuilder.inputResult(decodeResult, decodedInput).updateAndCommit(-1, false));

			this.pendingInputResults.add(decodedInputResult);
		}
		decodeResult.updateAndCommit(commitPosition, false);
		return decodeResult;
	}

	/**
	 * Binds an attribute value in {@linkplain AttributeBindMode#CONTEXT} mode.
	 *
	 * @param <T> the actual attribute type.
	 * @param attribute the attribute to bind.
	 * @param value the attribute value to bind.
	 */
	public <T> void bindContextValue(AttributeSpec<T> attribute, @NonNull T value) {
		LOG.debug("Binding context attribute '':{0}'' = ''{1}''", attribute, Strings.encode(Objects.toString(value)));

		this.decodeStack.peek().contextValues().put(attribute, value);
	}

	/**
	 * Binds an attribute value in {@linkplain AttributeBindMode#RESULT} mode.
	 *
	 * @param <T> the actual attribute type.
	 * @param scope the bind scope to use.
	 * @param attribute the attribute to bind.
	 * @param value the attribute value to bind.
	 */
	public <T> void bindResultValue(CompositeSpec scope, AttributeSpec<T> attribute, @NonNull T value) {
		LOG.debug("Binding result attribute ''{0}:{1}'' = ''{2}''", scope, attribute,
				Strings.encode(Objects.toString(value)));

		this.decodeStack.peek().builder().bindResultValue(scope, attribute, value);
	}

	@Override
	public <T> T getValue(AttributeSpec<T> attribute) {
		LOG.debug("Resolving attribute value ''{0}''", attribute);

		Scope result = this.decodeStack.peek();
		Object contextValue = result.contextValues().get(attribute);
		T value = (contextValue != null ? Check.isInstanceOf(contextValue, attribute.type())
				: result.builder().getValue(attribute, false));

		LOG.debug("Resolved attribute value ''{0}'' = ''{1}''", attribute, Strings.encode(Objects.toString(value)));

		return value;
	}

	/**
	 * Commits the already decoded {@linkplain FileScannerResult} instances and makes them visible to the
	 * {@linkplain FileScanner} user.
	 */
	public void commit() {
		commit(this.decodeStack.peek().builder());
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
