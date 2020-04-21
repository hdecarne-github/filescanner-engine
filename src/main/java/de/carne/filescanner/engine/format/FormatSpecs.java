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

import java.nio.ByteBuffer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Utility class providing {@linkplain FormatSpec} related functions.
 */
public final class FormatSpecs {

	/**
	 * The maximum match size.
	 */
	public static final long MAX_MATCH_SIZE = 0x100000l;

	private FormatSpecs() {
		// Prevent instantiation
	}

	/**
	 * Empty {@linkplain CompositeSpec} with no decode result.
	 */
	public static final CompositeSpec EMPTY = new EmptySpec();

	/**
	 * Checks whether a {@linkplain FormatSpec} is a result.
	 *
	 * @param spec the {@linkplain FormatSpec} to check.
	 * @return {@code true} if the submitted {@linkplain FormatSpec} is a result.
	 */
	public static boolean isResult(FormatSpec spec) {
		return spec instanceof CompositeSpec && ((CompositeSpec) spec).isResult();
	}

	/**
	 * Checks whether the given size attribute can be considered fixed size for format matching.
	 * <p>
	 * A size attribute is considered fixed size if it is a static value and does not exceed
	 * {@linkplain #MAX_MATCH_SIZE}.
	 * </p>
	 *
	 * @param size the size attribute to check.
	 * @return {@code true} if the given size attribute can be considered fixed size.
	 * @see FormatSpec#isFixedSize()
	 */
	public static boolean isFixedSize(Supplier<? extends Number> size) {
		return size instanceof FinalSupplier && isValidSize(size.get().longValue());
	}

	/**
	 * Gets the match size defined by the given size attribute.
	 *
	 * @param size the size attribute to check.
	 * @return the size attribute's value or {@code 0} if the size attribute is not a fixed size attribute.
	 * @see #isFixedSize(Supplier)
	 * @see FormatSpec#matchSize()
	 */
	public static int matchSize(Supplier<? extends Number> size) {
		int matchSize = 0;

		if (size instanceof FinalSupplier) {
			Number sizeValue = size.get();

			if (isValidSize(sizeValue.longValue())) {
				matchSize = sizeValue.intValue();
			}
		}
		return matchSize;
	}

	/**
	 * Checks whether the input data size matches by checking it's size against the given size attribute.
	 *
	 * @param buffer the input data to match.
	 * @param size the required input data size.
	 * @return {@code true} if the input data matches.
	 * @see FormatSpec#matches(ByteBuffer)
	 */
	public static boolean matches(ByteBuffer buffer, Supplier<? extends Number> size) {
		return matches(buffer, size, (b, s) -> {
			b.position(b.position() + s);
			return true;
		});
	}

	/**
	 * Checks whether the input data size matches by checking it's size against the given size attribute as well as the
	 * given matcher function.
	 *
	 * @param buffer the input data to match.
	 * @param size the required input data size.
	 * @param matcher the matcher function to invoke.
	 * @return {@code true} if the input data matches.
	 * @see FormatSpec#matches(ByteBuffer)
	 */
	public static boolean matches(ByteBuffer buffer, Supplier<? extends Number> size, Predicate<ByteBuffer> matcher) {
		return matches(buffer, size, (b, s) -> matcher.test(b));
	}

	/**
	 * Checks whether the input data size matches by checking it's size against the given size attribute as well as the
	 * given matcher function.
	 *
	 * @param buffer the input data to match.
	 * @param size the required input data size.
	 * @param matcher the matcher function to invoke.
	 * @return {@code true} if the input data matches.
	 * @see FormatSpec#matches(ByteBuffer)
	 */
	public static boolean matches(ByteBuffer buffer, Supplier<? extends Number> size,
			BiPredicate<ByteBuffer, Integer> matcher) {
		boolean match = true;

		if (size instanceof FinalSupplier) {
			match = false;

			Number sizeValue = size.get();

			if (isValidSize(sizeValue.longValue())) {
				int matchSize = sizeValue.intValue();

				if (matchSize <= buffer.remaining()) {
					match = matcher.test(buffer, matchSize);
				}
			}
		}
		return match;
	}

	private static boolean isValidSize(long value) {
		return 0 <= value && value <= MAX_MATCH_SIZE;
	}

}
