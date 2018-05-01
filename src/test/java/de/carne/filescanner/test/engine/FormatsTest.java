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
package de.carne.filescanner.test.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.filescanner.engine.Formats;
import de.carne.filescanner.engine.spi.Format;

/**
 * Test {@linkplain Formats} class.
 */
class FormatsTest {

	@Test
	void testFormats() {
		Formats formats = Formats.all();
		int formatCount = formats.enabledFormats().size();
		Format aFormat = formats.iterator().next();

		formats.enable(aFormat);

		Assertions.assertEquals(formatCount, formats.enabledFormats().size());

		formats.disable(aFormat);

		Assertions.assertEquals(formatCount - 1, formats.enabledFormats().size());

		formats.enable(aFormat);

		Assertions.assertEquals(formatCount, formats.enabledFormats().size());
	}

}
