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
package de.carne.filescanner.test.engine.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.format.PrettyFormat;

/**
 * Test {@linkplain PrettyFormat} class.
 */
class PrettyFormatTest {

	@Test
	void testByteFormat() {
		Assertions.assertEquals("-1", PrettyFormat.BYTE_FORMATTER.format(Byte.valueOf((byte) 0xff)));
		Assertions.assertEquals("{ }", PrettyFormat.BYTE_ARRAY_FORMATTER.format(new byte[0]));
		Assertions.assertEquals("{ -1 }", PrettyFormat.BYTE_ARRAY_FORMATTER.format(new byte[] { (byte) 0xff }));
		Assertions.assertEquals("{ -1, 127 }",
				PrettyFormat.BYTE_ARRAY_FORMATTER.format(new byte[] { (byte) 0xff, 127 }));
	}

	@Test
	void testShortFormat() {
		Assertions.assertEquals("-1", PrettyFormat.SHORT_FORMATTER.format(Short.valueOf((short) 0xffff)));
		Assertions.assertEquals("{ }", PrettyFormat.SHORT_ARRAY_FORMATTER.format(new short[0]));
		Assertions.assertEquals("{ -1 }", PrettyFormat.SHORT_ARRAY_FORMATTER.format(new short[] { (short) 0xffff }));
		Assertions.assertEquals("{ -1, 32767 }",
				PrettyFormat.SHORT_ARRAY_FORMATTER.format(new short[] { (short) 0xffff, 32767 }));
	}

	@Test
	void testIntFormat() {
		Assertions.assertEquals("-1", PrettyFormat.INT_FORMATTER.format(Integer.valueOf(0xffffffff)));
		Assertions.assertEquals("{ }", PrettyFormat.INT_ARRAY_FORMATTER.format(new int[0]));
		Assertions.assertEquals("{ -1 }", PrettyFormat.INT_ARRAY_FORMATTER.format(new int[] { 0xffffffff }));
		Assertions.assertEquals("{ -1, 2147483647 }",
				PrettyFormat.INT_ARRAY_FORMATTER.format(new int[] { 0xffffffff, 2147483647 }));
	}

	@Test
	void testLongFormat() {
		Assertions.assertEquals("-1", PrettyFormat.LONG_FORMATTER.format(Long.valueOf(0xffffffffffffffffl)));
		Assertions.assertEquals("{ }", PrettyFormat.LONG_ARRAY_FORMATTER.format(new long[0]));
		Assertions.assertEquals("{ -1 }", PrettyFormat.LONG_ARRAY_FORMATTER.format(new long[] { 0xffffffffffffffffl }));
		Assertions.assertEquals("{ -1, 9223372036854775807 }",
				PrettyFormat.LONG_ARRAY_FORMATTER.format(new long[] { 0xffffffffffffffffl, 9223372036854775807l }));
	}

}
