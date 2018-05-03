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
package de.carne.filescanner.engine;

import java.io.IOException;

import de.carne.boot.check.Nullable;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.transfer.FileScannerResultOutput;

/**
 * {@linkplain FileScanner} result object.
 * <p>
 * Results are of different types ({@link Type}) and are ordered hierarchically. The resulting structure is:
 *
 * <pre>
 *  Input               	The root input
 *  |
 *  +-Format            	An identified format/file structure
 *  | |
 *  | +-Format          	A nested format/file structure
 *  | | |
 *  | | +-Encoded (Input)	A nested encoded data stream
 *  | | | |
 *  | | | +-Input       	The input data representing the decoded data stream
 *  | | |   |
 *  | | |   +-...       	Goes on recursively
 * </pre>
 */
public interface FileScannerResult {

	/**
	 * The different types of {@linkplain FileScannerResult} objects.
	 */
	enum Type {

		/**
		 * Raw input stream.
		 */
		INPUT,

		/**
		 * Recognized format element.
		 */
		FORMAT,

		/**
		 * Encoded input stream.
		 */
		ENCODED_INPUT

	}

	/**
	 * Gets this {@linkplain FileScannerResult}'s type.
	 *
	 * @return this {@linkplain FileScannerResult}'s type.
	 */
	Type type();

	/**
	 * Gets this {@linkplain FileScannerResult} input.
	 *
	 * @return this {@linkplain FileScannerResult} input.
	 */
	FileScannerInput input();

	/**
	 * Gets this {@linkplain FileScannerResult} name.
	 *
	 * @return this {@linkplain FileScannerResult} name.
	 */
	String name();

	/**
	 * Gets this {@linkplain FileScannerResult} start position (within it's input).
	 *
	 * @return this {@linkplain FileScannerResult} start position (within it's input).
	 */
	long start();

	/**
	 * Gets this {@linkplain FileScannerResult} end position (within it's input).
	 *
	 * @return this {@linkplain FileScannerResult} end position (within it's input).
	 */
	long end();

	/**
	 * Gets this {@linkplain FileScannerResult} size.
	 *
	 * @return this {@linkplain FileScannerResult} size.
	 */
	long size();

	/**
	 * Gets this {@linkplain FileScannerResult} children result count.
	 *
	 * @return this {@linkplain FileScannerResult} children result count.
	 */
	int childrenCount();

	/**
	 * Gets this {@linkplain FileScannerResult} children results.
	 *
	 * @return this {@linkplain FileScannerResult} children results.
	 */
	FileScannerResult[] children();

	/**
	 * Renders this {@linkplain FileScannerResult}.
	 *
	 * @param out the {@linkplain FileScannerResultOutput} to render to.
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread has been interrupted.
	 */
	void render(FileScannerResultOutput out) throws IOException, InterruptedException;

	/**
	 * Sets the custom data object associated with this {@linkplain FileScannerResult}.
	 *
	 * @param data the data object to set.
	 */
	void setData(Object data);

	/**
	 * Gets the previously set custom data object associated with this {@linkplain FileScannerResult}.
	 *
	 * @param <T> the actual data type.
	 * @param dataType the actual data type to retrieve.
	 * @return the previously set custom data object associated with this {@linkplain FileScannerResult}.
	 */
	@Nullable
	<T> T getData(Class<T> dataType);

	/**
	 * Gets the unique key of this {@linkplain FileScannerResult}.
	 * 
	 * @return the unique key of this {@linkplain FileScannerResult}.
	 */
	byte[] key();

}
