/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.transfer;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.provider.util.FileNames;
import de.carne.io.IOUtil;

/**
 * {@linkplain FileScannerResultExportHandler} implementation for raw data export.
 */
public class RawFileScannerResultExporter implements FileScannerResultExportHandler, FileScannerResultRenderer {

	/**
	 * Predefined APPLICATION_OCTET_STREAM exporter.
	 */
	public static final RawFileScannerResultExporter APPLICATION_OCTET_STREAM_EXPORTER = new RawFileScannerResultExporter(
			"Raw", TransferType.APPLICATION_OCTET_STREAM, ".bin");

	/**
	 * Predefined IMAGE_BMP exporter.
	 */
	public static final RawFileScannerResultExporter IMAGE_BMP_EXPORTER = new RawFileScannerResultExporter(
			"BMP image file", TransferType.IMAGE_BMP, ".bmp");

	/**
	 * Predefined IMAGE_GIF exporter.
	 */
	public static final RawFileScannerResultExporter IMAGE_GIF_EXPORTER = new RawFileScannerResultExporter(
			"GIF image file", TransferType.IMAGE_GIF, ".gif");

	/**
	 * Predefined IMAGE_PNG exporter.
	 */
	public static final RawFileScannerResultExporter IMAGE_PNG_EXPORTER = new RawFileScannerResultExporter(
			"PNG image file", TransferType.IMAGE_PNG, ".png");

	private final String name;
	private final TransferType transferType;
	private final String extension;

	/**
	 * Constructs a new {@linkplain RawFileScannerResultExporter} instance.
	 *
	 * @param name the exporter's name.
	 * @param transferType the exporter's transfer data type.
	 * @param extension the file name extension to use.
	 */
	public RawFileScannerResultExporter(String name, TransferType transferType, String extension) {
		this.name = name;
		this.transferType = transferType;
		this.extension = extension;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public TransferType transferType() {
		return this.transferType;
	}

	@Override
	public String defaultFileExtension() {
		return this.extension;
	}

	@Override
	public String defaultFileName(FileScannerResult result) {
		String fileName;

		if (result.type() == FileScannerResult.Type.INPUT) {
			fileName = FileNames.normalizeFilePath(result.input().name());
		} else {
			fileName = FileNames.mangleFileName(result.name()) + defaultFileExtension();
		}
		return fileName;
	}

	@Override
	public void export(ExportTarget target, FileScannerResultRenderContext context) throws IOException {
		FileScannerResult result = context.result();

		target.setSize(result.size());
		try (ReadableByteChannel resultChannel = result.input().byteChannel(result.start(), result.end())) {
			IOUtil.copyChannel(target, resultChannel);
		}
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		if (out.isEmpty()) {
			out.enableOption(RenderOption.TRANSPARENCY);
		}

		FileScannerResult result = context.result();

		out.writeln(new RawTransferSource(result));
	}

	private class RawTransferSource implements TransferSource {

		private final FileScannerResult result;

		public RawTransferSource(FileScannerResult result) {
			this.result = result;
		}

		@Override
		public String name() {
			return RawFileScannerResultExporter.this.name();
		}

		@Override
		public TransferType transferType() {
			return RawFileScannerResultExporter.this.transferType();
		}

		@Override
		public void transfer(WritableByteChannel target) throws IOException {
			try (ReadableByteChannel resultChannel = this.result.input().byteChannel(this.result.start(),
					this.result.end())) {
				IOUtil.copyChannel(target, resultChannel);
			}
		}

	}

}
