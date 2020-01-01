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
package de.carne.filescanner.engine.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.carne.boot.check.Check;

/**
 * Base class for all kinds of scan input data streams.
 */
public abstract class FileScannerInput {

	private final String name;

	/**
	 * Constructs a new {@linkplain FileScannerInput} instance.
	 *
	 * @param name the input name.
	 */
	protected FileScannerInput(String name) {
		this.name = name;
	}

	/**
	 * Opens a file based {@linkplain FileScannerInput}.
	 *
	 * @param file the file to open.
	 * @return the opened {@linkplain FileScannerInput}.
	 * @throws IOException if an I/O error occurs.
	 */
	public static BufferedFileChannelInput open(Path file) throws IOException {
		return new BufferedFileChannelInput(new FileChannelInput(file, StandardOpenOption.READ));
	}

	/**
	 * Gets this {@linkplain FileScannerInput}'s name.
	 *
	 * @return this {@linkplain FileScannerInput}'s name.
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Gets this {@linkplain FileScannerInput}'s size (in bytes).
	 *
	 * @return this {@linkplain FileScannerInput}'s size (in bytes).
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract long size() throws IOException;

	/**
	 * Reads data from this {@linkplain FileScannerInput}.
	 *
	 * @param buffer the {@linkplain ByteBuffer} to read into.
	 * @param position the input position to read from.
	 * @return the number of bytes read or {@code -1} if EOF has been reached.
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract int read(ByteBuffer buffer, long position) throws IOException;

	/**
	 * Allocates a new {@linkplain ByteBuffer} of the submitted size and reads data from this
	 * {@linkplain FileScannerInput}.
	 * <p>
	 * The returned {@linkplain ByteBuffer} is already flipped and ready for processing the read data.
	 *
	 * @param position the input position to read from.
	 * @param size the number of bytes to read.
	 * @return the {@linkplain ByteBuffer} containing the read data.
	 * @throws IOException if an I/O error occurs.
	 */
	public ByteBuffer read(long position, int size) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(size);

		read(buffer, position);
		buffer.flip();
		return buffer;
	}

	/**
	 * Creates a {@linkplain FileScannerInputRange} instance backed up by this {@linkplain FileScannerInput} and
	 * restricted to the given range.
	 *
	 * @param start the start position of the requested range.
	 * @param end the end position of the requested range.
	 * @return the created {@linkplain FileScannerInputRange} instance.
	 * @throws IOException if an I/O error occurs.
	 */
	public FileScannerInputRange range(long start, long end) throws IOException {
		Check.assertTrue(0 <= start);
		Check.assertTrue(start <= end);
		Check.assertTrue(end <= size());

		return new FileScannerInputRange(this.name, this, 0, start, end);
	}

	/**
	 * Creates a {@linkplain SeekableByteChannel} instance backed up by this {@linkplain FileScannerInput} and
	 * restricted to the given range.
	 *
	 * @param start the start position of the requested range.
	 * @param end the end position of the requested range.
	 * @return the created {@linkplain SeekableByteChannel} instance.
	 * @throws IOException if an I/O error occurs.
	 */
	public SeekableByteChannel byteChannel(long start, long end) throws IOException {
		Check.assertTrue(0 <= start);
		Check.assertTrue(start <= end);
		Check.assertTrue(end <= size());

		return new FileScannerInputByteChannel(this, start, end);
	}

	/**
	 * Creates a {@linkplain InputStream} instance backed up by this {@linkplain FileScannerInput} and restricted to the
	 * given range.
	 *
	 * @param start the start position of the requested range.
	 * @param end the end position of the requested range.
	 * @return the created {@linkplain InputStream} instance.
	 * @throws IOException if an I/O error occurs.
	 */
	public InputStream inputStream(long start, long end) throws IOException {
		Check.assertTrue(0 <= start);
		Check.assertTrue(start <= end);
		Check.assertTrue(end <= size());

		return new FileScannerInputInputStream(this, start, end);
	}

	@Override
	public String toString() {
		return this.name;
	}

}
