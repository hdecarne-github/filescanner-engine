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
package de.carne.filescanner.provider.hfsplus;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.MappedFileScannerInput;

final class ForkData {

	private final long blockSize;
	private final long logicalSize;
	private final long[] extents;
	private final @Nullable ExtentsFile extentsFile;

	public ForkData(long blockSize, long logicalSize, int[] extents, @Nullable ExtentsFile extentsFile) {
		this.blockSize = blockSize;
		this.logicalSize = logicalSize;
		this.extents = new long[extents.length];
		for (int extentIndex = 0; extentIndex < extents.length; extentIndex++) {
			this.extents[extentIndex] = Integer.toUnsignedLong(extents[extentIndex]);
		}
		this.extentsFile = extentsFile;
	}

	public long blockSize() {
		return this.blockSize;
	}

	public long logicalSize() {
		return this.logicalSize;
	}

	public long position(long offset) throws IOException {
		long position = 0;
		long remainingOffset = offset;

		for (int extentIndex = 0; extentIndex < this.extents.length && position == 0; extentIndex += 2) {
			long startBlock = this.extents[extentIndex];
			long blockCount = this.extents[extentIndex + 1];

			if (startBlock == 0 && blockCount == 0) {
				throw new IOException("Invalid fork data offset: " + HexFormat.formatLong(offset));
			}

			long extentStart = (startBlock * this.blockSize) - 0x600;
			long extentSize = blockCount * this.blockSize;

			if (remainingOffset < extentSize) {
				position = extentStart + remainingOffset;
			} else {
				remainingOffset -= extentSize;
			}
		}
		if (position == 0) {
			throw new IOException("Extent overflow support not yet implemented");
		}
		return position;
	}

	public FileScannerInput map(FileScannerInput input, String name) throws IOException {
		MappedFileScannerInput mappedInput = new MappedFileScannerInput(name);
		long inputSize = 0;

		for (int extentIndex = 0; extentIndex < this.extents.length && inputSize < this.logicalSize; extentIndex += 2) {
			long startBlock = this.extents[extentIndex];
			long blockCount = this.extents[extentIndex + 1];

			if (startBlock == 0 && blockCount == 0) {
				break;
			}

			long extentStart = (startBlock * this.blockSize) - 0x600;
			long extentSize = (blockCount * this.blockSize);
			long mapSize = Math.min(extentSize, this.logicalSize - inputSize);

			mappedInput.add(input, extentStart, extentStart + mapSize);
			inputSize += mapSize;
		}
		if (inputSize < this.logicalSize) {
			throw new IOException("Extent overflow support not yet implemented");
		}
		return mappedInput;
	}

}
