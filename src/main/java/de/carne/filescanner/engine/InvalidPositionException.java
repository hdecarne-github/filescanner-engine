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

import java.text.MessageFormat;

import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.util.HexFormat;

/**
 * This exception is thrown if the requested read position is invalid.
 */
public class InvalidPositionException extends FormatDecodeException {

	// Serialization support
	private static final long serialVersionUID = -5612770270465296574L;

	/**
	 * Constructs a new {@linkplain InvalidPositionException} instance.
	 *
	 * @param input the read {@linkplain FileScannerInputRange}.
	 * @param position the read position.
	 */
	public InvalidPositionException(FileScannerInput input, long position) {
		super(MessageFormat.format("Invalid read position while accessing input ''{0}'' position {1}", input.name(),
				HexFormat.formatLong(position)));
	}

}
