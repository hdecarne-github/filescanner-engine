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
package de.carne.filescanner.engine.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.input.DecodedInputMapper;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Encoded input data stream specification.
 * <p>
 * If the encoded input's size is unknown a decoder has to be defined and is responsible for determining the actual
 * size. If size and decoder are set, the former limits the available data for the decoder.
 */
public final class EncodedInputSpec extends CompositeSpec {

	private final EncodedInputSpecConfig config;

	/**
	 * Constructs a new {@linkplain EncodedInputSpec} instance.
	 *
	 * @param encodedInputConfig the configuration to use for input decoding.
	 */
	public EncodedInputSpec(EncodedInputSpecConfig encodedInputConfig) {
		this.config = encodedInputConfig;
		result(this.config.encodedInputName());
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
	 * Gets the input decoder table.
	 *
	 * @return the input decoder table.
	 */
	public Supplier<InputDecoderTable> inputDecoderTable() {
		return this.config.inputDecoderTable();
	}

	/**
	 * Gets the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the decode result.
	 *
	 * @return the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the decode result.
	 */
	public Supplier<DecodedInputMapper> decodedInputMapper() {
		return this.config.decodedInputMapper();
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
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException {
		context.decodeEncodedInputs(this);
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		// Nothing to do here
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append('(').append(getClass().getTypeName()).append(')');
		return buffer.toString();
	}

}
