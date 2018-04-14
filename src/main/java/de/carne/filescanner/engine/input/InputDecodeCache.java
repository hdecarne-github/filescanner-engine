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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.carne.boot.check.Nullable;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.nio.compression.CompressionException;
import de.carne.nio.compression.spi.Decoder;
import de.carne.nio.file.FileUtil;
import de.carne.nio.file.attribute.FileAttributes;
import de.carne.util.SystemProperties;
import de.carne.util.Threads;

/**
 * This class implements a file based cache for storing and accessing decoded input data during a scan in a single cache
 * file.
 */
public final class InputDecodeCache implements Closeable {

	private static final Log LOG = new Log();

	/**
	 * The decode buffer size (in bytes).
	 */
	public static final int DECODE_BUFFER_SIZE;

	static {
		int defaultDecodeBufferSize = 0x1000;
		int decodeBufferSize = SystemProperties.intValue(InputDecodeCache.class, ".decodeBufferSize",
				defaultDecodeBufferSize);
		if (decodeBufferSize <= 0) {
			LOG.warning("Invalid decode buffer size {0}; using default", HexFormat.formatInt(decodeBufferSize));

			decodeBufferSize = defaultDecodeBufferSize;
		}
		DECODE_BUFFER_SIZE = decodeBufferSize;
	}

	private final Path cacheFilePath;
	private final FileChannel cacheFileChannel;
	private final FileScannerInput cacheFileInput;

	/**
	 * Constructs a new {@linkplain InputDecodeCache} instance.
	 *
	 * @throws IOException if an I/O error occurs during cache file creation.
	 */
	public InputDecodeCache() throws IOException {
		LOG.info("Creating decode cache...");

		this.cacheFilePath = Files.createTempFile(FileUtil.TMP_DIR, getClass().getSimpleName(), null,
				FileAttributes.userFileDefault(FileUtil.TMP_DIR));

		FileChannelInput fileChannelInput = new FileChannelInput(this.cacheFilePath, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.READ);

		this.cacheFileChannel = fileChannelInput.channel();
		this.cacheFileInput = new BufferedFileScannerInput(fileChannelInput);

		LOG.info("Decode cache ''{0}'' created", this.cacheFilePath);
	}

	/**
	 * Decode an input stream to the cache file.
	 *
	 * @param name the name of the encoded input stream.
	 * @param inputDecoder the {@linkplain InputDecoder} to use for input decoding.
	 * @param input the {@linkplain FileScannerInput} to read the encoded data from.
	 * @param start the position to start decoding at.
	 * @param end the maximum position to read to during decoding.
	 * @return a {@linkplain Decoded} instance containing the decode result.
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread has been interrupted.
	 */
	public synchronized Decoded decodeInput(String name, InputDecoder inputDecoder, FileScannerInput input, long start,
			long end) throws IOException, InterruptedException {
		long encodedSize = 0;
		Throwable decodeException = null;
		FileScannerInput decodedInput;

		if (InputDecoder.NONE.equals(inputDecoder)) {
			encodedSize = end - start;
			decodedInput = new FileScannerInputRange(name, input, start, start, end);
		} else {
			long decodedInputStart = this.cacheFileInput.size();
			long decodedInputEnd = decodedInputStart;

			try (ReadableByteChannel inputByteChannel = input.byteChannel(start, end)) {
				Decoder decoder = inputDecoder.newDecoder();
				ByteBuffer buffer = ByteBuffer.allocate(DECODE_BUFFER_SIZE);

				while (decoder.decode(buffer, inputByteChannel) >= 0) {
					encodedSize = decoder.totalIn();
					Threads.checkInterrupted();
					buffer.flip();
					decodedInputEnd += this.cacheFileChannel.write(buffer);
					buffer.rewind();
				}
			} catch (CompressionException e) {
				decodeException = e;
			}
			decodedInput = new FileScannerInputRange(name, this.cacheFileInput, decodedInputStart, decodedInputStart,
					decodedInputEnd);
		}

		return new Decoded(decodedInput, encodedSize, decodeException);
	}

	@Override
	public void close() throws IOException {
		LOG.info("Discarding decode cache ''{0}''...", this.cacheFilePath);

		this.cacheFileInput.close();
		Files.delete(this.cacheFilePath);
	}

	/**
	 * This class comprises the results of an
	 * {@linkplain InputDecodeCache#decodeInput(String, InputDecoder, FileScannerInput, long, long)} call.
	 */
	public static class Decoded {

		private final FileScannerInput input;
		private final long encodedSize;
		@Nullable
		private final Throwable decodeException;

		Decoded(FileScannerInput input, long encodedSize, @Nullable Throwable decodeException) {
			this.input = input;
			this.encodedSize = encodedSize;
			this.decodeException = decodeException;
		}

		/**
		 * Gets the {@linkplain FileScannerInput} instance providing access to the decoded input stream.
		 *
		 * @return the {@linkplain FileScannerInput} instance providing access to the decoded input stream.
		 */
		public FileScannerInput input() {
			return this.input;
		}

		/**
		 * Gets the number of encoded bytes.
		 *
		 * @return the number of encoded bytes.
		 */
		public long encodedSize() {
			return this.encodedSize;
		}

		/**
		 * Gets a possibly encountered decode exception.
		 *
		 * @return a possibly encountered decode exception or {@code null} if no exception occurred.
		 */
		@Nullable
		public Throwable decodeException() {
			return this.decodeException;
		}
	}

}
