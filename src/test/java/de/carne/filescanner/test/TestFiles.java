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
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.bmp", "w3c_home.bmp",
			"ac203a0a54460e46bf53bb2deec0a03409696c0cd83f2665703f1c70257c9127");

	/**
	 * http://distfiles.gentoo.org/distfiles/bzip2-1.0.3-r6.tbz2
	 */
	public static final TestFile BZIP2_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"http://distfiles.gentoo.org/distfiles/bzip2-1.0.3-r6.tbz2", "bzip2-1.0.3-r6.tbz2",
			"d0d8344e384f91c8deabb5dfa452d8bb9a44e66a03cd2ffd973451eec505c1cc");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.gif
	 */
	public static final TestFile GIF_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.gif", "w3c_home.gif",
			"d76201772ca87b79b75f631983a1563ba045c5b2411371cb1feb4181842dd8d6");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.tar.gz
	 */
	public static final TestFile GZIP_TAR_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.tar.gz", "certmgr-1.1.1.tar.gz",
			"0f80fd2d6a0b5d4901e21d6d1e02590ad1cb3897f8d7f98ef8d331f1f46c111c");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.jpg
	 */
	public static final TestFile JPEG_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.jpg", "w3c_home.jpg",
			"2381ec342efa47e6136def5e857ef1fef536ebb8aa024d481dbba68fa15c1b36");

	/**
	 * https://curl.haxx.se/download/curl-7.54.1.tar.lzma
	 */
	public static final TestFile LZMA_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://curl.haxx.se/download/curl-7.54.1.tar.lzma", "curl-7.54.1.tar.lzma",
			"2b7af34d4900887e0b4e0a9f545b9511ff774d07151ae4976485060d3e1bdb6e");

	/**
	 * https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.png
	 */
	public static final TestFile PNG_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"https://www.w3.org/People/mimasa/test/imgformat/img/w3c_home.png", "w3c_home.png",
			"3bd43feee0643fb7d007d2c7882a08fff2013a8ef217e6cbdcca343a6f09550a");

	/**
	 * http://skuld.bmsc.washington.edu/raster3d/examples/glow.tiff
	 */
	public static final TestFile TIFF_IMAGE = new RemoteTestFile(TEST_FILE_DIR,
			"http://skuld.bmsc.washington.edu/raster3d/examples/glow.tiff", "glow.tiff",
			"359a803ae0318df0487f7c5e9d8c7828bcc48409e3258198d99f214fae2d0733");

	/**
	 * https://distfiles.macports.org/MacPorts/MacPorts-2.5.4-10.14-Mojave.pkg
	 */
	public static final TestFile XAR_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://distfiles.macports.org/MacPorts/MacPorts-2.5.4-10.14-Mojave.pkg",
			"MacPorts-2.5.4-10.14-Mojave.pkg", "93033663bd2eb9d59df87468ed6693bf641a413799088275d16b21a9d82c6d15");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.zip
	 */
	public static final TestFile ZIP_ARCHIVE = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr-1.1.1.zip", "certmgr-1.1.1.zip",
			"b3eccc83dc23e151a429ae971ad21346464c09301f876c8401ab166173fea384");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.0/certmgr_macos_1_1_0.dmg
	 */
	public static final TestFile I4J_INSTALLER_MACOS = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.0/certmgr_macos_1_1_0.dmg",
			"certmgr_macos_1_1_0.dmg", "e9a15fb585488dd6d7f3d1f494bbc1be0974ff84b16359e0c780e18f6b52a792");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows_1_1_1.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows_1_1_1.exe",
			"certmgr_windows_1_1_1.exe", "a68a83bd49cd58d3f6dd83f60bbbbcc24bba9110b8f75abae49097040f63d435");

	/**
	 * https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows-x64_1_1_1.exe
	 */
	public static final TestFile I4J_INSTALLER_WINDOWS64 = new RemoteTestFile(TEST_FILE_DIR,
			"https://github.com/hdecarne/certmgr/releases/download/v1.1.1/certmgr_windows-x64_1_1_1.exe",
			"certmgr_windows-x64_1_1_1.exe", "05807f11f31cf1825bc478c02d56c99f512656342284add7fa8a1f70145b6107");

}
