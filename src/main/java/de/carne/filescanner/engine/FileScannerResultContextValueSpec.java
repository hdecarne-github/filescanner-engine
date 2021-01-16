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
package de.carne.filescanner.engine;

import java.util.function.Supplier;

import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Context value specification.
 *
 * @param <T> The actual value type.
 */
public class FileScannerResultContextValueSpec<T> implements Supplier<T> {

	private final Class<T> type;
	private final Supplier<String> name;

	/**
	 * Constructs a new {@linkplain FileScannerResultContextValueSpec} instance.
	 *
	 * @param type the value type.
	 * @param name the value name.
	 */
	public FileScannerResultContextValueSpec(Class<T> type, String name) {
		this(type, FinalSupplier.of(name));
	}

	/**
	 * Constructs a new {@linkplain FileScannerResultContextValueSpec} instance.
	 *
	 * @param type the value type.
	 * @param name the value name.
	 */
	public FileScannerResultContextValueSpec(Class<T> type, Supplier<String> name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * Gets the value's type.
	 *
	 * @return the value's type.
	 */
	public final Class<T> type() {
		return this.type;
	}

	/**
	 * Gets the value's name.
	 *
	 * @return the value's name.
	 */
	public final String name() {
		return this.name.get();
	}

	@Override
	public T get() {
		return FileScannerResultContext.get().getValue(this);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append('(');
		formatType(buffer, this.type);
		buffer.append(')');
		formatName(buffer, this.name);
		return buffer.toString();
	}

	protected void formatType(StringBuilder buffer, Class<?> formatType) {
		Class<?> componenentType = formatType.getComponentType();

		if (componenentType != null) {
			formatType(buffer, componenentType);
			buffer.append("[]");
		} else {
			buffer.append(formatType.getName());
		}
	}

	protected void formatName(StringBuilder buffer, Supplier<String> formatName) {
		buffer.append('\'').append(formatName).append('\'');
	}

}
