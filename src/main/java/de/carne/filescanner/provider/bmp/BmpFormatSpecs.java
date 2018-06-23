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
package de.carne.filescanner.provider.bmp;

import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.DWordSymbolRenderer;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.transfer.RawFileScannerResultExporter;

final class BmpFormatSpecs {

	private BmpFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "BMP image data";

	// Symbols and flags
	static final DWordSymbolRenderer BIH_COMPRESSION_SYMBOLS = new DWordSymbolRenderer();

	static {
		BIH_COMPRESSION_SYMBOLS.put(0, "BI_RGB");
		BIH_COMPRESSION_SYMBOLS.put(1, "BI_RLE8");
		BIH_COMPRESSION_SYMBOLS.put(2, "BI_RLE4");
		BIH_COMPRESSION_SYMBOLS.put(3, "BI_BITFIELDS");
		BIH_COMPRESSION_SYMBOLS.put(4, "BI_JPEG");
		BIH_COMPRESSION_SYMBOLS.put(5, "BI_PNG");
	}

	// Format specs
	static final StructSpec BMP_FILE_HEADER;

	static {
		StructSpec fileHeader = new StructSpec();

		fileHeader.result("BITMAPFILEHEADER");
		fileHeader.add(WordSpec.hex("bfType")).validate((short) 0x4d42);
		fileHeader.add(DWordSpec.size("bfSize"));
		fileHeader.add(WordSpec.hex("bfReserved1")).validate((short) 0x0000);
		fileHeader.add(WordSpec.hex("bfReserved2")).validate((short) 0x0000);
		fileHeader.add(DWordSpec.hex("bfOffBits"));
		BMP_FILE_HEADER = fileHeader;
	}

	static final StructSpec BMP_INFO_HEADER;

	static {
		StructSpec infoHeader = new StructSpec();

		infoHeader.result("BITMAPINFOHEADER");
		infoHeader.add(DWordSpec.size("biSize")).validate(40);
		infoHeader.add(DWordSpec.dec("biWidth"));
		infoHeader.add(DWordSpec.dec("biHeight"));
		infoHeader.add(WordSpec.dec("biPlanes")).validate((short) 1);
		infoHeader.add(WordSpec.dec("biBitCount"));
		infoHeader.add(DWordSpec.hex("biCompression")).renderer(BIH_COMPRESSION_SYMBOLS);
		infoHeader.add(DWordSpec.size("biSizeImage"));
		infoHeader.add(DWordSpec.dec("biXPelsPerMeter"));
		infoHeader.add(DWordSpec.dec("biYPelsPerMeter"));
		infoHeader.add(DWordSpec.dec("biClrUsed"));
		infoHeader.add(DWordSpec.dec("biClrImportant"));
		BMP_INFO_HEADER = infoHeader;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.export(RawFileScannerResultExporter.IMAGE_BMP_EXPORTER);
		formatSpec.result(FORMAT_NAME);
		formatSpec.add(BMP_FILE_HEADER);
		formatSpec.add(BMP_INFO_HEADER);
		formatSpec.render(RawFileScannerResultExporter.IMAGE_BMP_EXPORTER);
		FORMAT_SPEC = formatSpec;
	}

	// Setup bindings

	// Helpers

}
