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
package de.carne.filescanner.engine.util;

import java.util.function.Supplier;

/**
 * Utility class used for static {@code Supplier<String>} instances with a proper {@linkplain #toString()} function.
 */
public class StringSupplier implements Supplier<String> {

	private final String string;

	private StringSupplier(String string) {
		this.string = string;
	}

	/**
	 * Wrap the given {@linkplain String} in a {@linkplain StringSupplier} instance.
	 *
	 * @param string the {@linkplain String} to wrap.
	 * @return the created {@linkplain StringSupplier} instance.
	 */
	public static StringSupplier of(String string) {
		return new StringSupplier(string);
	}

	@Override
	public String get() {
		return this.string;
	}

	@Override
	public String toString() {
		return this.string;
	}

}
