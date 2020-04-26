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
package de.carne.filescanner.engine.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
import de.carne.filescanner.engine.transfer.renderer.PlainTextRenderer;
import de.carne.filescanner.provider.jvm.ClassFormat;
import de.carne.filescanner.provider.zip.ZipFormat;
import de.carne.test.api.io.TempDir;
import de.carne.test.diff.Diff;
import de.carne.test.diff.DiffResult;
import de.carne.test.extension.TempPathExtension;
import de.carne.text.MemoryUnitFormat;

/**
 * Test {@linkplain FileScanner} class.
 */
@ExtendWith(TempPathExtension.class)
class FileScannerTest {

	private static final Log LOG = new Log();

	@SuppressWarnings("null")
	@TempDir
	Path reportDir;

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
		runFileScanner(TestFiles.BMP_IMAGE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testBzip2ArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.BZIP2_ARCHIVE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testElfX86x642ImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.ELF_X86_64_IMAGE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testGifImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.GIF_IMAGE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testGzipArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.GZIP_TAR_ARCHIVE.getPath(),
				Formats.all().disable(ClassFormat.FORMAT_NAME).enabledFormats());
	}

	@Test
	void testJpegImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.JPEG_IMAGE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testLzmaArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.LZMA_ARCHIVE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testPngImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.PNG_IMAGE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testTiffImageFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.TIFF_IMAGE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testUdifFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.I4J_INSTALLER_MACOS.getPath(),
				Formats.all().disable(ZipFormat.FORMAT_NAME).enabledFormats());
	}

	@Test
	void testWindowsExeFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.I4J_INSTALLER_WINDOWS.getPath(),
				Formats.all().disable(ZipFormat.FORMAT_NAME).enabledFormats());
	}

	@Test
	void testWindows64ExeFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.I4J_INSTALLER_WINDOWS64.getPath(),
				Formats.all().disable(ZipFormat.FORMAT_NAME).enabledFormats());
	}

	@Test
	void testXarArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.XAR_ARCHIVE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testXmlFileFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.XML_FILE.getPath(), Formats.all().enabledFormats());
	}

	@Test
	void testZipArchiveFormat() throws IOException, InterruptedException {
		runFileScanner(TestFiles.ZIP_ARCHIVE.getPath(),
				Formats.all().disable(ClassFormat.FORMAT_NAME).enabledFormats());
	}

	private Status runFileScanner(Path file, Collection<Format> formats) throws IOException, InterruptedException {
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

			Path renderLog = renderResult(file, fileScanner.result());
			DiffResult<String> diffResult = diffRenderLog(renderLog);

			Assertions.assertEquals(DiffResult.lineMatch(), diffResult);
		}
		return status;
	}

	private Path renderResult(Path file, FileScannerResult result) throws IOException {
		Path resultLog = this.reportDir.resolve(file.getFileName() + ".log");

		try (Writer resultLogWriter = Files.newBufferedWriter(resultLog, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			renderResultHelper(resultLogWriter, result, true);
		}
		return resultLog;
	}

	private void renderResultHelper(Writer resultLogWriter, FileScannerResult result, boolean root) throws IOException {
		if (!root) {
			resultLogWriter.write("// " + result.toString() + System.lineSeparator());
			try (Renderer renderer = new PlainTextRenderer(resultLogWriter, false)) {
				RenderOutput.render(result, renderer, null, 0);
			}
			for (FileScannerResultExportHandler exportHandler : result.exportHandlers()) {
				resultLogWriter.write("// Export handler: " + exportHandler.name() + System.lineSeparator());
			}
		}
		for (FileScannerResult child : result.children()) {
			renderResultHelper(resultLogWriter, child, false);
		}
	}

	private DiffResult<String> diffRenderLog(Path renderLog) throws IOException {
		URL referenceLog = Objects.requireNonNull(getClass().getResource(renderLog.getFileName().toString()));
		DiffResult<String> diffResult;

		try (BufferedReader referenceLogReader = new BufferedReader(
				new InputStreamReader(referenceLog.openStream(), StandardCharsets.UTF_8));
				BufferedReader renderLogReader = Files.newBufferedReader(renderLog)) {
			diffResult = Diff.lines(referenceLogReader, renderLogReader);
		}
		return diffResult;
	}

}
