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
import de.carne.filescanner.engine.format.ConditionalSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.format.FixedStringSpec;
import de.carne.filescanner.engine.format.FormatSpec;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.format.VarArraySpec;
import de.carne.filescanner.engine.format.WordFlagRenderer;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.format.WordSymbolRenderer;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.filescanner.provider.util.DosDateRenderer;
import de.carne.filescanner.provider.util.DosTimeRenderer;

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
	static final WordSpec LFH_GENERAL_PURPOSE_BIT_FLAG = WordSpec.hex("general purpose bit flag");
	static final WordSpec LFH_COMPRESSION_METHOD = WordSpec.hex("compression method");
	static final DWordSpec LFH_COMPRESSED_SIZE = DWordSpec.size("compressed size");
	static final WordSpec LFH_FILE_NAME_LENGTH = WordSpec.size("file name length");
	static final WordSpec LFH_EXTRA_FIELD_LENGTH = WordSpec.size("extra field length");
	static final FixedStringSpec LFH_FILE_NAME = new FixedStringSpec("file name");

	static {
		StructSpec lfh = new StructSpec();

		lfh.result("Local file header");
		lfh.add(DWordSpec.hex("local file header signature").validate(0x04034b50));
		lfh.add(WordSpec.hex("version needed to extract"));
		lfh.add(LFH_GENERAL_PURPOSE_BIT_FLAG.renderer(GENERAL_PURPOSE_BIT_FLAG_SYMBOLS));
		lfh.add(LFH_COMPRESSION_METHOD.renderer(COMPRESSION_METHOD_SYMBOLS));
		lfh.add(WordSpec.hex("last mod file time").renderer(DosTimeRenderer.RENDERER));
		lfh.add(WordSpec.hex("last mod file date").renderer(DosDateRenderer.RENDERER));
		lfh.add(DWordSpec.hex("crc-32"));
		lfh.add(LFH_COMPRESSED_SIZE);
		lfh.add(DWordSpec.size("uncompressed size"));
		lfh.add(LFH_FILE_NAME_LENGTH);
		lfh.add(LFH_EXTRA_FIELD_LENGTH);
		lfh.add(LFH_FILE_NAME.size(LFH_FILE_NAME_LENGTH));
		lfh.add(new ByteArraySpec("extra field").size(LFH_EXTRA_FIELD_LENGTH));
		LOCAL_FILE_HEADER = lfh;
	}

	static final StructSpec DATA_DESCRIPTOR;

	static {
		StructSpec dd = new StructSpec();

		dd.result("Data Descriptor");
		dd.add(DWordSpec.hex("local file header signature").validate(0x08074b50));
		dd.add(DWordSpec.hex("crc-32"));
		dd.add(DWordSpec.size("compressed size"));
		dd.add(DWordSpec.size("uncompressed size"));
		DATA_DESCRIPTOR = dd;
	}

	static final StructSpec ZIP_ENTRY;

	static {
		StructSpec zipEntry = new StructSpec();

		zipEntry.result(() -> String.format("Zip entry \"%1$s\"", LFH_FILE_NAME.get()));
		zipEntry.add(LOCAL_FILE_HEADER);
		zipEntry.add(FormatSpecs.COMMIT);
		zipEntry.add(new EncodedInputSpec("file data").inputDecoder(ZipFormatSpecs::getInputDecoder)
				.decodedInputName(LFH_FILE_NAME).encodedInputSize(ZipFormatSpecs::getEncodedInputSize));
		zipEntry.add(new ConditionalSpec(ZipFormatSpecs::getDataDescriptorSpec));
		ZIP_ENTRY = zipEntry;
	}

	static final StructSpec CENTRAL_DIRECTORY_HEADER;
	static final WordSpec CDH_FILE_NAME_LENGTH = WordSpec.size("file name length");
	static final WordSpec CDH_EXTRA_FIELD_LENGTH = WordSpec.size("extra field length");
	static final WordSpec CDH_FILE_COMMENT_LENGTH = WordSpec.size("file comment length");
	static final FixedStringSpec CDH_FILE_NAME = new FixedStringSpec("file name");

	static {
		StructSpec cdh = new StructSpec();

		cdh.result(() -> String.format("Central directory header \"%1$s\"", CDH_FILE_NAME.get()));
		cdh.add(DWordSpec.hex("central file header signature").validate(0x02014b50));
		cdh.add(WordSpec.hex("version made by"));
		cdh.add(WordSpec.hex("version needed to extract"));
		cdh.add(WordSpec.hex("general purpose bit flag").renderer(GENERAL_PURPOSE_BIT_FLAG_SYMBOLS));
		cdh.add(WordSpec.hex("compression method").renderer(COMPRESSION_METHOD_SYMBOLS));
		cdh.add(WordSpec.hex("last mod file time").renderer(DosTimeRenderer.RENDERER));
		cdh.add(WordSpec.hex("last mod file date").renderer(DosDateRenderer.RENDERER));
		cdh.add(DWordSpec.hex("crc-32"));
		cdh.add(DWordSpec.size("compressed size"));
		cdh.add(DWordSpec.size("uncompressed size"));
		cdh.add(CDH_FILE_NAME_LENGTH);
		cdh.add(CDH_EXTRA_FIELD_LENGTH);
		cdh.add(CDH_FILE_COMMENT_LENGTH);
		cdh.add(WordSpec.dec("disk number start"));
		cdh.add(WordSpec.hex("internal file attributes"));
		cdh.add(DWordSpec.hex("external file attributes"));
		cdh.add(DWordSpec.hex("relative offset of local header"));
		cdh.add(CDH_FILE_NAME.size(CDH_FILE_NAME_LENGTH));
		cdh.add(new ByteArraySpec("extra field").size(CDH_EXTRA_FIELD_LENGTH));
		cdh.add(new FixedStringSpec("file comment").size(CDH_FILE_COMMENT_LENGTH));
		CENTRAL_DIRECTORY_HEADER = cdh;
	}

	static final StructSpec END_OF_CENTRAL_DIRECTORY;
	static final WordSpec EOCD_ZIP_FILE_COMMENT_LENGTH = WordSpec.size(".ZIP file comment length");

	static {
		StructSpec eocd = new StructSpec();

		eocd.result("End of central directory");
		eocd.add(DWordSpec.hex("end of central dir signature").validate(0x06054b50));
		eocd.add(WordSpec.dec("number of this disk"));
		eocd.add(WordSpec.dec("number of the disk with the start of the central directory"));
		eocd.add(WordSpec.dec("total number of entries in the central directory on this disk"));
		eocd.add(WordSpec.dec("total number of entries in the central directory"));
		eocd.add(DWordSpec.size("size of the central directory"));
		eocd.add(DWordSpec.hex("offset of start of central directory"));
		eocd.add(EOCD_ZIP_FILE_COMMENT_LENGTH);
		eocd.add(new FixedStringSpec(".ZIP file comment").size(EOCD_ZIP_FILE_COMMENT_LENGTH));
		END_OF_CENTRAL_DIRECTORY = eocd;
	}

	static final StructSpec CENTRAL_DIRECTORY;

	static {
		StructSpec cd = new StructSpec();

		cd.result("Central directory");
		cd.add(new VarArraySpec(CENTRAL_DIRECTORY_HEADER));
		cd.add(END_OF_CENTRAL_DIRECTORY);
		CENTRAL_DIRECTORY = cd;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.result(FORMAT_NAME);
		formatSpec.add(new VarArraySpec(ZIP_ENTRY));
		formatSpec.add(CENTRAL_DIRECTORY);
		FORMAT_SPEC = formatSpec;
	}

	// Setup bindings
	static {
		LFH_FILE_NAME_LENGTH.bind();
		LFH_EXTRA_FIELD_LENGTH.bind();
		LFH_GENERAL_PURPOSE_BIT_FLAG.bind(ZIP_ENTRY);
		LFH_COMPRESSION_METHOD.bind(ZIP_ENTRY);
		LFH_COMPRESSED_SIZE.bind(ZIP_ENTRY);
		LFH_FILE_NAME.bind(ZIP_ENTRY);

		CDH_FILE_NAME_LENGTH.bind();
		CDH_EXTRA_FIELD_LENGTH.bind();
		CDH_FILE_COMMENT_LENGTH.bind();
		CDH_FILE_NAME.bind(CENTRAL_DIRECTORY_HEADER);

		EOCD_ZIP_FILE_COMMENT_LENGTH.bind();
	}

	private static InputDecoder getInputDecoder() {
		short compressionMethod = LFH_COMPRESSION_METHOD.get().shortValue();
		InputDecoder inputDecoder;

		switch (compressionMethod) {
		case 0x00:
			inputDecoder = InputDecoder.NONE;
			break;
		case 0x08:
			inputDecoder = DeflatedInputDecoder.DECODER;
			break;
		default:
			inputDecoder = InputDecoder
					.unsupportedInputDecoder("ZIP compression method " + HexFormat.formatShort(compressionMethod));
		}
		return inputDecoder;
	}

	private static long getEncodedInputSize() {
		return ((LFH_GENERAL_PURPOSE_BIT_FLAG.get().intValue() & 0x0008) == 0
				? Integer.toUnsignedLong(LFH_COMPRESSED_SIZE.get().intValue())
				: -1l);
	}

	private static FormatSpec getDataDescriptorSpec() {
		return ((LFH_GENERAL_PURPOSE_BIT_FLAG.get().intValue() & 0x0008) == 0 ? FormatSpecs.EMPTY : DATA_DESCRIPTOR);
	}

}
