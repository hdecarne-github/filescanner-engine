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
package de.carne.filescanner.engine.transfer;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.provider.util.FileNames;
import de.carne.io.IOUtil;

/**
 * {@linkplain FileScannerResultExportHandler} and {@linkplain FileScannerResultRendererHandler} implementation for raw
 * data transfer.
 */
public class RawTransferHandler implements FileScannerResultExportHandler, FileScannerResultRendererHandler {

	/**
	 * Predefined APPLICATION_OCTET_STREAM transfer handler.
	 */
	public static final RawTransferHandler APPLICATION_OCTET_STREAM_TRANSFER = new RawTransferHandler("Raw",
			TransferType.APPLICATION_OCTET_STREAM, ".bin");

	/**
	 * Predefined IMAGE_BMP transfer handler.
	 */
	public static final RawTransferHandler IMAGE_BMP_TRANSFER = new RawTransferHandler("BMP image file",
			TransferType.IMAGE_BMP, ".bmp");

	/**
	 * Predefined IMAGE_GIF transfer handler.
	 */
	public static final RawTransferHandler IMAGE_GIF_TRANSFER = new RawTransferHandler("GIF image file",
			TransferType.IMAGE_GIF, ".gif");

	/**
	 * Predefined IMAGE_JPEG transfer handler.
	 */
	public static final RawTransferHandler IMAGE_JPEG_TRANSFER = new RawTransferHandler("JPEG image file",
			TransferType.IMAGE_JPEG, ".jpg");

	/**
	 * Predefined IMAGE_PNG transfer handler.
	 */
	public static final RawTransferHandler IMAGE_PNG_TRANSFER = new RawTransferHandler("PNG image file",
			TransferType.IMAGE_PNG, ".png");

	/**
	 * Predefined IMAGE_TIFF transfer handler.
	 */
	public static final RawTransferHandler IMAGE_TIFF_TRANSFER = new RawTransferHandler("TIFF image file",
			TransferType.IMAGE_TIFF, ".tif");

	/**
	 * Predefined TEXT_PLAIN transfer handler.
	 */
	public static final RawTransferHandler TEXT_PLAIN_TRANSFER = new RawTransferHandler("Plain text file",
			TransferType.TEXT_PLAIN, ".txt");

	/**
	 * Predefined TEXT_XML transfer handler.
	 */
	public static final RawTransferHandler TEXT_XML_TRANSFER = new RawTransferHandler("XML file", TransferType.TEXT_XML,
			".xml");

	/**
	 * Predefined TEXT_XML transfer handler.
	 */
	public static final RawTransferHandler APPLICATION_PDF_TRANSFER = new RawTransferHandler("PDF file",
			TransferType.APPLICATION_PDF, ".pdf");

	private final String name;
	private final TransferType transferType;
	private final String extension;

	/**
	 * Constructs a new {@linkplain RawTransferHandler} instance.
	 *
	 * @param name the transfer handler's name.
	 * @param transferType the exporter's transfer data type.
	 * @param extension the file name extension to use.
	 */
	public RawTransferHandler(String name, TransferType transferType, String extension) {
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
		try (ReadableByteChannel resultChannel = newResultChannel(result)) {
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

	protected ReadableByteChannel newResultChannel(FileScannerResult result) throws IOException {
		return result.input().byteChannel(result.start(), result.end());
	}

	private class RawTransferSource implements TransferSource {

		private final FileScannerResult result;

		public RawTransferSource(FileScannerResult result) {
			this.result = result;
		}

		@Override
		public String name() {
			return RawTransferHandler.this.name();
		}

		@Override
		public TransferType transferType() {
			return RawTransferHandler.this.transferType();
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
