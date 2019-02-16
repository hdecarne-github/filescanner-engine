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
package de.carne.filescanner.test.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.UnexpectedDataException;

/**
 * Test {@linkplain UnexpectedDataException} class.
 */
class UnexpectedDataExceptionTest {

	private static final String HINT = "Unexpected data";

	@Test
	void testExceptionMessage1() {
		String message = new UnexpectedDataException(HINT, -1l, Long.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fffffffffffffffh", message);
	}

	@Test
	void testExceptionMessage2() {
		String message = new UnexpectedDataException(HINT, -1l, Integer.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fffffffh", message);
	}

	@Test
	void testExceptionMessage3() {
		String message = new UnexpectedDataException(HINT, -1l, Short.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fffh", message);
	}

	@Test
	void testExceptionMessage4() {
		String message = new UnexpectedDataException(HINT, -1l, Byte.MAX_VALUE).getMessage();

		Assertions.assertEquals("Unexpected data: 7fh", message);
	}

	@Test
	void testExceptionMessage5() {
		String message = new UnexpectedDataException(HINT).getMessage();

		Assertions.assertEquals("Unexpected data: { ... }", message);
	}

	@Test
	void testExceptionMessage6() {
		String message = new UnexpectedDataException(HINT, 0xffl).getMessage();

		Assertions.assertEquals("Unexpected data [00000000000000ffh]: { ... }", message);
	}

	@Test
	void testExceptionMessage7() {
		String message = new UnexpectedDataException(HINT, -1l, new byte[] { 1, 2, 3, 4 }).getMessage();

		Assertions.assertEquals("Unexpected data: { 01h, 02h, 03h, 04h }", message);
	}

	@Test
	void testExceptionMessage8() {
		String message = new UnexpectedDataException(HINT, -1l, "Test message").getMessage();

		Assertions.assertEquals("Unexpected data: Test message", message);
	}

}
