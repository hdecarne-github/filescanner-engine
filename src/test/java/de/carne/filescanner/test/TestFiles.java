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
package de.carne.filescanner.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.carne.io.IOUtil;
import de.carne.nio.file.attribute.FileAttributes;

/**
 * This class provides access to external test files we use to test the various formats supported by the scan engine.
 */
public final class TestFiles {

	private TestFiles() {
		// Prevent instantiation
	}

	/**
	 * This class defines a single test file by it's remote URL as well as it's local name.
	 */
	public static class TestFile {

		private final String remote;
		private final String local;

		TestFile(String remote, String local) {
			this.remote = remote;
			this.local = local;
		}

		/**
		 * Get the local test file path.
		 *
		 * @return The local test file path.
		 * @throws IOException if an I/O error occurs.
		 */
		public Path path() throws IOException {
			return downloadAndResolve(this.remote, this.local);
		}

	}

	static Path downloadAndResolve(String remote, String local) throws IOException {
		Path localBasePath = Paths.get(System.getProperty("user.home"), ".filescanner-tests");

		Files.createDirectories(localBasePath, FileAttributes.userDirectoryDefault(localBasePath));

		Path localPath = localBasePath.resolve(local);

		if (!localPath.toFile().exists()) {
			URL remoteUrl = new URL(remote);

			try (InputStream remoteStream = remoteUrl.openStream()) {
				IOUtil.copyStream(localPath.toFile(), remoteStream);
			}
		}
		return localPath;
	}

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr-1.0.0.zip
	 */
	public static final TestFile ZIP_ARCHIVE = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr-1.0.0.zip", "certmgr-1.0.0.zip");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr_windows_1_0_0.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr_windows_1_0_0.exe",
			"certmgr_windows_1_0_0.exe");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr_windows-x64_1_0_0.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS64 = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr_windows-x64_1_0_0.exe",
			"certmgr_windows-x64_1_0_0.exe");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr_macos_1_0_0.dmg
	 */
	public static final TestFile I4J_INSTALLER_MACOS = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.0.0/certmgr_macos_1_0_0.dmg",
			"certmgr_macos_1_0_0.dmg");

}
