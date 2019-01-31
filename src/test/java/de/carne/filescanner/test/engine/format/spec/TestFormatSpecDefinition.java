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
package de.carne.filescanner.test.engine.format.spec;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.FormatSpecs;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.RawFileScannerResultExporter;

final class TestFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Test.formatspec"));
	}

	protected String specialText() {
		return getClass().getSimpleName();
	}

	protected FileScannerResultExportHandler specialExport() {
		return RawFileScannerResultExporter.APPLICATION_OCTET_STREAM_EXPORTER;
	}

	protected CompositeSpec conditionalSpec() {
		return FormatSpecs.EMPTY;
	}

	protected EncodedInputSpecConfig encodedInputSpecConfig() {
		return new EncodedInputSpecConfig(".");
	}

}