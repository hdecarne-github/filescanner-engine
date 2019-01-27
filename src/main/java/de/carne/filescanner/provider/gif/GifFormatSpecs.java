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
package de.carne.filescanner.provider.gif;

import java.util.Arrays;
import java.util.HashSet;

import de.carne.filescanner.engine.format.spec.ArraySpec;
import de.carne.filescanner.engine.format.spec.ByteArraySpec;
import de.carne.filescanner.engine.format.spec.ByteFlagRenderer;
import de.carne.filescanner.engine.format.spec.ByteRangeSpec;
import de.carne.filescanner.engine.format.spec.ByteSpec;
import de.carne.filescanner.engine.format.spec.ByteSymbolRenderer;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.ConditionalSpec;
import de.carne.filescanner.engine.format.spec.CharArraySpec;
import de.carne.filescanner.engine.format.spec.FormatSpecs;
import de.carne.filescanner.engine.format.spec.SequenceSpec;
import de.carne.filescanner.engine.format.spec.StructSpec;
import de.carne.filescanner.engine.format.spec.UnionSpec;
import de.carne.filescanner.engine.format.spec.WordSpec;
import de.carne.filescanner.engine.transfer.RawFileScannerResultExporter;

final class GifFormatSpecs {

	private GifFormatSpecs() {
		// Prevent instantiation
	}

	// Format name
	static final String FORMAT_NAME = "GIF image data";

	// Symbols and flags
	static final ByteFlagRenderer LSD_PACKED_FIELDS_SYMBOLS = new ByteFlagRenderer();

	static {
		LSD_PACKED_FIELDS_SYMBOLS.put((byte) 0x80, "Global Color Table Flag");
		LSD_PACKED_FIELDS_SYMBOLS.put((byte) 0x70, "Color Resolution");
		LSD_PACKED_FIELDS_SYMBOLS.put((byte) 0x08, "Sort Flag");
		LSD_PACKED_FIELDS_SYMBOLS.put((byte) 0x07, "Size of Global Color Table");
	}

	static final ByteFlagRenderer ID_PACKED_FIELDS_SYMBOLS = new ByteFlagRenderer();

	static {
		ID_PACKED_FIELDS_SYMBOLS.put((byte) 0x80, "Local Color Table Flag");
		ID_PACKED_FIELDS_SYMBOLS.put((byte) 0x40, "Interlace Flag");
		ID_PACKED_FIELDS_SYMBOLS.put((byte) 0x20, "Sort Flag");
		ID_PACKED_FIELDS_SYMBOLS.put((byte) 0x18, "Reserved");
		ID_PACKED_FIELDS_SYMBOLS.put((byte) 0x07, "Size of Local Color Table");
	}

	static final ByteSymbolRenderer EXTENSION_TYPE_SYMBOLS = new ByteSymbolRenderer();

	static {
		EXTENSION_TYPE_SYMBOLS.put((byte) 0xf9, "Graphic Control Extension");
		EXTENSION_TYPE_SYMBOLS.put((byte) 0xfe, "Comment Extension");
		EXTENSION_TYPE_SYMBOLS.put((byte) 0x01, "Plain Text Extension");
		EXTENSION_TYPE_SYMBOLS.put((byte) 0xff, "Application Extension");
	}

	static final ByteFlagRenderer GCE_PACKED_FIELDS_SYMBOLS = new ByteFlagRenderer();

	static {
		GCE_PACKED_FIELDS_SYMBOLS.put((byte) 0xe0, "Reserved");
		GCE_PACKED_FIELDS_SYMBOLS.put((byte) 0x1c, "Disposal Method");
		GCE_PACKED_FIELDS_SYMBOLS.put((byte) 0x02, "User Input Flag");
		GCE_PACKED_FIELDS_SYMBOLS.put((byte) 0x01, "Transparent Color Flag");
	}

	// Format specs
	static final StructSpec HEADER;

	static {
		StructSpec header = new StructSpec();

		header.result("Header");
		header.add(new CharArraySpec("Signature")).size(3).validate("GIF");
		header.add(new CharArraySpec("Version")).size(3).validate(new HashSet<>(Arrays.asList("87a", "89a")));
		HEADER = header;
	}

	static final StructSpec LOGICAL_SCREEN_DESCRIPTOR;
	static final ByteSpec LSD_PACKED_FIELDS = ByteSpec.hex("<Packed Fields>");

	static {
		StructSpec lsd = new StructSpec();

		lsd.result("Logical Screen Descriptor");
		lsd.add(WordSpec.dec("Logical Screen Width"));
		lsd.add(WordSpec.dec("Logical Screen Height"));
		lsd.add(LSD_PACKED_FIELDS).renderer(LSD_PACKED_FIELDS_SYMBOLS);
		lsd.add(ByteSpec.dec("Background Color Index"));
		lsd.add(ByteSpec.dec("Pixel Aspect Ratio"));
		LOGICAL_SCREEN_DESCRIPTOR = lsd;
	}

	static final ArraySpec GLOBAL_COLOR_TABLE;

	static {
		ByteArraySpec gctEntry = new ByteArraySpec("").size(3);
		ArraySpec gct = new ArraySpec("color[%1$d]", gctEntry);

		gct.result("Global Color Table");
		gct.size(GifFormatSpecs::getGlobalColorTableSize);
		GLOBAL_COLOR_TABLE = gct;
	}

	static final StructSpec IMAGE_DESCRIPTOR;
	static final ByteSpec ID_PACKED_FIELDS = ByteSpec.hex("<Packed Fields>");

	static {
		StructSpec id = new StructSpec();

		id.result("Image Descriptor");
		id.add(ByteSpec.hex("Image Separator")).validate((byte) 0x2c);
		id.add(WordSpec.dec("Image Left Position"));
		id.add(WordSpec.dec("Image Top Position"));
		id.add(WordSpec.dec("Image Width"));
		id.add(WordSpec.dec("Image Height"));
		id.add(ID_PACKED_FIELDS).renderer(ID_PACKED_FIELDS_SYMBOLS);
		IMAGE_DESCRIPTOR = id;
	}

	static final ArraySpec LOCAL_COLOR_TABLE;

	static {
		ByteArraySpec lctEntry = new ByteArraySpec("").size(3);
		ArraySpec lct = new ArraySpec("color[%1$d]", lctEntry);

		lct.result("Local Color Table");
		lct.size(GifFormatSpecs::getLocalColorTableSize);
		LOCAL_COLOR_TABLE = lct;
	}

	static final StructSpec IMAGE_DATA;
	static final ByteSpec IMAGE_DATA_BLOCK_SIZE = ByteSpec.size("Block Size");

	static {
		StructSpec id = new StructSpec();

		id.result("Image Data");
		id.add(ByteSpec.dec("LZW Minimum Code Size"));
		StructSpec dataBlock = new StructSpec();

		dataBlock.add(IMAGE_DATA_BLOCK_SIZE).validate(b -> b.byteValue() != 0);
		dataBlock.add(new ByteRangeSpec("Block Data")).size(GifFormatSpecs::getImageDataBlockSize);

		id.add(new SequenceSpec(dataBlock));

		StructSpec terminatorBlock = new StructSpec();

		terminatorBlock.add(ByteSpec.size("Block Size")).validate((byte) 0x00);
		id.add(terminatorBlock);
		IMAGE_DATA = id;
	}

	static final StructSpec IMAGE;

	static {
		StructSpec image = new StructSpec();

		image.result("Image Block");
		image.add(IMAGE_DESCRIPTOR);
		image.add(new ConditionalSpec(GifFormatSpecs::getLocalColorTableSpec));
		image.add(IMAGE_DATA);
		IMAGE = image;
	}

	static final StructSpec GENERIC_EXTENSION;
	static final ByteSpec GENERIC_EXTENSION_BLOCK_SIZE = ByteSpec.size("Block Size");

	static {
		StructSpec genericExtension = new StructSpec();

		genericExtension.result("Extension Block");
		genericExtension.add(ByteSpec.hex("Extension Introducer")).validate((byte) 0x21);
		genericExtension.add(ByteSpec.hex("Extension Type")).renderer(EXTENSION_TYPE_SYMBOLS);

		StructSpec dataBlock = new StructSpec();

		dataBlock.add(GENERIC_EXTENSION_BLOCK_SIZE).validate(b -> b.byteValue() != 0);
		dataBlock.add(new ByteRangeSpec("Block Data")).size(GifFormatSpecs::getGenericExtensionBlockSize);

		genericExtension.add(new SequenceSpec(dataBlock));

		StructSpec terminatorBlock = new StructSpec();

		terminatorBlock.add(ByteSpec.size("Block Size")).validate((byte) 0x00);
		genericExtension.add(terminatorBlock);
		GENERIC_EXTENSION = genericExtension;
	}

	static final StructSpec GRAPHIC_CONTROL_EXTENSION;

	static {
		StructSpec gce = new StructSpec();

		gce.result("Graphic Control Extension");
		gce.add(ByteSpec.hex("Extension Introducer")).validate((byte) 0x21);
		gce.add(ByteSpec.hex("Graphic Control Label")).validate((byte) 0xf9).renderer(EXTENSION_TYPE_SYMBOLS);
		gce.add(ByteSpec.size("Block Size")).validate((byte) 4);
		gce.add(ByteSpec.hex("<Packed Fields>")).renderer(GCE_PACKED_FIELDS_SYMBOLS);
		gce.add(WordSpec.dec("Delay Time"));
		gce.add(ByteSpec.dec("Transparent Color Index"));
		gce.add(ByteSpec.size("Block Terminator")).validate((byte) 0);
		GRAPHIC_CONTROL_EXTENSION = gce;
	}

	static final UnionSpec BLOCK;

	static {
		UnionSpec block = new UnionSpec();

		block.add(IMAGE);
		block.add(GRAPHIC_CONTROL_EXTENSION);
		block.add(GENERIC_EXTENSION);
		BLOCK = block;
	}

	static final StructSpec TRAILER;

	static {
		StructSpec trailer = new StructSpec();

		trailer.result("Trailer");
		trailer.add(ByteSpec.hex("GIF Trailer")).validate((byte) 0x3b);
		TRAILER = trailer;
	}

	static final StructSpec FORMAT_SPEC;

	static {
		StructSpec formatSpec = new StructSpec();

		formatSpec.export(RawFileScannerResultExporter.IMAGE_GIF_EXPORTER);
		formatSpec.result(FORMAT_NAME);
		formatSpec.add(HEADER);
		formatSpec.add(LOGICAL_SCREEN_DESCRIPTOR);
		formatSpec.add(new ConditionalSpec(GifFormatSpecs::getGlobalColorTableSpec));
		formatSpec.add(new SequenceSpec(BLOCK));
		formatSpec.add(TRAILER);
		formatSpec.render(RawFileScannerResultExporter.IMAGE_GIF_EXPORTER);
		FORMAT_SPEC = formatSpec;
	}

	// Setup bindings
	static {
		LSD_PACKED_FIELDS.bind(FORMAT_SPEC);
		ID_PACKED_FIELDS.bind(IMAGE);
		IMAGE_DATA_BLOCK_SIZE.bind();
		GENERIC_EXTENSION_BLOCK_SIZE.bind();
	}

	// Helpers
	private static int getGlobalColorTableSize() {
		return 2 << (LSD_PACKED_FIELDS.get().byteValue() & 0x07);
	}

	private static CompositeSpec getGlobalColorTableSpec() {
		return ((LSD_PACKED_FIELDS.get().byteValue() & 0x80) != 0 ? GLOBAL_COLOR_TABLE : FormatSpecs.EMPTY);
	}

	private static int getLocalColorTableSize() {
		return 2 << (ID_PACKED_FIELDS.get().byteValue() & 0x07);
	}

	private static CompositeSpec getLocalColorTableSpec() {
		return ((ID_PACKED_FIELDS.get().byteValue() & 0x80) != 0 ? LOCAL_COLOR_TABLE : FormatSpecs.EMPTY);
	}

	private static int getGenericExtensionBlockSize() {
		return GENERIC_EXTENSION_BLOCK_SIZE.get().byteValue() & 0xff;
	}

	private static int getImageDataBlockSize() {
		return IMAGE_DATA_BLOCK_SIZE.get().byteValue() & 0xff;
	}

}
