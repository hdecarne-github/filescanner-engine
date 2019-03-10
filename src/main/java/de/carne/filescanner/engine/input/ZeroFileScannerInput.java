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
package de.carne.filescanner.engine.input;

import java.nio.ByteBuffer;

/**
 * {@linkplain FileScannerInput} implementation providing a zero bytes.
 */
public class ZeroFileScannerInput extends FileScannerInput {

	private static final byte[] ZEROS = new byte[512];

	private final long size;

	/**
	 * Constructs a new {@linkplain ZeroFileScannerInput} instance.
	 *
	 * @param size of the zero input stream.
	 */
	public ZeroFileScannerInput(long size) {
		super("<zeros>");
		this.size = size;
	}

	@Override
	public void close() {
		// Nothing to do here
	}

	@Override
	public long size() {
		return this.size;
	}

	@Override
	public int read(ByteBuffer buffer, long position) {
		int read = (int) Math.min(buffer.remaining(), this.size - position);
		int remaining = read;

		while (remaining > 0) {
			int putLength = Math.min(buffer.remaining(), ZEROS.length);

			buffer.put(ZEROS, 0, putLength);
			remaining -= putLength;
		}
		return read;
	}

}
