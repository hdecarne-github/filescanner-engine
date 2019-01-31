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
package de.carne.filescanner.provider.zip;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.spec.CharArraySpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.FormatSpecs;
import de.carne.filescanner.engine.format.spec.WordSpec;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.util.Lazy;

/**
 * See Zip.formatspec
 */
final class ZipFormatSpecDefinition extends FormatSpecDefinition {

	private Lazy<CompositeSpec> zipFormatSpec = resolveLazy("ZIP_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> lfhSpec = resolveLazy("LOCAL_FILE_HEADER", CompositeSpec.class);
	private Lazy<CompositeSpec> ddSpec = resolveLazy("DATA_DESCRIPTOR", CompositeSpec.class);

	private Lazy<WordSpec> lfhGenerapPurposeBitFlag = resolveLazy("LFH_GENERAL_PURPOSE_BIT_FLAG", WordSpec.class);
	private Lazy<WordSpec> lfhCompressionMethod = resolveLazy("LFH_COMPRESSION_METHOD", WordSpec.class);
	private Lazy<DWordSpec> lfhCompressedSize = resolveLazy("LFH_COMPRESSED_SIZE", DWordSpec.class);
	private Lazy<CharArraySpec> lfhFileName = resolveLazy("LFH_FILE_NAME", CharArraySpec.class);

	public CompositeSpec getZipFormatSpec() {
		return this.zipFormatSpec.get();
	}

	public CompositeSpec getLocalFileHeaderSpec() {
		return this.lfhSpec.get();
	}

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Zip.formatspec"));
	}

	protected EncodedInputSpecConfig getZipEntryEncodedInputConfig() {
		return new EncodedInputSpecConfig("file data").encodedInputSize(this::getEncodedInputSize)
				.inputDecoder(this::getInputDecoder).decodedInputName(this::getDecodedInputName);
	}

	private long getEncodedInputSize() {
		int bitFlag = Short.toUnsignedInt(this.lfhGenerapPurposeBitFlag.get().get().shortValue());
		boolean ddPresent = (bitFlag & 0x0008) != 0;

		return (ddPresent ? -1l : Integer.toUnsignedLong(this.lfhCompressedSize.get().get().intValue()));
	}

	private InputDecoder getInputDecoder() {
		short compressionMethod = this.lfhCompressionMethod.get().get().shortValue();
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

	private String getDecodedInputName() {
		return this.lfhFileName.get().get();
	}

	protected CompositeSpec getDataDescriptorSpec() {
		int bitFlag = Short.toUnsignedInt(this.lfhGenerapPurposeBitFlag.get().get().shortValue());
		boolean ddPresent = (bitFlag & 0x0008) != 0;

		return (ddPresent ? this.ddSpec.get() : FormatSpecs.EMPTY);
	}

}