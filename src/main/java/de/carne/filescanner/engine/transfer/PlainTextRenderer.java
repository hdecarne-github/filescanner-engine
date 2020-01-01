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

	/**
	 * Constructs a new {@linkplain PlainTextRenderer} instance.
	 *
	 * @param writer the {@linkplain Writer} to write the rendered output into.
	 */
	public PlainTextRenderer(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}

	@Override
	public boolean isStyled() {
		return false;
	}

	@Override
	public void emitText(int indent, RenderStyle style, String text, boolean lineBreak) throws IOException {
		for (int indentCount = 0; indentCount < indent; indentCount++) {
			this.writer.write(INDENT);
		}
		this.writer.write(text);
		if (lineBreak) {
			this.writer.write(System.lineSeparator());
		}
	}

	@Override
	public void emitMediaData(int indent, RenderStyle style, TransferSource source, boolean lineBreak)
			throws IOException {
		// Do nothing
	}

	@Override
	public String toString() {
		return this.writer.toString();
	}

}
