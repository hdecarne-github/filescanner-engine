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
package de.carne.filescanner.engine.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Utility class used for {@code Supplier} instances that actually provide a final value and therefore can be displayed
 * during debugging or logging by an informative string representation.
 *
 * @param <T> the supplied type.
 */
public class FinalSupplier<T> implements Supplier<T> {

	private final T value;

	private FinalSupplier(T value) {
		this.value = value;
	}

	/**
	 * Wrap the given value in a {@linkplain FinalSupplier} instance.
	 *
	 * @param <T> the type to wrap.
	 * @param value the value to wrap.
	 * @return the created {@linkplain FinalSupplier} instance.
	 */
	public static <T> FinalSupplier<T> of(T value) {
		return new FinalSupplier<>(value);
	}

	@Override
	public T get() {
		return this.value;
	}

	@Override
	public String toString() {
		return Objects.toString(this.value);
	}

}
