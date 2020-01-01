/*
 * Copyright (c) 2007-2020 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.provider.bzip2;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.carne.boot.check.Check;
import de.carne.filescanner.engine.FileScannerResultContextValueSpecs;
import de.carne.filescanner.engine.format.spec.ByteSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.provider.util.Bzip2InputDecoder;
import de.carne.nio.compression.bzip2.Bzip2BlockSize;
import de.carne.nio.compression.bzip2.Bzip2DecoderProperties;
import de.carne.nio.file.FileUtil;
import de.carne.util.Lazy;

/**
 * See Bzip2.formatspec
 */
final class Bzip2FormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Bzip2.formatspec"));
	}

	private Lazy<CompositeSpec> bzip2FormatSpec = resolveLazy("BZIP2_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> bzip2HeaderSpec = resolveLazy("BZIP2_HEADER", CompositeSpec.class);

	private Lazy<ByteSpec> bzip2BlockSize = resolveLazy("BLOCK_SIZE", ByteSpec.class);

	public CompositeSpec formatSpec() {
		return this.bzip2FormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.bzip2HeaderSpec.get();
	}

	protected EncodedInputSpecConfig bzip2EncodedInputConfig() {
		return new EncodedInputSpecConfig("Compressed data").decodedInputName(this::decodedInputName)
				.inputDecoderTable(this::inputDecoderTable);
	}

	private static final Map<String, String> MANGLED_EXTENSION_MAP = new HashMap<>();

	static {
		MANGLED_EXTENSION_MAP.put("tb2", ".tar");
		MANGLED_EXTENSION_MAP.put("tbz", ".tar");
		MANGLED_EXTENSION_MAP.put("tbz2", ".tar");
	}

	private String decodedInputName() {
		String[] splitInputName = FileUtil.splitPath(FileScannerResultContextValueSpecs.INPUT_NAME.get());

		return splitInputName[1] + MANGLED_EXTENSION_MAP.getOrDefault(splitInputName[2], "");
	}

	private InputDecoderTable inputDecoderTable() {
		Bzip2DecoderProperties properties = Bzip2InputDecoder.defaultProperties();
		byte blockSize = this.bzip2BlockSize.get().get();

		switch (blockSize) {
		case '1':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE1);
			break;
		case '2':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE2);
			break;
		case '3':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE3);
			break;
		case '4':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE4);
			break;
		case '5':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE5);
			break;
		case '6':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE6);
			break;
		case '7':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE7);
			break;
		case '8':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE8);
			break;
		case '9':
			properties.setBlockSizeProperty(Bzip2BlockSize.SIZE9);
			break;
		default:
			throw Check.fail("Unexpected block size: %x", blockSize);
		}
		return InputDecoderTable.build(new Bzip2InputDecoder(properties));
	}

}
