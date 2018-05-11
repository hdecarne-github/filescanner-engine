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

import java.io.IOException;
import java.io.InputStream;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultExporter;

/**
 * {@linkplain FileScannerResultExporter} implementation for raw data export.
 */
public class RawFileScannerResultExporter implements FileScannerResultExporter {

	/**
	 * The single instance of this exporter.
	 */
	public static final RawFileScannerResultExporter EXPORTER = new RawFileScannerResultExporter();

	private static final String NAME = "Raw";

	private RawFileScannerResultExporter() {
		// prevent instantiation
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public Type type() {
		return Type.APPLICATION_BINARY;
	}

	@Override
	public String defaultStreamName(FileScannerResult result) {
		String streamName;

		if (result.type() == FileScannerResult.Type.INPUT) {
			streamName = result.input().name();
		} else {
			// TODO: Mangle result name
			streamName = result.name() + ".bin";
		}
		return streamName;
	}

	@Override
	public InputStream stream(FileScannerResult result) throws IOException {
		return result.input().inputStream(result.start(), result.end());
	}

}
