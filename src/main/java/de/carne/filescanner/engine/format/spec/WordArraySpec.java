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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Word array based format attribute specification.
 * <p>
 * The array size has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class WordArraySpec extends AttributeSpec<short[]> {

	private Supplier<? extends Number> size = FinalSupplier.of(Integer.valueOf(0));

	/**
	 * Constructs a new {@linkplain WordArraySpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public WordArraySpec(Supplier<String> name) {
		super(short[].class, Arrays::equals, name);
		format(HexFormat.SHORT_ARRAY_FORMATTER);
	}

	/**
	 * Constructs a new {@linkplain WordArraySpec} instance.
	 *
	 * @param name The attribute's name.
	 */
	public WordArraySpec(String name) {
		this(FinalSupplier.of(name));
	}

	/**
	 * Sets the size (in bytes) of this byte array attribute.
	 *
	 * @param sizeSupplier the size (in bytes) of this byte array attribute.
	 * @return the updated {@linkplain WordArraySpec} instance for chaining.
	 */
	public WordArraySpec size(Supplier<? extends Number> sizeSupplier) {
		this.size = sizeSupplier;
		return this;
	}

	/**
	 * Sets the size (in bytes) of this byte array attribute.
	 *
	 * @param sizeValue the size (in bytes) of this byte array attribute.
	 * @return the updated {@linkplain WordArraySpec} instance for chaining.
	 */
	public WordArraySpec size(int sizeValue) {
		this.size = FinalSupplier.of(sizeValue);
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return (this.size instanceof FinalSupplier);
	}

	@Override
	public int matchSize() {
		return (isFixedSize() ? (this.size.get().intValue() << 1) : 0);
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return !isFixedSize()
				|| ((this.size.get().intValue() << 1) <= buffer.remaining() && validateValue(decodeValue(buffer)));
	}

	@Override
	protected short[] decodeValue(FileScannerResultInputContext context) throws IOException {
		return context.readValue(this.size.get().intValue() << 1, this::decodeValue);
	}

	private short[] decodeValue(ByteBuffer buffer) {
		ByteBuffer slice = buffer.slice();

		slice.limit(this.size.get().intValue() << 1);

		short[] value = new short[slice.remaining() >> 1];
		int valueIndex = 0;

		while (slice.hasRemaining()) {
			value[valueIndex] = slice.getShort();
			valueIndex++;
		}
		buffer.position(buffer.position() + slice.position());
		return value;
	}

}
