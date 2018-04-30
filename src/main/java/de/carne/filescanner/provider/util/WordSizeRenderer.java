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
package de.carne.filescanner.provider.util;

import java.io.IOException;

import de.carne.filescanner.engine.format.AttributeRenderer;
import de.carne.filescanner.engine.transfer.FileScannerResultOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.text.MemoryUnitFormat;

/**
 * {@linkplain AttributeRenderer} used for rendering of word based size values.
 */
public class WordSizeRenderer implements AttributeRenderer<Short> {

	/**
	 * The single renderer instance.
	 */
	public static final WordSizeRenderer RENDERER = new WordSizeRenderer();

	private WordSizeRenderer() {
		// Prevent instantiation
	}

	@Override
	public void render(FileScannerResultOutput out, Short value) throws IOException, InterruptedException {
		render(out, value.shortValue());
	}

	/**
	 * Renders the given value.
	 *
	 * @param out the {@linkplain FileScannerResultOutput} buffer to render into.
	 * @param value the value to render.
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the render thread has been interrupted.
	 */
	public void render(FileScannerResultOutput out, short value) throws IOException, InterruptedException {
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.write(MemoryUnitFormat.getMemoryUnitInstance().format(value & 0xffff));
	}

}