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

import de.carne.filescanner.engine.util.ByteHelper;

/**
 * Test {@linkplain ByteHelper} class.
 */
class ByteHelperTest {

	@Test
	void testDecodeUnsigned() {
		Assertions.assertEquals(0xff, ByteHelper.decodeUnsigned("0xff") & 0xff);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			ByteHelper.decodeUnsigned("0x100");
		});
	}

	@Test
	void testDecodeUnsignedArray() {
		byte[] unsignedBytes = ByteHelper.decodeUnsignedArray(new @NonNull String[] { "0xff", "0xfe" });

		Assertions.assertEquals(0xff, unsignedBytes[0] & 0xff);
		Assertions.assertEquals(0xfe, unsignedBytes[1] & 0xff);
		Assertions.assertThrows(NumberFormatException.class, () -> {
			ByteHelper.decodeUnsignedArray(new @NonNull String[] { "0x100" });
		});
	}

	@Test
	void testToUnsignedInt() {
		Assertions.assertEquals(0xff, ByteHelper.toUnsignedInt(Byte.valueOf((byte) 0xff)));
		Assertions.assertEquals(0xff, ByteHelper.toUnsignedInt(Short.valueOf((short) 0xff)));
		Assertions.assertEquals(0xff, ByteHelper.toUnsignedInt(Integer.valueOf(0xff)));
		Assertions.assertEquals(0xff, ByteHelper.toUnsignedInt(Long.valueOf(0xffl)));

		Short shortValue = Short.valueOf((short) 0x100);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ByteHelper.toUnsignedInt(shortValue);
		});

		Integer intValue = Integer.valueOf(0x100);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ByteHelper.toUnsignedInt(intValue);
		});

		Long longValue = Long.valueOf(0x100l);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ByteHelper.toUnsignedInt(longValue);
		});
	}

}
