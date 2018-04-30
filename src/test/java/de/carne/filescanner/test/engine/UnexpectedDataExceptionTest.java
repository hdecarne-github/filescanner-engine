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
package de.carne.filescanner.test.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.UnexpectedDataException;

/**
 * Test {@linkplain UnexpectedDataException} class.
 */
class UnexpectedDataExceptionTest {

	@Test
	void testExceptionMessage() {
		String message1 = new UnexpectedDataException(Long.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fffffffffffffffh", message1);

		String message2 = new UnexpectedDataException(Integer.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fffffffh", message2);

		String message3 = new UnexpectedDataException(Short.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fffh", message3);

		String message4 = new UnexpectedDataException(Byte.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fh", message4);

		String message5 = new UnexpectedDataException().getMessage();

		Assertions.assertEquals("Unexpected data: { ... }", message5);

		String message6 = new UnexpectedDataException(new byte[] { 1, 2, 3, 4 }).getMessage();

		Assertions.assertEquals("Unexpected data: { 01h, 02h, 03h, 04h }", message6);

		String message7 = new UnexpectedDataException("Test message").getMessage();

		Assertions.assertEquals("Unexpected data: Test message", message7);
	}

}
