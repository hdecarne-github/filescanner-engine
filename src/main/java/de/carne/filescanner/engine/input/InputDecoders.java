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
package de.carne.filescanner.engine.input;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.carne.nio.compression.CompressionInfos;
import de.carne.nio.compression.CompressionProperty;
import de.carne.nio.compression.spi.Decoder;

/**
 * Utility class providing {@linkplain InputDecoder} related functions.
 */
public class InputDecoders {

	private InputDecoders() {
		// Prevent instantiation
	}

	/**
	 * The identity decoder instance used to indicate that no decoding is required.
	 */
	public static final InputDecoder IDENTITY = new NoInputDecoder("stored");

	/**
	 * The zero decoder instance used to indicate a range of zeros (for sparse file handling).
	 */
	public static final InputDecoder ZERO = new NoInputDecoder("sparse");

	/**
	 * Creates an {@linkplain InputDecoder} instance for an unsupported encoding format that fails with a corresponding
	 * error message.
	 *
	 * @param name the name of the unsupported encoding format.
	 * @return the created {@linkplain InputDecoder} instance.
	 */
	public static InputDecoder unsupportedInputDecoder(String name) {
		return new NoInputDecoder(name);
	}

	private static class NoInputDecoder extends InputDecoder {

		NoInputDecoder(String name) {
			super(name);
		}

		@Override
		public Decoder newDecoder() throws IOException {
			throw new IOException("Invaid or unsupported encoding format: " + name());
		}

		@Override
		public CompressionInfos decoderProperties() {
			return new CompressionInfos() {

				@Override
				public Iterator<CompressionProperty> iterator() {
					return Collections.emptyIterator();
				}

				@Override
				public Object getProperty(CompressionProperty property) {
					throw new NoSuchElementException();
				}

				@Override
				public byte getByteProperty(CompressionProperty property) {
					throw new NoSuchElementException();
				}

				@Override
				public int getIntProperty(CompressionProperty property) {
					throw new NoSuchElementException();
				}

				@Override
				public long getLongProperty(CompressionProperty property) {
					throw new NoSuchElementException();
				}

				@Override
				public boolean getBooleanProperty(CompressionProperty property) {
					throw new NoSuchElementException();
				}

				@Override
				public Enum<?> getEnumProperty(CompressionProperty property) {
					throw new NoSuchElementException();
				}

			};
		}
	}

}
