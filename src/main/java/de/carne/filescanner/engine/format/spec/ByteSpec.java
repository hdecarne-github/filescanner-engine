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

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.PrettyFormat;

/**
 * Byte (8-bit) format attribute specification.
 */
public final class ByteSpec extends NumberAttributeSpec<Byte> {

	/**
	 * Constructs a new {@linkplain ByteSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public ByteSpec(Supplier<String> name) {
		super(Byte.class, name);
	}

	/**
	 * Constructs a new {@linkplain ByteSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public ByteSpec(String name) {
		super(Byte.class, name);
	}

	/**
	 * Convenience function for constructing a {@linkplain ByteSpec} instance with pre-configured decimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static ByteSpec dec(Supplier<String> name) {
		return dec(new ByteSpec(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain ByteSpec} instance with pre-configured decimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static ByteSpec dec(String name) {
		return dec(new ByteSpec(name));
	}

	private static ByteSpec dec(ByteSpec spec) {
		spec.format(PrettyFormat.BYTE_FORMATTER);
		return spec;
	}

	/**
	 * Convenience function for constructing a {@linkplain ByteSpec} instance with pre-configured hexadecimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static ByteSpec hex(Supplier<String> name) {
		return hex(new ByteSpec(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain ByteSpec} instance with pre-configured hexadecimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static ByteSpec hex(String name) {
		return hex(new ByteSpec(name));
	}

	private static ByteSpec hex(ByteSpec spec) {
		spec.format(HexFormat.BYTE_FORMATTER);
		return spec;
	}

	/**
	 * Convenience function for constructing a {@linkplain ByteSpec} instance with pre-configured decimal format and
	 * size renderer.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static ByteSpec size(Supplier<String> name) {
		return size(new ByteSpec(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain ByteSpec} instance with pre-configured decimal format and
	 * size renderer.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static ByteSpec size(String name) {
		return size(new ByteSpec(name));
	}

	private static ByteSpec size(ByteSpec spec) {
		spec.format(PrettyFormat.BYTE_FORMATTER);
		spec.renderer(ByteSizeRenderer.RENDERER);
		return spec;
	}

	@Override
	protected int size() {
		return 1;
	}

	@Override
	protected Byte decodeValue(ByteBuffer buffer) {
		return buffer.get();
	}

}
