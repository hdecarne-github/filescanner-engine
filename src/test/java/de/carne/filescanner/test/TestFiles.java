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
package de.carne.filescanner.test;

import java.nio.file.Path;

import de.carne.nio.file.FileUtil;
import de.carne.test.io.RemoteTestFile;
import de.carne.test.io.TestFile;

/**
 * This class provides access to external test files we use to test the various formats supported by the scan engine.
 */
public final class TestFiles {

	private static final Path TEST_FILE_DIR = FileUtil.userHomeDir().resolve(".tests")
			.resolve(TestFiles.class.getPackage().getName());

	private TestFiles() {
		// Prevent instantiation
	}

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.bmp
	 */
	public static final TestFile BMP_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.bmp", "w3c_home.bmp");

	/**
	 * http://distfiles.gentoo.org/distfiles/bzip2-1.0.3-r6.tbz2
	 */
	public static final TestFile BZIP2_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"http://distfiles.gentoo.org/distfiles/bzip2-1.0.3-r6.tbz2", "bzip2-1.0.3-r6.tbz2");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.gif
	 */
	public static final TestFile GIF_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.gif", "w3c_home.gif");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.tar.gz
	 */
	public static final TestFile GZIP_TAR_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.tar.gz",
			"certmgr-1.1.1.tar.gz");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.jpg
	 */
	public static final TestFile JPEG_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.jpg", "w3c_home.jpg");

	/**
	 * https://curl.haxx.se/download/curl-7.54.1.tar.lzma
	 */
	public static final TestFile LZMA_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://curl.haxx.se/download/curl-7.54.1.tar.lzma", "curl-7.54.1.tar.lzma");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.png
	 */
	public static final TestFile PNG_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.png", "w3c_home.png");

	/**
	 * http://skuld.bmsc.washington.edu/raster3d/examples/glow.tiff
	 */
	public static final TestFile TIFF_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"http://skuld.bmsc.washington.edu/raster3d/examples/glow.tiff", "glow.tiff");

	/**
	 * https://distfiles.macports.org/MacPorts/MacPorts-2.5.4-10.14-Mojave.pkg
	 */
	public static final TestFile XAR_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://distfiles.macports.org/MacPorts/MacPorts-2.5.4-10.14-Mojave.pkg",
			"MacPorts-2.5.4-10.14-Mojave.pkg");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.zip
	 */
	public static final TestFile ZIP_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.zip", "certmgr-1.1.1.zip");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.0/certmgr_macos_1_1_0.dmg
	 */
	public static final TestFile I4J_INSTALLER_MACOS = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.0/certmgr_macos_1_1_0.dmg",
			"certmgr_macos_1_1_0.dmg");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows_1_1_1.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows_1_1_1.exe",
			"certmgr_windows_1_1_1.exe");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows-x64_1_1_1.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS64 = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows-x64_1_1_1.exe",
			"certmgr_windows-x64_1_1_1.exe");

}
