/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.format;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Double word attribute {@linkplain FlagRenderer}.
 */
public class DWordFlagRenderer extends FlagRenderer<Integer> {

	// Serialization support
	private static final long serialVersionUID = -2295541110505555035L;

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

		return (value.intValue() & flagValue) == flagValue;
	}

	@Override
	protected String formatFlag(Integer flag) {
		StringBuilder buffer = new StringBuilder();
		int flagValue = flag.intValue();

		for (int mask = MSB; mask != 0; mask = shift(mask)) {
			buffer.append((flagValue & mask) == mask ? '1' : '.');
		}
		return buffer.toString();
	}

	static int shift(int flag) {
		return flag >>> 1;
	}

}
