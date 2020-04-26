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
package de.carne.filescanner.engine.transfer.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RenderOption;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.transfer.TransferType;
import de.carne.filescanner.engine.transfer.renderer.textstyle.XMLStyle;

/**
 * {@linkplain FileScannerResultRenderHandler} implementation for styled text rendering.
 */
public class StyledTextRenderHandler implements FileScannerResultRenderHandler {

	private final @Nullable Function<CharStream, Lexer> lexerFactory;
	private final Map<Integer, RenderStyle> styleMap = new HashMap<>();
	private final Charset charset;

	/**
	 * Predefined TEXT_PLAIN/UTF-8 renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_ASCII_RENDER_HANDLER = new StyledTextRenderHandler(
			TransferType.TEXT_PLAIN, StandardCharsets.US_ASCII);

	/**
	 * Predefined TEXT_PLAIN/UTF-8 renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_ISO8859_RENDER_HANDLER = new StyledTextRenderHandler(
			TransferType.TEXT_PLAIN, StandardCharsets.ISO_8859_1);

	/**
	 * Predefined TEXT_PLAIN/UTF-8 renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_UTF8_RENDER_HANDLER = new StyledTextRenderHandler(
			TransferType.TEXT_PLAIN, StandardCharsets.UTF_8);

	/**
	 * Predefined TEXT_PLAIN/UTF-16LE renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_UTF16LE_RENDER_HANDLER = new StyledTextRenderHandler(
			TransferType.TEXT_PLAIN, StandardCharsets.UTF_16LE);

	/**
	 * Predefined TEXT_PLAIN/UTF-16BE renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_UTF16BE_RENDER_HANDLER = new StyledTextRenderHandler(
			TransferType.TEXT_PLAIN, StandardCharsets.UTF_16BE);

	/**
	 * Predefined TEXT_XML renderer handler.
	 */
	public static final StyledTextRenderHandler XML_RENDER_HANDLER = new StyledTextRenderHandler(TransferType.TEXT_XML);

	/**
	 * Constructs a new {@linkplain StyledTextRenderHandler} instance.
	 *
	 * @param transferType the {@linkplain TransferType} used to determine the render style.
	 */
	public StyledTextRenderHandler(TransferType transferType) {
		this(transferType, StandardCharsets.UTF_8);
	}

	/**
	 * Constructs a new {@linkplain StyledTextRenderHandler} instance.
	 *
	 * @param transferType the {@linkplain TransferType} used to determine the render style.
	 * @param charset the {@linkplain Charset} of the text to display.
	 */
	public StyledTextRenderHandler(TransferType transferType, Charset charset) {
		switch (transferType) {
		case TEXT_PLAIN:
			this.lexerFactory = null;
			break;
		case TEXT_XML:
			this.lexerFactory = XMLStyle::new;
			this.styleMap.put(XMLStyle.COMMENT, RenderStyle.COMMENT);
			this.styleMap.put(XMLStyle.PREAMBLE, RenderStyle.LABEL);
			this.styleMap.put(XMLStyle.DTD, RenderStyle.LABEL);
			this.styleMap.put(XMLStyle.DEFAULT, RenderStyle.VALUE);
			break;
		default:
			throw new IllegalArgumentException("Unsupported transfer type: " + transferType);
		}
		this.charset = charset;
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		out.enableOption(RenderOption.WRAP);

		Function<CharStream, Lexer> checkedLexerFactory = this.lexerFactory;

		if (checkedLexerFactory != null && out.isStyled()) {
			FileScannerResult result = context.result();
			CharStream lexerInput = new StyledTextCharStream(result.input().range(context.position(), result.end()),
					this.charset);
			Lexer lexer = checkedLexerFactory.apply(lexerInput);

			while (!lexer._hitEOF) {
				Token token = lexer.nextToken();
				int tokenType = token.getType();

				if (tokenType != 1) {
					out.setStyle(this.styleMap.getOrDefault(tokenType, RenderStyle.NORMAL));
					out.write(token.getText());
				} else {
					out.writeln();
				}
			}
		} else {
			try (BufferedReader lineReader = newLineReader(context)) {
				String line;

				while ((line = lineReader.readLine()) != null) {
					out.writeln(line);
				}
			}
		}
		// context.skip(context.remaining());
	}

	private BufferedReader newLineReader(FileScannerResultRenderContext context) throws IOException {
		return new BufferedReader(new InputStreamReader(newResultStream(context), this.charset));
	}

	protected InputStream newResultStream(FileScannerResultRenderContext context) throws IOException {
		FileScannerResult result = context.result();

		return result.input().inputStream(context.position(), result.end());
	}

}
