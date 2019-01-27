/*
 * Copyright (c) 2007-2019 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.provider.zip;

import java.io.IOException;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.spi.Format;

/**
 * <a href="https://en.wikipedia.org/wiki/Zip_(file_format)">Zip file format</a> decoder.
 */
public class ZipFormat extends Format {

	private static final String FORMAT_NAME = "ZIP archive";

	private final ZipFormatSpecDefinition zipFormatSpecDefinition;

	/**
	 * Constructs a new {@linkplain ZipFormat} instance.
	 */
	public ZipFormat() {
		super(FORMAT_NAME);
		this.zipFormatSpecDefinition = new ZipFormatSpecDefinition();
		this.zipFormatSpecDefinition.load();
		registerHeaderSpec(this.zipFormatSpecDefinition.getLocalFileHeaderSpec());
	}

	@Override
	public FileScannerResult decode(FileScannerResultDecodeContext context) throws IOException {
		return context.decodeComposite(this.zipFormatSpecDefinition.getZipFormatSpec());
	}

}
