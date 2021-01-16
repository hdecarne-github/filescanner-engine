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
package de.carne.filescanner.engine.format;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Byte attribute {@linkplain SymbolRenderer}.
 */
public class ByteSymbolRenderer extends SymbolRenderer<Byte> {

	// Serialization support
	private static final long serialVersionUID = -7652553942215636375L;

	/**
	 * Adds a value symbol.
	 *
	 * @param value the value to add the symbol for.
	 * @param symbol the symbol to add.
	 * @return the previously associated symbol (may be {@code null}).
	 */
	@Nullable
	public String put(byte value, String symbol) {
		return put(Byte.valueOf(value), symbol);
	}

}
