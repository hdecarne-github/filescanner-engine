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
package de.carne.filescanner.engine.transfer.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.FileScannerResults;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.transfer.TransferSource;
import de.carne.filescanner.engine.transfer.TransferType;
import de.carne.mcd.MCDOutput;
import de.carne.mcd.MachineCodeDecoder;
import de.carne.mcd.PlainMCDOutput;
import de.carne.mcd.jvmdecoder.ClassFileDecoder;
import de.carne.mcd.x86decoder.X86b16Decoder;
import de.carne.mcd.x86decoder.X86b32Decoder;
import de.carne.mcd.x86decoder.X86b64Decoder;
import de.carne.util.logging.Log;

/**
 * {@linkplain FileScannerResultExportHandler} and {@linkplain FileScannerResultRenderHandler} implementation for
 * machine code data decodable via the java-mcd library.
 */
public class McdTransferHandler implements FileScannerResultExportHandler, FileScannerResultRenderHandler {

	private static final Log LOG = new Log();

	private static final String EXTENSION_TXT = ".txt";

	/**
	 * Java class file handler.
	 */
	public static final McdTransferHandler JAVA_CLASS_FILE_TRANSFER = new McdTransferHandler(ClassFileDecoder::new,
			ClassFileDecoder.NAME, EXTENSION_TXT);

	/**
	 * x86-16 code handler.
	 */
	public static final McdTransferHandler X86B16_TRANSFER = new McdTransferHandler(X86b16Decoder::new,
			X86b16Decoder.NAME, EXTENSION_TXT);

	/**
	 * x86-32 code handler.
	 */
	public static final McdTransferHandler X86B32_TRANSFER = new McdTransferHandler(X86b32Decoder::new,
			X86b32Decoder.NAME, EXTENSION_TXT);

	/**
	 * x86-64 code handler.
	 */
	public static final McdTransferHandler X86B64_TRANSFER = new McdTransferHandler(X86b64Decoder::new,
			X86b64Decoder.NAME, EXTENSION_TXT);

	private final Supplier<MachineCodeDecoder> mcd;
	private final String name;
	private final String extension;

	private McdTransferHandler(Supplier<MachineCodeDecoder> mcd, String name, String extension) {
		this.mcd = mcd;
		this.name = name;
		this.extension = extension;
	}

	@Override
	public String name() {
		return this.name;
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
	public TransferSource export(FileScannerResultRenderContext context) throws IOException {
		Supplier<MachineCodeDecoder> exportMcd = this.mcd;
		FileScannerResult exportResult = context.result();

		return new TransferSource() {

			@Override
			public String name() {
				return McdTransferHandler.this.name();
			}

			@Override
			public TransferType transferType() {
				return McdTransferHandler.this.transferType();
			}

			@Override
			public long size() {
				return -1;
			}

			@Override
			public void transfer(WritableByteChannel target) throws IOException {
				try (ReadableByteChannel in = exportResult.input().byteChannel(exportResult.start(),
						exportResult.end()); PlainMCDOutput out = new PlainMCDOutput(target, false)) {
					exportMcd.get().decode(in, out);
				}
			}

			@Override
			public void transfer(OutputStream target) throws IOException {
				try (WritableByteChannel targetChannel = Channels.newChannel(target)) {
					transfer(targetChannel);
				}
			}
		};

	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		FileScannerResult result = context.result();
		long renderStart = context.position();

		try (ReadableByteChannel in = result.input().byteChannel(renderStart, result.end())) {
			long decodeStart = renderStart - result.start();
			long decoded = this.mcd.get().decode(in, new MCDRenderOutput(out), decodeStart);

			context.skip(decoded);
		} catch (IOException e) {
			LOG.error(e, "Failed to completely decode ''{0}''", this.name);
		}
	}

	private static class MCDRenderOutput implements MCDOutput {

		private final RenderOutput out;

		MCDRenderOutput(RenderOutput out) {
			this.out = out;
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
