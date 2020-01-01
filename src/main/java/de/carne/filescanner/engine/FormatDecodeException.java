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
package de.carne.filescanner.engine;

import java.io.IOException;

/**
 * Base class for all kind of decoding exceptions.
 */
public abstract class FormatDecodeException extends IOException {

	// Serialization support
	private static final long serialVersionUID = 6404595481364372888L;

	/**
	 * Constructs a new {@linkplain FormatDecodeException} instance.
	 *
	 * @param message the exception message to use.
	 */
	public FormatDecodeException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@linkplain FormatDecodeException} instance.
	 *
	 * @param message the exception message to use.
	 * @param cause the causing exception.
	 */
	public FormatDecodeException(String message, Throwable cause) {
		super(message, cause);
	}

}
