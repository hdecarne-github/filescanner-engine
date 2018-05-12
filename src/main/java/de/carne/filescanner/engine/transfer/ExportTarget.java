/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.transfer;

import java.io.IOException;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Export handler interface responsible for writing export data.
 */
public interface ExportTarget extends WritableByteChannel, InterruptibleChannel {

	/**
	 * Sets the size of upcoming export data stream.
	 * <p>
	 * The given size is used to report export progress to the user. In order to work this size has to be set prior to
	 * any {@link #write(java.nio.ByteBuffer)} call.
	 *
	 * @param size the size to set.
	 * @throws IOException if an I/O error occurs.
	 */
	void setSize(long size) throws IOException;

}
