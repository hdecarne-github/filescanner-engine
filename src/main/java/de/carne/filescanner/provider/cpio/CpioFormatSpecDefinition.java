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
package de.carne.filescanner.provider.cpio;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CharArraySpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.filescanner.provider.util.Alignment;
import de.carne.util.Lazy;

/**
 * See Cpio.formatspec
 */
final class CpioFormatSpecDefinition extends FormatSpecDefinition {

	public CpioFormatSpecDefinition() {

	}

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Cpio.formatspec"));
	}

	private Lazy<CompositeSpec> cpioFormatSpec = resolveLazy("CPIO_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> cpioHeaderSpec = resolveLazy("CPIO_ENTRY", CompositeSpec.class);

	private Lazy<CharArraySpec> odcNameSize = resolveLazy("ODC_NAME_SIZE", CharArraySpec.class);
	private Lazy<CharArraySpec> odcFileSize = resolveLazy("ODC_FILE_SIZE", CharArraySpec.class);
	private Lazy<CharArraySpec> odcPathName = resolveLazy("ODC_PATH_NAME", CharArraySpec.class);

	private Lazy<CharArraySpec> newcNameSize = resolveLazy("NEWC_NAME_SIZE", CharArraySpec.class);
	private Lazy<CharArraySpec> newcFileSize = resolveLazy("NEWC_FILE_SIZE", CharArraySpec.class);
	private Lazy<CharArraySpec> newcPathName = resolveLazy("NEWC_PATH_NAME", CharArraySpec.class);

	public CompositeSpec formatSpec() {
		return this.cpioFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.cpioHeaderSpec.get();
	}

	public Integer odcNameSize() {
		String nameSizeString = this.odcNameSize.get().get();

		return parseOctInt(nameSizeString);
	}

	public EncodedInputSpecConfig odcEncodedInputConfig() {
		return new EncodedInputSpecConfig("data").decodedInputName(this.odcPathName.get()::getStripped)
				.inputDecoderTable(this::odcInputDecoderTable);
	}

	private InputDecoderTable odcInputDecoderTable() {
		String fileSizeString = this.odcFileSize.get().get();
		long fileSize = parseOctLong(fileSizeString);

		return InputDecoderTable.build(InputDecoders.IDENTITY, -1, fileSize, -1);
	}

	public Integer newcNameSize() {
		String nameSizeString = this.newcNameSize.get().get();
		int nameSize = parseHexInt(nameSizeString);

		return Alignment.dword(2, nameSize);
	}

	public EncodedInputSpecConfig newcEncodedInputConfig() {
		return new EncodedInputSpecConfig("data").decodedInputName(this.newcPathName.get()::getStripped)
				.inputDecoderTable(this::newcInputDecoderTable);
	}

	private InputDecoderTable newcInputDecoderTable() {
		String fileSizeString = this.newcFileSize.get().get();
		long fileSize = parseHexLong(fileSizeString);

		return InputDecoderTable.build(InputDecoders.IDENTITY, -1, Alignment.dword(fileSize), fileSize);
	}

	private int parseOctInt(String string) {
		int value = 0;

		for (char c : string.toCharArray()) {
			if (c < '0' || '7' < c) {
				break;
			}
			value <<= 3;
			value += c - '0';
		}
		return value;
	}

	private int parseHexInt(String string) {
		int value = 0;

		for (char c : string.toCharArray()) {
			if ('0' <= c && c <= '9') {
				value <<= 4;
				value += c - '0';
			} else if ('a' <= c && c <= 'f') {
				value <<= 4;
				value += 10 + c - 'a';
			} else if ('A' <= c && c <= 'F') {
				value <<= 4;
				value += 10 + c - 'A';
			}
		}
		return value;
	}

	private long parseOctLong(String string) {
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

	private long parseHexLong(String string) {
		long value = 0;

		for (char c : string.toCharArray()) {
			if ('0' <= c && c <= '9') {
				value <<= 4;
				value += c - '0';
			} else if ('a' <= c && c <= 'f') {
				value <<= 4;
				value += 10 + c - 'a';
			} else if ('A' <= c && c <= 'F') {
				value <<= 4;
				value += 10 + c - 'A';
			}
		}
		return value;
	}

}
