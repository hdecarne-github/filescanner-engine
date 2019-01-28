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
package de.carne.filescanner.engine.format.spec;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;

/**
 * {@linkplain AttributeRenderer} implementation which renders a comment for each flag set in the attribute value.
 *
 * @param <T> The actual attribute type.
 */
public abstract class FlagRenderer<T> extends LinkedHashMap<T, String> implements AttributeRenderer<T> {

	// Serialization support
	private static final long serialVersionUID = -5863180152604163026L;

	@Override
	public void render(RenderOutput out, T value) throws IOException {
		Iterator<T> flags = flags();
		Iterator<Map.Entry<T, String>> symbolEntries = entrySet().iterator();
		@Nullable T currentSymbolFlag = null;
		@Nullable T nextSymbolFlag = (symbolEntries.hasNext() ? symbolEntries.next().getKey() : null);
		@Nullable T unknownFlags = null;

		while (flags.hasNext()) {
			T flag = flags.next();

			if (currentSymbolFlag != null && !testFlag(currentSymbolFlag, flag)) {
				renderFlag(out, value, currentSymbolFlag);
				currentSymbolFlag = null;
			}
			if (currentSymbolFlag == null) {
				if (nextSymbolFlag != null && testFlag(nextSymbolFlag, flag)) {
					if (unknownFlags != null && testFlag(value, unknownFlags)) {
						renderFlag(out, value, unknownFlags);
						unknownFlags = null;
					}
					currentSymbolFlag = nextSymbolFlag;
					nextSymbolFlag = (symbolEntries.hasNext() ? symbolEntries.next().getKey() : null);
				} else {
					unknownFlags = (unknownFlags != null ? combineFlags(unknownFlags, flag) : flag);
				}
			}
		}
		if (currentSymbolFlag != null) {
			renderFlag(out, value, currentSymbolFlag);
		} else if (unknownFlags != null && testFlag(value, unknownFlags)) {
			renderFlag(out, value, unknownFlags);
		}
	}

	private void renderFlag(RenderOutput out, T value, T flag) throws IOException {
		out.writeln();
		out.setStyle(RenderStyle.VALUE).write(formatFlag(value, flag));
		out.setStyle(RenderStyle.COMMENT).write(" // ").write(getOrDefault(flag, "?"));
	}

	/**
	 * Gets the {@linkplain Iterator} instance for flag enumeration.
	 *
	 * @return the {@linkplain Iterator} instance for flag enumeration.
	 */
	protected abstract Iterator<T> flags();

	/**
	 * Tests whether a flag is set.
	 *
	 * @param value the value to test the flag against.
	 * @param flag the flag to test.
	 * @return {@code true} if the flag is set.
	 */
	protected abstract boolean testFlag(T value, T flag);

	/**
	 * Combines two flag values into one.
	 *
	 * @param flag1 the first flag to combine.
	 * @param flag2 the second flag to combine.
	 * @return the combined flag.
	 */
	protected abstract T combineFlags(T flag1, T flag2);

	/**
	 * Formats a a single flag.
	 *
	 * @param value the value containing the flags.
	 * @param flag the flag value to format.
	 * @return the formatted flag value.
	 */
	protected abstract String formatFlag(T value, T flag);

}
