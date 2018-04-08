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
package de.carne.filescanner.engine.input;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.carne.filescanner.engine.format.HexFormat;

/**
 * {@linkplain FileScannerInput} implementation which restricts access to a specific range of an existing
 * {@linkplain FileScannerInput} instance.
 */
public class FileScannerInputRange extends FileScannerInput {

	private final FileScannerInput input;
	private final long start;
	private final long end;

	FileScannerInputRange(FileScannerInput input, long start, long end) {
		super(input.name());
		this.input = input;
		this.start = start;
		this.end = end;
	}

	/**
	 * Gets the {@linkplain FileScannerInput} instances backing up this {@linkplain FileScannerInputRange} instance.
	 *
	 * @return the {@linkplain FileScannerInput} instances backing up this {@linkplain FileScannerInputRange} instance.
	 */
	public FileScannerInput input() {
		return this.input;
	}

	/**
	 * Gets the range start position.
	 *
	 * @return the range start position.
	 */
	public long start() {
		return this.start;
	}

	/**
	 * Gets the range end position.
	 *
	 * @return the range end position.
	 */
	public long end() {
		return this.end;
	}

	@Override
	public void close() {
		// Nothing to do here
	}

	@Override
	public long size() throws IOException {
		return this.end - this.start;
	}

	@Override
	public int read(ByteBuffer buffer, long position) throws IOException {
		int read;

		if (position + buffer.remaining() <= this.end) {
			read = this.input.read(buffer, position);
		} else if (position < this.end) {
			ByteBuffer limitedBuffer = buffer.duplicate();

			limitedBuffer.limit((int) (this.end - position));
			read = this.input.read(limitedBuffer, position);
			buffer.position(limitedBuffer.position());
		} else {
			read = -1;
		}
		return read;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append(super.toString());
		buffer.append('[');
		HexFormat.formatLong(buffer, this.start);
		buffer.append('-');
		HexFormat.formatLong(buffer, this.end);
		buffer.append(']');
		return buffer.toString();
	}

}
