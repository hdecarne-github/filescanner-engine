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
 * Byte attribute {@linkplain FlagRenderer}.
 */
public class ByteFlagRenderer extends FlagRenderer<Byte> {

	// Serialization support
	private static final long serialVersionUID = -8064762212173430381L;

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

		return (value.byteValue() & flagValue) == flagValue;
	}

	@Override
	protected String formatFlag(Byte flag) {
		StringBuilder buffer = new StringBuilder();
		byte flagValue = flag.byteValue();

		for (byte mask = MSB; mask != 0; mask = shift(mask)) {
			buffer.append((flagValue & mask) == mask ? '1' : '.');
		}
		return buffer.toString();
	}

	static byte shift(byte flag) {
		return (byte) ((flag & 0xff) >>> 1);
	}

}