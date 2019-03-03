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
package de.carne.filescanner.engine.input;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import de.carne.boot.check.Check;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;

/**
 * Defines the actual mapping of one or more encoded data sections to a resulting decoded input data stream.
 *
 * @see InputDecodeCache#decodeInput(String, InputDecoderTable, FileScannerInput, long)
 */
public class InputDecoderTable implements Iterable<InputDecoderTable.Entry> {

	private final LinkedList<Entry> entries = new LinkedList<>();

	/**
	 * Builds a new {@linkplain InputDecoderTable} instance.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding. decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public static InputDecoderTable build(InputDecoder inputDecoder) {
		return new InputDecoderTable().add(inputDecoder);
	}

	/**
	 * Adds a {@linkplain InputDecoder} to the table.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding. decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public InputDecoderTable add(InputDecoder inputDecoder) {
		return add(-1l, inputDecoder, -1l);
	}

	/**
	 * Builds a new {@linkplain InputDecoderTable} instance.
	 *
	 * @param offset the offset to start decoding at (use {@code -1l} to start at current position).
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public static InputDecoderTable build(long offset, InputDecoder inputDecoder) {
		return new InputDecoderTable().add(offset, inputDecoder, -1l);
	}

	/**
	 * Adds a {@linkplain InputDecoder} to the table.
	 *
	 * @param offset the offset to start decoding at (use {@code -1l} to start at current position).
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public InputDecoderTable add(long offset, InputDecoder inputDecoder) {
		return add(offset, inputDecoder, -1l);
	}

	/**
	 * Builds a new {@linkplain InputDecoderTable} instance.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @param length the length of the encoded data stream (use {@code -1l} to determine the length automatically during
	 * decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public static InputDecoderTable build(InputDecoder inputDecoder, long length) {
		return new InputDecoderTable().add(-1l, inputDecoder, length);
	}

	/**
	 * Adds a {@linkplain InputDecoder} to the table.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @param length the length of the encoded data stream (use {@code -1l} to determine the length automatically during
	 * decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public InputDecoderTable add(InputDecoder inputDecoder, long length) {
		return add(-1l, inputDecoder, length);
	}

	/**
	 * Builds a new {@linkplain InputDecoderTable} instance.
	 *
	 * @param offset the offset to start decoding at (use {@code -1l} to start at current position).
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @param length the length of the encoded data stream (use {@code -1l} to determine the length automatically during
	 * decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public static InputDecoderTable build(long offset, InputDecoder inputDecoder, long length) {
		return new InputDecoderTable().add(offset, inputDecoder, length);
	}

	/**
	 * Adds a {@linkplain InputDecoder} to the table.
	 *
	 * @param offset the offset to start decoding at (use {@code -1l} to start at current position).
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @param length the length of the encoded data stream (use {@code -1l} to determine the length automatically during
	 * decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public InputDecoderTable add(long offset, InputDecoder inputDecoder, long length) {
		Check.assertTrue(!(InputDecoders.IDENTITY.equals(inputDecoder) || InputDecoders.ZERO.equals(inputDecoder))
				|| length >= 0);

		this.entries.add(new Entry(offset, inputDecoder, length));
		return this;
	}

	/**
	 * Gets the number table entries.
	 *
	 * @return the number table entries.
	 */
	public int size() {
		return this.entries.size();
	}

	/**
	 * Renders decoder table informations.
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @throws IOException if an I/O error occurs.
	 */
	public void render(RenderOutput out) throws IOException {
		if (this.entries.size() == 1) {
			this.entries.get(0).inputDecoder().render(out);
		} else {
			int entryIndex = 0;

			for (Entry entry : this.entries) {
				StringBuilder entryComment = new StringBuilder();

				entryComment.append("// encoder mapping[").append(entryIndex).append("] ");

				long entryOffset = entry.offset();

				if (entryOffset >= 0) {
					HexFormat.formatLong(entryComment, entryOffset);
				} else {
					entryComment.append('*');
				}

				long entryLength = entry.length();

				if (entryLength >= 0) {
					entryComment.append(":+");
					HexFormat.formatLong(entryComment, entryLength);
				} else {
					entryComment.append(":*");
				}
				out.setStyle(RenderStyle.COMMENT).writeln(entryComment.toString());
				entry.inputDecoder().render(out);
				entryIndex++;
			}
		}
	}

	/**
	 * A single {@linkplain InputDecoderTable} entry defining how to map an encoded input data section.
	 */
	public class Entry {

		private final long offset;
		private final InputDecoder inputDecoder;
		private final long length;

		Entry(long offset, InputDecoder inputDecoder, long length) {
			this.offset = offset;
			this.inputDecoder = inputDecoder;
			this.length = length;
		}

		/**
		 * Gets the offset to start decoding at.
		 * <p>
		 * A negative offset indicates that decoding should start at the current position.
		 * </p>
		 *
		 * @return the offset to start decoding at.
		 */
		public long offset() {
			return this.offset;
		}

		/**
		 * Gets the {@linkplain InputDecoder} to use for decoding.
		 *
		 * @return the {@linkplain InputDecoder} to use for decoding.
		 */
		public InputDecoder inputDecoder() {
			return this.inputDecoder;
		}

		/**
		 * Gets the length of the encoded data.
		 * <p>
		 * A negative length indicates that the actual length has to be determined during decoding.
		 * </p>
		 *
		 * @return the length of the encoded data.
		 */
		public long length() {
			return this.length;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();

			buffer.append(super.toString());
			buffer.append('[');
			if (this.offset >= 0) {
				HexFormat.formatLong(buffer, this.offset);
			} else {
				buffer.append('*');
			}
			buffer.append(':');
			if (this.length >= 0) {
				HexFormat.formatLong(buffer, this.length);
			} else {
				buffer.append('*');
			}
			buffer.append("]:");
			buffer.append(this.inputDecoder.name());
			return buffer.toString();
		}

	}

	@Override
	public Iterator<Entry> iterator() {
		return this.entries.iterator();
	}

}
