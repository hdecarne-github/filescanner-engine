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

import de.carne.boot.check.Check;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.nio.compression.spi.Decoder;

/**
 * {@code InputDecoder} instances are used to decode nested input streams.
 */
public abstract class InputDecoder {

	/**
	 * The identity decoder instance used to indicate that no decoding is required.
	 */
	public static final InputDecoder NONE = new InputDecoder("<none>") {

		@Override
		public Decoder newDecoder() {
			throw Check.fail();
		}

	};

	private final String name;

	/**
	 * Constructs a new {@linkplain InputDecoder} instance.
	 *
	 * @param name this decoder's name.
	 */
	protected InputDecoder(String name) {
		this.name = name;
	}

	/**
	 * Creates an {@linkplain InputDecoder} instance for an unsupported encoding format that fails with a corresponding
	 * error message.
	 *
	 * @param name the name of the unsupported encoding format.
	 * @return the created {@linkplain InputDecoder} instance.
	 */
	public static InputDecoder unsupportedInputDecoder(String name) {
		return new InputDecoder(name) {

			@Override
			public Decoder newDecoder() throws IOException {
				throw new IOException("Unsupported encoding format: " + name());
			}

		};
	}

	/**
	 * Gets this decoder's name.
	 *
	 * @return this decoder's name.
	 */
	public final String name() {
		return this.name;
	}

	/**
	 * Gets a new {@linkplain Decoder} instance for input decoding.
	 *
	 * @return a new {@linkplain Decoder} instance for input decoding.
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract Decoder newDecoder() throws IOException;

	/**
	 * Renders detailed decoder informations (if available).
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @throws IOException if an I/O error occurs.
	 */
	public void render(RenderOutput out) throws IOException {
		out.setStyle(RenderStyle.NORMAL).write("decoder");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln("'" + this.name + "'");
	}

}
