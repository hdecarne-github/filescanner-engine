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
package de.carne.filescanner.provider.hfsplus;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.MappedFileScannerInput;
import de.carne.filescanner.engine.util.HexFormat;

final class ForkData {

	public static final byte DATA_FORK = 0x00;
	public static final byte RESOURCE_FORK = (byte) 0xff;

	private final BlockDevice blockDevice;
	private final int fileId;
	private final byte forkType;
	private final long logicalSize;
	private final long[] extents;
	private final @Nullable ExtentsFile extentsFile;

	public ForkData(BlockDevice blockDevice, int fileId, byte forkType, long logicalSize, int[] extents,
			@Nullable ExtentsFile extentsFile) {
		this.blockDevice = blockDevice;
		this.fileId = fileId;
		this.forkType = forkType;
		this.logicalSize = logicalSize;
		this.extents = new long[extents.length];
		for (int extentIndex = 0; extentIndex < extents.length; extentIndex++) {
			this.extents[extentIndex] = Integer.toUnsignedLong(extents[extentIndex]);
		}
		this.extentsFile = extentsFile;
	}

	public BlockDevice blockDevice() {
		return this.blockDevice;
	}

	public long logicalSize() {
		return this.logicalSize;
	}

	public long position(long offset) throws IOException {
		long position = 0;
		long remainingOffset = offset;

		while (position == 0) {
			long currentBlock = this.blockDevice.block(offset - remainingOffset);
			long[] currentExtents = getExtents(currentBlock);

			for (int extentIndex = 0; extentIndex < currentExtents.length && position == 0; extentIndex += 2) {
				long startBlock = currentExtents[extentIndex];
				long blockCount = currentExtents[extentIndex + 1];

				if (startBlock == 0 && blockCount == 0) {
					throw new IOException("Invalid fork data offset: " + HexFormat.formatLong(offset));
				}

				long extentStart = this.blockDevice.offset(startBlock);
				long extentSize = this.blockDevice.size(blockCount);

				if (remainingOffset < extentSize) {
					position = extentStart + remainingOffset;
				} else {
					remainingOffset -= extentSize;
				}
			}
		}
		return position;
	}

	private long[] getExtents(long startBlock) throws IOException {
		long[] foundExtents;

		if (startBlock == 0) {
			foundExtents = this.extents;
		} else if (this.extentsFile != null) {
			foundExtents = this.extentsFile.getExtents(this.fileId, this.forkType, startBlock);
		} else {
			throw new IOException("Invalid fork data block: " + startBlock);
		}
		return foundExtents;
	}

	public FileScannerInput map(String name) throws IOException {
		MappedFileScannerInput mappedInput = new MappedFileScannerInput(name);
		long inputSize = 0;

		while (inputSize < this.logicalSize) {
			long currentBlock = this.blockDevice.block(inputSize);
			long[] currentExtents = getExtents(currentBlock);

			for (int extentIndex = 0; extentIndex < currentExtents.length
					&& inputSize < this.logicalSize; extentIndex += 2) {
				long startBlock = currentExtents[extentIndex];
				long blockCount = currentExtents[extentIndex + 1];

				if (startBlock == 0 && blockCount == 0) {
					break;
				}

				long extentStart = this.blockDevice.offset(startBlock);
				long extentSize = this.blockDevice.size(blockCount);
				long mapSize = Math.min(extentSize, this.logicalSize - inputSize);

				mappedInput.add(this.blockDevice.input(), extentStart, extentStart + mapSize);
				inputSize += mapSize;
			}
		}
		return mappedInput;
	}

}
