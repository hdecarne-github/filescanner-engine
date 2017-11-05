/*
 * Copyright (c) 2007-2017 Holger de Carne and contributors, All Rights Reserved.
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

import java.nio.ByteOrder;

import de.carne.filescanner.provider.spi.Format;

/**
 * <a href="https://en.wikipedia.org/wiki/Zip_(file_format)">Zip file format</a> decoder.
 */
public class ZipFormat extends Format {

	/**
	 * Construct {@linkplain ZipFormat}.
	 */
	public ZipFormat() {
		super(ZipFormatSpecs.NAME_ZIP_FORMAT, ByteOrder.LITTLE_ENDIAN);
	}

}
