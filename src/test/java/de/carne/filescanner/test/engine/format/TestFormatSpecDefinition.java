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
package de.carne.filescanner.test.engine.format;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.ScanSpecConfig;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RawTransferHandler;

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

	public CompositeSpec conditionalSpec() {
		return FormatSpecs.EMPTY;
	}

	public ScanSpecConfig scanConfig() {
		return new ScanSpecConfig(1, buffer -> false);
	}

	public EncodedInputSpecConfig encodedInputSpecConfig() {
		return new EncodedInputSpecConfig(".");
	}

}
