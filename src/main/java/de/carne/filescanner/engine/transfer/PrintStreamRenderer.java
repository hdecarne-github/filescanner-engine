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
import java.io.PrintStream;

/**
 * Simple {@linkplain PrintStream} based {@linkplain Renderer} implementation suitable for writing scan results to a
 * {@linkplain PrintStream}.
 */
public class PrintStreamRenderer implements Renderer {

	private final PrintStream ps;
	private final boolean autoClose;

	/**
	 * Constructs a new {@linkplain PrintStreamRenderer} instance.
	 *
	 * @param ps the {@linkplain PrintStream} to print to.
	 * @param autoClose whether to close the underlying {@linkplain PrintStream} on {@linkplain Renderer} close.
	 */
	public PrintStreamRenderer(PrintStream ps, boolean autoClose) {
		this.ps = ps;
		this.autoClose = autoClose;
	}

	@Override
	public void emitText(RenderStyle style, String text, boolean lineBreak) throws IOException {
		if (lineBreak) {
			this.ps.println(text);
		} else {
			this.ps.print(text);
		}
	}

	@Override
	public void close() {
		if (this.autoClose) {
			this.ps.close();
		}
	}

}
