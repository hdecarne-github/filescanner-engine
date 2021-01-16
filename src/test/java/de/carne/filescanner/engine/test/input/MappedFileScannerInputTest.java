/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.test.input;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.input.BufferedFileChannelInput;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.MappedFileScannerInput;
import de.carne.filescanner.engine.input.ZeroFileScannerInput;
import de.carne.filescanner.engine.test.TestFiles;

/**
 * Test {@linkplain MappedFileScannerInput} class.
 */
class MappedFileScannerInputTest {

	@Test
	void testMapping() throws IOException {
		try (BufferedFileChannelInput input = FileScannerInput.open(TestFiles.ZIP_ARCHIVE.getPath())) {
			MappedFileScannerInput mapping = new MappedFileScannerInput("<mapped inputs>");

			mapping.add(new ZeroFileScannerInput(100), 0, 100);
			mapping.add(input, 0, 10);
			mapping.add(input, 200, 210);

			Assertions.assertEquals(120, mapping.size());

			byte[] data = new byte[10];

			Assertions.assertEquals(10, mapping.read(ByteBuffer.wrap(data), 0));
			Assertions.assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, data);
			Assertions.assertEquals(10, mapping.read(ByteBuffer.wrap(data), 95));
			Assertions.assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 80, 75, 3, 4, 10 }, data);
			Assertions.assertEquals(10, mapping.read(ByteBuffer.wrap(data), 105));
			Assertions.assertArrayEquals(new byte[] { 0, 0, 8, 8, 0, 24, 92, 68, 51, 91 }, data);
			Assertions.assertEquals(1, mapping.read(ByteBuffer.wrap(data), 119));
			Assertions.assertArrayEquals(new byte[] { 115, 0, 8, 8, 0, 24, 92, 68, 51, 91 }, data);
			Assertions.assertEquals(-1, mapping.read(ByteBuffer.wrap(data), 120));
		}
	}

}
