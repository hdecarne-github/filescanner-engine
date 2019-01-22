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
package de.carne.filescanner.engine.format.spec;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Double word attribute {@linkplain SymbolRenderer}.
 */
public class DWordSymbolRenderer extends SymbolRenderer<Integer> {

	// Serialization support
	private static final long serialVersionUID = 8051383114011052983L;

	/**
	 * Adds a value symbol.
	 *
	 * @param value the value to add the symbol for.
	 * @param symbol the symbol to add.
	 * @return the previously associated symbol (may be {@code null}).
	 */
	@Nullable
	public String put(int value, String symbol) {
		return put(Integer.valueOf(value), symbol);
	}

}
