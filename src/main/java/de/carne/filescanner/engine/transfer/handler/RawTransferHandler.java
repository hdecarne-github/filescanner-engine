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
package de.carne.filescanner.engine.transfer.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResult.Type;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RenderOption;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.TransferSource;
import de.carne.filescanner.engine.transfer.TransferType;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.FileScannerResults;
import de.carne.io.IOUtil;

/**
 * {@linkplain FileScannerResultExportHandler} and {@linkplain FileScannerResultRenderHandler} implementation for raw
 * data transfer.
 */
public class RawTransferHandler implements FileScannerResultExportHandler, FileScannerResultRenderHandler {

	/**
	 * Predefined APPLICATION_OCTET_STREAM transfer handler.
	 */
	public static final RawTransferHandler APPLICATION_OCTET_STREAM_TRANSFER = new RawTransferHandler("Raw",
			TransferType.APPLICATION_OCTET_STREAM, ".bin");

	/**
	 * Predefined IMAGE_BMP transfer handler.
	 */
	public static final RawTransferHandler IMAGE_BMP_TRANSFER = new RawTransferHandler("BMP image file",
			TransferType.IMAGE_BMP, ".bmp", RenderOption.TRANSPARENCY);

	/**
	 * Predefined IMAGE_GIF transfer handler.
	 */
	public static final RawTransferHandler IMAGE_GIF_TRANSFER = new RawTransferHandler("GIF image file",
			TransferType.IMAGE_GIF, ".gif", RenderOption.TRANSPARENCY);

	/**
	 * Predefined IMAGE_JPEG transfer handler.
	 */
	public static final RawTransferHandler IMAGE_JPEG_TRANSFER = new RawTransferHandler("JPEG image file",
			TransferType.IMAGE_JPEG, ".jpg", RenderOption.TRANSPARENCY);

	/**
	 * Predefined IMAGE_PNG transfer handler.
	 */
	public static final RawTransferHandler IMAGE_PNG_TRANSFER = new RawTransferHandler("PNG image file",
			TransferType.IMAGE_PNG, ".png", RenderOption.TRANSPARENCY);

	/**
	 * Predefined IMAGE_TIFF transfer handler.
	 */
	public static final RawTransferHandler IMAGE_TIFF_TRANSFER = new RawTransferHandler("TIFF image file",
			TransferType.IMAGE_TIFF, ".tif", RenderOption.TRANSPARENCY);

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
	private final Set<RenderOption> renderOptions = new HashSet<>();

	/**
	 * Constructs a new {@linkplain RawTransferHandler} instance.
	 *
	 * @param name the transfer handler's name.
	 * @param transferType the exporter's transfer data type.
	 * @param extension the file name extension to use.
	 * @param renderOptions the render options to use.
	 */
	public RawTransferHandler(String name, TransferType transferType, String extension,
			@NonNull RenderOption... renderOptions) {
		this.name = name;
		this.transferType = transferType;
		this.extension = extension;
		for (RenderOption renderOption : renderOptions) {
			this.renderOptions.add(renderOption);
		}
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
	public String defaultFileName(FileScannerResult result) throws IOException {
		return FileScannerResults.defaultFileName(result,
				(result.type() != Type.INPUT ? defaultFileExtension() : null));
	}

	@Override
	public TransferSource export(FileScannerResultRenderContext context) throws IOException {
		return new RawTransferSource(context.result());
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		if (out.isEmpty()) {
			for (RenderOption renderOption : this.renderOptions) {
				out.enableOption(renderOption);
			}
		}

		FileScannerResult result = context.result();

		out.writeln(new RawTransferSource(result));
		context.skip(context.remaining());
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
		public long size() {
			return this.result.size();
		}

		@Override
		public void transfer(WritableByteChannel target) throws IOException {
			try (ReadableByteChannel resultChannel = this.result.input().byteChannel(this.result.start(),
					this.result.end())) {
				IOUtil.copyChannel(target, resultChannel);
			}
		}

		@Override
		public void transfer(OutputStream target) throws IOException {
			try (InputStream resultStream = this.result.input().inputStream(this.result.start(), this.result.end())) {
				IOUtil.copyStream(target, resultStream);
			}
		}

	}

}
