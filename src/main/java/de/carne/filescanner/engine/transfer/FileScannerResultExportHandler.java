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
package de.carne.filescanner.engine.transfer;

import de.carne.filescanner.engine.FileScannerResult;

/**
 * Interface used for exporting a file scanner result.
 */
public interface FileScannerResultExportHandler extends FileScannerResultExporter {

	/**
	 * Gets the name of this {@linkplain FileScannerResultExportHandler}.
	 *
	 * @return the name of this {@linkplain FileScannerResultExportHandler}.
	 */
	String name();

	/**
	 * Gets the transfer data type generated by this exporter.
	 *
	 * @return the transfer data type generated by this exporter.
	 */
	TransferType transferType();

	/**
	 * Gets the default file extension to use for a file export.
	 * 
	 * @return the default file extension to use for a file export.
	 */
	String defaultFileExtension();

	/**
	 * Gets the default file name to use for a file export.
	 *
	 * @param result the {@linkplain FileScannerResult} instance to export.
	 * @return the default file name to use for a file export.
	 */
	String defaultFileName(FileScannerResult result);

}
