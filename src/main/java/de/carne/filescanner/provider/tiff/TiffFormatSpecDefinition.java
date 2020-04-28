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
package de.carne.filescanner.provider.tiff;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.DecodeAtSpec;
import de.carne.filescanner.engine.format.FormatSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.RangeAttributeSpec;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.util.Lazy;

/**
 * See TIFF.formatspec
 */
final class TiffFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("TIFF.formatspec"));
	}

	private Lazy<CompositeSpec> tiffFormatSpec = resolveLazy("TIFF_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> tiffHeaderLESpec = resolveLazy("TIFF_HEADER_CLASSIC_LE", CompositeSpec.class);
	private Lazy<CompositeSpec> tiffDirectoryLESpec = resolveLazy("TIFF_DIRECTORY_LE", CompositeSpec.class);

	private Lazy<WordSpec> tdirType = resolveLazy("TDIR_TYPE", WordSpec.class);
	private Lazy<DWordSpec> tdirCount = resolveLazy("TDIR_COUNT", DWordSpec.class);
	private Lazy<DWordSpec> tdirOffset = resolveLazy("TDIR_OFFSET", DWordSpec.class);

	private Lazy<DWordSpec> nextDirOff = resolveLazy("NEXT_DIR_OFF", DWordSpec.class);

	public CompositeSpec formatSpec() {
		return this.tiffFormatSpec.get();
	}

	public List<CompositeSpec> headerSpecs() {
		List<CompositeSpec> headerSpecs = new ArrayList<>();

		headerSpecs.add(this.tiffHeaderLESpec.get());
		return headerSpecs;
	}

	public FormatSpec directoryEntryLE() {
		int dataType = this.tdirType.get().get().intValue();
		int dataCount = this.tdirCount.get().get().intValue();
		int dataSize = calculateDataSize(dataType, dataCount);
		FormatSpec resultSpec = FormatSpecs.EMPTY;

		if (dataSize > 4) {
			long dataOffset = this.tdirOffset.get().get().longValue();
			StructSpec entrySpec = new StructSpec();

			entrySpec.result("TIFFEntry");
			entrySpec.add(new RangeAttributeSpec("").size(dataSize));
			resultSpec = new DecodeAtSpec(entrySpec).position(dataOffset).level(1);
		}
		return resultSpec;
	}

	private int calculateDataSize(int dataType, int dataCount) {
		int dataSize;

		switch (dataType) {
		case 1:
		case 2:
		case 6:
		case 7:
			dataSize = dataCount;
			break;
		case 3:
		case 8:
			dataSize = dataCount << 1;
			break;
		case 4:
		case 9:
		case 11:
			dataSize = dataCount << 2;
			break;
		case 5:
		case 10:
		case 12:
			dataSize = dataCount << 3;
			break;
		default:
			dataSize = -1;
		}
		return dataSize;
	}

	public FormatSpec nextDirectoryLE() {
		Integer nextDirOffValue = this.nextDirOff.get().get();
		FormatSpec nextDirectorySpec;

		if (nextDirOffValue.intValue() != 0) {
			DecodeAtSpec directoryAtSpec = new DecodeAtSpec(this.tiffDirectoryLESpec.get());

			directoryAtSpec.position(IntHelper.toUnsignedLong(nextDirOffValue));
			directoryAtSpec.level(1);
			nextDirectorySpec = directoryAtSpec;
		} else {
			nextDirectorySpec = FormatSpecs.EMPTY;
		}
		return nextDirectorySpec;
	}

	public FileScannerResultRenderHandler tiffRenderer() {
		return RawTransferHandler.IMAGE_TIFF_TRANSFER;
	}

	public FileScannerResultExportHandler tiffExporter() {
		return RawTransferHandler.IMAGE_TIFF_TRANSFER;
	}

}
