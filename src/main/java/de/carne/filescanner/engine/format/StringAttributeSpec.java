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
package de.carne.filescanner.engine.format;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.filescanner.engine.util.PrettyFormat;
import de.carne.filescanner.engine.util.StringHelper;

/**
 * Base class for {@linkplain String} based format attribute specifications.
 */
public abstract class StringAttributeSpec extends AttributeSpec<String> {

	private Supplier<Charset> charset = FinalSupplier.of(StandardCharsets.US_ASCII);

	/**
	 * Constructs a new {@linkplain StringAttributeSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public StringAttributeSpec(Supplier<String> name) {
		super(String.class, String::equals, name);
		format(PrettyFormat.STRING_FORMATTER);
	}

	/**
	 * Constructs a new {@linkplain StringAttributeSpec} instance.
	 *
	 * @param name The attribute's name.
	 */
	public StringAttributeSpec(String name) {
		this(FinalSupplier.of(name));
	}

	/**
	 * Sets the {@linkplain Charset} of this {@linkplain String} attribute.
	 *
	 * @param charsetSupplier the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain StringAttributeSpec} instance for chaining.
	 */
	public StringAttributeSpec charset(Supplier<Charset> charsetSupplier) {
		this.charset = charsetSupplier;
		return this;
	}

	/**
	 * Sets the {@linkplain Charset} of this {@linkplain String} attribute.
	 *
	 * @param charsetValue the size (in bytes) of this {@linkplain String} attribute.
	 * @return the updated {@linkplain StringAttributeSpec} instance for chaining.
	 */
	public StringAttributeSpec charset(Charset charsetValue) {
		this.charset = () -> charsetValue;
		return this;
	}

	/**
	 * Gets the {@linkplain Charset} of this {@linkplain String} attribute.
	 *
	 * @return the {@linkplain Charset} of this {@linkplain String} attribute.
	 */
	public Charset charset() {
		return this.charset.get();
	}

	/**
	 * Gets the stripped {@linkplain String} attribute value.
	 *
	 * @return the stripped {@linkplain String} attribute value.
	 * @see StringHelper#strip(String)
	 */
	public String getStripped() {
		return StringHelper.strip(get());
	}

}
