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
package de.carne.filescanner.provider.lzma;

import java.io.IOException;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.spi.Format;

/**
 * <a href="https://en.wikipedia.org/wiki/Lempel-Ziv-Markov_chain_algorithm">LZMA file format</a> decoder.
 */
public class LzmaFormat extends Format {

	/**
	 * Format name.
	 */
	public static final String FORMAT_NAME = "LZMA archive";

	private final LzmaFormatSpecDefinition formatSpecDefinition;

	/**
	 * Constructs a new {@linkplain LzmaFormat} instance.
	 */
	public LzmaFormat() {
		super(FORMAT_NAME);
		this.formatSpecDefinition = new LzmaFormatSpecDefinition();
		this.formatSpecDefinition.load();
		registerHeaderSpec(this.formatSpecDefinition.headerSpec());
		// To reduce the number of false positives this format is considered absolute
		setAbsolute(true);
	}

	@Override
	public FileScannerResult decode(FileScannerResultDecodeContext context) throws IOException {
		return context.decodeComposite(this.formatSpecDefinition.formatSpec());
	}

}
