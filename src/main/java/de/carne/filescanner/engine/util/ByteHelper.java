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
 * Utility class providing {@linkplain Byte} related functions.
 */
public final class ByteHelper {

	private ByteHelper() {
		// Prevent instantiation
	}

	/**
	 * Decode unsigned {@code byte} value.
	 *
	 * @param string the input to decode.
	 * @return the decoded value.
	 */
	public static Byte decodeUnsigned(String string) {
		short value = Short.decode(string).shortValue();

		if (value > 0xff) {
			throw new NumberFormatException("Value " + value + " out of range from input " + string);
		}
		return Byte.valueOf((byte) (value & 0xff));
	}

}
