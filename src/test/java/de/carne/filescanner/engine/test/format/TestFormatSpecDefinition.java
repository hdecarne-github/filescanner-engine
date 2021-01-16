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
package de.carne.filescanner.engine.test.format;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.ValueStreamerFactory;
import de.carne.filescanner.engine.ValueStreamerStatus;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;

final class TestFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Test.formatspec"));
	}

	public String specialText() {
		return getClass().getSimpleName();
	}

	public FileScannerResultRenderHandler customRenderer() {
		return RawTransferHandler.APPLICATION_OCTET_STREAM_TRANSFER;
	}

	public FileScannerResultExportHandler customExport() {
		return RawTransferHandler.APPLICATION_OCTET_STREAM_TRANSFER;
	}

	public ValueStreamerFactory customScanner() {
		return () -> (buffer -> ValueStreamerStatus.FAILED);
	}

	public CompositeSpec customConditionalSpec() {
		return FormatSpecs.EMPTY;
	}

	public EncodedInputSpecConfig customEncodedInputSpecConfig() {
		return new EncodedInputSpecConfig(".");
	}

}
