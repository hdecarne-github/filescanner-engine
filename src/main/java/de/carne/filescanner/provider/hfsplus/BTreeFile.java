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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BiConsumer;

import de.carne.filescanner.engine.InsufficientDataException;
import de.carne.filescanner.engine.UnexpectedDataException;
import de.carne.filescanner.engine.input.FileScannerInput;

abstract class BTreeFile<K extends Comparable<K>> {

	private static final int MIN_NODE_SIZE = 512;
	private static final int MAX_NODE_SIZE = 32768;

	private static final int HEADER_RECORD_SIZE = 106;

	private final ForkData forkData;
	private int nodeSize = -1;
	private int rootNode = 0;
	private int firstLeafNode = 0;

	public BTreeFile(ForkData forkData) {
		this.forkData = forkData;
	}

	public void walkLeafNodes(FileScannerInput input, BiConsumer<K, ByteBuffer> consumer) throws IOException {
		if (this.nodeSize < 0) {
			readHeaderNode(input);
		}
		if (this.firstLeafNode != 0) {
			ByteBuffer nodeBuffer = ByteBuffer.allocate(this.nodeSize).order(ByteOrder.BIG_ENDIAN);
			int currentLeafNode = this.firstLeafNode;

			while (currentLeafNode != 0) {
				nodeBuffer.clear();

				long nodePosition = this.forkData.position(Integer.toUnsignedLong(currentLeafNode) * this.nodeSize);

				input.read(nodeBuffer, nodePosition);
				if (nodeBuffer.hasRemaining()) {
					throw new InsufficientDataException(input, nodePosition, nodeBuffer.capacity(),
							nodeBuffer.position());
				}
				nodeBuffer.flip();

				int numRecords = Short.toUnsignedInt(nodeBuffer.getShort(10));

				for (int recordNumber = 1; recordNumber <= numRecords; recordNumber++) {
					int recordOffset = Short.toUnsignedInt(nodeBuffer.getShort(this.nodeSize - (recordNumber * 2)));
					int recordLength = Short.toUnsignedInt(
							nodeBuffer.getShort(this.nodeSize - ((recordNumber + 1) * 2))) - recordOffset;

					nodeBuffer.position(recordOffset);

					int keyLength = Short.toUnsignedInt(nodeBuffer.getShort());

					ByteBuffer keyBuffer = nodeBuffer.slice();

					keyBuffer.limit(keyLength);

					K nodeKey = decodeNodeKey(keyBuffer);

					nodeBuffer.position(recordOffset + 2 + keyLength);

					ByteBuffer valueBuffer = nodeBuffer.slice();

					valueBuffer.limit(recordLength - keyLength);
					consumer.accept(nodeKey, valueBuffer);
				}
				currentLeafNode = nodeBuffer.getInt(0);
			}
		}
	}

	protected abstract K decodeNodeKey(ByteBuffer nodeBuffer) throws IOException;

	private void readHeaderNode(FileScannerInput input) throws IOException {
		ByteBuffer headerRecordBuffer = ByteBuffer.allocate(HEADER_RECORD_SIZE).order(ByteOrder.BIG_ENDIAN);
		long forkDataStart = this.forkData.position(0);

		input.read(headerRecordBuffer, forkDataStart);
		if (headerRecordBuffer.hasRemaining()) {
			throw new InsufficientDataException(input, forkDataStart, headerRecordBuffer.capacity(),
					headerRecordBuffer.position());
		}
		headerRecordBuffer.flip();

		int uncheckedNodSize = Short.toUnsignedInt(headerRecordBuffer.getShort(32));

		if (uncheckedNodSize < MIN_NODE_SIZE || MAX_NODE_SIZE < uncheckedNodSize) {
			throw new UnexpectedDataException("Unexpected node size", forkDataStart + 32, uncheckedNodSize);
		}
		this.nodeSize = uncheckedNodSize;
		this.rootNode = headerRecordBuffer.getInt(16);
		this.firstLeafNode = headerRecordBuffer.getInt(24);
	}

}
