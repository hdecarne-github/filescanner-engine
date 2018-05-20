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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * {@linkplain String} based format attribute specification.
 * <p>
 * The string length has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class FixedStringSpec extends AttributeSpec<String> {

	private boolean fixedSize = true;
	private Supplier<? extends Number> size = FinalSupplier.of(Integer.valueOf(0));
	private Supplier<Charset> charset = FinalSupplier.of(StandardCharsets.US_ASCII);

	/**
	 * Constructs a new {@linkplain FixedStringSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public FixedStringSpec(Supplier<String> name) {
		super(String.class, name);
		format(PrettyFormat.STRING_FORMATTER);
	}

	/**
	 * Constructs a new {@linkplain FixedStringSpec} instance.
	 *
	 * @param name The attribute's name.
	 */
	public FixedStringSpec(String name) {
		super(String.class, name);
		format(PrettyFormat.STRING_FORMATTER);
	}

	/**
	 * Sets the size (in bytes) of this {@linkplain String} attribute.
	 *
	 * @param sizeSupplier the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain FixedStringSpec} instance for chaining.
	 */
	public FixedStringSpec size(Supplier<? extends Number> sizeSupplier) {
		this.fixedSize = false;
		this.size = sizeSupplier;
		return this;
	}

	/**
	 * Sets the size (in bytes) of this {@linkplain String} attribute.
	 *
	 * @param sizeValue the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain FixedStringSpec} instance for chaining.
	 */
	public FixedStringSpec size(int sizeValue) {
		this.fixedSize = true;
		this.size = Integer.valueOf(sizeValue)::intValue;
		return this;
	}

	/**
	 * Sets the {@linkplain Charset} of this {@linkplain String} attribute.
	 *
	 * @param charsetSupplier the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain FixedStringSpec} instance for chaining.
	 */
	public FixedStringSpec charset(Supplier<Charset> charsetSupplier) {
		this.charset = charsetSupplier;
		return this;
	}

	/**
	 * Sets the {@linkplain Charset} of this {@linkplain String} attribute.
	 *
	 * @param charsetValue the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain FixedStringSpec} instance for chaining.
	 */
	public FixedStringSpec charset(Charset charsetValue) {
		this.charset = () -> charsetValue;
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return this.fixedSize;
	}

	@Override
	public int matchSize() {
		return (this.fixedSize ? this.size.get().intValue() : 0);
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return !this.fixedSize
				|| (this.size.get().intValue() <= buffer.remaining() && validateValue(decodeValue(buffer)));
	}

	@Override
	protected String decodeValue(FileScannerResultInputContext context) throws IOException {
		return context.readValue(this.size.get().intValue(), this::decodeValue);
	}

	private String decodeValue(ByteBuffer buffer) {
		ByteBuffer slice = buffer.slice();

		slice.limit(this.size.get().intValue());

		String value = this.charset.get().decode(slice).toString();

		buffer.position(buffer.position() + slice.position());
		return value;
	}

}
