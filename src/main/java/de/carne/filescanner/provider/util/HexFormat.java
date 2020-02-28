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
package de.carne.filescanner.provider.util;

import de.carne.filescanner.engine.format.AttributeFormatter;

/**
 * Hexadecimal format support.
 */
public final class HexFormat {

	private HexFormat() {
		// Prevent instantiation
	}

	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@linkplain Byte} values..
	 */
	public static final AttributeFormatter<Byte> BYTE_FORMATTER = HexFormat::formatByte;

	/**
	 * Formats {@code byte} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatByte(StringBuilder buffer, byte value) {
		buffer.append(HEX_CHARS[(value >> 4) & 0xf]);
		buffer.append(HEX_CHARS[value & 0xf]);
		buffer.append('h');
		return buffer;
	}

	/**
	 * Formats {@code byte} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatByte(byte value) {
		return formatByte(new StringBuilder(), value).toString();
	}

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@code byte[]} values..
	 */
	public static final AttributeFormatter<byte[]> BYTE_ARRAY_FORMATTER = HexFormat::formatByteArray;

	/**
	 * Formats {@code byte[]} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatByteArray(StringBuilder buffer, byte[] value) {
		buffer.append('{');

		boolean firstElement = true;

		for (byte valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			formatByte(buffer, valueElement);
		}
		buffer.append(" }");
		return buffer;
	}

	/**
	 * Formats {@code byte[]} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatByteArray(byte[] value) {
		return formatByteArray(new StringBuilder(), value).toString();
	}

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@linkplain Short} values..
	 */
	public static final AttributeFormatter<Short> SHORT_FORMATTER = HexFormat::formatShort;

	/**
	 * Formats {@code short} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatShort(StringBuilder buffer, short value) {
		buffer.append(HEX_CHARS[(value >> 12) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 8) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 4) & 0xf]);
		buffer.append(HEX_CHARS[value & 0xf]);
		buffer.append('h');
		return buffer;
	}

	/**
	 * Formats {@code short} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatShort(short value) {
		return formatShort(new StringBuilder(), value).toString();
	}

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@code short[]} values..
	 */
	public static final AttributeFormatter<short[]> SHORT_ARRAY_FORMATTER = HexFormat::formatShortArray;

	/**
	 * Formats {@code short[]} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatShortArray(StringBuilder buffer, short[] value) {
		buffer.append('{');

		boolean firstElement = true;

		for (short valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			formatShort(buffer, valueElement);
		}
		buffer.append(" }");
		return buffer;
	}

	/**
	 * Formats {@code short[]} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatShortArray(short[] value) {
		return formatShortArray(new StringBuilder(), value).toString();
	}

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@linkplain Integer} values..
	 */
	public static final AttributeFormatter<Integer> INT_FORMATTER = HexFormat::formatInt;

	/**
	 * Formats {@code int} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatInt(StringBuilder buffer, int value) {
		buffer.append(HEX_CHARS[(value >> 28) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 24) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 20) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 16) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 12) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 8) & 0xf]);
		buffer.append(HEX_CHARS[(value >> 4) & 0xf]);
		buffer.append(HEX_CHARS[value & 0xf]);
		buffer.append('h');
		return buffer;
	}

	/**
	 * Formats {@code int} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatInt(int value) {
		return formatInt(new StringBuilder(), value).toString();
	}

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@code int[]} values..
	 */
	public static final AttributeFormatter<int[]> INT_ARRAY_FORMATTER = HexFormat::formatIntArray;

	/**
	 * Formats {@code int[]} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatIntArray(StringBuilder buffer, int[] value) {
		buffer.append('{');

		boolean firstElement = true;

		for (int valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			formatInt(buffer, valueElement);
		}
		buffer.append(" }");
		return buffer;
	}

	/**
	 * Formats {@code int[]} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatIntArray(int[] value) {
		return formatIntArray(new StringBuilder(), value).toString();
	}

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@linkplain Long} values..
	 */
	public static final AttributeFormatter<Long> LONG_FORMATTER = HexFormat::formatLong;

	/**
	 * Formats {@code long} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatLong(StringBuilder buffer, long value) {
		buffer.append(HEX_CHARS[(int) ((value >> 60) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 56) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 52) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 48) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 44) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 40) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 36) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 32) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 28) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 24) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 20) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 16) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 12) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 8) & 0xf)]);
		buffer.append(HEX_CHARS[(int) ((value >> 4) & 0xf)]);
		buffer.append(HEX_CHARS[(int) (value & 0xf)]);
		buffer.append('h');
		return buffer;
	}

	/**
	 * Formats {@code long} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatLong(long value) {
		return formatLong(new StringBuilder(), value).toString();
	}

	/**
	 * Hexadecimal {@linkplain AttributeFormatter} for {@code long[]} values..
	 */
	public static final AttributeFormatter<long[]> LONG_ARRAY_FORMATTER = HexFormat::formatLongArray;

	/**
	 * Formats {@code long[]} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatLongArray(StringBuilder buffer, long[] value) {
		buffer.append('{');

		boolean firstElement = true;

		for (long valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			formatLong(buffer, valueElement);
		}
		buffer.append(" }");
		return buffer;
	}

	/**
	 * Formats {@code long[]} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatLongArray(long[] value) {
		return formatLongArray(new StringBuilder(), value).toString();
	}

}
