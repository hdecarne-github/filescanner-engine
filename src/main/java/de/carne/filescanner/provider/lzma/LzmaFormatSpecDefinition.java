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
package de.carne.filescanner.provider.lzma;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.carne.filescanner.engine.FileScannerResultContextValueSpecs;
import de.carne.filescanner.engine.format.spec.ByteSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.QWordSpec;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.provider.util.LzmaInputDecoder;
import de.carne.nio.compression.lzma.LzmaDecoderProperties;
import de.carne.nio.file.FileUtil;
import de.carne.util.Lazy;

/**
 * See Lzma.formatspec
 */
final class LzmaFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Lzma.formatspec"));
	}

	private Lazy<CompositeSpec> lzmaFormatSpec = resolveLazy("LZMA_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> lzmaHeaderSpec = resolveLazy("LZMA_HEADER", CompositeSpec.class);

	private Lazy<ByteSpec> lzmaProperties = resolveLazy("PROPERTIES", ByteSpec.class);
	private Lazy<DWordSpec> dictionarySize = resolveLazy("DICTIONARY_SIZE", DWordSpec.class);
	private Lazy<QWordSpec> uncompressedSize = resolveLazy("UNCOMPRESSED_SIZE", QWordSpec.class);

	@Override
	protected void afterLoad() {
		this.lzmaProperties.get().validate(LzmaFormatSpecDefinition::validateLzmaProperties);
		this.dictionarySize.get().validate(LzmaFormatSpecDefinition::validateDictionarySize);
		this.uncompressedSize.get().validate(LzmaFormatSpecDefinition::validateUncompressedSize);
	}

	private static final byte LZMA_PROPERTIES_3_0_2 = (byte) 0x5d;

	private static boolean validateLzmaProperties(Byte value) {
		byte propertiesValue = value.byteValue();

		return propertiesValue == LZMA_PROPERTIES_3_0_2;
	}

	private static final int MIN_DICTIONARY_SIZE = 1 << 16;
	private static final int MAX_DICTIONARY_SIZE = 1 << 26;

	private static boolean validateDictionarySize(Integer value) {
		int dictionarySizeValue = value.intValue();
		int lowestBit = Integer.lowestOneBit(dictionarySizeValue);
		int highestBit = Integer.highestOneBit(dictionarySizeValue);

		return highestBit != 0 && (lowestBit == highestBit || (lowestBit << 1) == highestBit)
				&& MIN_DICTIONARY_SIZE <= dictionarySizeValue && dictionarySizeValue <= MAX_DICTIONARY_SIZE;
	}

	private static final long UNKNOWN_UNCOMPRESSED_SIZE = -1l;
	private static final long MIN_UNCOMPRESSED_SIZE = 1l;
	private static final long MAX_UNCOMPRESSED_SIZE = 1l << 38;

	private static boolean validateUncompressedSize(Long value) {
		long uncompressedSizeValue = value.longValue();

		return uncompressedSizeValue == UNKNOWN_UNCOMPRESSED_SIZE
				|| (MIN_UNCOMPRESSED_SIZE <= uncompressedSizeValue && uncompressedSizeValue <= MAX_UNCOMPRESSED_SIZE);
	}

	public CompositeSpec formatSpec() {
		return this.lzmaFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.lzmaHeaderSpec.get();
	}

	protected EncodedInputSpecConfig lzmaEncodedInputConfig() {
		return new EncodedInputSpecConfig("Compressed data").decodedInputName(this::decodedInputName)
				.inputDecoderTable(this::inputDecoderTable);
	}

	private static final Map<String, String> MANGLED_EXTENSION_MAP = new HashMap<>();

	static {
		MANGLED_EXTENSION_MAP.put("tlz", ".tar");
	}

	private String decodedInputName() {
		String[] splitInputName = FileUtil.splitPath(FileScannerResultContextValueSpecs.INPUT_NAME.get());

		return splitInputName[1] + MANGLED_EXTENSION_MAP.getOrDefault(splitInputName[2], "");
	}

	private InputDecoderTable inputDecoderTable() {
		LzmaDecoderProperties properties = LzmaInputDecoder.defaultProperties();

		properties.setLcLpBpProperty(this.lzmaProperties.get().get());
		properties.setDictionarySizeProperty(this.dictionarySize.get().get());
		properties.setDecodedSizeProperty(this.uncompressedSize.get().get());
		return InputDecoderTable.build(new LzmaInputDecoder(properties));
	}

}
