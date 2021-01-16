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
package de.carne.filescanner.engine.transfer.handler;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.RenderOption;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.transfer.renderer.textstyle.PlainStyle;
import de.carne.filescanner.engine.transfer.renderer.textstyle.XMLStyle;

/**
 * {@linkplain FileScannerResultRenderHandler} implementation for styled text rendering.
 */
public class StyledTextRenderHandler implements FileScannerResultRenderHandler {

	private static class Style {

		private final Function<CharStream, Lexer> lexerFactory;
		private final int lineBreakTokenType;
		private final Map<Integer, RenderStyle> styleMap = new HashMap<>();

		private Style(Function<CharStream, Lexer> lexerFactory, int lineBreakTokenType) {
			this.lexerFactory = lexerFactory;
			this.lineBreakTokenType = lineBreakTokenType;
		}

		static Style withLexer(Function<CharStream, Lexer> lexerFactory, int lineBreakType) {
			return new Style(lexerFactory, lineBreakType);
		}

		Style withStyle(int tokenType, RenderStyle style) {
			this.styleMap.put(tokenType, style);
			return this;
		}

		Lexer lexer(CharStream input) {
			return this.lexerFactory.apply(input);
		}

		boolean isLineBreakTokenType(int tokenType) {
			return this.lineBreakTokenType == tokenType;
		}

		RenderStyle style(int tokenType) {
			return this.styleMap.getOrDefault(tokenType, RenderStyle.NORMAL);
		}

	}

	private static final Style PLAIN_STYLE = Style.withLexer(PlainStyle::new, PlainStyle.NEWLINE);
	private static final Style XML_STYLE = Style.withLexer(XMLStyle::new, XMLStyle.NEWLINE)
			.withStyle(XMLStyle.COMMENT, RenderStyle.COMMENT).withStyle(XMLStyle.PREAMBLE, RenderStyle.LABEL)
			.withStyle(XMLStyle.DTD, RenderStyle.LABEL).withStyle(XMLStyle.DEFAULT, RenderStyle.VALUE);

	private final Style style;
	private final Charset charset;

	/**
	 * Predefined TEXT_PLAIN/UTF-8 renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_ASCII_RENDER_HANDLER = new StyledTextRenderHandler(PLAIN_STYLE,
			StandardCharsets.US_ASCII);

	/**
	 * Predefined TEXT_PLAIN/UTF-8 renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_ISO8859_RENDER_HANDLER = new StyledTextRenderHandler(PLAIN_STYLE,
			StandardCharsets.ISO_8859_1);

	/**
	 * Predefined TEXT_PLAIN/UTF-8 renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_UTF8_RENDER_HANDLER = new StyledTextRenderHandler(PLAIN_STYLE,
			StandardCharsets.UTF_8);

	/**
	 * Predefined TEXT_PLAIN/UTF-16LE renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_UTF16LE_RENDER_HANDLER = new StyledTextRenderHandler(PLAIN_STYLE,
			StandardCharsets.UTF_16LE);

	/**
	 * Predefined TEXT_PLAIN/UTF-16BE renderer handler.
	 */
	public static final StyledTextRenderHandler PLAIN_UTF16BE_RENDER_HANDLER = new StyledTextRenderHandler(PLAIN_STYLE,
			StandardCharsets.UTF_16BE);

	/**
	 * Predefined TEXT_XML/UTF-8 renderer handler.
	 */
	public static final StyledTextRenderHandler XML_UTF8_RENDER_HANDLER = new StyledTextRenderHandler(XML_STYLE,
			StandardCharsets.UTF_8);

	/**
	 * Constructs a XML style render handler for the given {@linkplain Charset}.
	 *
	 * @param charset the {@linkplain Charset} to use for XML decoding.
	 * @return a XML style render handler for the given {@linkplain Charset}.
	 */
	public static final StyledTextRenderHandler xmlRenderHandler(Charset charset) {
		return (StandardCharsets.UTF_8.equals(charset) ? XML_UTF8_RENDER_HANDLER
				: new StyledTextRenderHandler(XML_STYLE, charset));
	}

	private StyledTextRenderHandler(Style style, Charset charset) {
		this.style = style;
		this.charset = charset;
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		out.enableOption(RenderOption.WRAP);

		StyledTextCharStream lexerInput = newLexerInput(context);
		Lexer lexer = this.style.lexer(lexerInput);

		while (!lexer._hitEOF) {
			Token token = lexer.nextToken();
			int tokenType = token.getType();

			if (!this.style.isLineBreakTokenType(tokenType)) {
				out.setStyle(this.style.style(tokenType));
				out.write(token.getText());
			} else {
				out.writeln();
			}
		}
		context.skip(lexerInput.decodedBytes());
	}

	private StyledTextCharStream newLexerInput(FileScannerResultRenderContext context) throws IOException {
		FileScannerResult result = context.result();
		ReadableByteChannel channel = result.input().byteChannel(context.position(), result.end());

		return new StyledTextCharStream(channel, context.remaining(), this.charset);
	}

}
