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
package de.carne.filescanner.provider.jpeg;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Supplier;

import de.carne.filescanner.engine.format.spec.ByteArraySpec;
import de.carne.filescanner.engine.format.spec.ByteRangeSpec;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.StructSpec;
import de.carne.filescanner.engine.format.spec.SequenceSpec;
import de.carne.filescanner.engine.transfer.RawFileScannerResultExporter;

final class JpegFormatSpecs {

	private JpegFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "JPEG image data";

	// Format specs
	static final StructSpec PNG_FILE_SIGNATURE;

	static {
		StructSpec fileSignature = new StructSpec();

		fileSignature.byteOrder(ByteOrder.BIG_ENDIAN);
		fileSignature.result("PNG file signature");
		fileSignature.add(new ByteArraySpec("signature").size(8).validate(
				value -> Arrays.equals(value, new byte[] { (byte) 0x86, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a })));
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

	static final StructSpec IEND_CHUNK;
	static final DWordSpec IEND_CHUNK_LENGTH = DWordSpec.size("Length");
	static final DWordSpec IEND_CHUNK_TYPE = DWordSpec.hex("Chunk Type");

	static {
		StructSpec iendChunk = new StructSpec();

		iendChunk.byteOrder(ByteOrder.BIG_ENDIAN);
		iendChunk.result("IEND chunk");
		iendChunk.add(IEND_CHUNK_LENGTH);
		iendChunk.add(IEND_CHUNK_TYPE).validate(0x49454e44);
		iendChunk.add(new ByteRangeSpec("Chunk Data")).size(IEND_CHUNK_LENGTH);
		iendChunk.add(DWordSpec.hex("CRC"));
		IEND_CHUNK = iendChunk;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.byteOrder(ByteOrder.BIG_ENDIAN).export(RawFileScannerResultExporter.IMAGE_PNG_EXPORTER);
		formatSpec.result(FORMAT_NAME);
		formatSpec.add(PNG_FILE_SIGNATURE);
		formatSpec.add(new SequenceSpec(GENERIC_CHUNK));
		formatSpec.add(IEND_CHUNK);
		formatSpec.render(RawFileScannerResultExporter.IMAGE_PNG_EXPORTER);
		FORMAT_SPEC = formatSpec;
	}

	// Setup bindings
	static {
		GENERIC_CHUNK_LENGTH.bind();
		GENERIC_CHUNK_TYPE.bind();
		IEND_CHUNK_LENGTH.bind();
		IEND_CHUNK_TYPE.bind();
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
