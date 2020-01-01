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

import de.carne.filescanner.provider.util.Apple;

/**
 * Test {@linkplain Apple} class.
 */
class AppleTest {

	@Test
	void testAppleDateToLocalDateTime() {
		LocalDateTime localDateTime1 = Apple.appleDateToLocalDateTime(0);
		LocalDateTime localDateTime2 = Apple.appleDateToLocalDateTime(-1);
		LocalDateTime localDateTime3 = Apple.appleDateToLocalDateTime(0xd6ed2af3);

		Assertions.assertEquals(LocalDateTime.of(1904, 1, 1, 0, 0, 0), localDateTime1);
		Assertions.assertEquals(LocalDateTime.of(2040, 2, 6, 6, 28, 15), localDateTime2);
		Assertions.assertEquals(LocalDateTime.of(2018, 4, 6, 13, 47, 31), localDateTime3);
	}

}
