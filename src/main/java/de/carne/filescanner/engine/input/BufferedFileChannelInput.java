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

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.logging.Log;
import de.carne.filescanner.provider.util.HexFormat;
import de.carne.util.SystemProperties;

/**
 * {@linkplain FileScannerInput} class that implements a per thread cache strategy to speed up access to an underlying
 * {@linkplain FileScannerInput} instance.
 */
public class BufferedFileChannelInput extends FileScannerInput implements Closeable {

	private final Map<Thread, Buffer> threadBuffers = new HashMap<>();
	private final FileChannelInput input;

	/**
	 * Constructs a new {@linkplain BufferedFileChannelInput} instance.
	 *
	 * @param input the {@linkplain FileScannerInput} instance to buffer.
	 */
	public BufferedFileChannelInput(FileChannelInput input) {
		super(input.name());
		this.input = input;
	}

	@Override
	public void close() throws IOException {
		try {
			this.input.close();
		} finally {
			clearBuffers();
		}
	}

	@Override
	public long size() throws IOException {
		return this.input.size();
	}

	@Override
	public int read(ByteBuffer buffer, long position) throws IOException {
		return getBuffer().read(this.input, buffer, position);
	}

	@Override
	public ByteBuffer read(long position, int size) throws IOException {
		return getBuffer().read(this.input, position, size);
	}

	private synchronized Buffer getBuffer() {
		return this.threadBuffers.computeIfAbsent(Thread.currentThread(), t -> new Buffer());
	}

	private synchronized void clearBuffers() {
		this.threadBuffers.clear();
	}

	private static class Buffer {

		private static final Log LOG = new Log();

		private static final int DEFAULT_BUFFER_SIZE = 0x10000;
		private static final int BUFFER_SIZE;

		static {
			int bufferSize = SystemProperties.intValue(BufferedFileChannelInput.class, ".bufferSize",
					DEFAULT_BUFFER_SIZE);
			int alignedBufferSize = ((bufferSize >>> 13) << 13);

			if (bufferSize != alignedBufferSize) {
				LOG.warning("Unaligned buffer size {0}; using default", HexFormat.formatInt(bufferSize));

				bufferSize = DEFAULT_BUFFER_SIZE;
			}
			LOG.info("Using input buffer size {0}", HexFormat.formatInt(bufferSize));
			BUFFER_SIZE = bufferSize;
		}

		private SoftReference<@Nullable ByteBuffer> bufferReference = new SoftReference<>(null);
		private long bufferPosition = -1;

		public Buffer() {
			// Nothing to do here
		}

		public int read(FileScannerInput input, ByteBuffer buffer, long position) throws IOException {
			int bufferRemaining = buffer.remaining();
			int read;

			if (bufferRemaining <= (BUFFER_SIZE >> 1)) {
				ByteBuffer cacheBuffer = read0(input, position, bufferRemaining);

				read = cacheBuffer.remaining();
				buffer.put(cacheBuffer);
			} else {
				read = input.read(buffer, position);
			}
			return (read > 0 ? read : -1);
		}

		public ByteBuffer read(FileScannerInput input, long position, int size) throws IOException {
			ByteBuffer buffer;

			if (size <= (BUFFER_SIZE >> 1)) {
				buffer = read0(input, position, size);
			} else {
				buffer = input.read(position, size);
			}
			return buffer;
		}

		private ByteBuffer read0(FileScannerInput input, long position, int size) throws IOException {
			ByteBuffer buffer = mapBuffer(input, position, size);
			ByteBuffer readBuffer = buffer.slice().asReadOnlyBuffer();

			readBuffer.limit(Math.min(readBuffer.remaining(), (int) (position - this.bufferPosition + size)));
			readBuffer.position(Math.min(readBuffer.limit(), (int) (position - this.bufferPosition)));
			return readBuffer;
		}

		private ByteBuffer mapBuffer(FileScannerInput input, long position, int size) throws IOException {
			ByteBuffer buffer = this.bufferReference.get();

			if (buffer == null) {
				buffer = ByteBuffer.allocate(BUFFER_SIZE);

				long newBufferPosition = position & ~((BUFFER_SIZE >> 1) - 1);

				buffer.clear();
				input.read(buffer, newBufferPosition);
				buffer.flip();
				this.bufferPosition = newBufferPosition;
				this.bufferReference = new SoftReference<>(buffer);
			} else if (position < this.bufferPosition
					|| (this.bufferPosition + buffer.capacity()) < (position + size)) {
				long newBufferPosition = position & ~((BUFFER_SIZE >> 1) - 1);

				buffer.clear();
				input.read(buffer, newBufferPosition);
				buffer.flip();
				this.bufferPosition = newBufferPosition;
			}
			return buffer;
		}

	}

}
