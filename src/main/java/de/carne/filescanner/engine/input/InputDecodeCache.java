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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.InvalidPositionException;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.nio.compression.spi.Decoder;
import de.carne.nio.file.FileUtil;
import de.carne.nio.file.attribute.FileAttributes;
import de.carne.util.SystemProperties;

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
		int defaultDecodeBufferSize = 0x10000;
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
	private long cacheExtent = 0;

	/**
	 * Constructs a new {@linkplain InputDecodeCache} instance.
	 *
	 * @throws IOException if an I/O error occurs during cache file creation.
	 */
	public InputDecodeCache() throws IOException {
		LOG.info("Creating decode cache...");

		Path tmpDir = FileUtil.tmpDir();

		this.cacheFilePath = Files.createTempFile(tmpDir, getClass().getSimpleName(), null,
				FileAttributes.userFileDefault(tmpDir));

		@SuppressWarnings("resource") FileChannelInput fileChannelInput = new FileChannelInput(this.cacheFilePath,
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);

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
	 */
	public synchronized Decoded decodeInput(String name, InputDecoder inputDecoder, FileScannerInput input, long start,
			long end) throws IOException {
		if (end > input.size()) {
			throw new InvalidPositionException(input, end);
		}

		long encodedSize = 0;
		FileScannerInput decodedInput;

		if (InputDecoders.NONE.equals(inputDecoder)) {
			encodedSize = end - start;
			decodedInput = new FileScannerInputRange(name, input, start, start, end);
		} else {
			this.cacheFileChannel.position(this.cacheExtent);

			long decodedInputStart = this.cacheExtent;
			long decodedInputEnd = decodedInputStart;

			try (ReadableByteChannel inputByteChannel = input.byteChannel(start, end)) {
				Decoder decoder = inputDecoder.newDecoder();
				ByteBuffer buffer = ByteBuffer.allocate(DECODE_BUFFER_SIZE);

				while (decoder.decode(buffer, inputByteChannel) >= 0) {
					encodedSize = decoder.totalIn();
					buffer.flip();
					decodedInputEnd += this.cacheFileChannel.write(buffer);
					buffer.clear();
				}
			} catch (IOException e) {
				throw new InputDecoderException(inputDecoder, e);
			}
			decodedInput = new FileScannerInputRange(name, this.cacheFileInput, decodedInputStart, decodedInputStart,
					decodedInputEnd);
			this.cacheExtent = decodedInputEnd;
		}
		return new Decoded(decodedInput, encodedSize);
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

		private final FileScannerInput decodedInput;
		private final long encodedSize;

		Decoded(FileScannerInput decodedInput, long encodedSize) {
			this.decodedInput = decodedInput;
			this.encodedSize = encodedSize;
		}

		/**
		 * Gets the {@linkplain FileScannerInput} instance providing access to the decoded input stream.
		 *
		 * @return the {@linkplain FileScannerInput} instance providing access to the decoded input stream.
		 */
		public FileScannerInput decodedInput() {
			return this.decodedInput;
		}

		/**
		 * Gets the number of encoded bytes.
		 *
		 * @return the number of encoded bytes.
		 */
		public long encodedSize() {
			return this.encodedSize;
		}

	}

}
