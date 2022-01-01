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
package de.carne.filescanner.engine.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Utility class providing Unix related functions.
 */
public final class Unix {

	private Unix() {
		// Prevent instantiation
	}

	/**
	 * Converts Unit date value to a {@linkplain LocalDateTime} instance.
	 *
	 * @param date the Unix date value to convert.
	 * @return the created {@linkplain LocalDateTime} instance
	 */
	public static LocalDateTime unixDateToLocalDateTime(int date) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(Integer.toUnsignedLong(date) * 1000), ZoneId.of("UTC"));
	}

	/**
	 * Formats a unix mode value to it's symbolic representation.
	 *
	 * @param type the file type to use.
	 * @param mode the mode value to format.
	 * @return the formated mode value.
	 */
	public static String formatMode(char type, int mode) {
		char[] modeChars = new char[] { type, 'r', 'w', 'x', 'r', 'w', 'x', 'r', 'w', 'x' };
		int permission = 0x0100;

		for (int modeCharIndex = 1; modeCharIndex < modeChars.length; modeCharIndex++) {
			if ((mode & permission) == 0) {
				modeChars[modeCharIndex] = '-';
			}
			permission >>= 1;
		}
		if ((mode & 0x0800) != 0) {
			modeChars[3] = (modeChars[3] != '-' ? 's' : 'S');
		}
		if ((mode & 0x0400) != 0) {
			modeChars[6] = (modeChars[6] != '-' ? 's' : 'S');
		}
		if ((mode & 0x0200) != 0) {
			modeChars[9] = (modeChars[9] != '-' ? 't' : 'T');
		}
		return new String(modeChars);
	}

}
