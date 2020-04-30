/*
 * Copyright (c) 2007-2020 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.input;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import de.carne.util.logging.Log;

/**
 * {@linkplain FileChannel} based {@linkplain FileScannerInput}.
 */
class FileChannelInput extends FileScannerInput implements Closeable {

	private static final Log LOG = new Log();

	private final Path file;
	private final FileChannel fileChannel;

	FileChannelInput(Path file, OpenOption... options) throws IOException {
		super(file.toAbsolutePath().toString());

		LOG.info("Opening input ''{0}''...", file);

		this.file = file;
		this.fileChannel = FileChannel.open(file, options);
	}

	public FileChannel channel() {
		return this.fileChannel;
	}

	@Override
	public void close() throws IOException {
		LOG.info("Closing input ''{0}''...", this.file);

		this.fileChannel.close();
	}

	@Override
	public long size() throws IOException {
		return this.fileChannel.size();
	}

	@Override
	public int read(ByteBuffer buffer, long position) throws IOException {
		return this.fileChannel.read(buffer, position);
	}

}
