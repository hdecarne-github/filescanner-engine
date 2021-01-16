/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Objects;

import de.carne.filescanner.engine.ValueStreamerFactory;
import de.carne.filescanner.engine.ValueStreamerStatus;
import de.carne.filescanner.engine.format.ByteSpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;
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

	public FileScannerResultRenderHandler jpegRenderer() {
		return RawTransferHandler.IMAGE_JPEG_TRANSFER;
	}

	public FileScannerResultExportHandler jpegExporter() {
		return RawTransferHandler.IMAGE_JPEG_TRANSFER;
	}

	public Integer app0ThumbnailSize() {
		return Byte.toUnsignedInt(this.xThumbnail.get().get().byteValue())
				* Byte.toUnsignedInt(this.yThumbnail.get().get().byteValue());
	}

	public Integer genericMarkerDataSize() {
		return Short.toUnsignedInt(this.genericLength.get().get().shortValue()) - 2;
	}

	public ValueStreamerFactory sosScanner() {
		return () -> this::streamSos;
	}

	private ValueStreamerStatus streamSos(ByteBuffer buffer) {
		ValueStreamerStatus status = ValueStreamerStatus.FAILED;

		while (buffer.remaining() >= Short.BYTES && status != ValueStreamerStatus.COMPLETE) {
			int bufferPosition = buffer.position();
			int markerValue = Short.toUnsignedInt(buffer.getShort());

			if ((markerValue & 0xff00) == 0xff00 && markerValue != 0xff00 && markerValue != 0xffff
					&& (markerValue < 0xffd0 || 0xffd7 < markerValue)) {
				buffer.position(bufferPosition);
				status = ValueStreamerStatus.COMPLETE;
			} else {
				buffer.position(bufferPosition + 1);
				status = ValueStreamerStatus.STREAMING;
			}
		}
		return status;
	}

}
