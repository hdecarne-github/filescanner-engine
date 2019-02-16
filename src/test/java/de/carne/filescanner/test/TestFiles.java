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
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.bmp
	 */
	public static final TestFile BMP_IMAGE = new TestFile(
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.bmp", "w3c_home.bmp");

	/**
	 * http://distfiles.gentoo.org/distfiles/bzip2-1.0.3-r6.tbz2
	 */
	public static final TestFile BZIP2_ARCHIVE = new TestFile(
			"http://distfiles.gentoo.org/distfiles/bzip2-1.0.3-r6.tbz2", "bzip2-1.0.3-r6.tbz2");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.gif
	 */
	public static final TestFile GIF_IMAGE = new TestFile(
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.gif", "w3c_home.gif");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.tar.gz
	 */
	public static final TestFile GZIP_TAR_ARCHIVE = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.tar.gz",
			"certmgr-1.1.1.tar.gz");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.jpg
	 */
	public static final TestFile JPEG_IMAGE = new TestFile(
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.jpg", "w3c_home.jpg");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.png
	 */
	public static final TestFile PNG_IMAGE = new TestFile(
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.png", "w3c_home.png");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.zip
	 */
	public static final TestFile ZIP_ARCHIVE = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.zip", "certmgr-1.1.1.zip");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows_1_1_1.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows_1_1_1.exe",
			"certmgr_windows_1_1_1.exe");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows-x64_1_1_1.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS64 = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows-x64_1_1_1.exe",
			"certmgr_windows-x64_1_1_1.exe");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.0/certmgr_macos_1_1_0.dmg
	 */
	public static final TestFile I4J_INSTALLER_MACOS = new TestFile(
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.0/certmgr_macos_1_1_0.dmg",
			"certmgr_macos_1_1_0.dmg");

}
