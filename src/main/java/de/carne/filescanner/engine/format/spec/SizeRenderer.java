/*
 * Copyright (c) 2007-2020 Holger de Carne and contributors, All Rights Reserved.
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
 * Size value rendering support.
 */
public final class SizeRenderer {

	private SizeRenderer() {
		// Prevent instantiation
	}

	/**
	 * {@linkplain AttributeRenderer} for {@linkplain Byte} based size values.
	 */
	public static final AttributeRenderer<Byte> BYTE_RENDERER = SizeRenderer::renderByteSize;

	/**
	 * Renders the given {@code byte} based size values.
	 *
	 * @param out the {@linkplain RenderOutput} buffer to render into.
	 * @param value the value to render.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void renderByteSize(RenderOutput out, byte value) throws IOException {
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.write(MemoryUnitFormat.getMemoryUnitInstance().format((value & 0xff) * 1.0));
	}

	/**
	 * {@linkplain AttributeRenderer} for {@linkplain Short} based size values.
	 */
	public static final AttributeRenderer<Short> SHORT_RENDERER = SizeRenderer::renderShortSize;

	/**
	 * Renders the given {@code short} based size values.
	 *
	 * @param out the {@linkplain RenderOutput} buffer to render into.
	 * @param value the value to render.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void renderShortSize(RenderOutput out, short value) throws IOException {
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.write(MemoryUnitFormat.getMemoryUnitInstance().format((value & 0xffff) * 1.0));
	}

	/**
	 * {@linkplain AttributeRenderer} for {@linkplain Integer} based size values.
	 */
	public static final AttributeRenderer<Integer> INT_RENDERER = SizeRenderer::renderIntSize;

	/**
	 * Renders the given {@code int} based size values.
	 *
	 * @param out the {@linkplain RenderOutput} buffer to render into.
	 * @param value the value to render.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void renderIntSize(RenderOutput out, int value) throws IOException {
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.write(MemoryUnitFormat.getMemoryUnitInstance().format((value & 0xffffffffl) * 1.0));
	}

	/**
	 * {@linkplain AttributeRenderer} for {@linkplain Long} based size values.
	 */
	public static final AttributeRenderer<Long> LONG_RENDERER = SizeRenderer::renderLongSize;

	/**
	 * Renders the given {@code long} based size values.
	 *
	 * @param out the {@linkplain RenderOutput} buffer to render into.
	 * @param value the value to render.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void renderLongSize(RenderOutput out, long value) throws IOException {
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.write(MemoryUnitFormat.getMemoryUnitInstance().format(value * 1.0));
	}

}
