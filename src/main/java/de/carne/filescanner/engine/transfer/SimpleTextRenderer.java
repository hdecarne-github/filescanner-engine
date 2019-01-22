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
package de.carne.filescanner.engine.transfer;

import java.io.IOException;
import java.io.Writer;

import de.carne.filescanner.engine.util.EmitCounter;

/**
 * {@linkplain Renderer} implementation suitable for creating a plain {@linkplain String} representation of a scan
 * result.
 */
public class SimpleTextRenderer implements Renderer {

	private final Writer writer;

	/**
	 * Constructs a new {@linkplain SimpleTextRenderer} instance.
	 *
	 * @param writer the {@linkplain Writer} to write the rendered output into.
	 */
	public SimpleTextRenderer(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}

	@Override
	public int emitText(RenderStyle style, String text, boolean lineBreak) throws IOException {
		EmitCounter counter = new EmitCounter();

		this.writer.write(counter.count(text));
		if (lineBreak) {
			this.writer.write(counter.count(System.lineSeparator()));
		}
		return counter.value();
	}

	@Override
	public int emitMediaData(RenderStyle style, TransferSource source, boolean lineBreak) throws IOException {
		// Do nothing
		return 0;
	}

	@Override
	public String toString() {
		return this.writer.toString();
	}

}
