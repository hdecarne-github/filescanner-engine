/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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

/**
 * String &amp; character format support.
 */
public class StringFormat {

	private StringFormat() {
		// Prevent instantiation
	}

	/**
	 * Formats {@code String} to it's quoted form.
	 *
	 * @param buffer the {@linkplain StringBuilder} to format into.
	 * @param value the value to format.
	 * @return the updated {@linkplain StringBuilder} for chaining.
	 */
	public static StringBuilder formatString(StringBuilder buffer, String value) {
		buffer.append("'");
		value.chars().forEach(c -> {
			if (c == '"') {
				buffer.append("\\\"");
			} else if (c == '\\') {
				buffer.append("\\\\");
			} else if (32 <= c && c <= 126) {
				buffer.append((char) c);
			} else {
				switch (c) {
				case 0:
					buffer.append("\\0");
					break;
				case 8:
					buffer.append("\\b");
					break;
				case 9:
					buffer.append("\\t");
					break;
				case 10:
					buffer.append("\\n");
					break;
				case 12:
					buffer.append("\\f");
					break;
				case 13:
					buffer.append("\\r");
					break;
				default:
					buffer.append("\\u").append(HexFormat.formatShort((short) (c & 0xffff)));
				}
			}
		});
		buffer.append('"');
		return buffer;
	}

}
