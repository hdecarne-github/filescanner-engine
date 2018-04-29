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
package de.carne.filescanner.provider.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;

import de.carne.boot.Exceptions;

/**
 * Utility class providing DOS related functions.
 */
public final class Dos {

	private Dos() {
		// Prevent instantiation
	}

	private static final LocalTime DEFAULT_LOCAL_TIME = LocalTime.of(0, 0, 0);

	/**
	 * Converts DOS time value to a {@linkplain LocalTime} instance.
	 *
	 * @param time the DOS time value to convert.
	 * @return the created {@linkplain LocalTime} instance
	 */
	public static LocalTime dosTimeToLocalTime(short time) {
		int hour = ((time & 0xffff) >>> 11) & 0x001f;
		int minute = ((time & 0xffff) >>> 5) & 0x003f;
		int second = (time & 0x001f) * 2;
		LocalTime localTime;

		try {
			localTime = LocalTime.of(hour, minute, second);
		} catch (DateTimeException e) {
			Exceptions.warn(e);
			localTime = DEFAULT_LOCAL_TIME;
		}
		return localTime;
	}

	private static final LocalDate DEFAULT_LOCAL_DATE = LocalDate.of(1980, 1, 1);

	/**
	 * Converts DOS data value to a {@linkplain LocalDate} instance.
	 *
	 * @param date the DOS date value to convert.
	 * @return the created {@linkplain LocalDate} instance
	 */
	public static LocalDate dosDateToLocalDate(short date) {
		int year = 1980 + (((date & 0xffff) >>> 9) & 0x007f);
		int month = ((date & 0xffff) >>> 5) & 0x000f;
		int day = date & 0x001f;
		LocalDate localDate;

		try {
			localDate = LocalDate.of(year, month, day);
		} catch (DateTimeException e) {
			Exceptions.warn(e);
			localDate = DEFAULT_LOCAL_DATE;
		}
		return localDate;
	}

}
