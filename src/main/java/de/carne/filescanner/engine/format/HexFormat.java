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
package de.carne.filescanner.engine.format;

import de.carne.filescanner.engine.format.spec.AttributeFormatter;

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
	public static final AttributeFormatter<byte[]> BYTES_FORMATTER = HexFormat::formatBytes;

	/**
	 * Formats {@code byte[]} value to hexadecimal.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatBytes(StringBuilder buffer, byte[] value) {
		buffer.append("{ ");
		for (byte valueElement : value) {
			buffer.append(HEX_CHARS[(valueElement >> 4) & 0xf]);
			buffer.append(HEX_CHARS[valueElement & 0xf]);
			buffer.append("h ");
		}
		buffer.append('}');
		return buffer;
	}

	/**
	 * Formats {@code byte[]} value to hexadecimal.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatBytes(byte[] value) {
		return formatBytes(new StringBuilder(), value).toString();
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

}
