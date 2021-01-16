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
import java.util.LinkedList;
import java.util.List;

import de.carne.filescanner.engine.input.DecodedInputMapper;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.util.logging.Log;

class HfsPlusInputMapper extends DecodedInputMapper {

	private static final Log LOG = new Log();

	private final long blockSize;
	private final long extentsFileSize;
	private final int[] extentsFileExtents;
	private final long catalogFileSize;
	private final int[] catalogFileExtents;

	public HfsPlusInputMapper(long blockSize, long extentsFileSize, int[] extentsFileExtents, long catalogFileSize,
			int[] catalogFileExtents) {
		super("disk image blocks");
		this.blockSize = blockSize;
		this.extentsFileSize = extentsFileSize;
		this.extentsFileExtents = extentsFileExtents;
		this.catalogFileSize = catalogFileSize;
		this.catalogFileExtents = catalogFileExtents;
	}

	@Override
	public List<FileScannerInput> map(FileScannerInput input) throws IOException {
		List<FileScannerInput> inputs = new LinkedList<>();

		try {
			BlockDevice blockDevice = new BlockDevice(input, this.blockSize);
			ForkData extentsFileForkData = new ForkData(blockDevice, 3, ForkData.DATA_FORK, this.extentsFileSize,
					this.extentsFileExtents, null);
			ExtentsFile extentsFile = new ExtentsFile(extentsFileForkData);
			ForkData catalogFileForkData = new ForkData(blockDevice, 4, ForkData.DATA_FORK, this.catalogFileSize,
					this.catalogFileExtents, extentsFile);
			CatalogFile catalogFile = new CatalogFile(catalogFileForkData, extentsFile);

			catalogFile.walkFileTree(inputs::add);
		} catch (Exception e) {
			LOG.error(e, "Failed to decode HFS+ file system");
		}
		return inputs;
	}

}
