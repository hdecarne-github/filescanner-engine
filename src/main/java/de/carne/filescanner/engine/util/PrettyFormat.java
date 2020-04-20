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

import java.text.NumberFormat;

import de.carne.filescanner.engine.format.AttributeFormatter;
import de.carne.util.Strings;

/**
 * Pretty I18N aware and user ready format support.
 */
public final class PrettyFormat {

	private PrettyFormat() {
		// Prevent instantiation
	}

	/**
	 * {@linkplain AttributeFormatter} for {@linkplain Byte} based character values.
	 */
	public static final AttributeFormatter<Byte> BYTE_CHAR_FORMATTER = PrettyFormat::formatByteChar;

	/**
	 * Formats a {@code byte} based {@code char} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatByteChar(byte value) {
		return "'" + Strings.encode(String.valueOf((char) value)) + "'";
	}

	/**
	 * {@linkplain AttributeFormatter} for {@linkplain Byte} values.
	 */
	public static final AttributeFormatter<Byte> BYTE_FORMATTER = PrettyFormat::formatByteNumber;

	/**
	 * Formats a {@code byte} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatByteNumber(byte value) {
		return NumberFormat.getNumberInstance().format(value & 0xffl);
	}

	/**
	 * {@linkplain AttributeFormatter} for {@code byte[]} values.
	 */
	public static final AttributeFormatter<byte[]> BYTE_ARRAY_FORMATTER = PrettyFormat::formatByteNumberArray;

	/**
	 * Formats a {@code byte[]} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatByteNumberArray(byte[] value) {
		StringBuilder buffer = new StringBuilder();

		buffer.append('{');

		boolean firstElement = true;

		for (byte valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			buffer.append(formatByteNumber(valueElement));
		}
		buffer.append(" }");
		return buffer.toString();
	}

	/**
	 * {@linkplain AttributeFormatter} for {@linkplain Short} values.
	 */
	public static final AttributeFormatter<Short> SHORT_FORMATTER = PrettyFormat::formatShortNumber;

	/**
	 * Formats a {@code short} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatShortNumber(short value) {
		return NumberFormat.getNumberInstance().format(value & 0xffffl);
	}

	/**
	 * {@linkplain AttributeFormatter} for {@code short[]} values.
	 */
	public static final AttributeFormatter<short[]> SHORT_ARRAY_FORMATTER = PrettyFormat::formatShortNumberArray;

	/**
	 * Formats a {@code short[]} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatShortNumberArray(short[] value) {
		StringBuilder buffer = new StringBuilder();

		buffer.append('{');

		boolean firstElement = true;

		for (short valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			buffer.append(formatShortNumber(valueElement));
		}
		buffer.append(" }");
		return buffer.toString();
	}

	/**
	 * {@linkplain AttributeFormatter} for {@linkplain Integer} values.
	 */
	public static final AttributeFormatter<Integer> INT_FORMATTER = PrettyFormat::formatIntNumber;

	/**
	 * Formats a {@code int} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatIntNumber(int value) {
		return NumberFormat.getNumberInstance().format(value & 0xffffffffl);
	}

	/**
	 * {@linkplain AttributeFormatter} for {@code int[]} values.
	 */
	public static final AttributeFormatter<int[]> INT_ARRAY_FORMATTER = PrettyFormat::formatIntNumberArray;

	/**
	 * Formats a {@code int[]} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatIntNumberArray(int[] value) {
		StringBuilder buffer = new StringBuilder();

		buffer.append('{');

		boolean firstElement = true;

		for (int valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			buffer.append(formatIntNumber(valueElement));
		}
		buffer.append(" }");
		return buffer.toString();
	}

	/**
	 * {@linkplain AttributeFormatter} for {@linkplain Long} values.
	 */
	public static final AttributeFormatter<Long> LONG_FORMATTER = PrettyFormat::formatLongNumber;

	/**
	 * Formats a {@code long} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatLongNumber(long value) {
		return NumberFormat.getNumberInstance().format(value);
	}

	/**
	 * {@linkplain AttributeFormatter} for {@code long[]} values.
	 */
	public static final AttributeFormatter<long[]> LONG_ARRAY_FORMATTER = PrettyFormat::formatLongNumberArray;

	/**
	 * Formats a {@code long[]} value.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatLongNumberArray(long[] value) {
		StringBuilder buffer = new StringBuilder();

		buffer.append('{');

		boolean firstElement = true;

		for (long valueElement : value) {
			if (firstElement) {
				firstElement = false;
				buffer.append(' ');
			} else {
				buffer.append(", ");
			}
			buffer.append(formatLongNumber(valueElement));
		}
		buffer.append(" }");
		return buffer.toString();
	}

	/**
	 * {@linkplain AttributeFormatter} for {@linkplain String} values.
	 */
	public static final AttributeFormatter<String> STRING_FORMATTER = PrettyFormat::formatString;

	/**
	 * Formats {@code String} to it's quoted form.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatString(StringBuilder buffer, String value) {
		buffer.append("\"");
		Strings.encode(buffer, StringHelper.strip(value));
		buffer.append('"');
		return buffer;
	}

	/**
	 * Formats {@code String} to it's quoted form.
	 *
	 * @param value the value to format.
	 * @return the formatted value.
	 */
	public static String formatString(String value) {
		return formatString(new StringBuilder(), value).toString();
	}

}
