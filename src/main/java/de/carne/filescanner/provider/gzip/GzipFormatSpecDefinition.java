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
package de.carne.filescanner.provider.gzip;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.AttributeSpecs;
import de.carne.filescanner.engine.format.spec.ByteSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.FormatSpecs;
import de.carne.filescanner.engine.format.spec.StringSpec;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.util.ByteHelper;
import de.carne.filescanner.provider.util.DeflateInputDecoder;
import de.carne.nio.file.FileUtil;
import de.carne.util.Lazy;

/**
 * See Gzip.formatspec
 */
final class GzipFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Gzip.formatspec"));
	}

	private Lazy<CompositeSpec> gzipFormatSpec = resolveLazy("GZIP_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> gzipHeaderSpec = resolveLazy("GZIP_HEADER", CompositeSpec.class);

	private Lazy<FormatSpec> gzipFextraSpec = resolveLazy("GZIP_HEADER_FEXTRA", FormatSpec.class);
	private Lazy<FormatSpec> gzipFnameSpec = resolveLazy("GZIP_HEADER_FNAME", FormatSpec.class);
	private Lazy<FormatSpec> gzipFcommentSpec = resolveLazy("GZIP_HEADER_FCOMMENT", FormatSpec.class);
	private Lazy<FormatSpec> gzipFhcrcSpec = resolveLazy("GZIP_HEADER_FHCRC", FormatSpec.class);

	private Lazy<ByteSpec> gzipHeaderFlg = resolveLazy("HEADER_FLG", ByteSpec.class);
	private Lazy<StringSpec> gzipHeaderFname = resolveLazy("HEADER_FNAME", StringSpec.class);

	public CompositeSpec formatSpec() {
		return this.gzipFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.gzipHeaderSpec.get();
	}

	protected FormatSpec fextraSpec() {
		int flg = ByteHelper.toUnsignedInt(this.gzipHeaderFlg.get().get());

		return ((flg & 0x04) == 0x04 ? this.gzipFextraSpec.get() : FormatSpecs.EMPTY);
	}

	protected FormatSpec fnameSpec() {
		int flg = ByteHelper.toUnsignedInt(this.gzipHeaderFlg.get().get());

		return ((flg & 0x08) == 0x08 ? this.gzipFnameSpec.get() : FormatSpecs.EMPTY);
	}

	protected FormatSpec fcommentSpec() {
		int flg = ByteHelper.toUnsignedInt(this.gzipHeaderFlg.get().get());

		return ((flg & 0x10) == 0x10 ? this.gzipFcommentSpec.get() : FormatSpecs.EMPTY);
	}

	protected FormatSpec fhcrcSpec() {
		int flg = ByteHelper.toUnsignedInt(this.gzipHeaderFlg.get().get());

		return ((flg & 0x02) == 0x02 ? this.gzipFhcrcSpec.get() : FormatSpecs.EMPTY);
	}

	protected EncodedInputSpecConfig gzipEncodedInputConfig() {
		return new EncodedInputSpecConfig("Compressed data").decodedInputName(this::decodedInputName)
				.inputDecoderTable(this::inputDecoderTable);
	}

	private static final Map<String, String> MANGLED_EXTENSION_MAP = new HashMap<>();

	static {
		MANGLED_EXTENSION_MAP.put("tgz", ".tar");
	}

	private String decodedInputName() {
		int flg = ByteHelper.toUnsignedInt(this.gzipHeaderFlg.get().get());
		StringBuilder decodedInputName = new StringBuilder();

		if ((flg & 0x08) == 0x08) {
			decodedInputName.append(this.gzipHeaderFname.get().get());
		} else {
			String[] splitInputName = FileUtil.splitPath(AttributeSpecs.INPUT_NAME.get());

			decodedInputName.append(splitInputName[1]);
			decodedInputName.append(MANGLED_EXTENSION_MAP.getOrDefault(splitInputName[2], ""));
		}
		return decodedInputName.toString();
	}

	private InputDecoderTable inputDecoderTable() {
		return InputDecoderTable.build(new DeflateInputDecoder());
	}

}
