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
package de.carne.filescanner.engine;

import java.lang.reflect.Array;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

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
		this(-1l);
	}

	/**
	 * Constructs a new {@linkplain UnexpectedDataException} instance.
	 *
	 * @param position the invalid data position.
	 */
	public UnexpectedDataException(long position) {
		super(formatData(position, null));
	}

	/**
	 * Constructs a new {@linkplain UnexpectedDataException} instance.
	 *
	 * @param position the invalid data position.
	 * @param data the invalid data.
	 */
	public UnexpectedDataException(long position, Object data) {
		super(formatData(position, data));
	}

	private static String formatData(long position, @Nullable Object data) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Unexpected data");
		if (position >= 0) {
			buffer.append(" [");
			HexFormat.formatLong(buffer, position);
			buffer.append("]: ");
		} else {
			buffer.append(": ");
		}
		if (data != null) {
			formatData(buffer, data);
		} else {
			buffer.append("{ ... }");
		}
		return buffer.toString();
	}

	@SuppressWarnings("squid:AssignmentInSubExpressionCheck")
	private static StringBuilder formatData(StringBuilder buffer, @Nullable Object data) {
		Class<?> dataType;

		if (data == null) {
			buffer.append("null");
		} else if ((dataType = data.getClass()).isArray()) {
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
			buffer.append(Strings.encode(Objects.toString(data)));
		}
		return buffer;
	}

}
