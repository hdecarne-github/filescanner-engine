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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.TransferSource;

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
	 * Gets this {@linkplain FileScannerResult}'s input.
	 *
	 * @return this {@linkplain FileScannerResult}'s input.
	 */
	FileScannerInput input();

	/**
	 * Gets this {@linkplain FileScannerResult}'s input result.
	 *
	 * @return this {@linkplain FileScannerResult}'s input result.
	 */
	FileScannerResult inputResult();

	/**
	 * Gets this {@linkplain FileScannerResult}'s name.
	 *
	 * @return this {@linkplain FileScannerResult}'s name.
	 */
	String name();

	/**
	 * Gets this {@linkplain FileScannerResult}'s start position (within it's input).
	 *
	 * @return this {@linkplain FileScannerResult}'s start position (within it's input).
	 */
	long start();

	/**
	 * Gets this {@linkplain FileScannerResult}'s end position (within it's input).
	 *
	 * @return this {@linkplain FileScannerResult}'s end position (within it's input).
	 */
	long end();

	/**
	 * Gets this {@linkplain FileScannerResult}'s size.
	 *
	 * @return this {@linkplain FileScannerResult}'s size.
	 */
	long size();

	/**
	 * Gets this {@linkplain FileScannerResult}'s children result count.
	 *
	 * @return this {@linkplain FileScannerResult}'s children result count.
	 */
	int childrenCount();

	/**
	 * Gets this {@linkplain FileScannerResult}'s children results.
	 *
	 * @return this {@linkplain FileScannerResult}'s children results.
	 */
	@NonNull
	FileScannerResult[] children();

	/**
	 * Renders this {@linkplain FileScannerResult}.
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @param renderHandler the {@linkplain FileScannerResultRenderHandler} to use for rendering. May {@code null} to
	 * use the default handler.
	 * @param offset the offset to start rendering at.
	 * @return the number of decoded bytes.
	 * @throws IOException if an I/O error occurs.
	 */
	long render(RenderOutput out, @Nullable FileScannerResultRenderHandler renderHandler, long offset)
			throws IOException;

	/**
	 * Gets the available {@linkplain FileScannerResultExportHandler} instances for this {@linkplain FileScannerResult}.
	 *
	 * @return the available {@linkplain FileScannerResultExportHandler}.
	 */
	@NonNull
	FileScannerResultExportHandler[] exportHandlers();

	/**
	 * Exports this {@linkplain FileScannerResult}.
	 *
	 * @param exportHandler the {@linkplain FileScannerResultExportHandler} instance to use.
	 * @return the {@linkplain TransferSource} instance representing the exported data.
	 * @throws IOException if an I/O error occurs.
	 */
	TransferSource export(FileScannerResultExportHandler exportHandler) throws IOException;

	/**
	 * Sets a custom data object associated with this {@linkplain FileScannerResult}.
	 *
	 * @param key the key to associate the data object with.
	 * @param data the data object to set.
	 * @see #getData(Object, Class)
	 */
	void setData(Object key, @Nullable Object data);

	/**
	 * Gets the previously set custom data object associated with this {@linkplain FileScannerResult}.
	 *
	 * @param <T> the actual data type.
	 * @param key the key to get the data object for.
	 * @param dataType the actual data type to retrieve.
	 * @return the previously set custom data object associated with this {@linkplain FileScannerResult}.
	 */
	@Nullable
	<T> T getData(Object key, Class<T> dataType);

	/**
	 * Gets the unique key of this {@linkplain FileScannerResult}.
	 *
	 * @return the unique key of this {@linkplain FileScannerResult}.
	 */
	byte[] key();

}
