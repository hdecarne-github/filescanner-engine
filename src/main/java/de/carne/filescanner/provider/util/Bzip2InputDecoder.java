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
package de.carne.filescanner.provider.util;

import java.io.IOException;

import de.carne.boot.check.Check;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.nio.compression.bzip2.Bzip2Decoder;
import de.carne.nio.compression.bzip2.Bzip2DecoderProperties;
import de.carne.nio.compression.bzip2.Bzip2Factory;
import de.carne.nio.compression.spi.Decoder;

/**
 * {@linkplain Bzip2Decoder} based {@linkplain InputDecoder}.
 */
public class Bzip2InputDecoder extends InputDecoder {

	private static final Bzip2Factory FACTORY = new Bzip2Factory();

	private final Bzip2DecoderProperties properties;

	/**
	 * Constructs a new {@linkplain Bzip2InputDecoder} instance.
	 *
	 * @param properties the decoder properties to use.
	 */
	public Bzip2InputDecoder(Bzip2DecoderProperties properties) {
		super(FACTORY.compressionName());
		this.properties = properties;
	}

	/**
	 * Gets the default decoder properties.
	 *
	 * @return the default decoder properties.
	 */
	public static Bzip2DecoderProperties defaultProperties() {
		return Check.isInstanceOf(FACTORY.defaultDecoderProperties(), Bzip2DecoderProperties.class);
	}

	@Override
	public Decoder newDecoder() throws IOException {
		return FACTORY.newDecoder(this.properties);
	}

}
