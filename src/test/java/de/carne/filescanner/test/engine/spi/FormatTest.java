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
package de.carne.filescanner.test.engine.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.spi.Format;

/**
 * Test {@linkplain Format} class.
 */
class FormatTest {

	private static final Log LOG = new Log();

	@Test
	void testLoadFormats() {
		Iterable<Format> formats = Format.providers();

		for (Format format : formats) {
			LOG.notice("Format: '{0}'", format.name());
		}
		Assertions.assertTrue(true);
	}

}
