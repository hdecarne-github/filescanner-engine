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
import java.nio.ByteBuffer;

class ExtentsFile extends BTreeFile<ExtentsFileKey> {

	public ExtentsFile(ForkData forkData) {
		super(forkData);
	}

	public long[] getExtents(int fileId, byte forkType, long startBlock) throws IOException {
		ExtentsFileKey key = new ExtentsFileKey(forkType, fileId, startBlock);
		ByteBuffer value = findLeafNode(key);
		long[] extents = new long[16];

		for (int extentIndex = 0; extentIndex < extents.length; extentIndex++) {
			extents[extentIndex] = Integer.toUnsignedLong(value.getInt());
		}
		return extents;
	}

	@Override
	protected ExtentsFileKey decodeNodeKey(ByteBuffer keyBuffer) throws IOException {
		byte forkType = keyBuffer.get();
		/* byte pad */ keyBuffer.get();
		int fileId = keyBuffer.getInt();
		int startBlock = keyBuffer.getInt();

		return new ExtentsFileKey(forkType, fileId, startBlock);
	}

}
