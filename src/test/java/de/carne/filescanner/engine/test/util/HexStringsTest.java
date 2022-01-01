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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.util.HexStrings;

/**
 * Test {@linkplain HexStrings} class.
 */
class HexStringsTest {

	@Test
	void testParseIntSuccess() throws IOException {
		Assertions.assertEquals(0, HexStrings.parseInt("0"));
		Assertions.assertEquals(1, HexStrings.parseInt(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(31, HexStrings.parseInt(new String(new byte[] { '0', '1', 'f', 0 })));
		Assertions.assertEquals(31, HexStrings.parseInt(new String(new byte[] { '0', '1', 'F', 0 })));
	}

	@Test
	void testParseIntFailure() {
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseInt(new String(new byte[] { 0, '1' })));
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseInt(new String(new byte[] { '1', 'g', 0 })));
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseInt(new String(new byte[] { '1', 'G', 0 })));
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseInt("100000000"));
	}

	@Test
	void testSafeParseInt() {
		Assertions.assertEquals(0, HexStrings.safeParseInt("0"));
		Assertions.assertEquals(1, HexStrings.safeParseInt(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(31, HexStrings.safeParseInt(new String(new byte[] { '0', '1', 'f', 0 })));
		Assertions.assertEquals(31, HexStrings.safeParseInt(new String(new byte[] { '0', '1', 'F', 0 })));
		Assertions.assertEquals(0, HexStrings.safeParseInt(new String(new byte[] { 0, '1' })));
		Assertions.assertEquals(1, HexStrings.safeParseInt(new String(new byte[] { '1', 'g', 0 })));
		Assertions.assertEquals(1, HexStrings.safeParseInt(new String(new byte[] { '1', 'G', 0 })));
		Assertions.assertEquals(0x10000000, HexStrings.safeParseInt("100000000"));
	}

	@Test
	void testParseLongSuccess() throws IOException {
		Assertions.assertEquals(0, HexStrings.parseLong("0"));
		Assertions.assertEquals(1, HexStrings.parseLong(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(31, HexStrings.parseLong(new String(new byte[] { '0', '1', 'f', 0 })));
		Assertions.assertEquals(31, HexStrings.parseLong(new String(new byte[] { '0', '1', 'F', 0 })));
	}

	@Test
	void testParseLongFailure() {
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseLong(new String(new byte[] { 0, '1' })));
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseLong(new String(new byte[] { '1', 'g', 0 })));
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseLong(new String(new byte[] { '1', 'G', 0 })));
		Assertions.assertThrows(IOException.class, () -> HexStrings.parseLong("10000000000000000"));
	}

	@Test
	void testSafeParseLong() {
		Assertions.assertEquals(0, HexStrings.safeParseLong("0"));
		Assertions.assertEquals(1, HexStrings.safeParseLong(new String(new byte[] { '0', '1', 0 })));
		Assertions.assertEquals(31, HexStrings.safeParseLong(new String(new byte[] { '0', '1', 'f', 0 })));
		Assertions.assertEquals(31, HexStrings.safeParseLong(new String(new byte[] { '0', '1', 'F', 0 })));
		Assertions.assertEquals(0, HexStrings.safeParseLong(new String(new byte[] { 0, '1' })));
		Assertions.assertEquals(1, HexStrings.safeParseLong(new String(new byte[] { '1', 'g', 0 })));
		Assertions.assertEquals(1, HexStrings.safeParseLong(new String(new byte[] { '1', 'G', 0 })));
		Assertions.assertEquals(0x1000000000000000l, HexStrings.safeParseLong("10000000000000000"));
	}

}
