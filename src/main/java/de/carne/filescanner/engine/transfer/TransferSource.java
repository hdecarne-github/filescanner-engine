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
package de.carne.filescanner.engine.transfer;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * Interface used to transfer byte based data during rendering.
 */
public interface TransferSource {

	/**
	 * Gets the name of the data to transfer.
	 * <p>
	 * This name is used as a textual representation of the data (e.g. as the alternate name during HTML rendering).
	 *
	 * @return the name of the data to transfer.
	 */
	String name();

	/**
	 * Gets the {@linkplain TransferType} of the data to transfer.
	 *
	 * @return the {@linkplain TransferType} of the data to transfer.
	 */
	TransferType transferType();

	/**
	 * Transfers the data.
	 *
	 * @param target the {@linkplain WritableByteChannel} to transfer the data to.
	 * @throws IOException if an I/O error occurs.
	 */
	void transfer(WritableByteChannel target) throws IOException;

}
