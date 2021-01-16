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
package de.carne.filescanner.engine.util;

import org.eclipse.jdt.annotation.NonNull;

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
	public static byte decodeUnsigned(String string) {
		short value = Short.decode(string).shortValue();

		if (value > 0xff) {
			throw new NumberFormatException("Value " + value + " out of range from input " + string);
		}
		return (byte) (value & 0xff);
	}

	/**
	 * Decode unsigned {@code byte[]} value.
	 *
	 * @param stringArray the input to decode.
	 * @return the decoded value.
	 */
	public static byte[] decodeUnsignedArray(@NonNull String[] stringArray) {
		int length = stringArray.length;
		byte[] valueArray = new byte[length];

		for (int index = 0; index < length; index++) {
			valueArray[index] = decodeUnsigned(stringArray[index]);
		}
		return valueArray;
	}

	/**
	 * Gets the unsigned {@code byte} value.
	 *
	 * @param byteNumber the {@linkplain Number} to convert.
	 * @return the unsigned {@code byte} value.
	 */
	public static int toUnsignedInt(Number byteNumber) {
		int unsignedInt;

		if (byteNumber instanceof Byte) {
			unsignedInt = Byte.toUnsignedInt(byteNumber.byteValue());
		} else if (byteNumber instanceof Short) {
			unsignedInt = Short.toUnsignedInt(byteNumber.shortValue());
		} else {
			unsignedInt = byteNumber.intValue();
		}
		if ((unsignedInt & ~0xff) != 0) {
			throw new IllegalArgumentException("Number exceeds byte value range: " + Integer.toHexString(unsignedInt));
		}
		return unsignedInt;
	}

}
