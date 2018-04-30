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

import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * Word (16-bit) attribute.
 */
public final class WordSpec extends FixedSizeAttributeSpec<Short> {

	/**
	 * Constructs a new {@linkplain WordSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public WordSpec(Supplier<String> name) {
		super(Short.class, name);
	}

	/**
	 * Constructs a new {@linkplain WordSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public WordSpec(String name) {
		super(Short.class, name);
	}

	/**
	 * Convenience function for constructing a {@linkplain WordSpec} instance with pre-configured hexadecimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static WordSpec hex(Supplier<String> name) {
		return hex(new WordSpec(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain WordSpec} instance with pre-configured hexadecimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static WordSpec hex(String name) {
		return hex(new WordSpec(name));
	}

	private static WordSpec hex(WordSpec spec) {
		spec.format(HexFormat.SHORT_FORMATTER);
		return spec;
	}

	/**
	 * Convenience function for constructing a {@linkplain WordSpec} instance with pre-configured decimal format and
	 * size renderer.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static WordSpec size(Supplier<String> name) {
		return size(new WordSpec(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain WordSpec} instance with pre-configured decimal format and
	 * size renderer.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static WordSpec size(String name) {
		return size(new WordSpec(name));
	}

	private static WordSpec size(WordSpec spec) {
		spec.format(PrettyFormat.SHORT_FORMATTER);
		spec.renderer(WordSizeRenderer.RENDERER);
		return spec;
	}

	@Override
	protected int size() {
		return 2;
	}

	@Override
	protected Short decodeValue(ByteBuffer buffer) {
		return buffer.getShort();
	}

}
