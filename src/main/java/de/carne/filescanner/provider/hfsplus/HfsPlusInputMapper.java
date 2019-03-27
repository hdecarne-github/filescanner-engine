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
import java.util.LinkedList;
import java.util.List;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.input.DecodedInputMapper;
import de.carne.filescanner.engine.input.FileScannerInput;

class HfsPlusInputMapper extends DecodedInputMapper {

	private static final Log LOG = new Log();

	private final CatalogFile catalogFile;

	public HfsPlusInputMapper(CatalogFile catalogFile) {
		super("disk image blocks");
		this.catalogFile = catalogFile;
	}

	@Override
	public List<FileScannerInput> map(FileScannerInput input) throws IOException {
		List<FileScannerInput> inputs = new LinkedList<>();

		try {
			this.catalogFile.walkFileTree(input, inputs::add);
		} catch (Exception e) {
			LOG.error(e, "Failed to decode HFS+ file system");
		}
		return inputs;
	}

}
