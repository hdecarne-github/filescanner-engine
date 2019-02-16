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
 * Utility class providing {@linkplain String} related functions.
 */
public final class StringHelper {

	private StringHelper() {
		// Prevent instantiation
	}

	/**
	 * Makes sure any subsequent {@code '\0'} are stripped from the submitted {@linkplain String}.
	 *
	 * @param string the {@linkplain String} to strip.
	 * @return the stripped {@linkplain String}.
	 */
	public static String strip(String string) {
		int length = string.length();
		int stripIndex = length - 1;

		while (stripIndex >= 0) {
			if (string.charAt(stripIndex) != '\0') {
				break;
			}
			stripIndex--;
		}
		return ((stripIndex + 1) == length ? string : string.substring(0, stripIndex + 1));
	}

}
