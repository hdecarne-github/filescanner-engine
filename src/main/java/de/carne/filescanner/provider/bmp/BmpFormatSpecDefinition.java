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
package de.carne.filescanner.provider.bmp;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.FormatSpecs;
import de.carne.filescanner.engine.format.spec.WordSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRendererHandler;
import de.carne.filescanner.engine.transfer.RawTransferHandler;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.filescanner.engine.util.ShortHelper;
import de.carne.util.Lazy;

/**
 * See GIF.formatspec
 */
final class BmpFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("BMP.formatspec"));
	}

	private Lazy<CompositeSpec> bmpFormatSpec = resolveLazy("BMP_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> bmpHeaderSpec = resolveLazy("BMP_FILE_HEADER", CompositeSpec.class);

	private Lazy<CompositeSpec> bitFieldsSpec = resolveLazy("BIT_FIELDS", CompositeSpec.class);
	private Lazy<CompositeSpec> rgbQuadColorTableSpec = resolveLazy("RGB_QUAD_COLOR_TABLE", CompositeSpec.class);

	private Lazy<DWordSpec> biWidth = resolveLazy("BI_WIDTH", DWordSpec.class);
	private Lazy<DWordSpec> biHeight = resolveLazy("BI_HEIGHT", DWordSpec.class);
	private Lazy<WordSpec> biBitCount = resolveLazy("BI_BIT_COUNT", WordSpec.class);
	private Lazy<DWordSpec> biCompression = resolveLazy("BI_COMPRESSION", DWordSpec.class);
	private Lazy<DWordSpec> biSizeImage = resolveLazy("BI_SIZE_IMAGE", DWordSpec.class);
	private Lazy<DWordSpec> biClrUsed = resolveLazy("BI_CLR_USED", DWordSpec.class);

	public CompositeSpec formatSpec() {
		return this.bmpFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.bmpHeaderSpec.get();
	}

	protected FileScannerResultRendererHandler bmpRenderer() {
		return RawTransferHandler.IMAGE_BMP_TRANSFER;
	}

	protected FileScannerResultExportHandler bmpExporter() {
		return RawTransferHandler.IMAGE_BMP_TRANSFER;
	}

	protected CompositeSpec colorTableSpec() {
		CompositeSpec colorTableSpec = FormatSpecs.EMPTY;

		if (this.biCompression.get().get() == 3) {
			colorTableSpec = this.bitFieldsSpec.get();
		} else if (this.biBitCount.get().get() <= 8) {
			colorTableSpec = this.rgbQuadColorTableSpec.get();

		}
		return colorTableSpec;
	}

	protected Integer colorTableSize() {
		int colorTableSize = this.biClrUsed.get().get();

		if (colorTableSize == 0) {
			colorTableSize = 2 << ShortHelper.toUnsignedInt(this.biBitCount.get().get());
		}
		return colorTableSize;
	}

	protected Long imageSize() {
		long imageSize = IntHelper.toUnsignedLong(this.biSizeImage.get().get());

		if (imageSize == 0) {
			long biWidthValue = this.biWidth.get().get();
			long biHeightValue = Math.abs(this.biHeight.get().get());
			int biBitCountValue = this.biBitCount.get().get();

			imageSize = (((biWidthValue * biBitCountValue + 31) >> 5) << 2) * biHeightValue;
		}
		return imageSize;
	}

}
