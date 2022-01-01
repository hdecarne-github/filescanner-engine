/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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

import org.eclipse.jdt.annotation.NonNull;

import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.HexFormat;
import de.carne.util.Check;

/**
 * Defines the actual mapping of one or more encoded data sections to a resulting decoded input data stream.
 *
 * @see InputDecodeCache#decodeInputs(DecodedInputMapper, InputDecoderTable, FileScannerInput, long)
 */
public class InputDecoderTable implements Iterable<InputDecoderTable.@NonNull Entry> {

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
	 * @param encodedSize the size of the encoded data stream (use {@code -1l} to determine the size automatically
	 * during decoding).
	 * @param decodedSize the size of the decoded data stream (use {@code -1l} to determine the size automatically
	 * during decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public static InputDecoderTable build(InputDecoder inputDecoder, long offset, long encodedSize, long decodedSize) {
		return new InputDecoderTable().add(inputDecoder, offset, encodedSize, decodedSize);
	}

	/**
	 * Adds a {@linkplain InputDecoder} to the table.
	 *
	 * @param inputDecoder the {@linkplain InputDecoder} to use for decoding.
	 * @param offset the offset to start decoding at (use {@code -1l} to start at current position).
	 * @param encodedSize the size of the encoded data stream (use {@code -1l} to determine the size automatically
	 * during decoding).
	 * @param decodedSize the size of the decoded data stream (use {@code -1l} to determine the size automatically
	 * during decoding).
	 * @return the updated {@linkplain InputDecoderTable} instance.
	 */
	public InputDecoderTable add(InputDecoder inputDecoder, long offset, long encodedSize, long decodedSize) {
		Check.assertTrue(!InputDecoders.IDENTITY.equals(inputDecoder) || encodedSize >= 0);
		Check.assertTrue(!InputDecoders.ZERO.equals(inputDecoder) || decodedSize >= 0);

		Entry entry = null;

		if (!this.entries.isEmpty()) {
			Entry lastEntry = this.entries.getLast();
			InputDecoder lastInputDecoder = lastEntry.inputDecoder();
			long lastOffset = lastEntry.offset();
			long lastEncodedSize = lastEntry.encodedSize();
			long lastDecodedSize = lastEntry.decodedSize();

			if ((offset < 0 || (lastOffset >= 0 && lastEncodedSize >= 0 && lastOffset + lastEncodedSize == offset))
					&& InputDecoders.isIdentityOrZero(lastInputDecoder) && lastInputDecoder.equals(inputDecoder)) {
				this.entries.removeLast();
				entry = new Entry(lastInputDecoder, lastOffset,
						(encodedSize >= 0 ? lastEncodedSize + encodedSize : encodedSize),
						(lastDecodedSize >= 0 && decodedSize >= 0 ? lastDecodedSize + decodedSize : -1l));
			}
		}
		this.entries.add(entry != null ? entry : new Entry(inputDecoder, offset, encodedSize, decodedSize));
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

				long entryEncodedSize = entry.encodedSize();

				entryComment.append(" encoded:");
				formatMappingValue(entryComment, entryEncodedSize);

				long entryDecodedSize = entry.decodedSize();

				entryComment.append(" decoded:");
				formatMappingValue(entryComment, entryDecodedSize);
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
		private final long encodedSize;
		private final long decodedSize;

		Entry(InputDecoder inputDecoder, long offset, long encodedSize, long decodedSize) {
			this.inputDecoder = inputDecoder;
			this.offset = offset;
			this.encodedSize = encodedSize;
			this.decodedSize = decodedSize;
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
		 * Gets the size of the encoded data.
		 * <p>
		 * A negative size indicates that the actual size has to be determined during decoding.
		 * </p>
		 *
		 * @return the size of the encoded data.
		 */
		public long encodedSize() {
			return this.encodedSize;
		}

		/**
		 * Gets the size of the decoded data.
		 * <p>
		 * A negative size indicates that the actual size has to be determined during decoding.
		 * </p>
		 *
		 * @return the size of the encoded data.
		 */
		public long decodedSize() {
			return this.decodedSize;
		}

	}

	@Override
	public Iterator<@NonNull Entry> iterator() {
		return this.entries.iterator();
	}

}
