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
package de.carne.filescanner.engine.util;

import java.io.IOException;

import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.nio.compression.CompressionInfos;
import de.carne.nio.compression.deflate.DeflateDecoder;
import de.carne.nio.compression.deflate.DeflateDecoderProperties;
import de.carne.nio.compression.deflate.DeflateFactory;
import de.carne.nio.compression.spi.Decoder;
import de.carne.util.Check;

/**
 * {@linkplain DeflateDecoder} based {@linkplain InputDecoder}.
 */
public class DeflateInputDecoder extends InputDecoder {

	private static final DeflateFactory FACTORY = new DeflateFactory();

	private final DeflateDecoderProperties properties;

	/**
	 * Constructs a new {@linkplain DeflateInputDecoder} instance.
	 */
	public DeflateInputDecoder() {
		this(defaultProperties());
	}

	/**
	 * Constructs a new {@linkplain DeflateInputDecoder} instance.
	 *
	 * @param properties the decoder properties to use.
	 */
	public DeflateInputDecoder(DeflateDecoderProperties properties) {
		super(FACTORY.compressionName());
		this.properties = properties;
	}

	/**
	 * Gets the default decoder properties.
	 *
	 * @return the default decoder properties.
	 */
	public static DeflateDecoderProperties defaultProperties() {
		return Check.isInstanceOf(FACTORY.defaultDecoderProperties(), DeflateDecoderProperties.class);
	}

	@Override
	public Decoder newDecoder() throws IOException {
		return FACTORY.newDecoder(this.properties);
	}

	@Override
	public CompressionInfos decoderProperties() {
		return this.properties;
	}

}
