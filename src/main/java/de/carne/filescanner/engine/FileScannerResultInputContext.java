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
package de.carne.filescanner.engine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import de.carne.filescanner.engine.format.FormatSpec;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.input.FileScannerInputRange;

/**
 * Input data processor base class responsible for input data access.
 */
public abstract class FileScannerResultInputContext extends FileScannerResultContext {

	private final FileScannerInputRange inputRange;
	private long position;
	private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

	/**
	 * Constructs a new {@linkplain FileScannerResultContext} instance.
	 *
	 * @param inputRange the {@linkplain FileScannerInputRange} to read from.
	 * @param position the initial read position.
	 */
	protected FileScannerResultInputContext(FileScannerInputRange inputRange, long position) {
		this.inputRange = inputRange;
		this.position = position;
	}

	/**
	 * Gets this context's {@linkplain FileScannerInputRange}.
	 *
	 * @return this context's {@linkplain FileScannerInputRange}.
	 */
	protected FileScannerInputRange inputRange() {
		return this.inputRange;
	}

	/**
	 * Gets this context's current read position.
	 *
	 * @return this context's current read position.
	 */
	protected long position() {
		return this.position;
	}

	/**
	 * Sets the read position for the next read operation.
	 *
	 * @param position the position to set.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void setPosition(long position) throws IOException {
		if (position < this.inputRange.start() || this.inputRange.end() < position) {
			throw new InvalidPositionException(this.inputRange, position);
		}
		this.position = position;
	}

	/**
	 * Sets the {@linkplain ByteOrder} for the next context operation.
	 *
	 * @param order the {@linkplain ByteOrder} to set.
	 */
	public void byteOrder(ByteOrder order) {
		this.byteOrder = order;
	}

	/**
	 * Matches the given {@linkplain FormatSpec} against the input data at the current position.
	 *
	 * @param spec the {@linkplain FormatSpec} to match.
	 * @return {@code true} if the remaining input data size is sufficient and matches the given
	 *         {@linkplain FormatSpec}.
	 * @throws IOException if an I/O error occurs.
	 */
	public boolean match(FormatSpec spec) throws IOException {
		int matchSize = spec.matchSize();
		boolean match = true;

		if (matchSize > 0) {
			ByteBuffer buffer = this.inputRange.read(this.position, matchSize);

			buffer.order(this.byteOrder);
			match = (buffer.remaining() <= matchSize ? spec.matches(buffer) : false);
		}
		return match;
	}

	/**
	 * Skips the given number of bytes and advances this context's position accordingly.
	 *
	 * @param size the number of bytes to skip.
	 * @throws IOException if an I/O error occurs.
	 */
	public void skip(long size) throws IOException {
		if (size < 0) {
			throw new UnexpectedDataException(size);
		}
		setPosition(this.position + size);
	}

	/**
	 * Reads and decodes a value.
	 *
	 * @param <T> the actual attribute type.
	 * @param size the size of the value to decode.
	 * @param decoder the decoder to use.
	 * @return the read value.
	 * @throws IOException if an I/O error occurs.
	 */
	public <T> T readValue(int size, InputDecoder<T> decoder) throws IOException {
		ByteBuffer buffer = readComplete(size);

		buffer.order(this.byteOrder);

		T value = decoder.decode(buffer);

		this.position += size;
		return value;
	}

	private ByteBuffer readComplete(int size) throws IOException {
		ByteBuffer buffer = this.inputRange.read(this.position, size);

		if (buffer.remaining() < size) {
			throw new InsufficientDataException(this.inputRange, this.position, size, buffer.remaining());
		}
		return buffer;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append(this.inputRange);
		buffer.append('[');
		HexFormat.formatLong(buffer, this.position);
		buffer.append(']');
		return buffer.toString();
	}

}
