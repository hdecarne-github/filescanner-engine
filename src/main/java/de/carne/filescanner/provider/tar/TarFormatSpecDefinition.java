/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.provider.tar;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CharArraySpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.util.Lazy;

/**
 * See Tar.formatspec
 */
final class TarFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Tar.formatspec"));
	}

	private Lazy<CompositeSpec> tarFormatSpec = resolveLazy("TAR_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> tarHeaderSpec = resolveLazy("TAR_HEADER", CompositeSpec.class);

	private Lazy<CompositeSpec> tarDataSpec = resolveLazy("TAR_DATA", CompositeSpec.class);

	private Lazy<CharArraySpec> headerName = resolveLazy("HEADER_NAME", CharArraySpec.class);
	private Lazy<CharArraySpec> headerSize = resolveLazy("HEADER_SIZE", CharArraySpec.class);

	public CompositeSpec formatSpec() {
		return this.tarFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.tarHeaderSpec.get();
	}

	public CompositeSpec tarEntryDataSpec() {
		long size = getEntryDataSize();

		return (size > 0 ? this.tarDataSpec.get() : FormatSpecs.EMPTY);
	}

	public EncodedInputSpecConfig tarDataEncodedInputConfig() {
		return new EncodedInputSpecConfig("data blocks").decodedInputName(this.headerName.get()::getStripped)
				.inputDecoderTable(this::getEntryDataInputDecoderTable);

	}

	public Integer tarDataUnusedSize() {
		return (512 - (int) (getEntryDataSize() % 512l)) % 512;
	}

	private InputDecoderTable getEntryDataInputDecoderTable() {
		return InputDecoderTable.build(InputDecoders.IDENTITY, -1l, getEntryDataSize(), -1l);
	}

	private long getEntryDataSize() {
		return parseValue(this.headerSize.get().get());
	}

	private long parseValue(String string) {
		long value = 0;

		for (char c : string.toCharArray()) {
			if (c < '0' || '7' < c) {
				break;
			}
			value <<= 3;
			value += c - '0';
		}
		return value;
	}

}
