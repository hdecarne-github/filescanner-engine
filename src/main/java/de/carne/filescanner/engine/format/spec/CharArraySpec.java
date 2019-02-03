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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * {@linkplain String} based format attribute specification.
 * <p>
 * The string length has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class CharArraySpec extends StringAttributeSpec {

	private Supplier<? extends Number> size = FinalSupplier.of(Integer.valueOf(0));

	/**
	 * Constructs a new {@linkplain CharArraySpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public CharArraySpec(Supplier<String> name) {
		super(name);
	}

	/**
	 * Constructs a new {@linkplain CharArraySpec} instance.
	 *
	 * @param name The attribute's name.
	 */
	public CharArraySpec(String name) {
		super(FinalSupplier.of(name));
	}

	/**
	 * Sets the size (in bytes) of this {@linkplain String} attribute.
	 *
	 * @param sizeSupplier the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain CharArraySpec} instance for chaining.
	 */
	public CharArraySpec size(Supplier<? extends Number> sizeSupplier) {
		this.size = sizeSupplier;
		return this;
	}

	/**
	 * Sets the size (in bytes) of this {@linkplain String} attribute.
	 *
	 * @param sizeValue the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain CharArraySpec} instance for chaining.
	 */
	public CharArraySpec size(int sizeValue) {
		this.size = FinalSupplier.of(sizeValue);
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return (this.size instanceof FinalSupplier);
	}

	@Override
	public int matchSize() {
		return (isFixedSize() ? this.size.get().intValue() : 0);
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return !isFixedSize()
				|| (this.size.get().intValue() <= buffer.remaining() && validateValue(decodeValue(buffer)));
	}

	@Override
	protected String decodeValue(FileScannerResultInputContext context) throws IOException {
		return context.readValue(this.size.get().intValue(), this::decodeValue);
	}

	private String decodeValue(ByteBuffer buffer) {
		ByteBuffer slice = buffer.slice();

		slice.limit(this.size.get().intValue());

		String value = charset().decode(slice).toString();

		buffer.position(buffer.position() + slice.position());
		return value;
	}

}
