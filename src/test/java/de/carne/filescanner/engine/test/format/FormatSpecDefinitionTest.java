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
package de.carne.filescanner.engine.test.format;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test {@FormatSpecDefinition} class.
 */
class FormatSpecDefinitionTest {

	private static final String TEST_FORMAT = "TestFormat";
	private static final String TEST_RENDERER = "TestRenderer";

	@Test
	void testFormatSpecDefinition() {
		TestFormatSpecDefinition testFormat = new TestFormatSpecDefinition();

		testFormat.addByteAttributeFormatter(TEST_FORMAT, value -> value.toString());
		testFormat.addWordAttributeFormatter(TEST_FORMAT, value -> value.toString());
		testFormat.addDWordAttributeFormatter(TEST_FORMAT, value -> value.toString());
		testFormat.addQWordAttributeFormatter(TEST_FORMAT, value -> value.toString());
		testFormat.addByteArrayAttributeFormatter(TEST_FORMAT, value -> Arrays.toString(value));
		testFormat.addWordArrayAttributeFormatter(TEST_FORMAT, value -> Arrays.toString(value));
		testFormat.addDWordArrayAttributeFormatter(TEST_FORMAT, value -> Arrays.toString(value));
		testFormat.addQWordArrayAttributeFormatter(TEST_FORMAT, value -> Arrays.toString(value));
		testFormat.addStringAttributeFormatter(TEST_FORMAT, value -> value);
		testFormat.addByteAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(value.toString()));
		testFormat.addWordAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(value.toString()));
		testFormat.addDWordAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(value.toString()));
		testFormat.addQWordAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(value.toString()));
		testFormat.addByteArrayAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(Arrays.toString(value)));
		testFormat.addWordArrayAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(Arrays.toString(value)));
		testFormat.addDWordArrayAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(Arrays.toString(value)));
		testFormat.addQWordArrayAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(Arrays.toString(value)));
		testFormat.addStringAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(value));
		testFormat.addStreamValueAttributeRenderer(TEST_RENDERER, (out, value) -> out.write(value.toString()));
		testFormat.load();
		Assertions.assertTrue(true);
	}

}
