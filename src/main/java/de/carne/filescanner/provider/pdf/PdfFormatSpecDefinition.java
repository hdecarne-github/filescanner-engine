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
package de.carne.filescanner.provider.pdf;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;
import de.carne.util.Lazy;

/**
 * See Pdf.formatspec
 */
final class PdfFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Pdf.formatspec"));
	}

	private Lazy<CompositeSpec> pdfFormatSpec = resolveLazy("PDF_STREAM", CompositeSpec.class);
	private Lazy<CompositeSpec> pdfMagicSpec = resolveLazy("PDF_MAGIC", CompositeSpec.class);

	public CompositeSpec formatSpec() {
		return this.pdfFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.pdfMagicSpec.get();
	}

	public FileScannerResultRenderHandler pdfRenderer() {
		return RawTransferHandler.APPLICATION_PDF_TRANSFER;
	}

	public FileScannerResultExportHandler pdfExporter() {
		return RawTransferHandler.APPLICATION_PDF_TRANSFER;
	}

}
