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
import de.carne.filescanner.provider.util.FileNames;

/**
 * Utility class providing {@linkplain FileScannerResult} related functions.
 */
public final class FileScannerResults {

	private FileScannerResults() {
		// prevent instantiation
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
