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
package de.carne.filescanner.provider.jpeg;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.ByteSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.ScanSpecConfig;
import de.carne.filescanner.engine.format.spec.WordSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RawTransferHandler;
import de.carne.nio.compression.InsufficientDataException;
import de.carne.util.Lazy;

/**
 * See JPEG.formatspec
 */
final class JpegFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("JPEG.formatspec"));
	}

	private Lazy<CompositeSpec> jpegFormatSpec = resolveLazy("JPEG_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> jpegHeaderSpec = resolveLazy("JPEG_SOI_APP0_MARKER", CompositeSpec.class);

	private Lazy<ByteSpec> xThumbnail = resolveLazy("X_THUMBNAIL", ByteSpec.class);
	private Lazy<ByteSpec> yThumbnail = resolveLazy("Y_THUMBNAIL", ByteSpec.class);
	private Lazy<WordSpec> genericLength = resolveLazy("GENERIC_LENGTH", WordSpec.class);

	public CompositeSpec formatSpec() {
		return this.jpegFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.jpegHeaderSpec.get();
	}

	protected FileScannerResultRenderHandler jpegRenderer() {
		return RawTransferHandler.IMAGE_JPEG_TRANSFER;
	}

	protected FileScannerResultExportHandler jpegExporter() {
		return RawTransferHandler.IMAGE_JPEG_TRANSFER;
	}

	protected Integer app0ThumbnailSize() {
		return Byte.toUnsignedInt(this.xThumbnail.get().get().byteValue())
				* Byte.toUnsignedInt(this.yThumbnail.get().get().byteValue());
	}

	protected Integer genericMarkerDataSize() {
		return Short.toUnsignedInt(this.genericLength.get().get().shortValue()) - 2;
	}

	protected ScanSpecConfig sosScanConfig() {
		return new ScanSpecConfig(2, this::matchMarker);
	}

	private boolean matchMarker(ByteBuffer buffer) throws IOException {
		if (buffer.remaining() < Short.BYTES) {
			throw new InsufficientDataException(Short.BYTES, buffer.remaining());
		}

		int bufferPosition = buffer.position();
		int markerValue = (buffer.getShort() & 0xffff);
		boolean matchStatus = ((markerValue & 0xff00) == 0xff00 && markerValue != 0xff00 && markerValue != 0xffff
				&& (markerValue < 0xffd0 || 0xffd7 < markerValue));

		buffer.position(matchStatus ? bufferPosition : bufferPosition + 1);
		return matchStatus;
	}

}
