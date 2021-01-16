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

import de.carne.filescanner.engine.input.FileScannerInput;

final class BlockDevice {

	private final FileScannerInput input;
	private final long blockSize;

	public BlockDevice(FileScannerInput input, long blockSize) {
		this.input = input;
		this.blockSize = blockSize;
	}

	public FileScannerInput input() {
		return this.input;
	}

	public long block(long offset) {
		return offset / this.blockSize;
	}

	public long offset(long block) {
		return Math.max((block * this.blockSize) - (this.blockSize - 0xa00), 0);
	}

	public long size(long blockCount) {
		return blockCount * this.blockSize;
	}

}
