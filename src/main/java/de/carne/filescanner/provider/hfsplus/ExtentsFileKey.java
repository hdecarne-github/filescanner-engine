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
package de.carne.filescanner.provider.hfsplus;

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

final class ExtentsFileKey implements Comparable<ExtentsFileKey> {

	private final byte forkType;
	private final int fileId;
	private final long startBlock;

	public ExtentsFileKey(byte forkType, int fileId, long startBlock) {
		this.forkType = forkType;
		this.fileId = fileId;
		this.startBlock = startBlock;
	}

	@Override
	public int compareTo(ExtentsFileKey o) {
		int comparison = 0;

		if (this.fileId != o.fileId) {
			comparison = Long.compare(Integer.toUnsignedLong(this.fileId), Integer.toUnsignedLong(o.fileId));
		} else if (this.forkType != o.forkType) {
			comparison = Integer.compare(Byte.toUnsignedInt(this.forkType), Byte.toUnsignedInt(o.forkType));
		} else if (this.startBlock != o.startBlock) {
			comparison = Long.compare(this.startBlock, o.startBlock);
		}
		return comparison;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new long[] { Byte.toUnsignedLong(this.forkType), Integer.toUnsignedLong(this.fileId),
				this.startBlock });
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return this == obj || (obj instanceof ExtentsFileKey && compareTo((ExtentsFileKey) obj) == 0);
	}

}
