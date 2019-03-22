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
package de.carne.filescanner.provider.udif;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.util.Strings;

final class BlkxDataDecoder {

	private static final int BUFFER_EXTENT_SIZE = 1024;

	private byte[] buffer = new byte[BUFFER_EXTENT_SIZE];
	private int decoded = 0;
	private @Nullable IOException exception = null;
	private int register = 0;
	private int bits = 0;
	private int padding = 0;

	public void reset() {
		this.decoded = 0;
		this.exception = null;
		this.register = 0;
		this.bits = 0;
		this.padding = 0;
	}

	public boolean isEmpty() {
		return this.decoded == 0;
	}

	public ByteBuffer getResult() throws IOException {
		flush();
		if (this.exception != null) {
			throw this.exception;
		}
		return ByteBuffer.wrap(this.buffer, 0, this.decoded);
	}

	public void feed(char[] ch, int start, int length) {
		int end = start + length;

		for (int chIndex = start; chIndex < end; chIndex++) {
			feed0(ch[chIndex]);
		}
	}

	@SuppressWarnings("squid:S3776")
	private void feed0(int c) {
		if (this.exception == null) {
			int value = -1;

			if ('A' <= c && c <= 'Z') {
				value = c - 'A';
			} else if ('a' <= c && c <= 'z') {
				value = 26 + (c - 'a');
			} else if ('0' <= c && c <= '9') {
				value = 52 + (c - '0');
			} else if (c == '+') {
				value = 62 + (c - '+');
			} else if (c == '/') {
				value = 63 + (c - '/');
			} else if (c == '=') {
				this.padding += 6;
				value = 0;
			} else if (!Character.isWhitespace(c)) {
				this.exception = new IOException("Unexpected character: " + Strings.encode("'" + (char) c + "'"));
			}
			if (value >= 0) {
				this.register = (this.register << 6) | value;
				this.bits += 6;
				if (this.bits == 24) {
					flush();
				}
			}
		}
	}

	private void flush() {
		if (this.padding < this.bits) {
			byte[] b = new byte[3];

			b[0] = (byte) ((this.register >> 16) & 0xff);
			b[1] = (byte) ((this.register >> 8) & 0xff);
			b[2] = (byte) (this.register & 0xff);

			int len = b.length - (this.padding / 6);

			if (this.buffer.length < this.decoded + len) {
				byte[] buffer2 = new byte[this.buffer.length + BUFFER_EXTENT_SIZE];

				System.arraycopy(this.buffer, 0, buffer2, 0, this.buffer.length);
				this.buffer = buffer2;
			}
			System.arraycopy(b, 0, this.buffer, this.decoded, len);
			this.decoded += len;
		}
		this.register = 0;
		this.bits = 0;
		this.padding = 0;
	}

}
