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
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;

/**
 * Base class fixed size attribute format elements.
 *
 * @param <T> The actual attribute type.
 */
public abstract class FixedSizeAttributeSpec<T> extends AttributeSpec<T> {

	/**
	 * Constructs a new {@linkplain AttributeSpec} instance.
	 *
	 * @param type the attribute's type.
	 * @param name the attribute's name.
	 */
	protected FixedSizeAttributeSpec(Class<T> type, Supplier<String> name) {
		super(type, name);
	}

	/**
	 * Constructs a new {@linkplain AttributeSpec} instance.
	 *
	 * @param type the attribute's type.
	 * @param name The attribute's name.
	 */
	protected FixedSizeAttributeSpec(Class<T> type, String name) {
		super(type, name);
	}

	@Override
	public final boolean isFixedSize() {
		return true;
	}

	@Override
	public final int matchSize() {
		return size();
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return size() <= buffer.remaining() && validateValue(decodeValue(buffer));
	}

	@Override
	protected T decodeValue(FileScannerResultInputContext context) throws IOException {
		return context.readValue(size(), this::decodeValue);
	}

	/**
	 * Gets the fixed size of attribute value represented by this instance.
	 *
	 * @return the fixed size of attribute value represented by this instance.
	 */
	protected abstract int size();

	/**
	 * Decodes the attribute value from the given {@linkplain ByteBuffer}.
	 *
	 * @param buffer the {@linkplain ByteBuffer} to decode from.
	 * @return the decoded attribute value.
	 */
	protected abstract T decodeValue(ByteBuffer buffer);

}
