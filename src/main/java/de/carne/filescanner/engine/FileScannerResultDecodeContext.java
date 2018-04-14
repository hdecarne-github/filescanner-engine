/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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
import java.util.Map;

import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.AttributeBindMode;
import de.carne.filescanner.engine.format.AttributeSpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.input.InputDecodeCache;
import de.carne.util.Strings;

/**
 * Input data processor base class used during result decoding.
 */
public class FileScannerResultDecodeContext extends FileScannerResultInputContext {

	private static final Log LOG = new Log();

	private final FileScanner fileScanner;
	private final Deque<Scope> decodeStack = new LinkedList<>();

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
	 * @throws InterruptedException if the decode thread has been interrupted.
	 */
	public FileScannerResult decode(CompositeSpec formatSpec) throws IOException, InterruptedException {
		LOG.debug("Decoding composite spec ''{0}''...", formatSpec);

		boolean isResultSpec = formatSpec.isResult();

		if (this.decodeStack.size() == 1 && !isResultSpec) {
			throw new IllegalArgumentException("Root format spec must be a result spec");
		}

		FileScannerResultBuilder decodeResult = this.decodeStack.peekFirst().builder();

		if (isResultSpec) {
			decodeResult = FileScannerResultBuilder.formatResult(decodeResult, formatSpec, inputRange(), position());
			this.decodeStack.addFirst(new Scope(decodeResult));
			try {
				run(() -> formatSpec.decodeComposite(this));
				if (this.decodeStack.size() > 2) {
					decodeResult.updateAndCommit(position(), false);
				}
			} finally {
				this.decodeStack.pop();
			}
		} else {
			formatSpec.decodeComposite(this);
		}
		return decodeResult;
	}

	/**
	 * Decodes a {@linkplain CompositeSpec}.
	 *
	 * @param formatSpec the {@linkplain CompositeSpec} to decode.
	 * @param position the position to start decoding at.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread has been interrupted.
	 */
	public FileScannerResult decode(CompositeSpec formatSpec, long position) throws IOException, InterruptedException {
		setPosition(position);
		return decode(formatSpec);
	}

	/**
	 * Decodes an {@linkplain EncodedInputSpec}.
	 *
	 * @param encodedInputSpec the {@linkplain EncodedInputSpec} to decode.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread has been interrupted.
	 */
	public FileScannerResult decode(EncodedInputSpec encodedInputSpec) throws IOException, InterruptedException {
		LOG.debug("Decoding encoded input spec ''{0}''...", encodedInputSpec);

		long decodeStart = position();
		long encodedInputSize = encodedInputSpec.encodedInputSize().get();
		long decodeEnd;
		FileScannerResultBuilder decodeResult;

		if (encodedInputSize >= 0) {
			decodeEnd = decodeStart + encodedInputSize;
			decodeResult = FileScannerResultBuilder.encodedInputResult(this.decodeStack.peekFirst().builder(),
					encodedInputSpec, inputRange(), decodeStart, decodeEnd);
		} else {
			decodeEnd = inputRange().end();
			decodeResult = FileScannerResultBuilder.encodedInputResult(this.decodeStack.peekFirst().builder(),
					encodedInputSpec, inputRange(), decodeStart, decodeStart);
		}

		InputDecodeCache.Decoded decoded = this.fileScanner.decodeInput(encodedInputSpec.decodedInputName().get(),
				encodedInputSpec.inputDecoder().get(), inputRange(), decodeStart, decodeEnd);
		long commitPosition = decodeStart + decoded.encodedSize();

		setPosition(commitPosition);
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
	public <T> void bindContextValue(AttributeSpec<T> attribute, T value) {
		LOG.debug("Binding context attribute '':{0}'' = ''{1}''", attribute, Strings.decode(value.toString()));

		this.decodeStack.peekFirst().contextValues().put(attribute, value);
	}

	/**
	 * Binds an attribute value in {@linkplain AttributeBindMode#RESULT} mode.
	 *
	 * @param <T> the actual attribute type.
	 * @param scope the bind scope to use.
	 * @param attribute the attribute to bind.
	 * @param value the attribute value to bind.
	 */
	public <T> void bindResultValue(CompositeSpec scope, AttributeSpec<T> attribute, T value) {
		LOG.debug("Binding result attribute ''{0}:{1}'' = ''{2}''", scope, attribute, Strings.decode(value.toString()));

		this.decodeStack.peekFirst().builder().bindResultValue(scope, attribute, value);
	}

	@Override
	public <T> T getValue(AttributeSpec<T> attribute) {
		LOG.debug("Resolving attribute value ''{0}''", attribute);

		Scope result = this.decodeStack.peekFirst();
		Object contextValue = result.contextValues().get(attribute);
		T value = (contextValue != null ? Check.isInstanceOf(contextValue, attribute.type())
				: result.builder().getValue(attribute, false));

		LOG.debug("Resolved attribute value ''{0}'' = ''{1}''", attribute, Strings.decode(value.toString()));

		return value;
	}

	/**
	 * Commits the already decoded {@linkplain FileScannerResult} instances and makes them visible to the
	 * {@linkplain FileScanner} user.
	 */
	public void commit() {
		FileScannerResultBuilder commitResult = this.decodeStack.peekFirst().builder().updateAndCommit(position(),
				true);

		this.fileScanner.onScanResultCommit(commitResult);
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
