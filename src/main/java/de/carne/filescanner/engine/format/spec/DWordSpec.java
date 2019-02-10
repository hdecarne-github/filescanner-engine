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
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Double word (32-bit) format attribute specification.
 */
public final class DWordSpec extends NumberAttributeSpec<Integer> {

	/**
	 * Constructs a new {@linkplain DWordSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public DWordSpec(Supplier<String> name) {
		super(Integer.class, name);
		format(HexFormat.INT_FORMATTER);
	}

	/**
	 * Constructs a new {@linkplain DWordSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public DWordSpec(String name) {
		this(FinalSupplier.of(name));
	}

	@Override
	protected int size() {
		return Integer.BYTES;
	}

	@Override
	protected Integer decodeValue(ByteBuffer buffer) {
		return buffer.getInt();
	}

}
