/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.test.util;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.util.LongHelper;

/**
 * Test {@linkplain LongHelper} class.
 */
class LongHelperTest {

	@Test
	void testDecodeUnsigned() {
		Assertions.assertEquals(0x7fffffffffffffffl,
				LongHelper.decodeUnsigned("0x7fffffffffffffff") & 0xffffffffffffffffl);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			LongHelper.decodeUnsigned("0x8000000000000000");
		});
	}

	@Test
	void testDecodeUnsignedArray() {
		long[] unsignedLongs = LongHelper
				.decodeUnsignedArray(new @NonNull String[] { "0x7fffffffffffffff", "0x7ffffffffffffffe" });

		Assertions.assertEquals(0x7fffffffffffffffl, unsignedLongs[0] & 0xffffffffffffffffl);
		Assertions.assertEquals(0x7ffffffffffffffel, unsignedLongs[1] & 0xffffffffffffffffl);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			LongHelper.decodeUnsignedArray(new @NonNull String[] { "0x8000000000000000" });
		});
	}

	@Test
	void testToUnsignedLong() {
		Assertions.assertEquals(0xffl, LongHelper.toUnsignedLong(Byte.valueOf((byte) 0xff)));
		Assertions.assertEquals(0xffffl, LongHelper.toUnsignedLong(Short.valueOf((short) 0xffff)));
		Assertions.assertEquals(0xffffffffl, LongHelper.toUnsignedLong(Integer.valueOf(0xffffffff)));
		Assertions.assertEquals(0xffffffffffffffffl, LongHelper.toUnsignedLong(Long.valueOf(0xffffffffffffffffl)));
	}

}
