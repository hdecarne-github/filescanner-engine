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

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Supplier;

import de.carne.filescanner.engine.format.spec.ByteArraySpec;
import de.carne.filescanner.engine.format.spec.ByteRangeSpec;
import de.carne.filescanner.engine.format.spec.ByteSpec;
import de.carne.filescanner.engine.format.spec.ByteSymbolRenderer;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.StructSpec;
import de.carne.filescanner.engine.format.spec.UnionSpec;
import de.carne.filescanner.engine.format.spec.VarArraySpec;
import de.carne.filescanner.engine.transfer.RawFileScannerResultExporter;

final class PngFormatSpecs {

	private PngFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "PNG image data";

	// Symbols and flags
	static final ByteSymbolRenderer COLOR_TYPE_SYMBOLS = new ByteSymbolRenderer();

	static {
		COLOR_TYPE_SYMBOLS.put((byte) 0, "Grayscale");
		COLOR_TYPE_SYMBOLS.put((byte) 2, "RGB");
		COLOR_TYPE_SYMBOLS.put((byte) 3, "Palette");
		COLOR_TYPE_SYMBOLS.put((byte) 4, "Grayscale + Alpha");
		COLOR_TYPE_SYMBOLS.put((byte) 6, "RGB + Alpha");
	}

	static final ByteSymbolRenderer COMPRESSION_METHOD_SYMBOLS = new ByteSymbolRenderer();

	static {
		COMPRESSION_METHOD_SYMBOLS.put((byte) 0, "deflate/inflate compression");
	}

	static final ByteSymbolRenderer FILTER_METHOD_SYMBOLS = new ByteSymbolRenderer();

	static {
		FILTER_METHOD_SYMBOLS.put((byte) 0, "adaptive filtering");
	}

	static final ByteSymbolRenderer INTERLACE_METHOD_SYMBOLS = new ByteSymbolRenderer();

	static {
		INTERLACE_METHOD_SYMBOLS.put((byte) 0, "no interlace");
		INTERLACE_METHOD_SYMBOLS.put((byte) 1, "Adam7 interlace");
	}

	// Format specs
	static final StructSpec PNG_FILE_SIGNATURE;

	static {
		StructSpec fileSignature = new StructSpec();

		fileSignature.byteOrder(ByteOrder.BIG_ENDIAN);
		fileSignature.result("PNG file signature");
		fileSignature.add(new ByteArraySpec("signature").size(8).validate(
				value -> Arrays.equals(value, new byte[] { (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a })));
		PNG_FILE_SIGNATURE = fileSignature;
	}

	static final StructSpec GENERIC_CHUNK;
	static final DWordSpec GENERIC_CHUNK_LENGTH = DWordSpec.size("Length");
	static final DWordSpec GENERIC_CHUNK_TYPE = DWordSpec.hex("Chunk Type");

	static {
		StructSpec genericChunk = new StructSpec();

		genericChunk.byteOrder(ByteOrder.BIG_ENDIAN);
		genericChunk.result(() -> formatChunkType(GENERIC_CHUNK_TYPE));
		genericChunk.add(GENERIC_CHUNK_LENGTH);
		genericChunk.add(GENERIC_CHUNK_TYPE).validate(value -> value.intValue() != 0x49454e44);
		genericChunk.add(new ByteRangeSpec("Chunk Data")).size(GENERIC_CHUNK_LENGTH);
		genericChunk.add(DWordSpec.hex("CRC"));
		GENERIC_CHUNK = genericChunk;
	}

	static final StructSpec IHDR_CHUNK;

	static {
		StructSpec ihdrChunk = new StructSpec();

		ihdrChunk.byteOrder(ByteOrder.BIG_ENDIAN);
		ihdrChunk.result("IHDR chunk");
		ihdrChunk.add(DWordSpec.size("Length")).validate(13);
		ihdrChunk.add(DWordSpec.hex("Chunk Type")).validate(0x49484452);
		ihdrChunk.add(DWordSpec.dec("Width"));
		ihdrChunk.add(DWordSpec.dec("Height"));
		ihdrChunk.add(ByteSpec.dec("Bit depth"));
		ihdrChunk.add(ByteSpec.hex("Color type")).renderer(COLOR_TYPE_SYMBOLS);
		ihdrChunk.add(ByteSpec.hex("Compression method")).renderer(COMPRESSION_METHOD_SYMBOLS);
		ihdrChunk.add(ByteSpec.hex("Filter method")).renderer(FILTER_METHOD_SYMBOLS);
		ihdrChunk.add(ByteSpec.hex("Interlace method")).renderer(INTERLACE_METHOD_SYMBOLS);
		ihdrChunk.add(DWordSpec.hex("CRC"));
		IHDR_CHUNK = ihdrChunk;
	}

	static final StructSpec IEND_CHUNK;

	static {
		StructSpec iendChunk = new StructSpec();

		iendChunk.byteOrder(ByteOrder.BIG_ENDIAN);
		iendChunk.result("IEND chunk");
		iendChunk.add(DWordSpec.size("Length")).validate(0);
		iendChunk.add(DWordSpec.hex("Chunk Type")).validate(0x49454e44);
		iendChunk.add(DWordSpec.hex("CRC"));
		IEND_CHUNK = iendChunk;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.byteOrder(ByteOrder.BIG_ENDIAN).export(RawFileScannerResultExporter.IMAGE_PNG_EXPORTER);
		formatSpec.result(FORMAT_NAME);
		formatSpec.add(PNG_FILE_SIGNATURE);

		UnionSpec chunkSpecs = new UnionSpec();

		chunkSpecs.add(IHDR_CHUNK);
		chunkSpecs.add(GENERIC_CHUNK);

		formatSpec.add(new VarArraySpec(chunkSpecs));
		formatSpec.add(IEND_CHUNK);
		formatSpec.render(RawFileScannerResultExporter.IMAGE_PNG_EXPORTER);
		FORMAT_SPEC = formatSpec;
	}

	// Setup bindings
	static {
		GENERIC_CHUNK_LENGTH.bind();
		GENERIC_CHUNK_TYPE.bind();
	}

	// Helpers
	private static String formatChunkType(Supplier<Integer> type) {
		int typeValue = type.get().intValue();
		StringBuilder typeString = new StringBuilder();

		typeString.append(mapChunkTypeChar((typeValue >>> 24) & 0xff));
		typeString.append(mapChunkTypeChar((typeValue >>> 16) & 0xff));
		typeString.append(mapChunkTypeChar((typeValue >>> 8) & 0xff));
		typeString.append(mapChunkTypeChar(typeValue & 0xff));
		typeString.append(" chunk");
		return typeString.toString();
	}

	private static char mapChunkTypeChar(int code) {
		return ((65 <= code && code <= 90) || (97 <= code && code <= 122) ? (char) code : '?');
	}

}
