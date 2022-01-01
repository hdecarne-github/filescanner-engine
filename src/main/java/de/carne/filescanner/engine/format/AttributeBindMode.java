/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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

/**
 * Bind mode for {@linkplain AttributeSpec} elements.
 */
public enum AttributeBindMode {

	/**
	 * No binding
	 */
	NONE,

	/**
	 * Bind to context (bind needs to done during decode as well as during render phase and the bound attribute is only
	 * available for the current result)
	 */
	CONTEXT,

	/**
	 * Bind to result (value can be retrieved during render phase without re-decoding it and the bound attribute is
	 * available to the current result as well as it's children)
	 */
	RESULT

}
