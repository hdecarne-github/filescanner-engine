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
package de.carne.filescanner.engine.test.util;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.util.ShortHelper;

/**
 * Test {@linkplain ShortHelper} class.
 */
class ShortHelperTest {

	@Test
	void testDecodeUnsigned() {
		Assertions.assertEquals(0xffff, ShortHelper.decodeUnsigned("0xffff") & 0xffff);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			ShortHelper.decodeUnsigned("0x10000");
		});
	}

	@Test
	void testDecodeUnsignedArray() {
		short[] unsignedShorts = ShortHelper.decodeUnsignedArray(new @NonNull String[] { "0xffff", "0xfffe" });

		Assertions.assertEquals(0xffff, unsignedShorts[0] & 0xffff);
		Assertions.assertEquals(0xfffe, unsignedShorts[1] & 0xffff);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			ShortHelper.decodeUnsignedArray(new @NonNull String[] { "0x10000" });
		});
	}

	@Test
	void testToUnsignedInt() {
		Assertions.assertEquals(0xff, ShortHelper.toUnsignedInt(Byte.valueOf((byte) 0xff)));
		Assertions.assertEquals(0xffff, ShortHelper.toUnsignedInt(Short.valueOf((short) 0xffff)));
		Assertions.assertEquals(0xffff, ShortHelper.toUnsignedInt(Integer.valueOf(0xffff)));
		Assertions.assertEquals(0xffff, ShortHelper.toUnsignedInt(Long.valueOf(0xffffl)));
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ShortHelper.toUnsignedInt(Integer.valueOf(0x10000));
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ShortHelper.toUnsignedInt(Long.valueOf(0x10000l));
		});
	}

}
