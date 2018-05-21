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

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultExporter;
import de.carne.filescanner.provider.util.FileNames;
import de.carne.io.IOUtil;

/**
 * {@linkplain FileScannerResultExporter} implementation for raw data export.
 */
public class RawFileScannerResultExporter implements FileScannerResultExporter {

	/**
	 * Predefined APPLICATION_OCTET_STREAM exporter.
	 */
	public static final RawFileScannerResultExporter APPLICATION_OCTET_STREAM_EXPORTER = new RawFileScannerResultExporter(
			"Raw", Type.APPLICATION_OCTET_STREAM, ".bin");

	/**
	 * Predefined PNG_IMAGE exporter.
	 */
	public static final RawFileScannerResultExporter PNG_IMAGE_EXPORTER = new RawFileScannerResultExporter(
			"PNG image file", Type.IMAGE_PNG, ".png");

	private final String name;
	private final Type type;
	private final String extension;

	/**
	 * Constructs a new {@linkplain RawFileScannerResultExporter} instance.
	 *
	 * @param name the exporter's name.
	 * @param type the exporter's type.
	 * @param extension the file name extension to use.
	 */
	public RawFileScannerResultExporter(String name, Type type, String extension) {
		this.name = name;
		this.type = type;
		this.extension = extension;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public Type type() {
		return this.type;
	}

	@Override
	public String defaultFileName(FileScannerResult result) {
		String fileName;

		if (result.type() == FileScannerResult.Type.INPUT) {
			fileName = FileNames.normalizeFilePath(result.input().name());
		} else {
			fileName = FileNames.mangleFileName(result.name()) + this.extension;
		}
		return fileName;
	}

	@Override
	public void export(FileScannerResult result, ExportTarget target) throws IOException {
		target.setSize(result.size());
		try (ReadableByteChannel resultChannel = result.input().byteChannel(result.start(), result.end())) {
			IOUtil.copyChannel(target, resultChannel);
		}
	}

}
