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
package de.carne.filescanner.engine.format;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Base interface for all format specification elements.
 */
public interface FormatSpec {

	/**
	 * Checks whether this format element is of fixed size and can be preloaded.
	 *
	 * @return {@code true} if this format element is of fixed size.
	 * @see #matchSize()
	 */
	boolean isFixedSize();

	/**
	 * Gets the maximum buffer size required to match this format element.
	 * <p>
	 * If this format element is of fixed size (see {@linkplain #isFixedSize()}) the match size is also the format
	 * element's size.
	 *
	 * @return the maximum buffer size required to match this format element.
	 */
	int matchSize();

	/**
	 * Matches this format element against input data.
	 *
	 * @param buffer the {@linkplain ByteBuffer} containing the input data to match.
	 * @return {@code true} if the submitted data matches the format element.
	 */
	boolean matches(ByteBuffer buffer);

	/**
	 * Decodes input data.
	 *
	 * @param context the {@linkplain FileScannerResultDecodeContext} instance to use for decoding.
	 * @throws IOException if an I/O error occurs.
	 */
	void decode(FileScannerResultDecodeContext context) throws IOException;

	/**
	 * Renders input data.
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @param context the {@linkplain FileScannerResultRenderContext} instance to use for rendering.
	 * @throws IOException if an I/O error occurs.
	 */
	void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException;

}
