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
package de.carne.filescanner.engine.util;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility class providing {@linkplain Short} related functions.
 */
public final class ShortHelper {

	private ShortHelper() {
		// Prevent instantiation
	}

	/**
	 * Decode unsigned {@code short} value.
	 *
	 * @param string the input to decode.
	 * @return the decoded value.
	 */
	public static short decodeUnsigned(String string) {
		int value = Integer.decode(string).intValue();

		if (value > 0xffff) {
			throw new NumberFormatException("Value " + value + " out of range from input " + string);
		}
		return (short) (value & 0xffff);
	}

	/**
	 * Decode unsigned {@code short[]} value.
	 *
	 * @param stringArray the input to decode.
	 * @return the decoded value.
	 */
	public static short[] decodeUnsignedArray(@NonNull String[] stringArray) {
		int length = stringArray.length;
		short[] valueArray = new short[length];

		for (int index = 0; index < length; index++) {
			valueArray[index] = decodeUnsigned(stringArray[index]);
		}
		return valueArray;
	}

	/**
	 * Gets the unsigned {@code short} value.
	 *
	 * @param shortNumber the {@linkplain Number} to convert.
	 * @return the unsigned {@code short} value.
	 */
	public static int toUnsignedInt(Number shortNumber) {
		int unsignedInt;

		if (shortNumber instanceof Byte) {
			unsignedInt = Byte.toUnsignedInt(shortNumber.byteValue());
		} else if (shortNumber instanceof Short) {
			unsignedInt = Short.toUnsignedInt(shortNumber.shortValue());
		} else {
			unsignedInt = shortNumber.intValue();
		}
		if ((unsignedInt & ~0xffff) != 0) {
			throw new IllegalArgumentException("Number exceeds short value range: " + Integer.toHexString(unsignedInt));
		}
		return unsignedInt;
	}

}
