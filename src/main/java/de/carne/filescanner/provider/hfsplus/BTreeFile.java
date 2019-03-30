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

	public ByteBuffer findLeafNode(K key) throws IOException {
		if (this.nodeSize < 0) {
			processHeaderNode();
		}

		ByteBuffer nodeBuffer = ByteBuffer.allocate(this.nodeSize).order(ByteOrder.BIG_ENDIAN);
		int currentNode = this.rootNode;

		while (currentNode != 0) {
			readNode(nodeBuffer, currentNode);

			int nodeKind = nodeBuffer.get(8);

			if (nodeKind == -1) {
				break;
			}
			if (nodeKind != 0) {
				throw new IOException("Unexpected node kind: " + nodeKind);
			}

			int numRecords = Short.toUnsignedInt(nodeBuffer.getShort(10));
			int nextNode = 0;

			for (int recordNumber = 1; recordNumber <= numRecords; recordNumber++) {
				int recordOffset = Short.toUnsignedInt(nodeBuffer.getShort(this.nodeSize - (recordNumber * 2)));

				nodeBuffer.position(recordOffset);

				K nodeKey = getNodeKey(nodeBuffer);
				int nodeValue = nodeBuffer.getInt();
				int comparison = nodeKey.compareTo(key);

				if (comparison <= 0) {
					nextNode = nodeValue;
				}
				if (comparison >= 0) {
					break;
				}
			}
			currentNode = nextNode;
		}

		ByteBuffer valueBuffer = null;

		if (currentNode != 0) {
			int numRecords = Short.toUnsignedInt(nodeBuffer.getShort(10));

			for (int recordNumber = 1; recordNumber <= numRecords; recordNumber++) {
				int recordOffset = Short.toUnsignedInt(nodeBuffer.getShort(this.nodeSize - (recordNumber * 2)));
				int recordLength = Short.toUnsignedInt(nodeBuffer.getShort(this.nodeSize - ((recordNumber + 1) * 2)))
						- recordOffset;

				nodeBuffer.position(recordOffset);

				K nodeKey = getNodeKey(nodeBuffer);

				if (nodeKey.equals(key)) {
					valueBuffer = nodeBuffer.slice();
					valueBuffer.limit(recordLength - (nodeBuffer.position() - recordOffset));
					break;
				}
			}
		}
		if (valueBuffer == null) {
			throw new IOException("Unexpected node key: " + key);
		}
		return valueBuffer;
	}

	public void walkLeafNodes(BiConsumer<K, ByteBuffer> consumer) throws IOException {
		if (this.nodeSize < 0) {
			processHeaderNode();
		}
		if (this.firstLeafNode != 0) {
			ByteBuffer nodeBuffer = ByteBuffer.allocate(this.nodeSize).order(ByteOrder.BIG_ENDIAN);
			int currentLeafNode = this.firstLeafNode;

			while (currentLeafNode != 0) {
				readNode(nodeBuffer, currentLeafNode);

				int numRecords = Short.toUnsignedInt(nodeBuffer.getShort(10));

				for (int recordNumber = 1; recordNumber <= numRecords; recordNumber++) {
					int recordOffset = Short.toUnsignedInt(nodeBuffer.getShort(this.nodeSize - (recordNumber * 2)));
					int recordLength = Short.toUnsignedInt(
							nodeBuffer.getShort(this.nodeSize - ((recordNumber + 1) * 2))) - recordOffset;

					nodeBuffer.position(recordOffset);

					K nodeKey = getNodeKey(nodeBuffer);
					ByteBuffer valueBuffer = nodeBuffer.slice();

					valueBuffer.limit(recordLength - (nodeBuffer.position() - recordOffset));
					consumer.accept(nodeKey, valueBuffer);
				}
				currentLeafNode = nodeBuffer.getInt(0);
			}
		}
	}

	protected abstract K decodeNodeKey(ByteBuffer nodeBuffer) throws IOException;

	private void processHeaderNode() throws IOException {
		ByteBuffer headerRecordBuffer = ByteBuffer.allocate(HEADER_RECORD_SIZE).order(ByteOrder.BIG_ENDIAN);
		FileScannerInput input = this.forkData.blockDevice().input();
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

	private void readNode(ByteBuffer buffer, int node) throws IOException {
		buffer.clear();

		long nodePosition = this.forkData.position(Integer.toUnsignedLong(node) * this.nodeSize);
		FileScannerInput input = this.forkData.blockDevice().input();

		input.read(buffer, nodePosition);
		if (buffer.hasRemaining()) {
			throw new InsufficientDataException(input, nodePosition, buffer.capacity(), buffer.position());
		}
		buffer.flip();
	}

	private K getNodeKey(ByteBuffer buffer) throws IOException {
		int keyLength = Short.toUnsignedInt(buffer.getShort());

		ByteBuffer keyBuffer = buffer.slice();

		keyBuffer.limit(keyLength);
		buffer.position(buffer.position() + keyLength);
		return decodeNodeKey(keyBuffer);
	}

}
