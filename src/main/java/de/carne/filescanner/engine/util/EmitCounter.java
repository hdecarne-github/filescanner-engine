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
package de.carne.filescanner.engine.util;

/**
 * Utility class used for counting the number of emitted characters (e.g. during rendering).
 */
public final class EmitCounter {

	private int value = 0;

	/**
	 * Counts the number of characters to be emitted.
	 *
	 * @param text the text to count.
	 * @return the submitted text for further processing.
	 */
	public String count(String text) {
		this.value += text.length();
		return text;
	}

	/**
	 * Gets the total number of counted characters.
	 *
	 * @return the total number of counted characters.
	 */
	public int value() {
		return this.value;
	}

}
