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
package de.carne.filescanner.engine.format;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import de.carne.filescanner.engine.transfer.FileScannerResultOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;

/**
 * {@linkplain AttributeRenderer} implementation which renders a comment for each flag set in the attribute value.
 *
 * @param <T> The actual attribute type.
 */
public abstract class FlagRenderer<T> extends HashMap<T, String> implements AttributeRenderer<T> {

	// Serialization support
	private static final long serialVersionUID = -5863180152604163026L;

	@Override
	public void render(FileScannerResultOutput out, T value) throws IOException, InterruptedException {
		Iterator<T> flags = flags();

		while (flags.hasNext()) {
			T flag = flags.next();

			if (testFlag(value, flag)) {
				out.writeln();
				out.setStyle(RenderStyle.VALUE).write(formatFlag(flag));
				out.setStyle(RenderStyle.COMMENT).write(" // ").write(getOrDefault(flag, "?"));
			}
		}
	}

	/**
	 * Gets the {@linkplain Iterator} instance for flag value enumeration.
	 * 
	 * @return the {@linkplain Iterator} instance for flag value enumeration.
	 */
	protected abstract Iterator<T> flags();

	/**
	 * Tests whether a flag value is set.
	 * 
	 * @param value the value to test the flag against.
	 * @param flag the flat value to test.
	 * @return {@code true} if the flag value is set.
	 */
	protected abstract boolean testFlag(T value, T flag);

	/**
	 * Formats a flag value.
	 * 
	 * @param flag the flag value to format.
	 * @return the formatted flag value.
	 */
	protected abstract String formatFlag(T flag);

}
