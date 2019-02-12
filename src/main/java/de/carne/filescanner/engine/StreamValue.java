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
import java.io.InputStream;

import org.eclipse.jdt.annotation.Nullable;

/**
 * {@linkplain InputStream} based class providing access to all kind if byte range based values.
 */
public class StreamValue extends InputStream {

	private final InputStream inputStream;
	private final long size;

	StreamValue(InputStream inputStream, long size) {
		this.inputStream = inputStream;
		this.size = size;
		this.inputStream.mark(0);
	}

	/**
	 * Gets the size of the byte range representing the value.
	 *
	 * @return the size of the byte range representing the value.
	 */
	public long size() {
		return this.size;
	}

	@Override
	public int read() throws IOException {
		return this.inputStream.read();
	}

	@Override
	public int read(byte @Nullable [] b) throws IOException {
		return this.inputStream.read(b);
	}

	@Override
	public int read(byte @Nullable [] b, int off, int len) throws IOException {
		return this.inputStream.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return this.inputStream.skip(n);
	}

	@Override
	public int available() throws IOException {
		return this.inputStream.available();
	}

	@Override
	public synchronized void mark(int readlimit) {
		this.inputStream.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		this.inputStream.reset();
	}

	@Override
	public boolean markSupported() {
		return this.inputStream.markSupported();
	}

	@Override
	public String toString() {
		return "{ ... }";
	}

}
