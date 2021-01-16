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
package de.carne.filescanner.engine.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.util.Check;

/**
 * {@linkplain FileScannerInput} implementation that provides a combined and mapped view to other input's data.
 */
public class MappedFileScannerInput extends FileScannerInput {

	private NavigableMap<Long, Mapping> mappings = new TreeMap<>();

	/**
	 * Constructs a new {@linkplain MappedFileScannerInput} instance.
	 *
	 * @param name the input name.
	 */
	public MappedFileScannerInput(String name) {
		super(name);
	}

	/**
	 * Adds a mapping for the given input data to this input.
	 *
	 * @param input the input to map.
	 * @return the updated {@linkplain MappedFileScannerInput} instance.
	 * @throws IOException if an I/O error occurs while determining the input's size.
	 */
	public MappedFileScannerInput add(FileScannerInput input) throws IOException {
		return add(input, 0, input.size());
	}

	/**
	 * Adds a mapping for the given input data section to this input.
	 *
	 * @param input the input to map.
	 * @param start the start position of the mapping.
	 * @param end the end position of the mapping.
	 * @return the updated {@linkplain MappedFileScannerInput} instance.
	 */
	public MappedFileScannerInput add(FileScannerInput input, long start, long end) {
		Check.assertTrue(start <= end);

		if (start < end) {
			this.mappings.put(size(), new Mapping(input, start, end));
		}
		return this;
	}

	@Override
	public long size() {
		Map.Entry<Long, Mapping> lastEntry = this.mappings.lastEntry();

		return (lastEntry != null ? lastEntry.getKey().longValue() + lastEntry.getValue().size() : 0l);
	}

	@Override
	public int read(ByteBuffer buffer, long position) throws IOException {
		long readPosition = position;
		int totalRead = -1;
		Map.Entry<Long, Mapping> entry = this.mappings.floorEntry(position);

		while (entry != null && buffer.hasRemaining()) {
			long mappingOffset = readPosition - entry.getKey();
			Mapping mapping = entry.getValue();
			long mappingRemaining = mapping.size() - mappingOffset;

			if (mappingRemaining > 0) {
				int read = mapping.read(buffer, mappingOffset);

				if (read > 0) {
					readPosition += read;
					totalRead = Math.max(0, totalRead) + read;
					entry = nextMapping(readPosition);
				} else {
					entry = null;
				}
			} else {
				entry = null;
			}
		}
		return totalRead;
	}

	private Map.@Nullable Entry<Long, Mapping> nextMapping(long nextOffset) {
		Long nextKey = Long.valueOf(nextOffset);
		Map.Entry<Long, Mapping> next = this.mappings.floorEntry(nextKey);

		return (next != null && next.getKey().equals(nextKey) ? next : null);
	}

	private class Mapping {

		private final FileScannerInput input;
		private final long start;
		private final long size;

		protected Mapping(FileScannerInput input, long start, long end) {
			this.input = input;
			this.start = start;
			this.size = end - start;
		}

		public long size() {
			return this.size;
		}

		public final int read(ByteBuffer buffer, long offset) throws IOException {
			int limit = (int) Math.min(buffer.remaining(), this.size - offset);
			ByteBuffer limitedBuffer = buffer.duplicate();

			limitedBuffer.limit(limitedBuffer.position() + limit);

			int read = this.input.read(limitedBuffer, this.start + offset);

			buffer.position(limitedBuffer.position());
			return read;
		}

	}

}
