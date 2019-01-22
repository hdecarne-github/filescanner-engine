/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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

import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.text.MemoryUnitFormat;

/**
 * {@linkplain AttributeRenderer} used for rendering of byte based size values.
 */
public class ByteSizeRenderer implements AttributeRenderer<Byte> {

	/**
	 * The single renderer instance.
	 */
	public static final ByteSizeRenderer RENDERER = new ByteSizeRenderer();

	private ByteSizeRenderer() {
		// Prevent instantiation
	}

	@Override
	public void render(RenderOutput out, Byte value) throws IOException {
		render(out, value.byteValue());
	}

	/**
	 * Renders the given value.
	 *
	 * @param out the {@linkplain RenderOutput} buffer to render into.
	 * @param value the value to render.
	 * @throws IOException if an I/O error occurs.
	 */
	public void render(RenderOutput out, byte value) throws IOException {
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.write(MemoryUnitFormat.getMemoryUnitInstance().format((value & 0xff) * 1.0));
	}

}
