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
		return add(inputDecoder, -1l, -1l, -1l);
	}

	/**
	 * Builds a new {@linkplain InputDecoderTable} instance.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @param offset the offset to start decoding at (use {@code -1l} to start at current position).
	 * @param encodedLength the length of the encoded data stream (use {@code -1l} to determine the length automatically
	 * during decoding).
	 * @param decodedLength the length of the decoded data stream (use {@code -1l} to determine the length automatically
	 * during decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public static InputDecoderTable build(InputDecoder inputDecoder, long offset, long encodedLength,
			long decodedLength) {
		return new InputDecoderTable().add(inputDecoder, offset, encodedLength, decodedLength);
	}

	/**
	 * Adds a {@linkplain InputDecoder} to the table.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @param offset the offset to start decoding at (use {@code -1l} to start at current position).
	 * @param encodedLength the length of the encoded data stream (use {@code -1l} to determine the length automatically
	 * during decoding).
	 * @param decodedLength the length of the decoded data stream (use {@code -1l} to determine the length automatically
	 * during decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public InputDecoderTable add(InputDecoder inputDecoder, long offset, long encodedLength, long decodedLength) {
		Check.assertTrue(!InputDecoders.IDENTITY.equals(inputDecoder) || encodedLength >= 0);
		Check.assertTrue(!InputDecoders.ZERO.equals(inputDecoder) || decodedLength >= 0);

		Entry entry = null;

		if (!this.entries.isEmpty()) {
			Entry lastEntry = this.entries.getLast();
			InputDecoder lastInputDecoder = lastEntry.inputDecoder();
			long lastOffset = lastEntry.offset();
			long lastEncodedLength = lastEntry.encodedLength();
			long lastDecodedLength = lastEntry.decodedLength();

			if ((offset < 0 || (lastOffset >= 0 && lastEncodedLength >= 0 && lastOffset + lastEncodedLength == offset))
					&& InputDecoders.isIdentityOrZero(lastInputDecoder) && lastInputDecoder.equals(inputDecoder)) {
				this.entries.removeLast();
				entry = new Entry(lastInputDecoder, lastOffset,
						(encodedLength >= 0 ? lastEncodedLength + encodedLength : encodedLength),
						(lastDecodedLength >= 0 && decodedLength >= 0 ? lastDecodedLength + decodedLength : -1l));
			}
		}
		this.entries.add(entry != null ? entry : new Entry(inputDecoder, offset, encodedLength, decodedLength));
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

				entryComment.append("// encoder mapping[").append(entryIndex).append("] offset:");

				long entryOffset = entry.offset();

				formatMappingValue(entryComment, entryOffset);

				long entryEncodedLength = entry.encodedLength();

				entryComment.append(" encoded:");
				formatMappingValue(entryComment, entryEncodedLength);

				long entryDecodedLength = entry.decodedLength();

				entryComment.append(" decoded:");
				formatMappingValue(entryComment, entryDecodedLength);
				out.setStyle(RenderStyle.COMMENT).writeln(entryComment.toString());
				entry.inputDecoder().render(out);
				entryIndex++;
			}
		}
	}

	private StringBuilder formatMappingValue(StringBuilder buffer, long value) {
		if (value >= 0) {
			HexFormat.formatLong(buffer, value);
		} else {
			buffer.append('*');
		}
		return buffer;
	}

	/**
	 * A single {@linkplain InputDecoderTable} entry defining how to map an encoded input data section.
	 */
	public class Entry {

		private final InputDecoder inputDecoder;
		private final long offset;
		private final long encodedLength;
		private final long decodedLength;

		Entry(InputDecoder inputDecoder, long offset, long encodedLength, long decodedLength) {
			this.inputDecoder = inputDecoder;
			this.offset = offset;
			this.encodedLength = encodedLength;
			this.decodedLength = decodedLength;
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
		 * Gets the length of the encoded data.
		 * <p>
		 * A negative length indicates that the actual length has to be determined during decoding.
		 * </p>
		 *
		 * @return the length of the encoded data.
		 */
		public long encodedLength() {
			return this.encodedLength;
		}

		/**
		 * Gets the length of the decoded data.
		 * <p>
		 * A negative length indicates that the actual length has to be determined during decoding.
		 * </p>
		 *
		 * @return the length of the encoded data.
		 */
		public long decodedLength() {
			return this.decodedLength;
		}

	}

	@Override
	public Iterator<Entry> iterator() {
		return this.entries.iterator();
	}

}
