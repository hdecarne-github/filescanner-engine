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
package de.carne.filescanner.engine.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.nio.compression.CompressionInfos;
import de.carne.nio.compression.CompressionProperty;
import de.carne.nio.compression.spi.Decoder;

/**
 * {@code InputDecoder} instances are used to decode nested input streams.
 */
public abstract class InputDecoder {

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
	 * Gets the decoder properties for this encoded input.
	 *
	 * @return the decoder properties for this encoded input.
	 */
	public abstract CompressionInfos decoderProperties();

	/**
	 * Renders decoder informations.
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @throws IOException if an I/O error occurs.
	 */
	public void render(RenderOutput out) throws IOException {
		out.setStyle(RenderStyle.NORMAL).write("encoding");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln("'" + this.name + "'");

		CompressionInfos decoderProperties = decoderProperties();
		List<CompressionProperty> sortedDecoderProperties = new ArrayList<>();

		decoderProperties().forEach(sortedDecoderProperties::add);
		Collections.sort(sortedDecoderProperties);

		for (CompressionProperty decoderProperty : decoderProperties) {
			out.setStyle(RenderStyle.NORMAL).write("decoderProperty[" + decoderProperty.key() + "]");
			out.setStyle(RenderStyle.OPERATOR).write(" = ");
			out.setStyle(RenderStyle.VALUE).writeln(decoderProperties.getProperty(decoderProperty).toString());
		}
	}

}
