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
package de.carne.filescanner.provider.png;

import java.io.IOException;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.spi.Format;

/**
 * <a href="https://en.wikipedia.org/wiki/Portable_Network_Graphics">Portable Network Graphics</a> decoder.
 */
public class PngFormat extends Format {

	private static final String FORMAT_NAME = "PNG image data";

	private final PngFormatSpecDefinition pngFormatSpecDefinition;

	/**
	 * Constructs a new {@linkplain PngFormat} instance.
	 */
	public PngFormat() {
		super(FORMAT_NAME);
		this.pngFormatSpecDefinition = new PngFormatSpecDefinition();
		this.pngFormatSpecDefinition.load();
		registerHeaderSpec(this.pngFormatSpecDefinition.pngHeaderSpec());
	}

	@Override
	public FileScannerResult decode(FileScannerResultDecodeContext context) throws IOException {
		return context.decodeComposite(this.pngFormatSpecDefinition.pngFormatSpec());
	}

}
