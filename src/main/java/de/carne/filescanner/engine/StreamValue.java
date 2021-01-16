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

import java.io.IOException;
import java.io.InputStream;

import de.carne.filescanner.engine.input.FileScannerInputRange;

/**
 * {@linkplain InputStream} based class providing access to all kind if byte range based values.
 */
public class StreamValue {

	private final FileScannerInputRange inputRange;
	private final long start;
	private final long end;

	StreamValue(FileScannerInputRange inputRange, long start, long end) {
		this.inputRange = inputRange;
		this.start = start;
		this.end = end;
	}

	/**
	 * Gets the size of the byte range representing this value.
	 *
	 * @return the size of the byte range representing this value.
	 */
	public long size() {
		return this.end - this.start;
	}

	/**
	 * Gets an {@linkplain InputStream} instance to access this value.
	 * 
	 * @return an {@linkplain InputStream} instance to access this value.
	 * @throws IOException if an I/O error occurs while accessing this value.
	 */
	public InputStream stream() throws IOException {
		return this.inputRange.inputStream(this.start, this.end);
	}

	@Override
	public String toString() {
		return "{ ... }";
	}

}
