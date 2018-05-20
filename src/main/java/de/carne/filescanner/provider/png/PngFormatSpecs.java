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
package de.carne.filescanner.provider.png;

import java.util.Arrays;

import de.carne.filescanner.engine.format.ByteArraySpec;
import de.carne.filescanner.engine.format.StructSpec;

final class PngFormatSpecs {

	private PngFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "PNG image";

	// Format specs
	static final StructSpec PNG_FILE_SIGNATURE;

	static {
		StructSpec fileSignature = new StructSpec();

		fileSignature.result("PNG file signature");
		fileSignature.add(new ByteArraySpec("signature").size(8).validate(
				value -> Arrays.equals(value, new byte[] { (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a })));
		PNG_FILE_SIGNATURE = fileSignature;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.result(FORMAT_NAME);
		formatSpec.add(PNG_FILE_SIGNATURE);
		FORMAT_SPEC = formatSpec;
	}

}
