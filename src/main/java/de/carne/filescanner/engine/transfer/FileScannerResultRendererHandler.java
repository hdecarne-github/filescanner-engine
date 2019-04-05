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
package de.carne.filescanner.engine.transfer;

import java.io.IOException;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;

/**
 * Render function for {@linkplain FileScannerResult} instances.
 */
@FunctionalInterface
public interface FileScannerResultRendererHandler {

	/**
	 * Renders the {@linkplain FileScannerResult} instances represented by the given
	 * {@linkplain FileScannerResultRenderContext}.
	 * 
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @param context the {@linkplain FileScannerResultRenderContext} to render.
	 * @throws IOException if an I/O error occurs.
	 */
	void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException;

}
