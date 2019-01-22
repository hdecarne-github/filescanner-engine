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
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.Nullable;

class FileScannerInputInputStream extends InputStream {

	private final FileScannerInput input;
	private final long start;
	private final long end;
	private long position;
	private long mark = -1;

	FileScannerInputInputStream(FileScannerInput input, long start, long end) {
		this.input = input;
		this.start = start;
		this.end = end;
		this.position = this.start;
	}

	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];
		int read = read(b, 0, 1);

		return (read > 0 ? b[0] & 0xff : -1);
	}

	@Override
	public synchronized int read(byte @Nullable [] b, int off, int len) throws IOException {
		if (b == null) {
			throw new IllegalArgumentException();
		}

		int readLen = (int) Math.min(len, this.end - this.position);
		int read;

		if (readLen > 0) {
			ByteBuffer buffer = ByteBuffer.wrap(b, off, len);

			read = this.input.read(buffer, this.position);
		} else {
			read = -1;
		}
		if (read > 0) {
			this.position += read;
		}
		return read;
	}

	@Override
	public synchronized long skip(long n) throws IOException {
		long oldPosition = this.position;

		if (n > 0) {
			this.position = Math.max(this.position + n, this.end);
		}
		return this.position - oldPosition;
	}

	@Override
	public synchronized int available() throws IOException {
		return (int) Math.min(this.end - this.position, Integer.MAX_VALUE);
	}

	@Override
	public synchronized void mark(int readlimit) {
		this.mark = this.position;
	}

	@Override
	public synchronized void reset() throws IOException {
		if (this.mark < 0) {
			throw new IOException("mark not set");
		}
		this.position = this.mark;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

}
