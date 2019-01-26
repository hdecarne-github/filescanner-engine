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
import de.carne.util.Strings;

/**
 * Pretty I18N aware and user ready format support.
 */
public final class PrettyFormat {

	private PrettyFormat() {
		// Prevent instantiation
	}

	private static final String NUMBER_FORMAT = "%1$,d";

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
		return String.format(NUMBER_FORMAT, value);
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
		return String.format(NUMBER_FORMAT, value);
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
		return String.format(NUMBER_FORMAT, value);
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
		return String.format(NUMBER_FORMAT, value);
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
		Strings.encode(buffer, value);
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
