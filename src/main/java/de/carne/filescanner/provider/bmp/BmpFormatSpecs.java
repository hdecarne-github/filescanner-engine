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

import de.carne.filescanner.engine.format.ByteArraySpec;
import de.carne.filescanner.engine.format.ByteRangeSpec;
import de.carne.filescanner.engine.format.ConditionalSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.DWordSymbolRenderer;
import de.carne.filescanner.engine.format.FixedArraySpec;
import de.carne.filescanner.engine.format.FormatSpec;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.format.WordSymbolRenderer;
import de.carne.filescanner.engine.transfer.RawFileScannerResultExporter;

final class BmpFormatSpecs {

	private BmpFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "BMP image data";

	// Symbols and flags
	static final WordSymbolRenderer BI_BIT_COUNT_SYMBOLS = new WordSymbolRenderer();

	static {
		BI_BIT_COUNT_SYMBOLS.put((short) 0, "JPEG/PNG image");
		BI_BIT_COUNT_SYMBOLS.put((short) 1, "Monochrome bitmap");
		BI_BIT_COUNT_SYMBOLS.put((short) 4, "16 color bitmap");
		BI_BIT_COUNT_SYMBOLS.put((short) 8, "256 color bitmap");
		BI_BIT_COUNT_SYMBOLS.put((short) 16, "16 bit true color bitmap");
		BI_BIT_COUNT_SYMBOLS.put((short) 24, "24 bit true color Monochrome bitmap");
		BI_BIT_COUNT_SYMBOLS.put((short) 32, "32 bit true color Monochrome bitmap");
	}

	static final DWordSymbolRenderer BI_COMPRESSION_SYMBOLS = new DWordSymbolRenderer();

	static {
		BI_COMPRESSION_SYMBOLS.put(0, "BI_RGB");
		BI_COMPRESSION_SYMBOLS.put(1, "BI_RLE8");
		BI_COMPRESSION_SYMBOLS.put(2, "BI_RLE4");
		BI_COMPRESSION_SYMBOLS.put(3, "BI_BITFIELDS");
		BI_COMPRESSION_SYMBOLS.put(4, "BI_JPEG");
		BI_COMPRESSION_SYMBOLS.put(5, "BI_PNG");
	}

	// Format specs
	static final StructSpec BMP_FILE_HEADER;
	static final DWordSpec BF_OFF_BITS = DWordSpec.hex("bfOffBits");

	static {
		StructSpec fileHeader = new StructSpec();

		fileHeader.result("BITMAPFILEHEADER");
		fileHeader.add(WordSpec.hex("bfType")).validate((short) 0x4d42);
		fileHeader.add(DWordSpec.size("bfSize"));
		fileHeader.add(WordSpec.hex("bfReserved1")).validate((short) 0x0000);
		fileHeader.add(WordSpec.hex("bfReserved2")).validate((short) 0x0000);
		fileHeader.add(BF_OFF_BITS);
		BMP_FILE_HEADER = fileHeader;
	}

	static final StructSpec BMP_INFO_HEADER;
	static final DWordSpec BI_WIDTH = DWordSpec.dec("biWidth");
	static final DWordSpec BI_HEIGHT = DWordSpec.dec("biHeight");
	static final WordSpec BI_BIT_COUNT = WordSpec.dec("biBitCount");
	static final DWordSpec BI_COMPRESSION = DWordSpec.hex("biCompression");
	static final DWordSpec BI_SIZE_IMAGE = DWordSpec.size("biSizeImage");
	static final DWordSpec BI_CLR_USED = DWordSpec.dec("biClrUsed");

	static {
		StructSpec infoHeader = new StructSpec();

		infoHeader.result("BITMAPINFOHEADER");
		infoHeader.add(DWordSpec.size("biSize")).validate(40);
		infoHeader.add(BI_WIDTH);
		infoHeader.add(BI_HEIGHT);
		infoHeader.add(WordSpec.dec("biPlanes")).validate((short) 1);
		infoHeader.add(BI_BIT_COUNT).validate(BI_BIT_COUNT_SYMBOLS.keySet()).renderer(BI_BIT_COUNT_SYMBOLS);
		infoHeader.add(BI_COMPRESSION).renderer(BI_COMPRESSION_SYMBOLS);
		infoHeader.add(BI_SIZE_IMAGE);
		infoHeader.add(DWordSpec.dec("biXPelsPerMeter"));
		infoHeader.add(DWordSpec.dec("biYPelsPerMeter"));
		infoHeader.add(BI_CLR_USED);
		infoHeader.add(DWordSpec.dec("biClrImportant"));
		BMP_INFO_HEADER = infoHeader;
	}

	static final StructSpec BIT_FIELDS;

	static {
		StructSpec bitFields = new StructSpec();

		bitFields.result("BITFIELDS");
		bitFields.add(DWordSpec.hex("red"));
		bitFields.add(DWordSpec.hex("green"));
		bitFields.add(DWordSpec.hex("blue"));
		BIT_FIELDS = bitFields;
	}

	static final FixedArraySpec RGB_QUAD_COLOR_TABLE;

	static {
		ByteArraySpec rgbQuadSpec = new ByteArraySpec("").size(4);
		FixedArraySpec colorTable = new FixedArraySpec("color[%1$d]", rgbQuadSpec);

		colorTable.result("COLORTABLE");
		colorTable.size(BmpFormatSpecs::getColorTableSize);
		RGB_QUAD_COLOR_TABLE = colorTable;
	}

	static final StructSpec IMAGE_DATA;

	static {
		StructSpec imageData = new StructSpec();

		imageData.result("IMAGEDATA");
		imageData.add(new ByteRangeSpec("image bytes").size(BmpFormatSpecs::getImageSize));
		IMAGE_DATA = imageData;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.export(RawFileScannerResultExporter.IMAGE_BMP_EXPORTER);
		formatSpec.result(FORMAT_NAME);
		formatSpec.add(BMP_FILE_HEADER);
		formatSpec.add(BMP_INFO_HEADER);
		formatSpec.add(new ConditionalSpec(BmpFormatSpecs::getColorTableSpec));
		formatSpec.add(IMAGE_DATA);
		formatSpec.render(RawFileScannerResultExporter.IMAGE_BMP_EXPORTER);
		FORMAT_SPEC = formatSpec;
	}

	// Setup bindings
	static {
		BF_OFF_BITS.bind(FORMAT_SPEC);
		BI_WIDTH.bind(FORMAT_SPEC);
		BI_HEIGHT.bind(FORMAT_SPEC);
		BI_BIT_COUNT.bind(FORMAT_SPEC);
		BI_COMPRESSION.bind(FORMAT_SPEC);
		BI_SIZE_IMAGE.bind(FORMAT_SPEC);
		BI_CLR_USED.bind(FORMAT_SPEC);
	}

	// Helpers
	private static FormatSpec getColorTableSpec() {
		FormatSpec ctSpec;

		if (BI_COMPRESSION.get().intValue() == 3) {
			ctSpec = BIT_FIELDS;
		} else if (BI_BIT_COUNT.get().intValue() > 8) {
			return FormatSpecs.EMPTY;
		} else {
			return RGB_QUAD_COLOR_TABLE;
		}
		return ctSpec;
	}

	private static int getColorTableSize() {
		int ctSize = BI_CLR_USED.get().intValue();

		if (ctSize == 0) {
			ctSize = 2 << BI_BIT_COUNT.get().intValue();
		}
		return ctSize;
	}

	private static int getImageSize() {
		int sizeImage = BI_SIZE_IMAGE.get().intValue();

		if (sizeImage <= 0) {
			sizeImage = (((BI_WIDTH.get().intValue() * BI_BIT_COUNT.get().intValue() + 31) >> 5) << 2)
					* Math.abs(BI_HEIGHT.get().intValue());
		}
		return sizeImage;
	}

}
