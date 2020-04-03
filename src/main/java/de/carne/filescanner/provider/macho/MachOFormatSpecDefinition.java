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
package de.carne.filescanner.provider.macho;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.QWordSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RangeRenderHandler;
import de.carne.filescanner.engine.transfer.RawTransferHandler;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.filescanner.provider.util.McdTransferHandler;
import de.carne.util.Lazy;

/**
 * See MachO.formatspec
 */
final class MachOFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("MachO.formatspec"));
	}

	private Lazy<CompositeSpec> machoFormatSpec = resolveLazy("MACHO_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> machHeaderSpec = resolveLazy("MACH_HEADER_64", CompositeSpec.class);

	private Lazy<DWordSpec> cpuType = resolveLazy("CPU_TYPE", DWordSpec.class);
	private Lazy<DWordSpec> sizeOfCmds = resolveLazy("SIZE_OF_CMDS", DWordSpec.class);
	private Lazy<DWordSpec> cmdSize = resolveLazy("CMD_SIZE", DWordSpec.class);
	private Lazy<DWordSpec> segment64Flags = resolveLazy("SEGMENT64_FLAGS", DWordSpec.class);
	private Lazy<QWordSpec> segment64Offset = resolveLazy("SEGMENT64_OFFSET", QWordSpec.class);
	private Lazy<QWordSpec> segment64Size = resolveLazy("SEGMENT64_SIZE", QWordSpec.class);

	public CompositeSpec formatSpec() {
		return this.machoFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.machHeaderSpec.get();
	}

	public Long cmdSize() {
		return IntHelper.toUnsignedLong(this.cmdSize.get().get()) - 8;
	}

	public Long segment64Position() {
		long minOffsetValue = 0x20l + Integer.toUnsignedLong(this.sizeOfCmds.get().get().intValue());
		long segment64OffsetValue = this.segment64Offset.get().get().longValue();

		return Math.max(minOffsetValue, segment64OffsetValue);
	}

	public Long segment64Size() {
		long segment64SizeValue = this.segment64Size.get().get().longValue();
		long minOffsetValue = 0x20l + Integer.toUnsignedLong(this.sizeOfCmds.get().get().intValue());
		long segment64OffsetValue = this.segment64Offset.get().get().longValue();

		if (segment64OffsetValue < minOffsetValue) {
			segment64SizeValue = Math.max(segment64SizeValue - (minOffsetValue - segment64OffsetValue), 0);
		}
		return segment64SizeValue;
	}

	public FileScannerResultRenderHandler segment64Renderer() {
		int cpuTypeValue = this.cpuType.get().get().intValue();
		int flags = this.segment64Flags.get().get().intValue();
		FileScannerResultRenderHandler renderer = RangeRenderHandler.RENDER_HANDLER;

		if ((flags & 0x4) == 0x04 /* VM_PROT_EXECUTE */ && cpuTypeValue == 0x1000007 /* CPU_TYPE_X86_64 */) {
			renderer = McdTransferHandler.X86B64_TRANSFER;
		}
		return renderer;
	}

	public FileScannerResultExportHandler segment64Exporter() {
		int cpuTypeValue = this.cpuType.get().get().intValue();
		int flags = this.segment64Flags.get().get().intValue();
		FileScannerResultExportHandler exporter = RawTransferHandler.APPLICATION_OCTET_STREAM_TRANSFER;

		if ((flags & 0x4) == 0x04 /* VM_PROT_EXECUTE */ && cpuTypeValue == 0x1000007 /* CPU_TYPE_X86_64 */) {
			exporter = McdTransferHandler.X86B64_TRANSFER;
		}
		return exporter;
	}

}
