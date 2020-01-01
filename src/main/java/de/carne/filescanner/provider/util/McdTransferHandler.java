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
package de.carne.filescanner.provider.util;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.FileScannerResults;
import de.carne.filescanner.engine.transfer.ExportTarget;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRendererHandler;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.transfer.TransferType;
import de.carne.mcd.common.MCDOutput;
import de.carne.mcd.common.MachineCodeDecoder;
import de.carne.mcd.jvm.ClassFileDecoder;

/**
 * {@linkplain FileScannerResultExportHandler} and {@linkplain FileScannerResultRendererHandler} implementation for
 * machine code data decodable via the java-mcd library.
 */
public class McdTransferHandler implements FileScannerResultExportHandler, FileScannerResultRendererHandler {

	private static final Log LOG = new Log();

	/**
	 * Java class file handler.
	 */
	public static final McdTransferHandler JAVA_CLASS_FILE_TRANSFER = new McdTransferHandler(new ClassFileDecoder(),
			".jcf");

	private final MachineCodeDecoder mcd;
	private final String extension;

	private McdTransferHandler(MachineCodeDecoder mcd, String extension) {
		this.mcd = mcd;
		this.extension = extension;
	}

	@Override
	public String name() {
		return this.mcd.name();
	}

	@Override
	public TransferType transferType() {
		return TransferType.TEXT_PLAIN;
	}

	@Override
	public String defaultFileExtension() {
		return this.extension;
	}

	@Override
	public String defaultFileName(FileScannerResult result) throws IOException {
		return FileScannerResults.defaultFileName(result, defaultFileExtension());
	}

	@Override
	public void export(ExportTarget target, FileScannerResultRenderContext context) throws IOException {
		FileScannerResult result = context.result();

		try (SeekableByteChannel mcdInput = result.input().byteChannel(result.start(), result.end())) {
			this.mcd.decode(mcdInput, target);
		}
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		FileScannerResult result = context.result();

		try (SeekableByteChannel mcdInput = result.input().byteChannel(result.start(), result.end());
				MCDOutput mcdOutput = new MCDRenderOutput(out)) {
			this.mcd.decode(mcdInput, mcdOutput);
		} catch (IOException e) {
			LOG.error(e, "Failed to completely decode ''{0}''", this.mcd.name());
		}
	}

	private static class MCDRenderOutput implements MCDOutput {

		private final RenderOutput out;

		MCDRenderOutput(RenderOutput out) {
			this.out = out;
		}

		@Override
		public void flush() throws IOException {
			// Nothing to do here
		}

		@Override
		public void close() throws IOException {
			// nothing to do here
		}

		@Override
		public MCDRenderOutput increaseIndent() throws IOException {
			this.out.increaseIndent();
			return this;
		}

		@Override
		public MCDRenderOutput decreaseIndent() throws IOException {
			this.out.decreaseIndent();
			return this;
		}

		@Override
		public MCDRenderOutput println() throws IOException {
			this.out.writeln();
			return this;
		}

		@Override
		public MCDRenderOutput print(String text) throws IOException {
			return printStyle(RenderStyle.NORMAL, text);
		}

		@Override
		public MCDRenderOutput println(String text) throws IOException {
			return printlnStyle(RenderStyle.NORMAL, text);
		}

		@Override
		public MCDRenderOutput printValue(String value) throws IOException {
			return printStyle(RenderStyle.VALUE, value);
		}

		@Override
		public MCDRenderOutput printlnValue(String value) throws IOException {
			return printlnStyle(RenderStyle.VALUE, value);
		}

		@Override
		public MCDRenderOutput printComment(String comment) throws IOException {
			return printStyle(RenderStyle.COMMENT, comment);
		}

		@Override
		public MCDRenderOutput printlnComment(String comment) throws IOException {
			return printlnStyle(RenderStyle.COMMENT, comment);
		}

		@Override
		public MCDRenderOutput printKeyword(String keyword) throws IOException {
			return printStyle(RenderStyle.KEYWORD, keyword);
		}

		@Override
		public MCDRenderOutput printlnKeyword(String keyword) throws IOException {
			return printlnStyle(RenderStyle.KEYWORD, keyword);
		}

		@Override
		public MCDRenderOutput printOperator(String operator) throws IOException {
			return printStyle(RenderStyle.OPERATOR, operator);
		}

		@Override
		public MCDRenderOutput printlnOperator(String operator) throws IOException {
			return printlnStyle(RenderStyle.OPERATOR, operator);
		}

		@Override
		public MCDRenderOutput printLabel(String label) throws IOException {
			return printStyle(RenderStyle.LABEL, label);
		}

		@Override
		public MCDRenderOutput printlnLabel(String label) throws IOException {
			return printlnStyle(RenderStyle.LABEL, label);
		}

		@Override
		public MCDRenderOutput printError(String error) throws IOException {
			return printStyle(RenderStyle.ERROR, error);
		}

		@Override
		public MCDRenderOutput printlnError(String error) throws IOException {
			return printlnStyle(RenderStyle.ERROR, error);
		}

		private MCDRenderOutput printStyle(RenderStyle style, String text) throws IOException {
			this.out.setStyle(style);
			this.out.write(text);
			return this;
		}

		private MCDRenderOutput printlnStyle(RenderStyle style, String text) throws IOException {
			this.out.setStyle(style);
			this.out.writeln(text);
			return this;
		}

	}

}
