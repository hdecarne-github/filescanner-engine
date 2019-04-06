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
package de.carne.filescanner.test.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.FileScanner;
import de.carne.filescanner.engine.FileScannerProgress;
import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerStatus;
import de.carne.filescanner.engine.Formats;
import de.carne.filescanner.engine.spi.Format;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.Renderer;
import de.carne.filescanner.engine.transfer.SimpleTextRenderer;
import de.carne.filescanner.engine.util.CombinedRenderer;
import de.carne.filescanner.engine.util.HtmlReportGenerator;
import de.carne.filescanner.test.TestFiles;
import de.carne.text.MemoryUnitFormat;

/**
 * Test {@linkplain FileScanner} class.
 */
class FileScannerTest {

	static final Log LOG = new Log();

	private final Renderer systemOutRenderer = new SimpleTextRenderer(new PrintWriter(System.out, true));

	private static class Status implements FileScannerStatus {

		final AtomicInteger scanStartedCount = new AtomicInteger(0);
		final AtomicInteger scanFinishedCount = new AtomicInteger(0);
		final AtomicInteger scanProgressCount = new AtomicInteger(0);
		final AtomicInteger scanResultCount = new AtomicInteger(0);
		final AtomicInteger scanExceptionCount = new AtomicInteger(0);

		Status() {
			// Make class package accessible
		}

		@Override
		public void scanStarted(FileScanner scanner) {
			LOG.info("scanStarted...");
			this.scanStartedCount.addAndGet(1);
		}

		@Override
		public void scanFinished(FileScanner scanner) {
			LOG.info("scanFinished");
			this.scanFinishedCount.addAndGet(1);
		}

		@Override
		public void scanProgress(FileScanner scanner, FileScannerProgress progress) {
			LOG.info("scanProgress");
			this.scanProgressCount.addAndGet(1);

			MemoryUnitFormat format = MemoryUnitFormat.getMemoryUnitInstance();

			System.out.println("Total input bytes: " + format.format(progress.totalInputBytes()));
			System.out.println("Scanned bytes    : " + format.format(progress.scannedBytes()));
			System.out.println("Scan progress    : " + progress.scanProgress());
			System.out.println("Scan time        : " + progress.scanTimeNanos());
			System.out.println("Scan rate        : " + format.format(progress.scanRate()) + "/s");
		}

		@Override
		public void scanResult(FileScanner scanner, FileScannerResult result) {
			LOG.info("scanResult");

			this.scanResultCount.addAndGet(1);
			System.out.println("Scan result: '" + result.name() + "' (type:" + result.type() + ")");
		}

		@Override
		public void scanException(FileScanner scanner, Exception cause) {
			LOG.info("scanException");
			this.scanExceptionCount.addAndGet(1);
		}

	}

	@Test
	void testBmpImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.BMP_IMAGE.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testBzip2ArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.BZIP2_ARCHIVE.path(), Formats.all().enabledFormats(), 2);
	}

	@Test
	void testGifImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.GIF_IMAGE.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testGzipArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.GZIP_TAR_ARCHIVE.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testJpegImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.JPEG_IMAGE.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testLzmaArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.LZMA_ARCHIVE.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testPngImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.PNG_IMAGE.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testUdifFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.I4J_INSTALLER_MACOS.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testWindowsExeFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.I4J_INSTALLER_WINDOWS.path(), Formats.all().enabledFormats(), 2);
	}

	@Test
	void testWindows64ExeFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.I4J_INSTALLER_WINDOWS64.path(), Formats.all().enabledFormats(), 2);
	}

	@Test
	void testXarArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.XAR_ARCHIVE.path(), Formats.all().enabledFormats(), 1);
	}

	@Test
	void testZipArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.ZIP_ARCHIVE.path(), Formats.all().enabledFormats(), 1);
	}

	private Status runFileScanner(Path file, Collection<Format> formats, int resultCount)
			throws IOException, InterruptedException {
		Status status = new Status();

		try (FileScanner fileScanner = FileScanner.scan(file, formats, status)) {
			do {
				synchronized (fileScanner) {
					fileScanner.wait();
				}
			} while (fileScanner.isScanning());

			Assertions.assertEquals(1, status.scanStartedCount.get());
			Assertions.assertEquals(1, status.scanFinishedCount.get());

			FileScannerProgress progress = fileScanner.progress();
			long elapsedNanos = System.nanoTime() - progress.scanStartedNanos();

			Assertions.assertTrue(elapsedNanos >= 0);
			Assertions.assertTrue(progress.scanTimeNanos() >= 0);
			Assertions.assertTrue(elapsedNanos >= progress.scanTimeNanos());
			Assertions.assertTrue(Files.size(file) <= progress.totalInputBytes());
			Assertions.assertEquals(progress.totalInputBytes(), progress.scannedBytes());
			Assertions.assertEquals(100, progress.scanProgress());
			Assertions.assertTrue(progress.scanRate() >= -1);

			FileScannerResult result = fileScanner.result();

			writeHtmlReport(file, fileScanner);
			verifyResult(fileScanner, result);
			renderResult(fileScanner.result());

			FileScannerResult[] formatResult = result.children();

			Assertions.assertEquals(resultCount, formatResult.length);
		}
		return status;
	}

	private void writeHtmlReport(Path file, FileScanner fileScanner) throws IOException {
		Path htmlReportFile = Paths.get(file.toString() + ".html");

		try (Writer out = Files.newBufferedWriter(htmlReportFile, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			HtmlReportGenerator htmlReport = new HtmlReportGenerator();

			htmlReport.write(fileScanner, out);
		}
	}

	private void verifyResult(FileScanner fileScanner, FileScannerResult result) {
		FileScannerResult[] resultPath = fileScanner.getResultPath(result.key());

		Assertions.assertEquals(result, resultPath[resultPath.length - 1]);
		for (FileScannerResult resultChild : result.children()) {
			verifyResult(fileScanner, resultChild);
		}
	}

	private void renderResult(FileScannerResult result) throws IOException, InterruptedException {
		try (Renderer renderer = new CombinedRenderer(this.systemOutRenderer)) {
			RenderOutput.render(result, renderer);
			for (FileScannerResultExportHandler exportHandler : result.exportHandlers()) {
				exportHandler.defaultFileName(result);
			}
			for (FileScannerResult resultChild : result.children()) {
				renderResult(resultChild);
			}
		}
	}

}
