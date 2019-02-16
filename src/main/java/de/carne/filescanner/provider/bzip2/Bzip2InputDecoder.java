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
package de.carne.filescanner.provider.bzip2;

import java.io.IOException;

import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.nio.compression.bzip2.Bzip2Factory;
import de.carne.nio.compression.spi.Decoder;

class Bzip2InputDecoder extends InputDecoder {

	public static final Bzip2InputDecoder DECODER = new Bzip2InputDecoder();

	private static final String NAME = "BZIP2";

	private final Bzip2Factory factory = new Bzip2Factory();

	private Bzip2InputDecoder() {
		super(NAME);
	}

	@Override
	public Decoder newDecoder() throws IOException {
		return this.factory.newDecoder();
	}

}
