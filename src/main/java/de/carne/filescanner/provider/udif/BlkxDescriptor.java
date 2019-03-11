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

import org.eclipse.jdt.annotation.Nullable;

final class BlkxDescriptor implements Comparable<BlkxDescriptor> {

	private final long blkxPosition;
	private final int dataChunkCount;
	private final long sectorStart;
	private final long sectorEnd;

	public BlkxDescriptor(long blkxPosition, int dataChunkCount, long sectorStart, long sectorEnd) {
		this.blkxPosition = blkxPosition;
		this.dataChunkCount = dataChunkCount;
		this.sectorStart = sectorStart;
		this.sectorEnd = sectorEnd;
	}

	public long blkxPosition() {
		return this.blkxPosition;
	}

	public int dataChunkCount() {
		return this.dataChunkCount;
	}

	public long sectorStart() {
		return this.sectorStart;
	}

	public long sectorEnd() {
		return this.sectorEnd;
	}

	@Override
	public int compareTo(BlkxDescriptor o) {
		return Long.signum(this.blkxPosition - o.blkxPosition);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.blkxPosition);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return this == obj || (obj instanceof BlkxDescriptor && compareTo((BlkxDescriptor) obj) == 0);
	}

}
