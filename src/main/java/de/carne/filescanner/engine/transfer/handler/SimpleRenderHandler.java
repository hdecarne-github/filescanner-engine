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
package de.carne.filescanner.engine.transfer.handler;

import java.io.IOException;

import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.FileScannerResults;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * {@linkplain FileScannerResultRenderHandler} for simple rendering.
 */
public class SimpleRenderHandler implements FileScannerResultRenderHandler {

	/**
	 * The single handler instance.
	 */
	public static final SimpleRenderHandler RENDER_HANDLER = new SimpleRenderHandler();

	private SimpleRenderHandler() {
		// Prevent instantiation
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		FileScannerResults.renderDefault(context.result(), out);
		context.skip(context.remaining());
	}

}
