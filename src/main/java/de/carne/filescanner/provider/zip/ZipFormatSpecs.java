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
package de.carne.filescanner.provider.zip;

import de.carne.filescanner.engine.format.ByteArraySpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.format.FixedStringSpec;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.PrettyFormat;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.format.WordFlagRenderer;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.format.WordSymbolRenderer;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.filescanner.provider.util.DWordSizeRenderer;
import de.carne.filescanner.provider.util.DosDateRenderer;
import de.carne.filescanner.provider.util.DosTimeRenderer;
import de.carne.filescanner.provider.util.WordSizeRenderer;

final class ZipFormatSpecs {

	private ZipFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "ZIP archive";

	// Symbols and flags
	static final WordFlagRenderer GENERAL_PURPOSE_BIT_FLAG_SYMBOLS = new WordFlagRenderer();

	static {
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x2000, "Central directory encrypted");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0800, "UTF-8 strings");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0040, "Strong encryption");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0020, "Compressed patched data");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0010, "Enhanced deflating");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0008,
				"crc-32, compressed size and uncompressed size are in data descriptor");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0004, "Compression flag 2");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0002, "Compression flag 1");
		GENERAL_PURPOSE_BIT_FLAG_SYMBOLS.put((short) 0x0001, "Encryption");
	}

	static final WordSymbolRenderer COMPRESSION_METHOD_SYMBOLS = new WordSymbolRenderer();

	static {
		COMPRESSION_METHOD_SYMBOLS.put((short) 0, "Stored (no compression)");
		COMPRESSION_METHOD_SYMBOLS.put((short) 1, "Shrunk");
		COMPRESSION_METHOD_SYMBOLS.put((short) 2, "Reduced with compression factor 1");
		COMPRESSION_METHOD_SYMBOLS.put((short) 3, "Reduced with compression factor 2");
		COMPRESSION_METHOD_SYMBOLS.put((short) 4, "Reduced with compression factor 3");
		COMPRESSION_METHOD_SYMBOLS.put((short) 5, "Reduced with compression factor 4");
		COMPRESSION_METHOD_SYMBOLS.put((short) 6, "Imploded");
		COMPRESSION_METHOD_SYMBOLS.put((short) 7, "Tokenizing compression algorithm");
		COMPRESSION_METHOD_SYMBOLS.put((short) 8, "Deflated");
		COMPRESSION_METHOD_SYMBOLS.put((short) 9, "Enhanced Deflating using Deflate64(tm)");
		COMPRESSION_METHOD_SYMBOLS.put((short) 10, "PKWARE Data Compression Library Imploding (old IBM TERSE)");
		COMPRESSION_METHOD_SYMBOLS.put((short) 12, "BZIP2 algorithm");
		COMPRESSION_METHOD_SYMBOLS.put((short) 14, "LZMA (EFS)");
		COMPRESSION_METHOD_SYMBOLS.put((short) 18, "IBM TERSE (new)");
		COMPRESSION_METHOD_SYMBOLS.put((short) 19, "IBM LZ77 z Architecture (PFS)");
		COMPRESSION_METHOD_SYMBOLS.put((short) 97, "WavPack compressed data");
		COMPRESSION_METHOD_SYMBOLS.put((short) 98, "PPMd version I, Rev 1");
	}

	// Format specs
	static final StructSpec LOCAL_FILE_HEADER;
	static final WordSpec LFH_GENERAL_PURPOSE_BIT_FLAG = new WordSpec("general purpose bit flag");
	static final WordSpec LFH_COMPRESSION_METHOD = new WordSpec("compression method");
	static final DWordSpec LFH_COMPRESSED_SIZE = new DWordSpec("compressed size");
	static final WordSpec LFH_FILE_NAME_LENGTH = new WordSpec("file name length");
	static final WordSpec LFH_EXTRA_FIELD_LENGTH = new WordSpec("extra field length");
	static final FixedStringSpec LFH_FILE_NAME = new FixedStringSpec("file name");

	static {
		StructSpec lfh = new StructSpec();

		lfh.result("Local file header");
		lfh.add(new DWordSpec("local file header signature").format(HexFormat.INT_FORMATTER).validate(0x04034b50));
		lfh.add(new WordSpec("version needed to extract").format(HexFormat.SHORT_FORMATTER));
		lfh.add(LFH_GENERAL_PURPOSE_BIT_FLAG.format(HexFormat.SHORT_FORMATTER)
				.renderer(GENERAL_PURPOSE_BIT_FLAG_SYMBOLS));
		lfh.add(LFH_COMPRESSION_METHOD.format(HexFormat.SHORT_FORMATTER).renderer(COMPRESSION_METHOD_SYMBOLS));
		lfh.add(new WordSpec("last mod file time").format(HexFormat.SHORT_FORMATTER)
				.renderer(DosTimeRenderer.RENDERER));
		lfh.add(new WordSpec("last mod file date").format(HexFormat.SHORT_FORMATTER)
				.renderer(DosDateRenderer.RENDERER));
		lfh.add(new DWordSpec("crc-32").format(HexFormat.INT_FORMATTER));
		lfh.add(LFH_COMPRESSED_SIZE.format(PrettyFormat.INT_FORMATTER).renderer(DWordSizeRenderer.RENDERER));
		lfh.add(new DWordSpec("uncompressed size").format(PrettyFormat.INT_FORMATTER)
				.renderer(DWordSizeRenderer.RENDERER));
		lfh.add(LFH_FILE_NAME_LENGTH.format(PrettyFormat.SHORT_FORMATTER).renderer(WordSizeRenderer.RENDERER).bind());
		lfh.add(LFH_EXTRA_FIELD_LENGTH.format(PrettyFormat.SHORT_FORMATTER).renderer(WordSizeRenderer.RENDERER).bind());
		lfh.add(LFH_FILE_NAME.size(LFH_FILE_NAME_LENGTH));
		lfh.add(new ByteArraySpec("extra field").size(LFH_EXTRA_FIELD_LENGTH));
		LOCAL_FILE_HEADER = lfh;
	}

	static final StructSpec ZIP_ENTRY;

	static {
		StructSpec zipEntry = new StructSpec();

		zipEntry.result(() -> String.format("Zip entry \"%1$s\"", LFH_FILE_NAME.get()));
		zipEntry.add(LOCAL_FILE_HEADER);
		zipEntry.add(new EncodedInputSpec("file data").inputDecoder(ZipFormatSpecs::getInputDecoder)
				.decodedInputName(LFH_FILE_NAME)
				.encodedInputSize(() -> Integer.toUnsignedLong(LFH_COMPRESSED_SIZE.get())));
		zipEntry.add(FormatSpecs.COMMIT);
		ZIP_ENTRY = zipEntry;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.result(FORMAT_NAME);
		formatSpec.add(ZIP_ENTRY);
		FORMAT_SPEC = formatSpec;
	}

	// Setup result scoped bindings
	static {
		LFH_GENERAL_PURPOSE_BIT_FLAG.bind(ZIP_ENTRY);
		LFH_COMPRESSION_METHOD.bind(ZIP_ENTRY);
		LFH_COMPRESSED_SIZE.bind(ZIP_ENTRY);
		LFH_FILE_NAME.bind(ZIP_ENTRY);
	}

	private static InputDecoder getInputDecoder() {
		short compressionMethod = LFH_COMPRESSION_METHOD.get();
		InputDecoder inputDecoder;

		switch (compressionMethod) {
		case 0x00:
			inputDecoder = InputDecoder.NONE;
			break;
		case 0x08:
			inputDecoder = DeflatedInputDecoder.INSTANCE;
			break;
		default:
			inputDecoder = InputDecoder
					.unsupportedInputDecoder("ZIP compression method " + HexFormat.formatShort(compressionMethod));
		}
		return inputDecoder;
	}

}
