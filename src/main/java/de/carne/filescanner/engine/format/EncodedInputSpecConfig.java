/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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

import java.util.function.Supplier;

import de.carne.filescanner.engine.input.DecodedInputMapper;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Configuration parameters for an {@linkplain EncodedInputSpec}.
 */
public final class EncodedInputSpecConfig {

	private static final InputDecoder UNDEFINED_INPUT_DECODER_TABLE = InputDecoders
			.unsupportedInputDecoder("<undefined>");
	private static final DecodedInputMapper DEFAULT_INPUT_MAPPER = new DecodedInputMapper("decoded.bin");

	private Supplier<String> encodedInputNameHolder;
	private Supplier<InputDecoderTable> inputDecoderTableHolder = FinalSupplier
			.of(InputDecoderTable.build(UNDEFINED_INPUT_DECODER_TABLE));
	private Supplier<DecodedInputMapper> decodedInputMapperHolder = FinalSupplier.of(DEFAULT_INPUT_MAPPER);

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
	 * Sets the {@linkplain InputDecoderTable} to use for decoding the encoded input data stream.
	 *
	 * @param inputDecoderTable the {@linkplain InputDecoderTable} to use.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig inputDecoderTable(InputDecoderTable inputDecoderTable) {
		return inputDecoderTable(FinalSupplier.of(inputDecoderTable));
	}

	/**
	 * Sets the {@linkplain InputDecoderTable} to use for decoding the encoded input data stream.
	 *
	 * @param inputDecoderTable the {@linkplain InputDecoderTable} to use.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig inputDecoderTable(Supplier<InputDecoderTable> inputDecoderTable) {
		this.inputDecoderTableHolder = inputDecoderTable;
		return this;
	}

	/**
	 * Gets the {@linkplain InputDecoderTable} to use for decoding the encoded input data stream.
	 *
	 * @return the {@linkplain InputDecoderTable} to use for decoding the encoded input data stream.
	 */
	public Supplier<InputDecoderTable> inputDecoderTable() {
		return this.inputDecoderTableHolder;
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
		return decodedInputMapper(new DecodedInputMapper(decodedInputName));
	}

	/**
	 * Sets the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the decode result.
	 *
	 * @param decodedInputMapper the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the actual
	 * decode result.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig decodedInputMapper(DecodedInputMapper decodedInputMapper) {
		return decodedInputMapper(FinalSupplier.of(decodedInputMapper));
	}

	/**
	 * Sets the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the decode result.
	 *
	 * @param decodedInputMapper the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the actual
	 * decode result.
	 * @return the update configuration.
	 */
	public EncodedInputSpecConfig decodedInputMapper(Supplier<DecodedInputMapper> decodedInputMapper) {
		this.decodedInputMapperHolder = decodedInputMapper;
		return this;
	}

	/**
	 * Gets the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the decode result.
	 *
	 * @return the {@linkplain DecodedInputMapper} to use for mapping the decoded input to the decode result.
	 */
	public Supplier<DecodedInputMapper> decodedInputMapper() {
		return this.decodedInputMapperHolder;
	}

}
