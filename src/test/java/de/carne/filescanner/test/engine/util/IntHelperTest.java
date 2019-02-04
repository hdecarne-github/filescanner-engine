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
package de.carne.filescanner.test.engine.util;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.util.IntHelper;

/**
 * Test {@linkplain IntHelper} class.
 */
class IntHelperTest {

	@Test
	void testDecodeUnsigned() {
		Assertions.assertEquals(0xffffffff, IntHelper.decodeUnsigned("0xffffffff") & 0xffffffff);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			IntHelper.decodeUnsigned("0x100000000");
		});
	}

	@Test
	void testDecodeUnsignedArray() {
		int[] unsignedInts = IntHelper.decodeUnsignedArray(new @NonNull String[] { "0xffffffff", "0xfffffffe" });

		Assertions.assertEquals(0xffffffff, unsignedInts[0] & 0xffffffff);
		Assertions.assertEquals(0xfffffffe, unsignedInts[1] & 0xffffffff);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			IntHelper.decodeUnsignedArray(new @NonNull String[] { "0x100000000" });
		});
	}

	@Test
	void testToUnsignedLong() {
		Assertions.assertEquals(0xffl, IntHelper.toUnsignedLong(Byte.valueOf((byte) 0xff)));
		Assertions.assertEquals(0xffffl, IntHelper.toUnsignedLong(Short.valueOf((short) 0xffff)));
		Assertions.assertEquals(0xffffffffl, IntHelper.toUnsignedLong(Integer.valueOf(0xffffffff)));
		Assertions.assertEquals(0xffffffffl, IntHelper.toUnsignedLong(Long.valueOf(0xffffffffl)));
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			IntHelper.toUnsignedLong(Long.valueOf(0x100000000l));
		});
	}

}
