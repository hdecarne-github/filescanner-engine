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
package de.carne.filescanner.engine.util;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class providing file name related functions.
 */
public final class FileNames {

	private FileNames() {
		// Prevent instantiation
	}

	private static final String REMOVE_FILE_NAME_CHARS = "^\\\\|^/|<|>|\\||:|\\\"|\\?|\\*";
	private static final String REPLACE_FILE_NAME_CHARS = "\\\\|/| |\\r\\n|\\r|\\n";

	/**
	 * Removes a common set of invalid file name characters from a file name.
	 * <p>
	 * Please note that the resulting file name might still be invalid on the current file system name or not suitable
	 * for the end user due to special characters.
	 *
	 * @param fileName the file name to mangle.
	 * @param extension the (optional) file name extension to mangle into the file name.
	 * @return the file name with the well known invalid characters removed.
	 */
	public static String mangleFileName(String fileName, @Nullable String extension) {
		String mangledFileName = fileName.replaceAll(REMOVE_FILE_NAME_CHARS, "")
				.replaceAll(REPLACE_FILE_NAME_CHARS, "_").trim();

		if (extension != null) {
			int extensionIndex = mangledFileName.lastIndexOf('.');

			if (extensionIndex > 0) {
				mangledFileName = mangledFileName.substring(0, extensionIndex) + extension;
			} else {
				mangledFileName += extension;
			}
		}
		return mangledFileName;
	}

}
