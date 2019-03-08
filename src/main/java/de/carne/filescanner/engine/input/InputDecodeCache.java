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

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.InsufficientDataException;
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

	private static final byte[] SPARSE_MARKER = new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef };

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
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ,
				StandardOpenOption.SPARSE);

		this.cacheFileChannel = fileChannelInput.channel();
		this.cacheFileInput = new BufferedFileScannerInput(fileChannelInput);

		LOG.info("Decode cache ''{0}'' created", this.cacheFilePath);
	}

	/**
	 * Decode an input stream to the cache file.
	 *
	 * @param name the name of the encoded input stream.
	 * @param inputDecoderTable the {@linkplain InputDecoderTable} to use for decoding.
	 * @param input the {@linkplain FileScannerInput} to read the encoded data from.
	 * @param start the position to start decoding at.
	 * @return a {@linkplain Decoded} instance containing the decode result.
	 * @throws IOException if an I/O error occurs.
	 */
	@SuppressWarnings({ "resource", "squid:S3776" })
	public synchronized Decoded decodeInput(String name, InputDecoderTable inputDecoderTable, FileScannerInput input,
			long start) throws IOException {
		// Discard any trailing data (caused by previously failed decode runs)
		this.cacheFileChannel.truncate(this.cacheExtent);

		long decodeOffset = 0;
		long decodedStart = this.cacheExtent;
		long decodedEnd = decodedStart;
		@Nullable FileScannerInput decodedInput = null;
		long inputSize = input.size();

		for (InputDecoderTable.Entry entry : inputDecoderTable) {
			long entryOffset = entry.offset();

			if (entryOffset >= 0) {
				if (decodeOffset > entryOffset) {
					throw new InvalidPositionException(input, start + entryOffset);
				}
				decodeOffset = entryOffset;
			}

			InputDecoder inputDecoder = entry.inputDecoder();
			long entryEncodedLength = entry.encodedLength();
			long inputAvailable = inputSize - (start + decodeOffset);
			long decodeLimit = inputSize;

			if (entryEncodedLength >= 0) {
				if (entryEncodedLength > inputAvailable) {
					throw new InsufficientDataException(input, start + decodeOffset, entryEncodedLength,
							inputAvailable);
				}
				decodeLimit = start + decodeOffset + entryEncodedLength;
			}

			if (InputDecoders.IDENTITY.equals(inputDecoder)) {
				if (inputDecoderTable.size() == 1) {
					// Use direct access
					decodedInput = new FileScannerInputRange(name, input, start, start, start + entryEncodedLength);
					decodeOffset = entryEncodedLength;
				} else {
					decodedEnd += copyInput(decodedEnd, input, start + decodeOffset,
							start + decodeOffset + entryEncodedLength);
					decodeOffset += entryEncodedLength;
				}
			} else if (InputDecoders.ZERO.equals(inputDecoder)) {
				decodedEnd += entry.decodedLength();
				this.cacheFileChannel.position(decodedEnd);
				// Enforce new cache file size
				this.cacheFileChannel.write(ByteBuffer.wrap(SPARSE_MARKER));
			} else {
				Decoder decoder = inputDecoder.newDecoder();

				decodedEnd += decodeInput(decodedEnd, input, start + decodeOffset, decodeLimit, inputDecoder, decoder);
				decodeOffset += decoder.totalIn();
			}
		}
		if (decodedInput == null) {
			decodedInput = new FileScannerInputRange(name, this.cacheFileInput, decodedStart, decodedStart, decodedEnd);
		}
		this.cacheExtent = decodedEnd;
		return new Decoded(decodedInput, decodeOffset);
	}

	private long copyInput(long position, FileScannerInput input, long copyStart, long copyEnd) throws IOException {
		long copied;

		try (ReadableByteChannel inputByteChannel = input.byteChannel(copyStart, copyEnd)) {
			copied = this.cacheFileChannel.transferFrom(inputByteChannel, position, copyEnd - copyStart);
		}
		return copied;
	}

	private long decodeInput(long position, FileScannerInput input, long decodeStart, long decodeLimit,
			InputDecoder inputDecoder, Decoder decoder) throws IOException {
		this.cacheFileChannel.position(position);

		long decoded = 0;

		try (ReadableByteChannel encodedByteChannel = input.byteChannel(decodeStart, decodeLimit)) {
			ByteBuffer buffer = ByteBuffer.allocate(DECODE_BUFFER_SIZE);

			while (decoder.decode(buffer, encodedByteChannel) >= 0) {
				buffer.flip();
				decoded += this.cacheFileChannel.write(buffer);
				buffer.clear();
			}
		} catch (IOException e) {
			throw new InputDecoderException(inputDecoder, e);
		}
		return decoded;
	}

	@Override
	public void close() throws IOException {
		LOG.info("Discarding decode cache ''{0}''...", this.cacheFilePath);

		this.cacheFileInput.close();
		Files.delete(this.cacheFilePath);
	}

	/**
	 * This class comprises the results of an
	 * {@linkplain InputDecodeCache#decodeInput(String, InputDecoderTable, FileScannerInput, long)} call.
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
