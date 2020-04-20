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

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CharArraySpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.filescanner.engine.util.Alignment;
import de.carne.filescanner.engine.util.HexStrings;
import de.carne.filescanner.engine.util.OctalStrings;
import de.carne.filescanner.engine.util.PrettyFormat;
import de.carne.filescanner.engine.util.SizeRenderer;
import de.carne.filescanner.engine.util.Unix;
import de.carne.filescanner.engine.util.UnixDateRenderer;
import de.carne.util.Lazy;

/**
 * See Cpio.formatspec
 */
final class CpioFormatSpecDefinition extends FormatSpecDefinition {

	public CpioFormatSpecDefinition() {
		addStringAttributeRenderer("OCTAL_PRETTY", OctalStrings.intRenderer(PrettyFormat.INT_FORMATTER));
		addStringAttributeRenderer("OCTAL_SIZE", OctalStrings.intRenderer(SizeRenderer.INT_RENDERER));
		addStringAttributeRenderer("OCTAL_DATE", OctalStrings.intRenderer(UnixDateRenderer.RENDERER));
		addStringAttributeRenderer("OCTAL_MODE", OctalStrings.intRenderer(CpioFormatSpecDefinition::formatMode));
		addStringAttributeRenderer("HEX_PRETTY", HexStrings.longRenderer(PrettyFormat.LONG_FORMATTER));
		addStringAttributeRenderer("HEX_SIZE", HexStrings.longRenderer(SizeRenderer.LONG_RENDERER));
		addStringAttributeRenderer("HEX_DATE", HexStrings.intRenderer(UnixDateRenderer.RENDERER));
		addStringAttributeRenderer("HEX_MODE", HexStrings.intRenderer(CpioFormatSpecDefinition::formatMode));
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

	public Integer odcNameSize() throws IOException {
		String nameSizeString = this.odcNameSize.get().get();

		return OctalStrings.parseInt(nameSizeString);
	}

	public EncodedInputSpecConfig odcEncodedInputConfig() {
		return new EncodedInputSpecConfig("data").decodedInputName(this.odcPathName.get()::getStripped)
				.inputDecoderTable(this::odcInputDecoderTable);
	}

	private InputDecoderTable odcInputDecoderTable() {
		String fileSizeString = this.odcFileSize.get().get();
		long fileSize = OctalStrings.safeParseLong(fileSizeString);

		return InputDecoderTable.build(InputDecoders.IDENTITY, -1, fileSize, -1);
	}

	public Integer newcNameSize() throws IOException {
		String nameSizeString = this.newcNameSize.get().get();
		int nameSize = HexStrings.parseInt(nameSizeString);

		return Alignment.dword(2, nameSize);
	}

	public EncodedInputSpecConfig newcEncodedInputConfig() {
		return new EncodedInputSpecConfig("data").decodedInputName(this.newcPathName.get()::getStripped)
				.inputDecoderTable(this::newcInputDecoderTable);
	}

	private InputDecoderTable newcInputDecoderTable() {
		String fileSizeString = this.newcFileSize.get().get();
		long fileSize = HexStrings.safeParseLong(fileSizeString);

		return InputDecoderTable.build(InputDecoders.IDENTITY, -1, Alignment.dword(fileSize), fileSize);
	}

	private static String formatMode(Integer mode) {
		int modeValue = mode.intValue();
		char type;

		switch (modeValue & 0170000) {
		case 0010000:
			type = 'p';
			break;
		case 0020000:
			type = 'c';
			break;
		case 0040000:
			type = 'd';
			break;
		case 0060000:
			type = 'b';
			break;
		case 0100000:
			type = '-';
			break;
		case 0120000:
			type = 'l';
			break;
		case 0140000:
			type = 's';
			break;
		default:
			type = '?';
		}
		return Unix.formatMode(type, modeValue);
	}

}
