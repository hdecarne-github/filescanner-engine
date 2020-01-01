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
package de.carne.filescanner.engine.format.spec;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Double word attribute {@linkplain FlagRenderer}.
 */
public class DWordFlagRenderer extends FlagRenderer<Integer> {

	// Serialization support
	private static final long serialVersionUID = -2295541110505555035L;

	/**
	 * Adds flag symbol.
	 *
	 * @param flag the flag to add the symbol for.
	 * @param symbol the symbol to add.
	 * @return the previously associated symbol (may be {@code null}).
	 */
	@Nullable
	public String put(int flag, String symbol) {
		return put(Integer.valueOf(flag), symbol);
	}

	private static final int MSB = 0b10000000000000000000000000000000;

	@Override
	protected Iterator<Integer> flags() {
		return new Iterator<Integer>() {

			private int nextFlag = MSB;

			@Override
			public boolean hasNext() {
				return this.nextFlag != 0;
			}

			@Override
			public Integer next() {
				if (this.nextFlag == 0) {
					throw new NoSuchElementException();
				}

				int flag = this.nextFlag;

				this.nextFlag = shift(this.nextFlag);
				return flag;
			}

		};
	}

	@Override
	protected boolean testFlag(Integer value, Integer flag) {
		int flagValue = flag.intValue();

		return (value.intValue() & flagValue) != 0;
	}

	@Override
	protected Integer combineFlags(Integer flag1, Integer flag2) {
		return Integer.valueOf(flag1.intValue() | flag2.intValue());
	}

	@Override
	protected String formatFlag(Integer value, Integer flag) {
		StringBuilder buffer = new StringBuilder();
		int valueValue = value.intValue();
		int flagValue = flag.intValue();

		for (int mask = MSB; mask != 0; mask = shift(mask)) {
			if ((flagValue & mask) != mask) {
				buffer.append('.');
			} else if ((flagValue & valueValue) != 0) {
				buffer.append('1');
			} else {
				buffer.append('0');
			}
		}
		return buffer.toString();
	}

	static int shift(int flag) {
		return flag >>> 1;
	}

}
