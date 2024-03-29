/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.util.Check;

class FileScannerInputByteChannel implements SeekableByteChannel {

	private final FileScannerInput input;
	private final long start;
	private final long end;
	private long position;

	FileScannerInputByteChannel(FileScannerInput input, long start, long end) {
		this.input = input;
		this.start = start;
		this.end = end;
		this.position = this.start;
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() {
		// Nothing to do here
	}

	@Override
	public synchronized int read(@Nullable ByteBuffer dst) throws IOException {
		Objects.requireNonNull(dst);

		int read;

		if (this.position + dst.remaining() <= this.end) {
			read = this.input.read(dst, this.position);
		} else if (this.position < this.end) {
			ByteBuffer limitedDst = dst.duplicate();

			limitedDst.limit((int) (this.end - this.position));
			read = this.input.read(limitedDst, this.position);
			dst.position(limitedDst.position());
		} else {
			read = -1;
		}
		if (read > 0) {
			this.position += read;
		}
		return read;
	}

	@Override
	public int write(@Nullable ByteBuffer src) throws IOException {
		throw new NonWritableChannelException();
	}

	@Override
	public long position() throws IOException {
		return this.position - this.start;
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		Check.isTrue(newPosition >= 0);

		this.position = newPosition;
		return this;
	}

	@Override
	public long size() throws IOException {
		return this.end - this.start;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		throw new NonWritableChannelException();
	}

}
