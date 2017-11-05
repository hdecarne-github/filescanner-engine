/*
 * Copyright (c) 2007-2017 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.provider.spi;

import java.nio.ByteOrder;
import java.util.ServiceLoader;

/**
 * Base class for all decodable file formats.
 */
public abstract class Format {

	private final String name;
	private final ByteOrder byteOrder;

	/**
	 * Load all registered {@linkplain Format} classes.
	 *
	 * @return The loaded {@linkplain Format} classes.
	 */
	public static Iterable<Format> loadFormats() {
		return ServiceLoader.load(Format.class);
	}

	/**
	 * Construct {@linkplain Format}.
	 *
	 * @param name The format name.
	 * @param byteOrder The format's byte order.
	 */
	protected Format(String name, ByteOrder byteOrder) {
		this.name = name;
		this.byteOrder = byteOrder;
	}

	/**
	 * Get the format name.
	 *
	 * @return The format name.
	 */
	public final String name() {
		return this.name;
	}

	/**
	 * Get the format's byte order.
	 *
	 * @return The format's byte order.
	 */
	public final ByteOrder byteOrder() {
		return this.byteOrder;
	}

}
