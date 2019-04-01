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
package de.carne.filescanner.provider.xar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.zip.InflaterInputStream;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.ExportTarget;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderer;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.TransferType;
import de.carne.io.IOUtil;

class TocExporter implements FileScannerResultExportHandler, FileScannerResultRenderer {

	@Override
	public void export(ExportTarget target, FileScannerResultRenderContext context) throws IOException {
		FileScannerResult result = context.result();

		target.setSize(result.size());
		try (ReadableByteChannel resultChannel = newChannelInputStream(result)) {
			IOUtil.copyChannel(target, resultChannel);
		}
	}

	@Override
	public String name() {
		return "TOC XML";
	}

	@Override
	public TransferType transferType() {
		return TransferType.TEXT_XML;
	}

	@Override
	public String defaultFileExtension() {
		return ".xml";
	}

	@Override
	public String defaultFileName(FileScannerResult result) {
		return "toc.xml";
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		try (BufferedReader lineReader = newBufferedReader(context.result())) {
			String line;

			while ((line = lineReader.readLine()) != null) {
				out.writeln(line);
			}
		}
	}

	private ReadableByteChannel newChannelInputStream(FileScannerResult result) throws IOException {
		return Channels.newChannel(new InflaterInputStream(result.input().inputStream(result.start(), result.end())));
	}

	private BufferedReader newBufferedReader(FileScannerResult result) throws IOException {
		return new BufferedReader(
				new InputStreamReader(new InflaterInputStream(result.input().inputStream(result.start(), result.end())),
						StandardCharsets.UTF_8));
	}

}
