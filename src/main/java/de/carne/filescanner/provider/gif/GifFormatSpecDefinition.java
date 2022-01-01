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
package de.carne.filescanner.provider.gif;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.ByteSpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;
import de.carne.util.Lazy;

/**
 * See GIF.formatspec
 */
final class GifFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("GIF.formatspec"));
	}

	private Lazy<CompositeSpec> gifFormatSpec = resolveLazy("GIF_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> gifHeaderSpec = resolveLazy("GIF_HEADER", CompositeSpec.class);

	private Lazy<CompositeSpec> gifGlobalColorTableSpec = resolveLazy("GIF_GLOBAL_COLOR_TABLE", CompositeSpec.class);
	private Lazy<CompositeSpec> gifLocalColorTableSpec = resolveLazy("GIF_LOCAL_COLOR_TABLE", CompositeSpec.class);

	private Lazy<ByteSpec> lsdPackedFields = resolveLazy("LSD_PACKED_FIELDS", ByteSpec.class);
	private Lazy<ByteSpec> imagePackedFields = resolveLazy("IMAGE_PACKED_FIELDS", ByteSpec.class);

	public CompositeSpec formatSpec() {
		return this.gifFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.gifHeaderSpec.get();
	}

	public FileScannerResultRenderHandler gifRenderer() {
		return RawTransferHandler.IMAGE_GIF_TRANSFER;
	}

	public FileScannerResultExportHandler gifExporter() {
		return RawTransferHandler.IMAGE_GIF_TRANSFER;
	}

	public CompositeSpec globalColorTableSpec() {
		CompositeSpec globalColorTableSpec = FormatSpecs.EMPTY;
		int packedFields = this.lsdPackedFields.get().get().byteValue() & 0xff;

		if ((packedFields & 0x80) == 0x80) {
			globalColorTableSpec = this.gifGlobalColorTableSpec.get();
		}
		return globalColorTableSpec;
	}

	public Integer globalColorTableSize() {
		int packedFields = this.lsdPackedFields.get().get().byteValue() & 0xff;

		return 2 << (packedFields & 0x07);
	}

	public CompositeSpec localColorTableSpec() {
		CompositeSpec localColorTableSpec = FormatSpecs.EMPTY;
		int packedFields = this.imagePackedFields.get().get().byteValue() & 0xff;

		if ((packedFields & 0x80) == 0x80) {
			localColorTableSpec = this.gifLocalColorTableSpec.get();
		}
		return localColorTableSpec;
	}

	public Integer localColorTableSize() {
		int packedFields = this.imagePackedFields.get().get().byteValue() & 0xff;

		return 2 << (packedFields & 0x07);
	}

}
