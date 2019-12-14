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
package de.carne.filescanner.provider.jbc;

import java.io.IOException;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.spi.Format;

/**
 * <a href="https://en.wikipedia.org/wiki/Java_class_file">Java Byte Code format</a> decoder.
 */
public class JbcFormat extends Format {

	private static final String FORMAT_NAME = "Java Byte Code";

	private final JbcFormatSpecDefinition formatSpecDefinition;

	/**
	 * Constructs a new {@linkplain JbcFormat} instance.
	 */
	public JbcFormat() {
		super(FORMAT_NAME);
		this.formatSpecDefinition = new JbcFormatSpecDefinition();
		this.formatSpecDefinition.load();
		registerHeaderSpec(this.formatSpecDefinition.headerSpec());
		setAbsolute(true);
	}

	@Override
	public FileScannerResult decode(FileScannerResultDecodeContext context) throws IOException {
		return context.decodeComposite(this.formatSpecDefinition.formatSpec());
	}

}
