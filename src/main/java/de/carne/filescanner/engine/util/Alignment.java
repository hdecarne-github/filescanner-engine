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
package de.carne.filescanner.engine.util;

/**
 * Utility class providing alignment functions.
 */
public final class Alignment {

	private Alignment() {
		// Prevent instantiation
	}

	/**
	 * Word aligns the submitted value.
	 *
	 * @param i the value to align.
	 * @return the aligned value.
	 */
	public static int word(int i) {
		return (i + 1) & ~1;
	}

	/**
	 * Word aligns the submitted value relative to the given base value.
	 *
	 * @param base the base value to align to.
	 * @param i the value to align.
	 * @return the aligned value.
	 */
	public static int word(int base, int i) {
		return base + word(i - base);
	}

	/**
	 * Word aligns the submitted value.
	 *
	 * @param l the value to align.
	 * @return the aligned value.
	 */
	public static long word(long l) {
		return (l + 1l) & ~1l;
	}

	/**
	 * Word aligns the submitted value relative to the given base value.
	 *
	 * @param base the base value to align to.
	 * @param l the value to align.
	 * @return the aligned value.
	 */
	public static long word(long base, long l) {
		return base + word(l - base);
	}

	/**
	 * Dword aligns the submitted value.
	 *
	 * @param i the value to align.
	 * @return the aligned value.
	 */
	public static int dword(int i) {
		return (i + 3) & ~3;
	}

	/**
	 * Word aligns the submitted value relative to the given base value.
	 *
	 * @param base the base value to align to.
	 * @param i the value to align.
	 * @return the aligned value.
	 */
	public static int dword(int base, int i) {
		return base + dword(i - base);
	}

	/**
	 * Dword aligns the submitted value.
	 *
	 * @param l the value to align.
	 * @return the aligned value.
	 */
	public static long dword(long l) {
		return (l + 3l) & ~3l;
	}

	/**
	 * Word aligns the submitted value relative to the given base value.
	 *
	 * @param base the base value to align to.
	 * @param l the value to align.
	 * @return the aligned value.
	 */
	public static long dword(long base, long l) {
		return base + dword(l - base);
	}

	/**
	 * Qword aligns the submitted value.
	 *
	 * @param i the value to align.
	 * @return the aligned value.
	 */
	public static int qword(int i) {
		return (i + 7) & ~7;
	}

	/**
	 * Word aligns the submitted value relative to the given base value.
	 *
	 * @param base the base value to align to.
	 * @param i the value to align.
	 * @return the aligned value.
	 */
	public static int qword(int base, int i) {
		return base + qword(i - base);
	}

	/**
	 * Qword aligns the submitted value.
	 *
	 * @param l the value to align.
	 * @return the aligned value.
	 */
	public static long qword(long l) {
		return (l + 7l) & ~7l;
	}

	/**
	 * Word aligns the submitted value relative to the given base value.
	 *
	 * @param base the base value to align to.
	 * @param l the value to align.
	 * @return the aligned value.
	 */
	public static long qword(long base, long l) {
		return base + qword(l - base);
	}

}
