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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.InsufficientDataException;
import de.carne.filescanner.engine.InvalidPositionException;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.io.Closeables;
import de.carne.nio.compression.spi.Decoder;
import de.carne.nio.file.FileUtil;
import de.carne.nio.file.attribute.FileAttributes;
import de.carne.util.SystemProperties;

/**
 * This class implements a the necessary access strategies for the various types of decoded input.
 */
public final class InputDecodeCache implements Closeable {

	private static final Log LOG = new Log();

	private static final int DEFAUL_DECODE_BUFFER_SIZE = 0x10000;
	private static final int DECODE_BUFFER_SIZE;

	static {
		int decodeBufferSize = SystemProperties.intValue(InputDecodeCache.class, ".decodeBufferSize",
				DEFAUL_DECODE_BUFFER_SIZE);
		if (decodeBufferSize <= 0) {
			LOG.warning("Invalid decode buffer size {0}; using default", HexFormat.formatInt(decodeBufferSize));

			decodeBufferSize = DEFAUL_DECODE_BUFFER_SIZE;
		}
		LOG.info("Using decode buffer size {0}", HexFormat.formatInt(decodeBufferSize));
		DECODE_BUFFER_SIZE = decodeBufferSize;
	}

	private final List<CacheFile> cacheFiles = new LinkedList<>();

	/**
	 * Decodes an input stream and maps it to a contiguous decoded input stream.
	 *
	 * @param name the name of the encoded input stream.
	 * @param inputDecoderTable the {@linkplain InputDecoderTable} to use for decoding.
	 * @param input the {@linkplain FileScannerInput} to read the encoded data from.
	 * @param start the position to start decoding at.
	 * @return a {@linkplain DecodeResult} instance containing the decode result.
	 * @throws IOException if an I/O error occurs.
	 */
	public DecodeResult decodeInput(String name, InputDecoderTable inputDecoderTable, FileScannerInput input,
			long start) throws IOException {
		DecodeResult decodeResult;

		switch (inputDecoderTable.size()) {
		case 0:
			decodeResult = decodeInput0(name, input, start);
			break;
		case 1:
			decodeResult = decodeInput1(name, inputDecoderTable.iterator().next(), input, start);
			break;
		default:
			decodeResult = decodeInputN(name, inputDecoderTable, input, start);
		}
		return decodeResult;
	}

	private DecodeResult decodeInput0(String name, FileScannerInput input, long start) {
		return new DecodeResult(new FileScannerInputRange(name, input, start, start, 0), start, 0);
	}

	private DecodeResult decodeInput1(String name, InputDecoderTable.Entry inputDecoderTableEntry,
			FileScannerInput input, long start) throws IOException {
		InputDecoder inputDecoder = inputDecoderTableEntry.inputDecoder();
		long decodePosition = start;
		long decodeOffset = inputDecoderTableEntry.offset();

		if (decodeOffset > 0) {
			decodePosition += decodeOffset;
		}

		long inputSize = input.size();

		if (decodePosition > inputSize) {
			throw new InvalidPositionException(input, decodePosition);
		}

		long encodedSize = inputDecoderTableEntry.encodedSize();
		long available = inputSize - decodePosition;

		if (encodedSize > 0) {
			if (encodedSize > available) {
				throw new InsufficientDataException(input, decodePosition, encodedSize, available);
			}
			available = encodedSize;
		}

		DecodeResult decodeResult;

		if (InputDecoders.isIdentity(inputDecoder)) {
			decodeResult = new DecodeResult(new FileScannerInputRange(name, input, decodePosition, decodePosition,
					decodePosition + encodedSize), decodePosition, encodedSize);
		} else if (InputDecoders.isZero(inputDecoder)) {
			decodeResult = new DecodeResult(new ZeroFileScannerInput(name, inputDecoderTableEntry.decodedSize()),
					decodePosition, 0);
		} else {
			decodeResult = decodeToCache(name, input, decodePosition, decodePosition + available, inputDecoder);
		}
		return decodeResult;
	}

	private DecodeResult decodeInputN(String name, InputDecoderTable inputDecoderTable, FileScannerInput input,
			long start) throws IOException {
		MappedFileScannerInput decodedInput = new MappedFileScannerInput(name);
		long decodePosition = start;
		long decodedEnd = start;

		for (InputDecoderTable.Entry inputDecoderTableEntry : inputDecoderTable) {
			DecodeResult result = decodeInput1(name, inputDecoderTableEntry, input,
					(inputDecoderTableEntry.offset() >= 0 ? start : decodePosition));

			decodedInput.add(result.decodedInput());
			decodePosition = result.decodePosition() + result.encodedSize();
			decodedEnd = Math.max(decodedEnd, decodePosition);
		}
		return new DecodeResult(decodedInput, start, decodedEnd - start);
	}

	private DecodeResult decodeToCache(String name, FileScannerInput input, long start, long limit,
			InputDecoder inputDecoder) throws IOException {
		DecodeResult decodeResult;

		try (CacheFileLock cacheFileLock = acquireCacheFileLock();
				ReadableByteChannel encodedByteChannel = input.byteChannel(start, limit)) {
			CacheFile cacheFile = cacheFileLock.get();
			FileChannel cacheFileChannel = cacheFile.channel();
			Decoder decoder = inputDecoder.newDecoder();
			long decodedPosition = cacheFile.beginDecode();
			long decodedSize = 0;

			ByteBuffer buffer = ByteBuffer.allocate(DECODE_BUFFER_SIZE);

			while (decoder.decode(buffer, encodedByteChannel) >= 0) {
				buffer.flip();
				decodedSize += cacheFileChannel.write(buffer);
				buffer.clear();
			}
			cacheFile.endDecode(decodedSize);
			decodeResult = new DecodeResult(new FileScannerInputRange(name, cacheFile.input(), decodedPosition,
					decodedPosition, decodedPosition + decodedSize), start, decoder.totalIn());
		} catch (IOException e) {
			throw new InputDecoderException(inputDecoder, e);
		}
		return decodeResult;
	}

	private synchronized CacheFileLock acquireCacheFileLock() throws IOException {
		CacheFile acquiredCacheFile = null;

		for (CacheFile cacheFile : this.cacheFiles) {
			if (cacheFile.tryLock()) {
				acquiredCacheFile = cacheFile;
				break;
			}
		}
		if (acquiredCacheFile == null) {
			LOG.info("Creating decode cache file...");

			Path tmpDir = FileUtil.tmpDir();
			Path cacheFilePath = Files.createTempFile(tmpDir, getClass().getSimpleName(), null,
					FileAttributes.userFileDefault(tmpDir));
			@SuppressWarnings("resource") FileChannelInput cacheFileChannelInput = new FileChannelInput(cacheFilePath,
					StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ,
					StandardOpenOption.SPARSE);

			acquiredCacheFile = new CacheFile(cacheFilePath, cacheFileChannelInput);
			this.cacheFiles.add(acquiredCacheFile);

			LOG.info("Decode cache file ''{0}'' created", acquiredCacheFile);
		}
		return new CacheFileLock(acquiredCacheFile);
	}

	@Override
	public void close() throws IOException {
		IOException closeException = null;

		for (CacheFile cacheFile : this.cacheFiles) {
			if (closeException == null) {
				try {
					cacheFile.close();
				} catch (IOException e) {
					closeException = e;
				}
			} else {
				Closeables.safeClose(closeException, cacheFile);
			}
		}
		this.cacheFiles.clear();
		if (closeException != null) {
			throw closeException;
		}
	}

	/**
	 * This class represents the results of an
	 * {@linkplain InputDecodeCache#decodeInput(String, InputDecoderTable, FileScannerInput, long)} call.
	 */
	public static class DecodeResult {

		private final FileScannerInput decodedInput;
		private final long decodePosition;
		private final long encodedSize;

		DecodeResult(FileScannerInput decodedInput, long decodePosition, long encodedSize) {
			this.decodedInput = decodedInput;
			this.decodePosition = decodePosition;
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
		 * Gets the actual position the decoding started.
		 *
		 * @return the actual position the decoding started.
		 */
		public long decodePosition() {
			return this.decodePosition;
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

	private static class CacheFile implements Closeable {

		private static final byte[] SPARSE_MARKER = new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef };

		private final AtomicBoolean lock = new AtomicBoolean(true);
		private final Path path;
		private final FileChannel channel;
		private final BufferedFileChannelInput input;
		private long extent = 0;

		CacheFile(Path path, FileChannelInput fileChannelInput) {
			this.path = path;
			this.channel = fileChannelInput.channel();
			this.input = new BufferedFileChannelInput(fileChannelInput);
		}

		public boolean tryLock() {
			return this.lock.compareAndSet(false, true);
		}

		public void release() {
			this.lock.set(false);
		}

		public long beginDecode() throws IOException {
			this.channel.truncate(this.extent);
			return this.extent;
		}

		public void endDecode(long decoded) throws IOException {
			this.extent += decoded;
			this.channel.write(ByteBuffer.wrap(SPARSE_MARKER), this.extent);
		}

		public FileChannel channel() {
			return this.channel;
		}

		public FileScannerInput input() {
			return this.input;
		}

		@Override
		public void close() throws IOException {
			this.input.close();
			Files.delete(this.path);
		}

		@Override
		public String toString() {
			return this.path.toString();
		}

	}

	private static class CacheFileLock implements AutoCloseable, Supplier<CacheFile> {

		private final CacheFile cacheFile;

		CacheFileLock(CacheFile cacheFile) {
			this.cacheFile = cacheFile;
		}

		@Override
		public CacheFile get() {
			return this.cacheFile;
		}

		@Override
		public void close() {
			this.cacheFile.release();
		}

	}

}
