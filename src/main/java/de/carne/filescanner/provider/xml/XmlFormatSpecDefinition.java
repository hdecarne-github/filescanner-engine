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
package de.carne.filescanner.provider.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.carne.filescanner.engine.ValueStreamer;
import de.carne.filescanner.engine.ValueStreamerFactory;
import de.carne.filescanner.engine.ValueStreamerStatus;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.ScanAttributeSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;
import de.carne.filescanner.engine.transfer.handler.StyledTextRenderHandler;
import de.carne.io.IOUtil;
import de.carne.util.Late;
import de.carne.util.Lazy;
import de.carne.util.logging.Log;

/**
 * See Xml.formatspec
 */
final class XmlFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Xml.formatspec"));
	}

	private Lazy<CompositeSpec> xmlFormatSpec = resolveLazy("XML_STREAM", CompositeSpec.class);
	private Lazy<CompositeSpec> xmlDeclSpec = resolveLazy("XML_DECL", CompositeSpec.class);
	private Lazy<ScanAttributeSpec> declSpec = resolveLazy("DECL", ScanAttributeSpec.class);

	public CompositeSpec formatSpec() {
		return this.xmlFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.xmlDeclSpec.get();
	}

	public ValueStreamerFactory declScanner() {
		return DeclScanner::new;
	}

	public FileScannerResultRenderHandler xmlRenderer() throws IOException {
		Charset charset = DeclScanner.streamCharset(this.declSpec.get().get().stream());

		return StyledTextRenderHandler.xmlRenderHandler(charset);
	}

	public FileScannerResultExportHandler xmlExporter() {
		return RawTransferHandler.TEXT_XML_TRANSFER;
	}

	private static final class DeclScanner implements ValueStreamer {

		private static final Log LOG = new Log();

		private static final int SIGNATURE_BYTE_ENCODING = 0x6d783f3c;
		private static final int SIGNATURE_UTF8_ENCODING = 0x3cbfbbef;
		private static final int SIGNATURE_UTF16LE_ENCODING = 0x003cfffe;
		private static final int SIGNATURE_UTF16BE_ENCODING = 0x3c00feff;

		private static final Pattern PATTERN_XML_DECL = Pattern
				.compile("<\\?xml version=\"[^\"]*\"( encoding=\"([^\"]*)\")?.*\\?>");

		private interface StepFunction {
			ValueStreamerStatus apply(ByteBuffer buffer);
		}

		private final Late<CharsetDecoder> decoderHolder = new Late<>();
		private String decodedCharsetName = StandardCharsets.UTF_8.name();
		private int bytesPerChar = 0;
		private final CharBuffer decodeBuffer = CharBuffer.allocate(256);
		private boolean decodeBufferUnderflow = false;
		private StepFunction step;

		DeclScanner() {
			this.step = this::streamInit;
		}

		public static Charset streamCharset(InputStream stream) throws IOException {
			ByteBuffer buffer = ByteBuffer.wrap(IOUtil.readAllBytes(stream));
			DeclScanner scanner = new DeclScanner();
			ValueStreamerStatus streamStatus = scanner.stream(buffer);

			if (streamStatus != ValueStreamerStatus.COMPLETE) {
				LOG.warning("Failed to decode XML charset (stream status: {})", streamStatus);
			}

			Charset charset;

			try {
				charset = Charset.forName(scanner.decodedCharsetName);
			} catch (IllegalCharsetNameException e) {
				LOG.warning(e, "Failed to instantiate charset ''{0}''", scanner.decodedCharsetName);

				charset = StandardCharsets.UTF_8;
			}
			return charset;
		}

		@Override
		public ValueStreamerStatus stream(ByteBuffer buffer) {
			ValueStreamerStatus status;

			do {
				status = this.step.apply(buffer);
			} while (status == ValueStreamerStatus.STREAMING && buffer.hasRemaining() && !this.decodeBufferUnderflow);
			return status;
		}

		private ValueStreamerStatus streamInit(ByteBuffer buffer) {
			ValueStreamerStatus status = ValueStreamerStatus.FAILED;

			if (buffer.hasRemaining()) {
				this.step = this::streamSignature;
				status = ValueStreamerStatus.STREAMING;
			}
			return status;
		}

		private ValueStreamerStatus streamSignature(ByteBuffer buffer) {
			ValueStreamerStatus status = ValueStreamerStatus.FAILED;

			if (buffer.remaining() >= 4) {
				byte signature0 = buffer.get();
				byte signature1 = buffer.get();
				byte signature2 = buffer.get();
				byte signature3 = buffer.get();
				int signature = (signature0 & 0xff) | ((signature1 & 0xff) << 8) | ((signature2 & 0xff) << 16)
						| ((signature3 & 0xff) << 24);

				if (signature == SIGNATURE_BYTE_ENCODING) {
					initDecoder(StandardCharsets.UTF_8, 1);
					this.decodeBuffer.append("<?xm");
					this.step = this::streamDecl;
					status = ValueStreamerStatus.STREAMING;
				} else if (signature == SIGNATURE_UTF8_ENCODING) {
					initDecoder(StandardCharsets.UTF_8, 1);
					this.decodeBuffer.append("<?");
					this.step = this::streamDecl;
					status = ValueStreamerStatus.STREAMING;
				} else if (signature == SIGNATURE_UTF16LE_ENCODING) {
					initDecoder(StandardCharsets.UTF_16LE, 2);
					this.decodeBuffer.append("<");
					this.step = this::streamDecl;
					status = ValueStreamerStatus.STREAMING;
				} else if (signature == SIGNATURE_UTF16BE_ENCODING) {
					initDecoder(StandardCharsets.UTF_16BE, 2);
					this.decodeBuffer.append("<");
					this.step = this::streamDecl;
					status = ValueStreamerStatus.STREAMING;
				}
			}
			return status;
		}

		private void initDecoder(Charset charset, int bpc) {
			this.decoderHolder.set(charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
					.onUnmappableCharacter(CodingErrorAction.REPLACE));
			this.decodedCharsetName = charset.name();
			this.bytesPerChar = bpc;
		}

		private ValueStreamerStatus streamDecl(ByteBuffer buffer) {
			ValueStreamerStatus status = ValueStreamerStatus.FAILED;
			int decodeStart = buffer.position();
			CoderResult coderResult = this.decoderHolder.get().decode(buffer, this.decodeBuffer, false);
			String decoded = this.decodeBuffer.duplicate().flip().toString();
			Matcher matcher = PATTERN_XML_DECL.matcher(decoded);

			if (matcher.lookingAt()) {
				String matcherGroup2 = matcher.group(2);

				if (matcherGroup2 != null) {
					this.decodedCharsetName = matcherGroup2;
				}
				buffer.position(decodeStart + (matcher.end() - 1) * this.bytesPerChar);
				status = ValueStreamerStatus.COMPLETE;
			} else if (coderResult.isOverflow()) {
				status = ValueStreamerStatus.FAILED;
			}
			this.decodeBufferUnderflow = coderResult.isUnderflow();
			return status;
		}

	}

}
