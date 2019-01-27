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

import java.util.function.Supplier;

import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Configuration parameters for {@linkplain EncodedInputSpec}.
 */
public final class EncodedInputSpecConfig {

	private Supplier<String> encodedInputNameHolder;
	private Supplier<Long> encodedInputSizeHolder = FinalSupplier.of(Long.valueOf(-1l));
	private Supplier<InputDecoder> inputDecoderHolder = FinalSupplier
			.of(InputDecoder.unsupportedInputDecoder("<undefined>"));
	private Supplier<String> decodedInputNameHolder = FinalSupplier.of("<undefined>");

	/**
	 * Constructs a new {@linkplain EncodedInputSpecConfig} instance.
	 *
	 * @param encodedInputName the name of the encoded input data stream.
	 */
	public EncodedInputSpecConfig(String encodedInputName) {
		this(FinalSupplier.of(encodedInputName));
	}

	/**
	 * Constructs a new {@linkplain EncodedInputSpecConfig} instance.
	 *
	 * @param encodedInputName the name of the encoded input data stream.
	 */
	public EncodedInputSpecConfig(Supplier<String> encodedInputName) {
		this.encodedInputNameHolder = encodedInputName;
	}

	/**
	 * Gets the name of the encoded input data stream.
	 *
	 * @return the name of the encoded input data stream.
	 */
	public Supplier<String> encodedInputName() {
		return this.encodedInputNameHolder;
	}

	/**
	 * Sets the size of the encoded input data stream.
	 * <p>
	 * If not set the input decoder is responsible for detecting the end of the input data stream.
	 * </p>
	 *
	 * @param encodedInputSize the size of the encoded input data stream.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig encodedInputSize(long encodedInputSize) {
		return encodedInputSize(FinalSupplier.of(Long.valueOf(encodedInputSize)));
	}

	/**
	 * Sets the size of the encoded input data stream.
	 * <p>
	 * If not set the input decoder is responsible for detecting the end of the input data stream.
	 * </p>
	 *
	 * @param encodedInputSize the size of the encoded input data stream.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig encodedInputSize(Supplier<Long> encodedInputSize) {
		this.encodedInputSizeHolder = encodedInputSize;
		return this;
	}

	/**
	 * Gets the size of the encoded input data stream.
	 * <p>
	 * May be {@code -1} indicating that the input decoder is responsible for detecting the end of the input data
	 * stream.
	 * </p>
	 *
	 * @return the size of the encoded input data stream.
	 */
	public Supplier<Long> encodedInputSize() {
		return this.encodedInputSizeHolder;
	}

	/**
	 * Sets the {@linkplain InputDecoder} to use for decoding the encoded input data stream.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig inputDecoder(InputDecoder inputDecoder) {
		return inputDecoder(FinalSupplier.of(inputDecoder));
	}

	/**
	 * Sets the {@linkplain InputDecoder} to use for decoding the encoded input data stream.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig inputDecoder(Supplier<InputDecoder> inputDecoder) {
		this.inputDecoderHolder = inputDecoder;
		return this;
	}

	/**
	 * Gets the {@linkplain InputDecoder} to use for decoding the encoded input data stream.
	 * 
	 * @return the {@linkplain InputDecoder} to use for decoding the encoded input data stream.
	 */
	public Supplier<InputDecoder> inputDecoder() {
		return this.inputDecoderHolder;
	}

	/**
	 * Sets the name of the decoded data stream.
	 *
	 * @param decodedInputName the name of the decoded data stream.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig decodedInputName(String decodedInputName) {
		return decodedInputName(FinalSupplier.of(decodedInputName));
	}

	/**
	 * Sets the name of the decoded data stream.
	 *
	 * @param decodedInputName the name of the decoded data stream.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig decodedInputName(Supplier<String> decodedInputName) {
		this.decodedInputNameHolder = decodedInputName;
		return this;
	}

	/**
	 * Gets the name of the decoded data stream.
	 *
	 * @return the name of the decoded data stream.
	 */
	public Supplier<String> decodedInputName() {
		return this.decodedInputNameHolder;
	}

}
