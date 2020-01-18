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
package de.carne.filescanner.provider.exe;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.WordSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRendererHandler;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.filescanner.engine.util.ShortHelper;
import de.carne.filescanner.provider.util.McdTransferHandler;
import de.carne.util.Lazy;

/**
 * See EXE.formatspec
 */
final class ExeFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("EXE.formatspec"));
	}

	private Lazy<CompositeSpec> exeFormatSpec = resolveLazy("EXE_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> exeHeaderSpec = resolveLazy("IMAGE_DOS_HEADER", CompositeSpec.class);

	private Lazy<WordSpec> imageFileMachine = resolveLazy("IMAGE_FILE_MACHINE", WordSpec.class);
	private Lazy<WordSpec> stubRelocationCount = resolveLazy("STUB_RELOCATION_COUNT", WordSpec.class);
	private Lazy<DWordSpec> stubNextHeaderOffset = resolveLazy("NEXT_HEADER_OFFSET", DWordSpec.class);
	private Lazy<DWordSpec> ntSectionCharacteristics = resolveLazy("NT_SECTION_CHARACTERISTICS", DWordSpec.class);

	private Lazy<CompositeSpec> genericSeciontSpec = resolveLazy("IMAGE_NT_GENERIC_SECTION", CompositeSpec.class);
	private Lazy<CompositeSpec> i386SeciontSpec = resolveLazy("IMAGE_NT_I386_SECTION", CompositeSpec.class);
	private Lazy<CompositeSpec> amd64SeciontSpec = resolveLazy("IMAGE_NT_AMD64_SECTION", CompositeSpec.class);

	public CompositeSpec formatSpec() {
		return this.exeFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.exeHeaderSpec.get();
	}

	protected Long stubTextSize() {
		int relocationCount = ShortHelper.toUnsignedInt(this.stubRelocationCount.get().get());
		long nextHeaderOffset = IntHelper.toUnsignedLong(this.stubNextHeaderOffset.get().get());

		return nextHeaderOffset - 0x40 - (relocationCount * 4);
	}

	protected FileScannerResultRendererHandler x86b16Renderer() {
		return McdTransferHandler.X86B16_TRANSFER;
	}

	protected FileScannerResultExportHandler x86b16Exporter() {
		return McdTransferHandler.X86B16_TRANSFER;
	}

	protected FileScannerResultRendererHandler x86b32Renderer() {
		return McdTransferHandler.X86B32_TRANSFER;
	}

	protected FileScannerResultExportHandler x86b32Exporter() {
		return McdTransferHandler.X86B32_TRANSFER;
	}

	protected FileScannerResultRendererHandler x86b64Renderer() {
		return McdTransferHandler.X86B64_TRANSFER;
	}

	protected FileScannerResultExportHandler x86b64Exporter() {
		return McdTransferHandler.X86B64_TRANSFER;
	}

	protected CompositeSpec sectionSpec() {
		int characteristics = this.ntSectionCharacteristics.get().get().intValue();
		CompositeSpec spec = this.genericSeciontSpec.get();

		if ((characteristics & 0x00000020) == 0x00000020) {
			int machine = ShortHelper.toUnsignedInt(this.imageFileMachine.get().get());

			switch (machine) {
			case 0x014c:
				spec = this.i386SeciontSpec.get();
				break;
			case 0x8664:
				spec = this.amd64SeciontSpec.get();
				break;
			default:
				// Nothing to do here
			}
		}
		return spec;
	}

}
