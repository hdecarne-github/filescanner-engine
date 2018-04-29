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
package de.carne.filescanner.test.provider.util;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.provider.util.Dos;

/**
 * Test {@linkplain Dos} class.
 */
class DosTest {

	@Test
	void testDosTimeToLocalTime() {
		LocalTime localTime1 = Dos.dosTimeToLocalTime((short) 0x0000);
		LocalTime localTime2 = Dos.dosTimeToLocalTime((short) 0xbf7d);
		LocalTime localTime3 = Dos.dosTimeToLocalTime((short) 0xffff);
		LocalTime localTime4 = Dos.dosTimeToLocalTime((short) 0xae47);

		Assertions.assertEquals(LocalTime.of(0, 0, 0), localTime1);
		Assertions.assertEquals(LocalTime.of(23, 59, 58), localTime2);
		Assertions.assertEquals(LocalTime.of(0, 0, 0), localTime3);
		Assertions.assertEquals(LocalTime.of(21, 50, 14), localTime4);
	}

	@Test
	void testDosDateToLocalDate() {
		LocalDate localDate1 = Dos.dosDateToLocalDate((short) 0x0000);
		LocalDate localDate2 = Dos.dosDateToLocalDate((short) 0xff9f);
		LocalDate localDate3 = Dos.dosDateToLocalDate((short) 0xffff);
		LocalDate localDate4 = Dos.dosDateToLocalDate((short) 0x4b26);

		Assertions.assertEquals(LocalDate.of(1980, 1, 1), localDate1);
		Assertions.assertEquals(LocalDate.of(2107, 12, 31), localDate2);
		Assertions.assertEquals(LocalDate.of(1980, 1, 1), localDate3);
		Assertions.assertEquals(LocalDate.of(2017, 9, 6), localDate4);
	}

}
