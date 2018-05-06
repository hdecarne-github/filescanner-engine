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
import java.io.InterruptedIOException;

/**
 * {@linkplain StringBuilder} based {@linkplain Renderer} implementation suitable for creating a plain
 * {@linkplain String} representation of a scan result.
 */
public class StringRenderer implements Renderer {

	private final int limit;
	private final StringBuilder buffer = new StringBuilder();

	/**
	 * Constructs a new {@linkplain StringRenderer} instance.
	 * <p>
	 * The created instance has no length limit set.
	 *
	 * @see StringRenderer#StringRenderer(int)
	 */
	public StringRenderer() {
		this(Integer.MAX_VALUE);
	}

	/**
	 * Constructs a new {@linkplain StringRenderer} instance.
	 * <p>
	 * As soon as the rendered {@linkplain String} length exceeds the given limit an {@linkplain InterruptedIOException}
	 * is thrown.
	 *
	 * @param limit the {@linkplain String} length limit to stop rendering at.
	 */
	public StringRenderer(int limit) {
		this.limit = limit;
	}

	@Override
	public void close() {
		// Nothing to do here
	}

	@Override
	public void emitText(RenderStyle style, String text, boolean lineBreak) throws IOException, InterruptedException {
		this.buffer.append(text);
		if (lineBreak) {
			this.buffer.append(System.lineSeparator());
		}
		if (this.buffer.length() >= this.limit) {
			InterruptedIOException interruptedIOException = new InterruptedIOException();

			interruptedIOException.bytesTransferred = this.buffer.length();
			throw interruptedIOException;
		}
	}

	@Override
	public String toString() {
		return this.buffer.toString();
	}

}
