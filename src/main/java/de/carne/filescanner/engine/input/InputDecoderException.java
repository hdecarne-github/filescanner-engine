/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.input;

import java.text.MessageFormat;

import de.carne.filescanner.engine.FormatDecodeException;

class InputDecoderException extends FormatDecodeException {

	// Serialization support
	private static final long serialVersionUID = -2391473250632750680L;

	public InputDecoderException(InputDecoder inputDecoder, Throwable cause) {
		super(MessageFormat.format("Failed to decode input ''{0}''", inputDecoder.name()), cause);
	}

}
