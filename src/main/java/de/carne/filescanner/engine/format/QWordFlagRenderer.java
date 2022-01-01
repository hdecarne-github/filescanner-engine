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
package de.carne.filescanner.engine.format;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Quat word attribute {@linkplain FlagRenderer}.
 */
public class QWordFlagRenderer extends FlagRenderer<Long> {

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
	public String put(long flag, String symbol) {
		return put(Long.valueOf(flag), symbol);
	}

	private static final long MSB = 0b1000000000000000000000000000000000000000000000000000000000000000l;

	@Override
	protected Iterator<Long> flags() {
		return new Iterator<>() {

			private long nextFlag = MSB;

			@Override
			public boolean hasNext() {
				return this.nextFlag != 0;
			}

			@Override
			public Long next() {
				if (this.nextFlag == 0) {
					throw new NoSuchElementException();
				}

				long flag = this.nextFlag;

				this.nextFlag = shift(this.nextFlag);
				return flag;
			}

		};
	}

	@Override
	protected boolean testFlag(Long value, Long flag) {
		long flagValue = flag.longValue();

		return (value.longValue() & flagValue) != 0;
	}

	@Override
	protected Long combineFlags(Long flag1, Long flag2) {
		return Long.valueOf(flag1.longValue() | flag2.longValue());
	}

	@Override
	protected String formatFlag(Long value, Long flag) {
		StringBuilder buffer = new StringBuilder();
		long valueValue = value.longValue();
		long flagValue = flag.longValue();

		for (long mask = MSB; mask != 0; mask = shift(mask)) {
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

	static long shift(long flag) {
		return flag >>> 1;
	}

}
