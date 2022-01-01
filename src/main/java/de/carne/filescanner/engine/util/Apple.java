/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Utility class providing Apple related functions.
 */
public final class Apple {

	private Apple() {
		// Prevent instantiation
	}

	private static final LocalDateTime BASE_LOCAL_DATE_TIME = LocalDateTime.of(1904, 1, 1, 0, 0, 0);

	/**
	 * Converts Apple date value to a {@linkplain LocalDateTime} instance.
	 *
	 * @param date the Apple date value to convert.
	 * @return the created {@linkplain LocalDateTime} instance
	 */
	public static LocalDateTime appleDateToLocalDateTime(int date) {
		return BASE_LOCAL_DATE_TIME.plus(Integer.toUnsignedLong(date), ChronoUnit.SECONDS);
	}

}
