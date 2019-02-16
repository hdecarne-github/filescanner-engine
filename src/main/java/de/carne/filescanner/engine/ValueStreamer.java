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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Functional interface for streamed value processing.
 */
@FunctionalInterface
public interface ValueStreamer {

	/**
	 * Stream value data.
	 * <p>
	 * The submitted buffer may contain less bytes than the requested chunk size. The number of decoded bytes is derived
	 * from the buffer's position movement.
	 * </p>
	 *
	 * @param buffer the {@linkplain ByteBuffer} to decode from.
	 * @return {@code true} if streaming should continue or {@false} if not.
	 * @throws IOException if a decode error occurs.
	 */
	boolean stream(ByteBuffer buffer) throws IOException;

}
