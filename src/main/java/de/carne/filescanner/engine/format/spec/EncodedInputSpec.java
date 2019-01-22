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
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Encoded input data stream specification.
 * <p>
 * If the encoded input's size is unknown a decoder has to be defined and is responsible for determining the actual
 * size. If size and decoder are set, the former limits the available data for the decoder.
 */
public final class EncodedInputSpec implements FormatSpec {

	private final Supplier<String> encodedInputName;
	private Supplier<Long> encodedInputSize = FinalSupplier.of(Long.valueOf(-1l));
	private Supplier<InputDecoder> inputDecoder = FinalSupplier.of(InputDecoder.NONE);
	private Supplier<String> decodedInputName = FinalSupplier.of("<decoded data>");

	/**
	 * Constructs a new {@linkplain EncodedInputSpec} instance.
	 *
	 * @param encodedInputName the encoded input's name.
	 */
	public EncodedInputSpec(Supplier<String> encodedInputName) {
		this.encodedInputName = encodedInputName;
	}

	/**
	 * Constructs a new {@linkplain EncodedInputSpec} instance.
	 *
	 * @param encodedInputName the encoded input's name.
	 */
	public EncodedInputSpec(String encodedInputName) {
		this(FinalSupplier.of(encodedInputName));
	}

	/**
	 * Gets the encoded input's name.
	 *
	 * @return the encoded input's name.
	 */
	public Supplier<String> encodedInputName() {
		return this.encodedInputName;
	}

	/**
	 * Sets the encoded input's size.
	 *
	 * @param size the size to set.
	 * @return the updated {@linkplain EncodedInputSpec} for chaining.
	 */
	public EncodedInputSpec encodedInputSize(Supplier<Long> size) {
		this.encodedInputSize = size;
		return this;
	}

	/**
	 * Sets the encoded input's size.
	 *
	 * @param size the size to set.
	 * @return the updated {@linkplain EncodedInputSpec} for chaining.
	 */
	public EncodedInputSpec encodedInputSize(Long size) {
		return encodedInputSize(FinalSupplier.of(size));
	}

	/**
	 * Gets the encoded input's size.
	 * <p>
	 * If the supplied value is {@code -1} the actual size is not known and has to be determined by the decoder.
	 *
	 * @return the encoded input's size.
	 */
	public Supplier<Long> encodedInputSize() {
		return this.encodedInputSize;
	}

	/**
	 * Sets the input decoder.
	 *
	 * @param decoder the decoder to set.
	 * @return the updated {@linkplain EncodedInputSpec} for chaining.
	 */
	public EncodedInputSpec inputDecoder(Supplier<InputDecoder> decoder) {
		this.inputDecoder = decoder;
		return this;
	}

	/**
	 * Sets the input decoder.
	 *
	 * @param decoder the decoder to set.
	 * @return the updated {@linkplain EncodedInputSpec} for chaining.
	 */
	public EncodedInputSpec inputDecoder(InputDecoder decoder) {
		this.inputDecoder = FinalSupplier.of(decoder);
		return this;
	}

	/**
	 * Gets the input decoder.
	 *
	 * @return the input decoder.
	 */
	public Supplier<InputDecoder> inputDecoder() {
		return this.inputDecoder;
	}

	/**
	 * Sets the decoded input's name.
	 *
	 * @param name the decoded input's name.
	 * @return the updated {@linkplain EncodedInputSpec} for chaining.
	 */
	public EncodedInputSpec decodedInputName(Supplier<String> name) {
		this.decodedInputName = name;
		return this;
	}

	/**
	 * Sets the decoded input's name.
	 *
	 * @param name the decoded input's name.
	 * @return the updated {@linkplain EncodedInputSpec} for chaining.
	 */
	public EncodedInputSpec decodedInputName(String name) {
		this.decodedInputName = FinalSupplier.of(name);
		return this;
	}

	/**
	 * Gets the decoded input's name.
	 *
	 * @return the decoded input's name.
	 */
	public Supplier<String> decodedInputName() {
		return this.decodedInputName;
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
