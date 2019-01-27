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
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Quad word (64-bit) format attribute specification.
 */
public final class QWordSpec extends NumberAttributeSpec<Long> {

	/**
	 * Constructs a new {@linkplain QWordSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public QWordSpec(Supplier<String> name) {
		super(Long.class, name);
		format(HexFormat.LONG_FORMATTER);
	}

	/**
	 * Constructs a new {@linkplain QWordSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public QWordSpec(String name) {
		this(FinalSupplier.of(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain QWordSpec} instance with pre-configured decimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static QWordSpec dec(Supplier<String> name) {
		return dec(new QWordSpec(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain QWordSpec} instance with pre-configured decimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static QWordSpec dec(String name) {
		return dec(new QWordSpec(name));
	}

	private static QWordSpec dec(QWordSpec spec) {
		spec.format(PrettyFormat.LONG_FORMATTER);
		return spec;
	}

	/**
	 * Convenience function for constructing a {@linkplain QWordSpec} instance with pre-configured hexadecimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static QWordSpec hex(Supplier<String> name) {
		return new QWordSpec(name);
	}

	/**
	 * Convenience function for constructing a {@linkplain QWordSpec} instance with pre-configured hexadecimal format.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static QWordSpec hex(String name) {
		return new QWordSpec(name);
	}

	/**
	 * Convenience function for constructing a {@linkplain QWordSpec} instance with pre-configured decimal format and
	 * size renderer.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static QWordSpec size(Supplier<String> name) {
		return size(new QWordSpec(name));
	}

	/**
	 * Convenience function for constructing a {@linkplain QWordSpec} instance with pre-configured decimal format and
	 * size renderer.
	 *
	 * @param name the attribute's name.
	 * @return the created spec instance.
	 */
	public static QWordSpec size(String name) {
		return size(new QWordSpec(name));
	}

	private static QWordSpec size(QWordSpec spec) {
		spec.format(PrettyFormat.LONG_FORMATTER);
		spec.renderer(SizeRenderer.LONG_RENDERER);
		return spec;
	}

	@Override
	protected int size() {
		return 4;
	}

	@Override
	protected Long decodeValue(ByteBuffer buffer) {
		return buffer.getLong();
	}

}
