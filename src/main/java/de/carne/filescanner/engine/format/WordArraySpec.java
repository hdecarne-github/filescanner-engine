/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.filescanner.engine.util.HexFormat;

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
	 * Sets the length of this word array attribute.
	 *
	 * @param lengthSupplier the length of this word array attribute.
	 * @return the updated {@linkplain WordArraySpec} instance for chaining.
	 */
	public WordArraySpec length(Supplier<? extends Number> lengthSupplier) {
		this.size = (lengthSupplier instanceof FinalSupplier ? FinalSupplier.of(lengthSupplier.get().intValue() << 1)
				: () -> lengthSupplier.get().intValue() << 1);
		return this;
	}

	/**
	 * Sets the length of this word array attribute.
	 *
	 * @param lengthValue the length of this word array attribute.
	 * @return the updated {@linkplain WordArraySpec} instance for chaining.
	 */
	public WordArraySpec length(int lengthValue) {
		this.size = FinalSupplier.of(lengthValue);
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return FormatSpecs.isFixedSize(this.size);
	}

	@Override
	public int matchSize() {
		return FormatSpecs.matchSize(this.size);
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return FormatSpecs.matches(buffer, this.size, b -> validateValue(decodeValue(b)));
	}

	@Override
	protected short[] decodeValue(FileScannerResultInputContext context) throws IOException {
		return context.readValue(this.size.get().intValue(), this::decodeValue);
	}

	private short[] decodeValue(ByteBuffer buffer) {
		ByteBuffer slice = buffer.slice();

		slice.limit(this.size.get().intValue());

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
