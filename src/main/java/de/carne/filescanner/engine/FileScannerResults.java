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
package de.carne.filescanner.engine;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.FileScannerResult.Type;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.FileNames;
import de.carne.filescanner.engine.util.HexFormat;
import de.carne.filescanner.engine.util.PrettyFormat;
import de.carne.text.MemoryUnitFormat;

/**
 * Utility class providing {@linkplain FileScannerResult} related functions.
 */
public final class FileScannerResults {

	private FileScannerResults() {
		// prevent instantiation
	}

	/**
	 * Renders the default result representation by writing the basic result attributes.
	 *
	 * @param result the {@linkplain FileScannerResult} instance to render.
	 * @param out the {@linkplain RenderOutput} instance to render into.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void renderDefault(FileScannerResult result, RenderOutput out) throws IOException {
		out.setStyle(RenderStyle.NORMAL).write("start");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln(HexFormat.formatLong(result.start()));
		out.setStyle(RenderStyle.NORMAL).write("end");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln(HexFormat.formatLong(result.end()));
		out.setStyle(RenderStyle.NORMAL).write("size");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).write(PrettyFormat.formatLongNumber(result.size()));
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.writeln(MemoryUnitFormat.getMemoryUnitInstance().format(result.size() * 1.0));
	}

	/**
	 * Determines the default file name most suitable for the given result.
	 *
	 * @param result the {@linkplain FileScannerResult} instance to determine the file name for.
	 * @param extension the (optional) file name extension to use for file name generation.
	 * @return the default file name to use for a file export.
	 * @throws IOException if an I/O error occurs.
	 */
	public static String defaultFileName(FileScannerResult result, @Nullable String extension) throws IOException {
		FileScannerInput input = isInput(result);

		return FileNames.mangleFileName((input != null ? input.name() : result.name()), extension);
	}

	private static @Nullable FileScannerInput isInput(FileScannerResult result) throws IOException {
		FileScannerInput input = result.input();

		return (result.type() == Type.INPUT || (result.start() == 0 && result.end() == input.size()) ? input : null);
	}

}
