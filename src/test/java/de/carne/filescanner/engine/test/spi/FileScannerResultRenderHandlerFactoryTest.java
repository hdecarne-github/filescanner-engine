/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.test.spi;

import java.util.SortedMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.spi.FileScannerResultRenderHandlerFactory;
import de.carne.filescanner.engine.spi.FileScannerResultRenderHandlerFactory.HandlerId;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;

/**
 * Test {@linkplain FileScannerResultRenderHandlerFactory} class.
 */
class FileScannerResultRenderHandlerFactoryTest {

	@Test
	void testGetHandlers() {
		SortedMap<HandlerId, FileScannerResultRenderHandler> handlers = FileScannerResultRenderHandlerFactory
				.getHandlers();

		Assertions.assertFalse(handlers.isEmpty());
	}

}
