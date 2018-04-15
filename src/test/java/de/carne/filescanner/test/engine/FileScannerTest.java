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
package de.carne.filescanner.test.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import de.carne.filescanner.engine.transfer.FileScannerResultOutput;
import de.carne.filescanner.engine.transfer.PrintStreamRenderer;
import de.carne.filescanner.engine.transfer.Renderer;
import de.carne.filescanner.test.TestFiles;
import de.carne.text.MemoryUnitFormat;

/**
 * Test {@linkplain FileScanner} class.
 */
class FileScannerTest {

	static final Log LOG = new Log();

	private static class Status implements FileScannerStatus {

		private final Renderer systemOutRenderer = new PrintStreamRenderer(System.out, false);
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
			try (FileScannerResultOutput out = new FileScannerResultOutput(this.systemOutRenderer)) {
				result.render(out);
			} catch (IOException | InterruptedException e) {
				LOG.error(e, "Failed to render scan result");
			}
		}

		@Override
		public void scanException(FileScanner scanner, Exception cause) {
			LOG.info("scanException");
			this.scanExceptionCount.addAndGet(1);
		}

	}

	@Test
	void testFileScanner() throws IOException, InterruptedException {
		Collection<Format> formats = Formats.all().enabledFormats();

		runFileScanner(TestFiles.ZIP_ARCHIVE.path(), formats);
		// runFileScanner(TestFiles.I4J_INSTALLER_MACOS.path(), formats);
		// runFileScanner(TestFiles.I4J_INSTALLER_WINDOWS.path(), formats);
		// runFileScanner(TestFiles.I4J_INSTALLER_WINDOWS64.path(), formats);
	}

	private Status runFileScanner(Path file, Collection<Format> formats) throws IOException, InterruptedException {
		Status status = new Status();
		FileScanner fileScanner = FileScanner.scan(file, formats, status);

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
		return status;
	}

}
