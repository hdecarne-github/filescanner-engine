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

import java.text.MessageFormat;

import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.input.FileScannerInput;

/**
 * This exception is thrown if not all requested data bytes could be read.
 */
public class InsufficientDataException extends FormatDecodeException {

	// Serialization support
	private static final long serialVersionUID = -8046503266460758913L;

	/**
	 * Constructs a new {@linkplain InsufficientDataException} instance.
	 *
	 * @param input the read {@linkplain FileScannerInput}.
	 * @param position the read position.
	 * @param requested the number of requested bytes.
	 * @param available the number of available bytes.
	 */
	public InsufficientDataException(FileScannerInput input, long position, int requested, int available) {
		super(MessageFormat.format(
				"Insufficient data while reading from input ''{0}'' position {1} (requested: {2} available: {3})",
				input.name(), HexFormat.formatLong(position), requested, available));
	}

}
