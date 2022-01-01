/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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

import de.carne.filescanner.engine.format.CharArraySpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.filescanner.engine.util.DeflateInputDecoder;
import de.carne.filescanner.engine.util.HexFormat;
import de.carne.util.Lazy;

/**
 * See Zip.formatspec
 */
final class ZipFormatSpecDefinition extends FormatSpecDefinition {

	private static final DeflateInputDecoder DEFLATE_INPUT_DECODER = new DeflateInputDecoder();

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Zip.formatspec"));
	}

	private Lazy<CompositeSpec> zipFormatSpec = resolveLazy("ZIP_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> lfhSpec = resolveLazy("LOCAL_FILE_HEADER", CompositeSpec.class);
	private Lazy<CompositeSpec> ddSpec = resolveLazy("DATA_DESCRIPTOR", CompositeSpec.class);

	private Lazy<WordSpec> lfhGenerapPurposeBitFlag = resolveLazy("LFH_GENERAL_PURPOSE_BIT_FLAG", WordSpec.class);
	private Lazy<WordSpec> lfhCompressionMethod = resolveLazy("LFH_COMPRESSION_METHOD", WordSpec.class);
	private Lazy<DWordSpec> lfhCompressedSize = resolveLazy("LFH_COMPRESSED_SIZE", DWordSpec.class);
	private Lazy<CharArraySpec> lfhFileName = resolveLazy("LFH_FILE_NAME", CharArraySpec.class);

	public CompositeSpec formatSpec() {
		return this.zipFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.lfhSpec.get();
	}

	public EncodedInputSpecConfig zipEntryEncodedInputConfig() {
		return new EncodedInputSpecConfig("file data").inputDecoderTable(this::inputDecoderTable)
				.decodedInputName(this::decodedInputName);
	}

	private long encodedInputSize() {
		return Integer.toUnsignedLong(this.lfhCompressedSize.get().get().intValue());
	}

	private long optionalEncodedInputSize() {
		int bitFlag = Short.toUnsignedInt(this.lfhGenerapPurposeBitFlag.get().get().shortValue());
		boolean ddPresent = (bitFlag & 0x0008) != 0;

		return (ddPresent ? -1l : Integer.toUnsignedLong(this.lfhCompressedSize.get().get().intValue()));
	}

	private InputDecoderTable inputDecoderTable() {
		short compressionMethod = this.lfhCompressionMethod.get().get().shortValue();
		InputDecoderTable inputDecoderTable;

		switch (compressionMethod) {
		case 0x00:
			inputDecoderTable = InputDecoderTable.build(InputDecoders.IDENTITY, -1l, encodedInputSize(), -1l);
			break;
		case 0x08:
			inputDecoderTable = InputDecoderTable.build(DEFLATE_INPUT_DECODER, -1l, optionalEncodedInputSize(), -1l);
			break;
		default:
			inputDecoderTable = InputDecoderTable.build(
					InputDecoders.unsupportedInputDecoder(
							"ZIP compression method " + HexFormat.formatShort(compressionMethod)),
					-1l, encodedInputSize(), -1l);
		}
		return inputDecoderTable;
	}

	private String decodedInputName() {
		return this.lfhFileName.get().get();
	}

	public CompositeSpec dataDescriptorSpec() {
		int bitFlag = Short.toUnsignedInt(this.lfhGenerapPurposeBitFlag.get().get().shortValue());
		boolean ddPresent = (bitFlag & 0x0008) != 0;

		return (ddPresent ? this.ddSpec.get() : FormatSpecs.EMPTY);
	}

}
