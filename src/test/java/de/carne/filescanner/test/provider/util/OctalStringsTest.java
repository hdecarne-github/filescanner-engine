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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.provider.util.OctalStrings;

/**
 * Test {@linkplain OctalStrings} class.
 */
class OctalStringsTest {

	@Test
	void testParseIntSuccess() throws IOException {
		Assertions.assertEquals(0, OctalStrings.parseInt("0"));
		Assertions.assertEquals(1, OctalStrings.parseInt(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(15, OctalStrings.parseInt(new String(new byte[] { '0', '1', '7', 0 })));
	}

	@Test
	void testParseIntFailure() {
		Assertions.assertThrows(IOException.class, () -> OctalStrings.parseInt(new String(new byte[] { 0, '1' })));
		Assertions.assertThrows(IOException.class, () -> OctalStrings.parseInt(new String(new byte[] { '1', '8', 0 })));
		Assertions.assertThrows(IOException.class, () -> OctalStrings.parseInt("77777777777"));
	}

	@Test
	void testSafeParseInt() {
		Assertions.assertEquals(0, OctalStrings.safeParseInt("0"));
		Assertions.assertEquals(1, OctalStrings.safeParseInt(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(15, OctalStrings.safeParseInt(new String(new byte[] { '0', '1', '7', 0 })));
		Assertions.assertEquals(0, OctalStrings.safeParseInt(new String(new byte[] { 0, '1' })));
		Assertions.assertEquals(1, OctalStrings.safeParseInt(new String(new byte[] { '1', '8', 0 })));
		Assertions.assertEquals(1073741823, OctalStrings.safeParseInt("77777777777"));
	}

	@Test
	void testParseLongSuccess() throws IOException {
		Assertions.assertEquals(0, OctalStrings.parseLong("0"));
		Assertions.assertEquals(1, OctalStrings.parseLong(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(15, OctalStrings.parseLong(new String(new byte[] { '0', '1', '7', 0 })));
	}

	@Test
	void testParseLongFailure() {
		Assertions.assertThrows(IOException.class, () -> OctalStrings.parseLong(new String(new byte[] { 0, '1' })));
		Assertions.assertThrows(IOException.class,
				() -> OctalStrings.parseLong(new String(new byte[] { '1', '8', 0 })));
		Assertions.assertThrows(IOException.class, () -> OctalStrings.parseLong("7777777777777777777777"));
	}

	@Test
	void testSafeParseLong() {
		Assertions.assertEquals(0, OctalStrings.safeParseLong("0"));
		Assertions.assertEquals(1, OctalStrings.safeParseLong(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(15, OctalStrings.safeParseLong(new String(new byte[] { '0', '1', '7', 0 })));
		Assertions.assertEquals(0, OctalStrings.safeParseLong(new String(new byte[] { 0, '1' })));
		Assertions.assertEquals(1, OctalStrings.safeParseLong(new String(new byte[] { '1', '8', 0 })));
		Assertions.assertEquals(9223372036854775807l, OctalStrings.safeParseLong("7777777777777777777777"));
	}

}
