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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.util.Strings;

class CatalogFile extends BTreeFile<CatalogFileKey> {

	private final ExtentsFile extentsFile;
	private final long blockSize;

	public CatalogFile(ForkData forkData, ExtentsFile extentsFile) {
		super(forkData);
		this.extentsFile = extentsFile;
		this.blockSize = forkData.blockSize();
	}

	public void walkFileTree(FileScannerInput input, Consumer<FileScannerInput> consumer) throws IOException {
		walkLeafNodes(input, new FileTreeConsumer(input, this.blockSize, this.extentsFile, consumer));
	}

	@Override
	protected CatalogFileKey decodeNodeKey(ByteBuffer keyBuffer) throws IOException {
		int parentId = keyBuffer.getInt();
		short nameLength = keyBuffer.getShort();
		String name = (nameLength > 0 ? StandardCharsets.UTF_16BE.decode(keyBuffer).toString() : "");

		return new CatalogFileKey(parentId, name);
	}

	private static class FileTreeConsumer implements BiConsumer<CatalogFileKey, ByteBuffer> {

		private static final Log LOG = new Log();

		private final FileScannerInput input;
		private final long blockSize;
		private final ExtentsFile extentsFile;
		private final Map<Integer, CatalogFileKey> folderCache = new HashMap<>();
		private final Consumer<FileScannerInput> consumer;

		FileTreeConsumer(FileScannerInput input, long blockSize, ExtentsFile extentsFile,
				Consumer<FileScannerInput> consumer) {
			this.input = input;
			this.blockSize = blockSize;
			this.extentsFile = extentsFile;
			this.consumer = consumer;
		}

		@Override
		public void accept(CatalogFileKey key, ByteBuffer valueBuffer) {
			int recordType = Short.toUnsignedInt(valueBuffer.getShort());

			try {
				switch (recordType) {
				case 1:
					processFolder(key, valueBuffer);
					break;
				case 2:
					processFile(key, valueBuffer);
					break;
				case 3:
					break;
				case 4:
					break;
				default:
					LOG.error("Ignoring unexpected record type {0} for catalog file key ''{1}''", recordType,
							Strings.encode(key.toString()));
				}
			} catch (IOException e) {
				LOG.error(e, "Failed to process catalog file key ''{0}''", Strings.encode(key.toString()));
			}
		}

		private void processFolder(CatalogFileKey key, ByteBuffer valueBuffer) {
			int folderId = valueBuffer.getInt(8);

			this.folderCache.put(folderId, key);
		}

		private void processFile(CatalogFileKey key, ByteBuffer valueBuffer) throws IOException {
			StringBuilder buffer = new StringBuilder();

			buildFolderPath(buffer, key.parentId());
			buffer.append(key.name());
			valueBuffer.position(88);

			ForkData dataFork = decodeForkData(valueBuffer);

			if (dataFork.logicalSize() != 0) {
				this.consumer.accept(dataFork.map(this.input, buffer.toString()));
			}
			if (HfsPlusFormat.DECODE_RESOURCE_FORK) {
				ForkData resourceFork = decodeForkData(valueBuffer);

				if (resourceFork.logicalSize() != 0) {
					buffer.append(":resourceFork");
					this.consumer.accept(resourceFork.map(this.input, buffer.toString()));
				}
			}
		}

		private void buildFolderPath(StringBuilder buffer, int folderId) {
			CatalogFileKey folderKey = this.folderCache.get(folderId);

			if (folderKey != null) {
				buildFolderPath(buffer, folderKey.parentId());
				buffer.append(folderKey.name()).append('/');
			}
		}

		private ForkData decodeForkData(ByteBuffer valueBuffer) {
			long logicalSize = valueBuffer.getLong();
			/* int clumpSize */ valueBuffer.getInt();
			/* int totalBlocks */ valueBuffer.getInt();
			int[] extents = new int[16];

			for (int extentIndex = 0; extentIndex < extents.length; extentIndex++) {
				extents[extentIndex] = valueBuffer.getInt();
			}
			return new ForkData(this.blockSize, logicalSize, extents, this.extentsFile);
		}

	}

}
