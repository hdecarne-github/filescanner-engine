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
package de.carne.filescanner.test.engine.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.input.BufferedFileChannelInput;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.test.TestFiles;

/**
 * Test {@linkplain FileScannerInput} class.
 */
class FileScannerInputTest {

	@Test
	void testFileScannerInput() throws IOException {
		try (BufferedFileChannelInput input = FileScannerInput.open(TestFiles.ZIP_ARCHIVE.getPath())) {
			Assertions.assertEquals(TestFiles.ZIP_ARCHIVE.getPath().toString(), input.name());
			Assertions.assertEquals(Files.size(TestFiles.ZIP_ARCHIVE.getPath()), input.size());

			ByteBuffer buffer = input.read(0x2e, 4);

			buffer.order(ByteOrder.LITTLE_ENDIAN);

			Assertions.assertEquals(4, buffer.remaining());
			Assertions.assertEquals(0x04034b50, buffer.getInt());
		}
	}

	@Test
	void testFileScannerInputByteChannel() throws IOException {
		try (BufferedFileChannelInput input = FileScannerInput.open(TestFiles.ZIP_ARCHIVE.getPath())) {
			try (ReadableByteChannel channel = input.byteChannel(0x2e, 0x2e + 4)) {
				Assertions.assertTrue(channel.isOpen());

				ByteBuffer buffer = ByteBuffer.allocate(4);

				buffer.order(ByteOrder.LITTLE_ENDIAN);
				channel.read(buffer);
				buffer.flip();

				Assertions.assertEquals(4, buffer.remaining());
				Assertions.assertEquals(0x04034b50, buffer.getInt());

				buffer.rewind();
				channel.read(buffer);
				buffer.flip();

				Assertions.assertEquals(0, buffer.remaining());
			}
			Assertions.assertEquals(4, input.read(0x2e, 4).remaining());
		}
	}

	@Test
	void testFileScannerInputStream() throws IOException {
		try (BufferedFileChannelInput input = FileScannerInput.open(TestFiles.ZIP_ARCHIVE.getPath())) {
			try (InputStream stream = input.inputStream(0x2e, 0x2e + 4)) {
				Assertions.assertTrue(stream.markSupported());
				Assertions.assertEquals(4, stream.available());

				stream.mark(4);
				stream.skip(4);

				Assertions.assertEquals(0, stream.available());

				stream.reset();

				Assertions.assertEquals(4, stream.available());

				byte[] buffer = new byte[4];

				Assertions.assertEquals(4, stream.read(buffer));
				Assertions.assertArrayEquals(new byte[] { 0x50, 0x4b, 0x03, 0x04 }, buffer);
				Assertions.assertEquals(-1, stream.read(buffer));
			}
			Assertions.assertEquals(4, input.read(0x2e, 4).remaining());
		}
	}

}
