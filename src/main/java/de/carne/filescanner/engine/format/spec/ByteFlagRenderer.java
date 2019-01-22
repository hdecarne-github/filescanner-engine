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
package de.carne.filescanner.engine.format.spec;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Byte attribute {@linkplain FlagRenderer}.
 */
public class ByteFlagRenderer extends FlagRenderer<Byte> {

	// Serialization support
	private static final long serialVersionUID = -8064762212173430381L;

	/**
	 * Adds flag symbol.
	 *
	 * @param flag the flag to add the symbol for.
	 * @param symbol the symbol to add.
	 * @return the previously associated symbol (may be {@code null}).
	 */
	@Nullable
	public String put(byte flag, String symbol) {
		return put(Byte.valueOf(flag), symbol);
	}

	private static final byte MSB = (byte) 0b10000000;

	@Override
	protected Iterator<Byte> flags() {
		return new Iterator<Byte>() {

			private byte nextFlag = MSB;

			@Override
			public boolean hasNext() {
				return this.nextFlag != 0;
			}

			@Override
			public Byte next() {
				if (this.nextFlag == 0) {
					throw new NoSuchElementException();
				}

				byte flag = this.nextFlag;

				this.nextFlag = shift(this.nextFlag);
				return flag;
			}

		};
	}

	@Override
	protected boolean testFlag(Byte value, Byte flag) {
		byte flagValue = flag.byteValue();

		return (value.byteValue() & flagValue) != 0;
	}

	@Override
	protected Byte combineFlags(Byte flag1, Byte flag2) {
		return Byte.valueOf((byte) ((flag1.byteValue() | flag2.byteValue()) & 0xff));
	}

	@Override
	protected String formatFlag(Byte value, Byte flag) {
		StringBuilder buffer = new StringBuilder();
		byte valueValue = value.byteValue();
		byte flagValue = flag.byteValue();

		for (byte mask = MSB; mask != 0; mask = shift(mask)) {
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

	static byte shift(byte flag) {
		return (byte) ((flag & 0xff) >>> 1);
	}

}
