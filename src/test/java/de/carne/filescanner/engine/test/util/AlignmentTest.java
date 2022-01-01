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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.util.Alignment;

/**
 * Test {@linkplain Alignment} class.
 */
class AlignmentTest {

	@Test
	void testAlignIntWord() {
		Assertions.assertEquals(0, Alignment.word(0));
		Assertions.assertEquals(2, Alignment.word(1));
		Assertions.assertEquals(2, Alignment.word(2));
		Assertions.assertEquals(0xfffffffe, Alignment.word(0xfffffffd));
		Assertions.assertEquals(1, Alignment.word(1, 1));
		Assertions.assertEquals(3, Alignment.word(1, 2));
		Assertions.assertEquals(3, Alignment.word(1, 3));
		Assertions.assertEquals(0xffffffff, Alignment.word(1, 0xfffffffe));
	}

	@Test
	void testAlignIntDWord() {
		Assertions.assertEquals(0, Alignment.dword(0));
		Assertions.assertEquals(4, Alignment.dword(1));
		Assertions.assertEquals(4, Alignment.dword(2));
		Assertions.assertEquals(4, Alignment.dword(3));
		Assertions.assertEquals(4, Alignment.dword(4));
		Assertions.assertEquals(0xfffffffc, Alignment.dword(0xfffffffb));
		Assertions.assertEquals(1, Alignment.dword(1, 1));
		Assertions.assertEquals(5, Alignment.dword(1, 2));
		Assertions.assertEquals(5, Alignment.dword(1, 3));
		Assertions.assertEquals(5, Alignment.dword(1, 4));
		Assertions.assertEquals(5, Alignment.dword(1, 5));
		Assertions.assertEquals(0xfffffffd, Alignment.dword(1, 0xfffffffc));
	}

	@Test
	void testAlignIntQWord() {
		Assertions.assertEquals(0, Alignment.qword(0));
		Assertions.assertEquals(8, Alignment.qword(1));
		Assertions.assertEquals(8, Alignment.qword(2));
		Assertions.assertEquals(8, Alignment.qword(3));
		Assertions.assertEquals(8, Alignment.qword(4));
		Assertions.assertEquals(8, Alignment.qword(5));
		Assertions.assertEquals(8, Alignment.qword(6));
		Assertions.assertEquals(8, Alignment.qword(7));
		Assertions.assertEquals(8, Alignment.qword(8));
		Assertions.assertEquals(0xfffffff8, Alignment.qword(0xfffffff7));
		Assertions.assertEquals(1, Alignment.qword(1, 1));
		Assertions.assertEquals(9, Alignment.qword(1, 2));
		Assertions.assertEquals(9, Alignment.qword(1, 3));
		Assertions.assertEquals(9, Alignment.qword(1, 4));
		Assertions.assertEquals(9, Alignment.qword(1, 5));
		Assertions.assertEquals(9, Alignment.qword(1, 6));
		Assertions.assertEquals(9, Alignment.qword(1, 7));
		Assertions.assertEquals(9, Alignment.qword(1, 8));
		Assertions.assertEquals(9, Alignment.qword(1, 9));
		Assertions.assertEquals(0xfffffff9, Alignment.qword(1, 0xfffffff8));
	}

	@Test
	void testAlignLongWord() {
		Assertions.assertEquals(0, Alignment.word(0));
		Assertions.assertEquals(2, Alignment.word(1));
		Assertions.assertEquals(2, Alignment.word(2));
		Assertions.assertEquals(0xfffffffffffffffel, Alignment.word(0xfffffffffffffffdl));
		Assertions.assertEquals(1, Alignment.word(1, 1));
		Assertions.assertEquals(3, Alignment.word(1, 2));
		Assertions.assertEquals(3, Alignment.word(1, 3));
		Assertions.assertEquals(0xffffffffffffffffl, Alignment.word(1, 0xfffffffffffffffel));
	}

	@Test
	void testAlignLongDWord() {
		Assertions.assertEquals(0, Alignment.dword(0));
		Assertions.assertEquals(4, Alignment.dword(1));
		Assertions.assertEquals(4, Alignment.dword(2));
		Assertions.assertEquals(4, Alignment.dword(3));
		Assertions.assertEquals(4, Alignment.dword(4));
		Assertions.assertEquals(0xfffffffffffffffcl, Alignment.dword(0xfffffffffffffffbl));
		Assertions.assertEquals(1, Alignment.dword(1, 1));
		Assertions.assertEquals(5, Alignment.dword(1, 2));
		Assertions.assertEquals(5, Alignment.dword(1, 3));
		Assertions.assertEquals(5, Alignment.dword(1, 4));
		Assertions.assertEquals(5, Alignment.dword(1, 5));
		Assertions.assertEquals(0xfffffffffffffffdl, Alignment.dword(1, 0xfffffffffffffffcl));
	}

	@Test
	void testAlignLongQWord() {
		Assertions.assertEquals(0, Alignment.qword(0));
		Assertions.assertEquals(8, Alignment.qword(1));
		Assertions.assertEquals(8, Alignment.qword(2));
		Assertions.assertEquals(8, Alignment.qword(3));
		Assertions.assertEquals(8, Alignment.qword(4));
		Assertions.assertEquals(8, Alignment.qword(5));
		Assertions.assertEquals(8, Alignment.qword(6));
		Assertions.assertEquals(8, Alignment.qword(7));
		Assertions.assertEquals(8, Alignment.qword(8));
		Assertions.assertEquals(0xfffffffffffffff8l, Alignment.qword(0xfffffffffffffff7l));
		Assertions.assertEquals(1, Alignment.qword(1, 1));
		Assertions.assertEquals(9, Alignment.qword(1, 2));
		Assertions.assertEquals(9, Alignment.qword(1, 3));
		Assertions.assertEquals(9, Alignment.qword(1, 4));
		Assertions.assertEquals(9, Alignment.qword(1, 5));
		Assertions.assertEquals(9, Alignment.qword(1, 6));
		Assertions.assertEquals(9, Alignment.qword(1, 7));
		Assertions.assertEquals(9, Alignment.qword(1, 8));
		Assertions.assertEquals(9, Alignment.qword(1, 9));
		Assertions.assertEquals(0xfffffffffffffff9l, Alignment.qword(1, 0xfffffffffffffff8l));
	}

}
