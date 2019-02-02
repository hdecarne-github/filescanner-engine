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
package de.carne.filescanner.engine.format.spec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Encoded input data stream specification.
 * <p>
 * If the encoded input's size is unknown a decoder has to be defined and is responsible for determining the actual
 * size. If size and decoder are set, the former limits the available data for the decoder.
 */
public final class EncodedInputSpec implements FormatSpec {

	private final EncodedInputSpecConfig config;

	/**
	 * Constructs a new {@linkplain EncodedInputSpec} instance.
	 *
	 * @param encodedInputConfig the configuration to use for input decoding.
	 */
	public EncodedInputSpec(EncodedInputSpecConfig encodedInputConfig) {
		this.config = encodedInputConfig;
	}

	/**
	 * Gets the encoded input's name.
	 *
	 * @return the encoded input's name.
	 */
	public Supplier<String> encodedInputName() {
		return this.config.encodedInputName();
	}

	/**
	 * Gets the encoded input's size.
	 * <p>
	 * If the supplied value is {@code -1} the actual size is not known and has to be determined by the decoder.
	 *
	 * @return the encoded input's size.
	 */
	public Supplier<Long> encodedInputSize() {
		return this.config.encodedInputSize();
	}

	/**
	 * Gets the input decoder.
	 *
	 * @return the input decoder.
	 */
	public Supplier<InputDecoder> inputDecoder() {
		return this.config.inputDecoder();
	}

	/**
	 * Gets the decoded input's name.
	 *
	 * @return the decoded input's name.
	 */
	public Supplier<String> decodedInputName() {
		return this.config.decodedInputName();
	}

	@Override
	public boolean isFixedSize() {
		return false;
	}

	@Override
	public int matchSize() {
		return 0;
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return true;
	}

	@Override
	public void decode(FileScannerResultDecodeContext context) throws IOException {
		context.decodeEncodedInput(this);
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		// Nothing to do here
	}

}
