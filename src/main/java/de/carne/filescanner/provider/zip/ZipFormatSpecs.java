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

import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.FixedStringSpec;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.format.WordSpec;

final class ZipFormatSpecs {

	private ZipFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "ZIP archive";

	// Format specs
	static final StructSpec LOCAL_FILE_HEADER;
	static final WordSpec LFH_FILE_NAME_LENGTH = new WordSpec("file name length");
	static final WordSpec LFH_EXTRA_FIELD_LENGTH = new WordSpec("extra field length");
	static final FixedStringSpec LFH_FILE_NAME = new FixedStringSpec("file name");

	static {
		StructSpec lfh = new StructSpec();

		lfh.result("Local file header");
		lfh.add(new DWordSpec("local file header signature").format(HexFormat.INT_FORMATTER).validate(0x04034b50));
		lfh.add(new WordSpec("version needed to extract").format(HexFormat.SHORT_FORMATTER));
		lfh.add(new WordSpec("general purpose bit flag").format(HexFormat.SHORT_FORMATTER));
		lfh.add(new WordSpec("compression method").format(HexFormat.SHORT_FORMATTER));
		lfh.add(new WordSpec("last mod file time").format(HexFormat.SHORT_FORMATTER));
		lfh.add(new WordSpec("last mod file date").format(HexFormat.SHORT_FORMATTER));
		lfh.add(new DWordSpec("crc-32").format(HexFormat.INT_FORMATTER));
		lfh.add(new DWordSpec("compressed size").format(HexFormat.INT_FORMATTER));
		lfh.add(new DWordSpec("uncompressed size").format(HexFormat.INT_FORMATTER));
		lfh.add(LFH_FILE_NAME_LENGTH.format(HexFormat.SHORT_FORMATTER).bind());
		lfh.add(LFH_EXTRA_FIELD_LENGTH.format(HexFormat.SHORT_FORMATTER).bind());
		lfh.add(LFH_FILE_NAME.size(LFH_FILE_NAME_LENGTH));
		lfh.add(FormatSpecs.COMMIT);
		LOCAL_FILE_HEADER = lfh;
	}

	static final StructSpec ZIP_ENTRY;

	static {
		StructSpec zipEntry = new StructSpec();

		zipEntry.result(() -> String.format("Zip entry [%1$s]", LFH_FILE_NAME.get()));
		zipEntry.add(LOCAL_FILE_HEADER);
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
		LFH_FILE_NAME.bind(ZIP_ENTRY);
	}

}
