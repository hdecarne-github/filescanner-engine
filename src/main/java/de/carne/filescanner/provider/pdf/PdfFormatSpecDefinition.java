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
package de.carne.filescanner.provider.pdf;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRendererHandler;
import de.carne.filescanner.engine.transfer.RawTransferHandler;
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

	protected FileScannerResultRendererHandler pdfRenderer() {
		return RawTransferHandler.APPLICATION_PDF_TRANSFER;
	}

	protected FileScannerResultExportHandler pdfExporter() {
		return RawTransferHandler.APPLICATION_PDF_TRANSFER;
	}

}
