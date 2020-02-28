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

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.provider.util.PrettyFormat;

/**
 * Test {@linkplain PrettyFormat} class.
 */
class PrettyFormatTest {

	@BeforeAll
	static void setTestLocale() {
		Locale.setDefault(Locale.GERMANY);
	}

	@Test
	void testByteFormat() {
		Assertions.assertEquals("'a'", PrettyFormat.BYTE_CHAR_FORMATTER.format(Byte.valueOf((byte) 'a')));
		Assertions.assertEquals("'\\0'", PrettyFormat.BYTE_CHAR_FORMATTER.format(Byte.valueOf((byte) 0)));
		Assertions.assertEquals("255", PrettyFormat.BYTE_FORMATTER.format(Byte.valueOf((byte) 0xff)));
		Assertions.assertEquals("{ }", PrettyFormat.BYTE_ARRAY_FORMATTER.format(new byte[0]));
		Assertions.assertEquals("{ 255 }", PrettyFormat.BYTE_ARRAY_FORMATTER.format(new byte[] { (byte) 0xff }));
		Assertions.assertEquals("{ 255, 127 }",
				PrettyFormat.BYTE_ARRAY_FORMATTER.format(new byte[] { (byte) 0xff, 127 }));
	}

	@Test
	void testShortFormat() {
		Assertions.assertEquals("65.535", PrettyFormat.SHORT_FORMATTER.format(Short.valueOf((short) 0xffff)));
		Assertions.assertEquals("{ }", PrettyFormat.SHORT_ARRAY_FORMATTER.format(new short[0]));
		Assertions.assertEquals("{ 65.535 }",
				PrettyFormat.SHORT_ARRAY_FORMATTER.format(new short[] { (short) 0xffff }));
		Assertions.assertEquals("{ 65.535, 32.767 }",
				PrettyFormat.SHORT_ARRAY_FORMATTER.format(new short[] { (short) 0xffff, 32767 }));
	}

	@Test
	void testIntFormat() {
		Assertions.assertEquals("4.294.967.295", PrettyFormat.INT_FORMATTER.format(Integer.valueOf(0xffffffff)));
		Assertions.assertEquals("{ }", PrettyFormat.INT_ARRAY_FORMATTER.format(new int[0]));
		Assertions.assertEquals("{ 4.294.967.295 }", PrettyFormat.INT_ARRAY_FORMATTER.format(new int[] { 0xffffffff }));
		Assertions.assertEquals("{ 4.294.967.295, 2.147.483.647 }",
				PrettyFormat.INT_ARRAY_FORMATTER.format(new int[] { 0xffffffff, 2147483647 }));
	}

	@Test
	void testLongFormat() {
		Assertions.assertEquals("-1", PrettyFormat.LONG_FORMATTER.format(Long.valueOf(0xffffffffffffffffl)));
		Assertions.assertEquals("{ }", PrettyFormat.LONG_ARRAY_FORMATTER.format(new long[0]));
		Assertions.assertEquals("{ -1 }", PrettyFormat.LONG_ARRAY_FORMATTER.format(new long[] { 0xffffffffffffffffl }));
		Assertions.assertEquals("{ -1, 9.223.372.036.854.775.807 }",
				PrettyFormat.LONG_ARRAY_FORMATTER.format(new long[] { 0xffffffffffffffffl, 9223372036854775807l }));
	}

}
