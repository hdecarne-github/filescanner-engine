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
package de.carne.filescanner.engine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.FormatSpec;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.util.HexFormat;
import de.carne.util.Check;

/**
 * Input data processor base class responsible for input data access.
 */
public abstract class FileScannerResultInputContext extends FileScannerResultContext {

	private final FileScannerInputRange inputRange;
	private long initialPosition;
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
		this.initialPosition = position;
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
	public long position() {
		return this.position;
	}

	/**
	 * Gets this context's remaining data bytes.
	 *
	 * @return this context's remaining data bytes.
	 */
	public long remaining() {
		return this.inputRange.end() - this.position;
	}

	/**
	 * Gets this context's decoded data bytes.
	 *
	 * @return this context's decoded data bytes.
	 */
	public long decoded() {
		return this.position - this.initialPosition;
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
	 * @return the previously set {@linkplain ByteOrder}.
	 */
	protected ByteOrder byteOrder(ByteOrder order) {
		ByteOrder previousByteOrder = this.byteOrder;

		this.byteOrder = order;
		return previousByteOrder;
	}

	/**
	 * Matches the given {@linkplain FormatSpec} against the input data at the current position.
	 *
	 * @param spec the {@linkplain FormatSpec} to match.
	 * @return {@code true} if the remaining input data size is sufficient and matches the given
	 * {@linkplain FormatSpec}.
	 * @throws IOException if an I/O error occurs.
	 */
	public boolean matchFormat(FormatSpec spec) throws IOException {
		int matchSize = spec.matchSize();
		boolean match = true;

		if (matchSize > 0) {
			ByteBuffer buffer = this.inputRange.read(this.position, matchSize);

			buffer.order(this.byteOrder);
			match = spec.matches(buffer);
		}
		return match;
	}

	/**
	 * Matches the given {@linkplain CompositeSpec} against the input data at the current position.
	 *
	 * @param spec the {@linkplain FormatSpec} to match.
	 * @return {@code true} if the remaining input data size is sufficient and matches the given
	 * {@linkplain FormatSpec}.
	 * @throws IOException if an I/O error occurs.
	 */
	public boolean matchComposite(CompositeSpec spec) throws IOException {
		this.byteOrder = spec.byteOrder();
		return matchFormat(spec);
	}

	/**
	 * Skips the given number of bytes and advances this context's position accordingly.
	 *
	 * @param size the number of bytes to skip.
	 * @throws IOException if an I/O error occurs.
	 */
	public void skip(long size) throws IOException {
		if (size < 0) {
			throw new InsufficientDataException(this.inputRange, this.position, size,
					this.inputRange.end() - this.position);
		}
		setPosition(this.position + size);
	}

	/**
	 * Reads and decodes an integral value.
	 *
	 * @param <T> the actual value type.
	 * @param size the size of the value to decode.
	 * @param decoder the decoder to use.
	 * @return the read value.
	 * @throws IOException if an I/O or decode error occurs.
	 */
	public <T> T readValue(int size, ValueDecoder<T> decoder) throws IOException {
		ByteBuffer buffer = readComplete(size);

		buffer.order(this.byteOrder);

		T value = decoder.decode(buffer);

		this.position += size;
		return value;
	}

	/**
	 * Reads and decodes a streamed value.
	 *
	 * @param <T> the actual attribute type.
	 * @param chunkSize the minimum chunk size to read and to decode.
	 * @param decoder the stream decoder to use.
	 * @return the decoded value.
	 * @throws IOException if an I/O or decode error occurs.
	 */
	public <T> T readValue(int chunkSize, StreamValueDecoder<T> decoder) throws IOException {
		Check.assertTrue(chunkSize > 0);

		ByteBuffer buffer = ByteBuffer.allocate(Math.max(2 * chunkSize, 64)).order(this.byteOrder).limit(0);
		ValueStreamerStatus status;

		do {
			if (buffer.remaining() < chunkSize) {
				buffer.clear();
				this.inputRange.read(buffer, this.position);
				buffer.flip();
			}

			int decodeStart = buffer.position();

			status = decoder.stream(buffer);

			int decodeEnd = buffer.position();

			if (decodeStart < decodeEnd && status != ValueStreamerStatus.FAILED) {
				this.position += decodeEnd - decodeStart;
			} else if (status == ValueStreamerStatus.FAILED
					|| (status == ValueStreamerStatus.STREAMING && decodeStart == decodeEnd)
					|| decodeStart > decodeEnd) {
				throw new DecodeFailureException(this.inputRange, this.position + decodeStart);
			}
		} while (status == ValueStreamerStatus.STREAMING);
		return decoder.decode();
	}

	/**
	 * Streams a value (represented by a byte range).
	 *
	 * @param size the size of the byte range representing the value.
	 * @param skip whether to skip the given byte range prior to streaming.
	 * @return the {@linkplain StreamValue} instance providing access to the value bytes.
	 * @throws IOException if an I/O error occurs while accessing the value.
	 */
	public StreamValue streamValue(long size, boolean skip) throws IOException {
		if (skip) {
			skip(size);
		}
		return new StreamValue(this.inputRange, this.position - size, this.position);
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
