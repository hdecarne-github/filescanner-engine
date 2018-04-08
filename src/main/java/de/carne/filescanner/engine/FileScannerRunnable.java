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
package de.carne.filescanner.engine;

import java.io.IOException;

/**
 * {@linkplain FileScanner} specific {@linkplain Runnable} derivative aware of scan specific exceptions.
 */
@FunctionalInterface
public interface FileScannerRunnable {

	/**
	 * Run {@linkplain FileScanner} logic.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread is interrupted.
	 */
	void run() throws IOException, InterruptedException;

}
