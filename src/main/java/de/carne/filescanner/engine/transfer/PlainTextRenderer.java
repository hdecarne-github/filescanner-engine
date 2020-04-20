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
package de.carne.filescanner.engine.transfer;

import java.io.IOException;
import java.io.Writer;

/**
 * {@linkplain Renderer} implementation suitable for creating a plain {@linkplain String} representation of a scan
 * result.
 */
public class PlainTextRenderer implements Renderer {

	private static final String INDENT = "    ";

	private final Writer writer;
	private final boolean autoClose;

	/**
	 * Constructs a new {@linkplain PlainTextRenderer} instance.
	 *
	 * @param writer the {@linkplain Writer} to write the rendered output into.
	 */
	public PlainTextRenderer(Writer writer) {
		this(writer, true);
	}

	/**
	 * Constructs a new {@linkplain PlainTextRenderer} instance.
	 *
	 * @param writer the {@linkplain Writer} to write the rendered output into.
	 * @param autoClose whether to close the {@linkplain Writer} when this instance is closed.
	 */
	public PlainTextRenderer(Writer writer, boolean autoClose) {
		this.writer = writer;
		this.autoClose = autoClose;
	}

	@Override
	public void close() throws IOException {
		if (this.autoClose) {
			this.writer.close();
		} else {
			this.writer.flush();
		}
	}

	@Override
	public boolean isStyled() {
		return false;
	}

	@Override
	public void emitText(int indent, RenderStyle style, String text, boolean lineBreak) throws IOException {
		emitIndent(indent);
		this.writer.write(text);
		emitLineBreak(lineBreak);
	}

	@Override
	public void emitMediaData(int indent, RenderStyle style, TransferSource source, boolean lineBreak)
			throws IOException {
		emitIndent(indent);
		this.writer.write("[" + source.transferType().mimeType() + "]");
		emitLineBreak(lineBreak);
	}

	private void emitIndent(int indent) throws IOException {
		for (int indentCount = 0; indentCount < indent; indentCount++) {
			this.writer.write(INDENT);
		}
	}

	private void emitLineBreak(boolean lineBreak) throws IOException {
		if (lineBreak) {
			this.writer.write(System.lineSeparator());
		}
	}

	@Override
	public String toString() {
		return this.writer.toString();
	}

}
