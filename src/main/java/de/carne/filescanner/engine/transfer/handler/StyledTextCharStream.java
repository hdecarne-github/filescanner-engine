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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.util.Check;
import de.carne.util.Exceptions;
import de.carne.util.logging.Log;

class StyledTextCharStream implements CharStream {

	private static final Log LOG = new Log();

	private static final int READ_BUFFER_SIZE = 1024;
	private static final int DECODE_BUFFER_SIZE = 2048;

	private final ReadableByteChannel channel;
	private final long channelSize;
	private boolean channelEof = false;
	private final CharsetDecoder decoder;
	private final ByteBuffer readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE).position(READ_BUFFER_SIZE);
	private CharBuffer decodeBuffer0 = CharBuffer.allocate(DECODE_BUFFER_SIZE);
	private int decoded0 = 0;
	private CharBuffer decodeBuffer1 = CharBuffer.allocate(DECODE_BUFFER_SIZE);
	private int decoded1 = 0;
	private int decodeBufferDisplacement = 0;
	private int markIndex = -1;
	private long decodedBytes = 0;

	StyledTextCharStream(ReadableByteChannel channel, long channelSize, Charset charset) {
		this.channel = channel;
		this.channelSize = channelSize;
		this.decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	public long decodedBytes() {
		return this.decodedBytes;
	}

	@Override
	public void consume() {
		if (this.decodeBuffer0.hasRemaining()) {
			this.decodeBuffer0.get();
		} else {
			this.decodeBuffer1.get();
		}
		compactDecodeBuffer();
	}

	@Override
	public int LA(int i) {
		feedDecodeBuffer(i);

		int position0 = this.decodeBuffer0.position();
		int position1 = this.decodeBuffer1.position();
		int lookPosition = position0 + position1 + (i > 0 ? i - 1 : i);
		int symbol;

		if (lookPosition < this.decoded0) {
			symbol = this.decodeBuffer0.get(lookPosition);
		} else if (lookPosition - this.decoded0 < this.decoded1) {
			symbol = this.decodeBuffer1.get(lookPosition - this.decoded0);
		} else {
			symbol = EOF;
		}
		return symbol;
	}

	@Override
	public int mark() {
		int marker = this.markIndex;

		this.markIndex = index();
		return marker;
	}

	@Override
	public void release(int marker) {
		this.markIndex = marker;
	}

	@Override
	public int index() {
		return this.decodeBufferDisplacement + this.decodeBuffer0.position() + this.decodeBuffer1.position();
	}

	@Override
	public void seek(int index) {
		int seekPosition = index - this.decodeBufferDisplacement;

		if (seekPosition < 0) {
			LOG.error("");
		}

		int i = this.decodeBuffer0.position() + this.decodeBuffer1.position() - seekPosition;

		if (i > 0) {
			feedDecodeBuffer(i);
		}

		if (seekPosition <= this.decoded0) {
			this.decodeBuffer0.position(seekPosition);
		} else {
			this.decodeBuffer0.position(this.decoded0);
			this.decodeBuffer1.position(seekPosition - this.decoded0);
		}
		compactDecodeBuffer();
	}

	@Override
	public int size() {
		return (int) Math.min(this.channelSize, Integer.MAX_VALUE);
	}

	@Override
	public String getSourceName() {
		return this.channel.toString();
	}

	@Override
	public String getText(@Nullable Interval interval) {
		Objects.requireNonNull(interval);

		int a = interval.a - this.decodeBufferDisplacement;
		int b = interval.b - this.decodeBufferDisplacement;

		int length = interval.length();
		char[] buffer = new char[length];

		if (b < this.decoded0) {
			CharBuffer intervalBuffer0 = this.decodeBuffer0.duplicate().position(a).limit(b + 1);

			intervalBuffer0.get(buffer);
		} else if (a >= this.decoded0) {
			CharBuffer intervalBuffer1 = this.decodeBuffer1.duplicate().position(a - this.decoded0)
					.limit(b - this.decoded0 + 1);

			intervalBuffer1.get(buffer, 0, intervalBuffer1.remaining());
		} else {
			CharBuffer intervalBuffer0 = this.decodeBuffer0.duplicate().position(a).limit(this.decoded0);
			int length0 = intervalBuffer0.remaining();

			intervalBuffer0.get(buffer, 0, length0);

			CharBuffer intervalBuffer1 = this.decodeBuffer1.duplicate().position(0).limit(length - length0);

			intervalBuffer1.get(buffer, length0, intervalBuffer1.remaining());
		}
		return new String(buffer);
	}

	@SuppressWarnings("java:S3776")
	private void feedDecodeBuffer(int i) {
		if (i > 0) {
			int position0 = this.decodeBuffer0.position();
			int position1 = this.decodeBuffer1.position();
			int feed = position0 + position1 + i - (this.decoded0 + this.decoded1);

			if (feed > 0) {
				try {
					CoderResult decodeResult = (this.readBuffer.hasRemaining() ? CoderResult.OVERFLOW
							: CoderResult.UNDERFLOW);

					do {
						if (decodeResult.isUnderflow() && this.decodedBytes < this.channelSize) {
							feedReadBuffer();
						}

						int readBufferStart = this.readBuffer.position();
						int capacity = this.decodeBuffer0.capacity();

						if (this.decoded0 < capacity) {
							int feeds0 = Math.min(feed, capacity - this.decoded0);
							CharBuffer decodeBuffer = this.decodeBuffer0.duplicate();

							decodeBuffer.position(this.decoded0);
							decodeBuffer.limit(this.decoded0 + feeds0);
							decodeResult = this.decoder.decode(this.readBuffer, decodeBuffer, this.channelEof);

							int nextDecoded0 = decodeBuffer.position();

							feed -= (nextDecoded0 - this.decoded0);
							this.decoded0 = nextDecoded0;
						} else if (this.decoded1 < capacity) {
							int feeds1 = Math.min(feed, capacity - this.decoded1);
							CharBuffer decodeBuffer = this.decodeBuffer1.duplicate();

							decodeBuffer.position(this.decoded1);
							decodeBuffer.limit(this.decoded1 + feeds1);
							decodeResult = this.decoder.decode(this.readBuffer, decodeBuffer, this.channelEof);

							int nextDecoded1 = decodeBuffer.position();

							feed -= (nextDecoded1 - this.decoded1);
							this.decoded1 = nextDecoded1;
						} else {
							Check.fail("Insufficent LA buffer");
						}
						this.decodedBytes += (this.readBuffer.position() - readBufferStart);
					} while (feed > 0 && (!this.channelEof || this.readBuffer.hasRemaining()));
				} catch (IOException e) {
					throw Exceptions.toRuntime(e);
				}
			}
		}
	}

	private void feedReadBuffer() throws IOException {
		if (this.readBuffer.hasRemaining()) {
			ByteBuffer remainingBuffer = this.readBuffer.duplicate();

			this.readBuffer.clear();
			this.readBuffer.put(remainingBuffer);
		} else {
			this.readBuffer.clear();
		}

		int read = 0;

		while (read >= 0 && this.readBuffer.hasRemaining()) {
			read = this.channel.read(this.readBuffer);
			if (read < 0) {
				this.channelEof = true;
			}
		}
		this.readBuffer.flip();
	}

	private void compactDecodeBuffer() {
		if (!this.decodeBuffer0.hasRemaining() && !this.decodeBuffer1.hasRemaining()) {
			this.decodeBufferDisplacement += this.decoded0;

			CharBuffer nextDecodeBuffer1 = this.decodeBuffer0.clear();

			this.decodeBuffer0 = this.decodeBuffer1;
			this.decoded0 = this.decoded1;
			this.decodeBuffer1 = nextDecodeBuffer1;
			this.decoded1 = 0;
			if (0 <= this.markIndex && this.markIndex < this.decodeBufferDisplacement) {
				LOG.warning("Mark buffer exceeded: {0}", this.decodeBufferDisplacement - this.markIndex);
			}
		}
	}

}
