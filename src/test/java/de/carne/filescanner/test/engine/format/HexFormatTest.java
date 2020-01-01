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
package de.carne.filescanner.test.engine.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.format.HexFormat;

/**
 * Test {@linkplain HexFormat} class.
 */
class HexFormatTest {

	@Test
	void testByteFormat() {
		Assertions.assertEquals("ffh", HexFormat.BYTE_FORMATTER.format(Byte.valueOf((byte) 0xff)));
		Assertions.assertEquals("{ }", HexFormat.BYTE_ARRAY_FORMATTER.format(new byte[0]));
		Assertions.assertEquals("{ ffh }", HexFormat.BYTE_ARRAY_FORMATTER.format(new byte[] { (byte) 0xff }));
		Assertions.assertEquals("{ ffh, 7fh }", HexFormat.BYTE_ARRAY_FORMATTER.format(new byte[] { (byte) 0xff, 127 }));
	}

	@Test
	void testShortFormat() {
		Assertions.assertEquals("ffffh", HexFormat.SHORT_FORMATTER.format(Short.valueOf((short) 0xffff)));
		Assertions.assertEquals("{ }", HexFormat.SHORT_ARRAY_FORMATTER.format(new short[0]));
		Assertions.assertEquals("{ ffffh }", HexFormat.SHORT_ARRAY_FORMATTER.format(new short[] { (short) 0xffff }));
		Assertions.assertEquals("{ ffffh, 7fffh }",
				HexFormat.SHORT_ARRAY_FORMATTER.format(new short[] { (short) 0xffff, 32767 }));
	}

	@Test
	void testIntFormat() {
		Assertions.assertEquals("ffffffffh", HexFormat.INT_FORMATTER.format(Integer.valueOf(0xffffffff)));
		Assertions.assertEquals("{ }", HexFormat.INT_ARRAY_FORMATTER.format(new int[0]));
		Assertions.assertEquals("{ ffffffffh }", HexFormat.INT_ARRAY_FORMATTER.format(new int[] { 0xffffffff }));
		Assertions.assertEquals("{ ffffffffh, 7fffffffh }",
				HexFormat.INT_ARRAY_FORMATTER.format(new int[] { 0xffffffff, 2147483647 }));
	}

	@Test
	void testLongFormat() {
		Assertions.assertEquals("ffffffffffffffffh",
				HexFormat.LONG_FORMATTER.format(Long.valueOf(0xffffffffffffffffl)));
		Assertions.assertEquals("{ }", HexFormat.LONG_ARRAY_FORMATTER.format(new long[0]));
		Assertions.assertEquals("{ ffffffffffffffffh }",
				HexFormat.LONG_ARRAY_FORMATTER.format(new long[] { 0xffffffffffffffffl }));
		Assertions.assertEquals("{ ffffffffffffffffh, 7fffffffffffffffh }",
				HexFormat.LONG_ARRAY_FORMATTER.format(new long[] { 0xffffffffffffffffl, 9223372036854775807l }));
	}

}
