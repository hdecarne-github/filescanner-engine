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
 * Quad word array based format attribute specification.
 * <p>
 * The array size has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class QWordArraySpec extends AttributeSpec<long[]> {

	private Supplier<? extends Number> size = FinalSupplier.of(Integer.valueOf(0));

	/**
	 * Constructs a new {@linkplain QWordArraySpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public QWordArraySpec(Supplier<String> name) {
		super(long[].class, Arrays::equals, name);
		format(HexFormat.LONG_ARRAY_FORMATTER);
	}

	/**
	 * Constructs a new {@linkplain QWordArraySpec} instance.
	 *
	 * @param name The attribute's name.
	 */
	public QWordArraySpec(String name) {
		this(FinalSupplier.of(name));
	}

	/**
	 * Sets the length of this qword array attribute.
	 *
	 * @param lengthSupplier the length of this qword array attribute.
	 * @return the updated {@linkplain QWordArraySpec} instance for chaining.
	 */
	public QWordArraySpec length(Supplier<? extends Number> lengthSupplier) {
		this.size = (lengthSupplier instanceof FinalSupplier ? FinalSupplier.of(lengthSupplier.get().intValue() << 3)
				: () -> lengthSupplier.get().intValue() << 3);
		return this;
	}

	/**
	 * Sets the length of this qword array attribute.
	 *
	 * @param lengthValue the length of this qword array attribute.
	 * @return the updated {@linkplain QWordArraySpec} instance for chaining.
	 */
	public QWordArraySpec length(int lengthValue) {
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
	protected long[] decodeValue(FileScannerResultInputContext context) throws IOException {
		return context.readValue(this.size.get().intValue(), this::decodeValue);
	}

	private long[] decodeValue(ByteBuffer buffer) {
		ByteBuffer slice = buffer.slice();

		slice.limit(this.size.get().intValue());

		long[] value = new long[slice.remaining() >> 3];
		int valueIndex = 0;

		while (slice.hasRemaining()) {
			value[valueIndex] = slice.getLong();
			valueIndex++;
		}
		buffer.position(buffer.position() + slice.position());
		return value;
	}

}
