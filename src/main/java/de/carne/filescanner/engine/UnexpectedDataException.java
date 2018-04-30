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
package de.carne.filescanner.engine;

import java.lang.reflect.Array;

import de.carne.filescanner.engine.format.HexFormat;
import de.carne.util.Strings;

/**
 * This exception is thrown if unexpected data has been encountered.
 */
public class UnexpectedDataException extends FormatDecodeException {

	// Serialization support
	private static final long serialVersionUID = -5971215303926847087L;

	/**
	 * Constructs a new {@linkplain UnexpectedDataException} instance.
	 */
	public UnexpectedDataException() {
		super("Unexpected data: { ... }");
	}

	/**
	 * Constructs a new {@linkplain UnexpectedDataException} instance.
	 *
	 * @param data the invalid data.
	 */
	public UnexpectedDataException(Object data) {
		super(formatData(new StringBuilder(), data).toString());
	}

	private static StringBuilder formatData(StringBuilder buffer, Object data) {
		if (buffer.length() == 0) {
			buffer.append("Unexpected data: ");
		}

		Class<?> dataType = data.getClass();

		if (dataType.isArray()) {
			buffer.append("{ ");

			int length = Array.getLength(data);

			for (int index = 0; index < length; index++) {
				if (index > 0) {
					buffer.append(", ");
				}
				formatData(buffer, Array.get(data, index));
			}
			buffer.append(" }");
		} else if (dataType.equals(Long.class)) {
			buffer.append(HexFormat.formatLong((Long) data));
		} else if (dataType.equals(Integer.class)) {
			buffer.append(HexFormat.formatInt((Integer) data));
		} else if (dataType.equals(Short.class)) {
			buffer.append(HexFormat.formatShort((Short) data));
		} else if (dataType.equals(Byte.class)) {
			buffer.append(HexFormat.formatByte((Byte) data));
		} else {
			buffer.append(Strings.encode(data.toString()));
		}
		return buffer;
	}

}
