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
package de.carne.filescanner.engine;

/**
 * Callback interface receiving {@linkplain FileScanner} status information during a scan operation.
 */
public interface FileScannerStatus {

	/**
	 * Called whenever a new scan is started.
	 *
	 * @param scanner the calling {@linkplain FileScanner}.
	 */
	void scanStarted(FileScanner scanner);

	/**
	 * Called whenever a scan is finished.
	 *
	 * @param scanner the calling {@linkplain FileScanner}.
	 */
	void scanFinished(FileScanner scanner);

	/**
	 * Called periodically during a scan.
	 *
	 * @param scanner the calling {@linkplain FileScanner}.
	 * @param progress the current {@linkplain FileScannerProgress}.
	 */
	void scanProgress(FileScanner scanner, FileScannerProgress progress);

	/**
	 * Called whenever new scan results are available.
	 * <p>
	 * The submitted result object is the root node of the updated or created sub-tree within the overall result tree.
	 *
	 * @param scanner the calling {@linkplain FileScanner}.
	 * @param result the new or updated scan result.
	 */
	void scanResult(FileScanner scanner, FileScannerResult result);

	/**
	 * Called whenever an exception occurs.
	 *
	 * @param scanner the calling {@linkplain FileScanner}.
	 * @param cause the causing exception.
	 */
	void scanException(FileScanner scanner, Exception cause);

}
