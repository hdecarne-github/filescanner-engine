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
package de.carne.filescanner.test.provider.util;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.provider.util.Unix;

/**
 * Test {@linkplain Unix} class.
 */
class UnixTest {

	@Test
	void testUnixDateToLocalDateTime() {
		LocalDateTime localDateTime1 = Unix.unixDateToLocalDateTime(0);
		LocalDateTime localDateTime2 = Unix.unixDateToLocalDateTime(-1);
		LocalDateTime localDateTime3 = Unix.unixDateToLocalDateTime(0xd6ed2af3);

		Assertions.assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0, 0), localDateTime1);
		Assertions.assertEquals(LocalDateTime.of(2106, 2, 7, 6, 28, 15), localDateTime2);
		Assertions.assertEquals(LocalDateTime.of(2084, 4, 6, 13, 47, 31), localDateTime3);
	}

	@Test
	void testFormatMode() {
		Assertions.assertEquals("?---------", Unix.formatMode('?', 0b000000000000));
		Assertions.assertEquals("?rwxrwxrwx", Unix.formatMode('?', 0b000111111111));
		Assertions.assertEquals("?--S--S--T", Unix.formatMode('?', 0b111000000000));
		Assertions.assertEquals("?rwsrwsrwt", Unix.formatMode('?', 0b111111111111));
	}

}
