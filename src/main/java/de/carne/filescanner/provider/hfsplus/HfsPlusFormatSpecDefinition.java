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
package de.carne.filescanner.provider.hfsplus;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.DWordArraySpec;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.QWordSpec;
import de.carne.filescanner.engine.input.DecodedInputMapper;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.util.Lazy;

/**
 * See HfsPlus.formatspec
 */
final class HfsPlusFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("HfsPlus.formatspec"));
	}

	private Lazy<CompositeSpec> hfsPlusFormatSpec = resolveLazy("HFSPLUS_DISK_IMAGE", CompositeSpec.class);
	private Lazy<CompositeSpec> hfsPlusHeaderSpec = resolveLazy("IMAGE_HEADER", CompositeSpec.class);

	private Lazy<DWordSpec> blockSize = resolveLazy("BLOCK_SIZE", DWordSpec.class);
	private Lazy<DWordSpec> totalBlocks = resolveLazy("TOTAL_BLOCKS", DWordSpec.class);
	private Lazy<QWordSpec> extentsLogicalSize = resolveLazy("EXTENTS_LOGICAL_SIZE", QWordSpec.class);
	private Lazy<DWordArraySpec> extentsExtents = resolveLazy("EXTENTS_EXTENTS", DWordArraySpec.class);
	private Lazy<QWordSpec> catalogLogicalSize = resolveLazy("CATALOG_LOGICAL_SIZE", QWordSpec.class);
	private Lazy<DWordArraySpec> catalogExtents = resolveLazy("CATALOG_EXTENTS", DWordArraySpec.class);

	public CompositeSpec formatSpec() {
		return this.hfsPlusFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.hfsPlusHeaderSpec.get();
	}

	protected EncodedInputSpecConfig hfsplusEncodedInputConfig() {
		return new EncodedInputSpecConfig("disk image blocks").decodedInputMapper(this::decodedInputMapper)
				.inputDecoderTable(this::inputDecoderTable);
	}

	private DecodedInputMapper decodedInputMapper() {
		return new HfsPlusInputMapper(this.blockSize.get().get(), this.extentsLogicalSize.get().get(),
				this.extentsExtents.get().get(), this.catalogLogicalSize.get().get(), this.catalogExtents.get().get());
	}

	private InputDecoderTable inputDecoderTable() {
		long blockSizeValue = IntHelper.toUnsignedLong(this.blockSize.get().get());
		long totalBlocksValue = IntHelper.toUnsignedLong(this.totalBlocks.get().get());
		long totalBlockSize = (blockSizeValue != 0 ? (totalBlocksValue * blockSizeValue) - 0xa00 : 0);

		return InputDecoderTable.build(InputDecoders.IDENTITY, 0, totalBlockSize, totalBlockSize);
	}

}
