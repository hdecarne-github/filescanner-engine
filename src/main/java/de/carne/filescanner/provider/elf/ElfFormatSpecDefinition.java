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
package de.carne.filescanner.provider.elf;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.QWordSpec;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.McdTransferHandler;
import de.carne.filescanner.engine.transfer.handler.RangeRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;
import de.carne.util.Lazy;

/**
 * See ELF.formatspec
 */
final class ElfFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("ELF.formatspec"));
	}

	private Lazy<CompositeSpec> elfFormatSpec = resolveLazy("ELF_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> elfHeaderSpec = resolveLazy("ELF_HEADER_64", CompositeSpec.class);

	private Lazy<WordSpec> elf64Machine = resolveLazy("ELF_MACHINE_64", WordSpec.class);

	private Lazy<DWordSpec> section64Type = resolveLazy("SHEADER_TYPE_64", DWordSpec.class);
	private Lazy<QWordSpec> section64Flags = resolveLazy("SHEADER_FLAGS_64", QWordSpec.class);
	private Lazy<QWordSpec> section64Offset = resolveLazy("SHEADER_OFFSET_64", QWordSpec.class);
	private Lazy<QWordSpec> section64Size = resolveLazy("SHEADER_SIZE_64", QWordSpec.class);

	public CompositeSpec formatSpec() {
		return this.elfFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.elfHeaderSpec.get();
	}

	public Long section64Offset() {
		int section64TypeValue = this.section64Type.get().get().intValue();

		return (section64TypeValue != 8 /* SHT_NOBITS */ ? this.section64Offset.get().get() : 0l);
	}

	public Long section64Size() {
		int section64TypeValue = this.section64Type.get().get().intValue();

		return (section64TypeValue != 8 /* SHT_NOBITS */ ? this.section64Size.get().get() : 0l);
	}

	public FileScannerResultRenderHandler section64Renderer() {
		int machine = this.elf64Machine.get().get().intValue();
		long flags = this.section64Flags.get().get().longValue();
		FileScannerResultRenderHandler renderer = RangeRenderHandler.RENDER_HANDLER;

		if ((flags & 0x4) == 0x04 /* SHF_EXECINSTR */ && machine == 0x3e /* EM_X86_64 */) {
			renderer = McdTransferHandler.X86B64_TRANSFER;
		}
		return renderer;
	}

	public FileScannerResultExportHandler section64Exporter() {
		int machine = this.elf64Machine.get().get().intValue();
		long flags = this.section64Flags.get().get().longValue();
		FileScannerResultExportHandler exporter = RawTransferHandler.APPLICATION_OCTET_STREAM_TRANSFER;

		if ((flags & 0x4) == 0x04 /* SHF_EXECINSTR */ && machine == 0x3e /* EM_X86_64 */) {
			exporter = McdTransferHandler.X86B64_TRANSFER;
		}
		return exporter;
	}

}
