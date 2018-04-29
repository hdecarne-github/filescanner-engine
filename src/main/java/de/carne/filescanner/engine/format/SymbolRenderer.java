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
package de.carne.filescanner.engine.format;

import java.io.IOException;
import java.util.HashMap;

import de.carne.filescanner.engine.transfer.FileScannerResultOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;

/**
 * {@linkplain AttributeRenderer} implementation which renders a comment showing the attribute value's symbolic name (if
 * available).
 * 
 * @param <T> The actual attribute type.
 */
public class SymbolRenderer<T> extends HashMap<T, String> implements AttributeRenderer<T> {

	// Serialization support
	private static final long serialVersionUID = -7227668070648773298L;

	@Override
	public void render(FileScannerResultOutput out, T value) throws IOException, InterruptedException {
		String symbol = get(value);

		if (symbol != null) {
			out.setStyle(RenderStyle.COMMENT).write(" // ").write(symbol);
		}
	}

}
