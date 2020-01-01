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
package de.carne.filescanner.test.engine;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.InvalidPositionException;
import de.carne.filescanner.engine.input.BufferedFileChannelInput;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.test.TestFiles;

/**
 * Test {@linkplain InvalidPositionException} class.
 */
class InvalidPositionExceptionTest {

	@Test
	void testExceptionMessage() throws IOException {
		try (BufferedFileChannelInput input = FileScannerInput.open(TestFiles.ZIP_ARCHIVE.path())) {

			Assertions.assertEquals("Invalid read position 0000000000000042h (input: '" + input + "')",
					new InvalidPositionException(input, 0x42).getMessage());

			FileScannerInputRange inputRange = input.range(0, 2);

			Assertions.assertEquals(
					"Invalid read position 0000000000000042h (input: '" + input
							+ "[0000000000000000h-0000000000000002h]')",
					new InvalidPositionException(inputRange, 0x42).getMessage());
		}
	}

}
