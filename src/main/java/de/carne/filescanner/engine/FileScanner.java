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
package de.carne.filescanner.engine;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.carne.boot.Exceptions;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.FormatMatcherBuilder.Matcher;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.input.InputDecodeCache;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.filescanner.engine.spi.Format;
import de.carne.util.SystemProperties;
import de.carne.util.Threads;

/**
 * FileScanner engine.
 */
public final class FileScanner implements Closeable {

	private static final Log LOG = new Log();

	private static final int THREAD_COUNT = SystemProperties.intValue(
			FileScanner.class.getPackage().getName() + ".threadCount", Runtime.getRuntime().availableProcessors());

	private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
	private final FormatMatcherBuilder formatMatcherBuilder;
	private final InputDecodeCache inputDecodeCache;
	private final FileScannerStatus status;
	private final FileScannerResultBuilder result;
	private int runningScanTasks = 0;
	private long scanStartedNanos = 0;
	private long scanTimeNanos = 0;
	private long lastProgressTimeNanos = 0;
	private long totalInputBytes = 0;
	private long scannedBytes = 0;

	private FileScanner(FileScannerInput input, Collection<Format> formats, FileScannerStatus status)
			throws IOException {
		this.formatMatcherBuilder = new FormatMatcherBuilder(formats);
		this.inputDecodeCache = new InputDecodeCache();
		this.status = status;
		this.result = FileScannerResultBuilder.inputResult(input);
		this.result.updateAndCommit(-1, true);
		queueScanTask(() -> scanRootInput(this.result));
	}

	private void scanRootInput(FileScannerResultBuilder inputResult) {
		LOG.info("Starting scan (using {0} threads)...", THREAD_COUNT);
		scanStarted();
		scanInput(inputResult);
	}

	private void scanInput(FileScannerResultBuilder inputResult) {
		LOG.notice("Scanning input ''{0}''...", inputResult.name());

		scanProgress(inputResult.size(), 0);
		onScanResultCommit(inputResult);
		try {
			scanInputRange(inputResult, inputResult.input(), inputResult.start(), inputResult.end());
		} catch (InterruptedException e) {
			Exceptions.ignore(e);
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			LOG.warning(e, "An exception occurred while scanning input ''{0}''", inputResult.name());

			callStatus(() -> this.status.scanException(this, e));
		}
	}

	@SuppressWarnings("squid:S3776")
	private void scanInputRange(FileScannerResultBuilder parent, FileScannerInput input, long start, long end)
			throws IOException, InterruptedException {
		Matcher formatMatcher = this.formatMatcherBuilder.matcher();
		long scanPosition = start;
		FileScannerInputRange scanRange = input.range(scanPosition, end);

		while (scanPosition < end) {
			Threads.checkInterrupted();

			List<Format> matchingFormats = formatMatcher.match(scanRange, scanPosition);
			FileScannerResult decodeResult = null;
			long decodeResultSize = -1;

			for (Format format : matchingFormats) {
				Threads.checkInterrupted();

				FileScannerResultDecodeContext context = new FileScannerResultDecodeContext(this, parent, scanRange,
						scanPosition);

				try {
					decodeResult = format.decode(context);
					decodeResultSize = decodeResult.size();
					if (decodeResultSize > 0) {
						break;
					}
					LOG.info("Format ''{0}'' failed to decode input", format.name());
				} catch (FormatDecodeException e) {
					LOG.warning(e, "Format ''{0}'' failed to decode input", format.name());
				}
			}
			if (decodeResult != null && decodeResultSize > 0) {
				scanProgress(0, decodeResultSize);

				long decodeResultStart = decodeResult.start();

				if (scanPosition < decodeResultStart) {
					long reScanPosition = scanPosition;

					queueScanTask(() -> scanInputRange(parent, input, reScanPosition, decodeResultStart));
				}
				scanPosition = decodeResultStart + decodeResultSize;
				scanRange = input.range(scanPosition, end);
			} else {
				scanProgress(0, 1);
				scanPosition++;
			}
		}
	}

	InputDecodeCache.Decoded decodeInput(String name, InputDecoder inputDecoder, FileScannerInput input, long start,
			long end) throws IOException, InterruptedException {
		return this.inputDecodeCache.decodeInput(name, inputDecoder, input, start, end);
	}

	void queueInputResults(Collection<FileScannerResultBuilder> inputResults) {
		for (FileScannerResultBuilder inputResult : inputResults) {
			queueScanTask(() -> scanInput(inputResult));
		}
	}

	void onScanResultCommit(FileScannerResultBuilder scanResult) {
		callStatus(() -> this.status.scanResult(this, scanResult));
	}

	/**
	 * Create a {@linkplain FileScanner} and scan the submitted file.
	 *
	 * @param file The file to scan.
	 * @param formats The {@linkplain Format}s to scan for.
	 * @param status The callback interface receiving scan status updates.
	 * @return The created {@linkplain FileScanner}.
	 * @throws IOException if an I/O error occurs.
	 */
	public static FileScanner scan(Path file, Collection<Format> formats, FileScannerStatus status) throws IOException {
		return new FileScanner(FileScannerInput.open(file), formats, status);
	}

	/**
	 * Get the current scan status of this {@linkplain FileScanner}.
	 *
	 * @return {@code true} if the scanner is currently scanning.
	 */
	public synchronized boolean isScanning() {
		return this.runningScanTasks > 0;
	}

	/**
	 * Get the current scan progress.
	 *
	 * @return The current scan progress.
	 * @see FileScannerProgress
	 */
	public synchronized FileScannerProgress progress() {
		return new FileScannerProgress(this.scanStartedNanos, this.scanTimeNanos, this.scannedBytes,
				this.totalInputBytes);
	}

	/**
	 * Get the root {@linkplain FileScannerResult}.
	 *
	 * @return The root {@linkplain FileScannerResult}.
	 */
	public FileScannerResult result() {
		return this.result;
	}

	@Override
	public void close() throws IOException {
		this.threadPool.shutdownNow();
		this.result.input().close();
		this.inputDecodeCache.close();
	}

	private void queueScanTask(FileScannerRunnable task) {
		synchronized (this) {
			this.runningScanTasks++;
		}
		this.threadPool.execute(() -> {
			try {
				task.run();
			} catch (InterruptedException e) {
				Exceptions.ignore(e);
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				LOG.warning(e, "Scan thread failed with exception");
			} finally {
				boolean scanFinished;

				synchronized (this) {
					this.runningScanTasks--;
					scanFinished = this.runningScanTasks == 0;
				}
				if (scanFinished) {
					scanFinished();
				}
			}
		});
	}

	private void scanProgress(long totalInputBytesDelta, long scannedBytesDelta) {
		FileScannerProgress reportProgress = null;

		synchronized (this) {
			this.totalInputBytes += totalInputBytesDelta;
			this.scannedBytes += scannedBytesDelta;

			long currentNanos = System.nanoTime();

			this.scanTimeNanos = currentNanos - this.scanStartedNanos;
			if (totalInputBytesDelta > 0 || this.scannedBytes == this.totalInputBytes
					|| (this.scanTimeNanos - this.lastProgressTimeNanos > 700000000l)) {
				this.lastProgressTimeNanos = this.scanTimeNanos;
				reportProgress = new FileScannerProgress(this.scanStartedNanos, this.scanTimeNanos, this.scannedBytes,
						this.totalInputBytes);
			}
		}
		if (reportProgress != null) {
			FileScannerProgress progress = reportProgress;

			callStatus(() -> this.status.scanProgress(this, progress));
		}
	}

	private void scanStarted() {
		synchronized (this) {
			this.scanStartedNanos = System.nanoTime();
			notifyAll();
		}
		callStatus(() -> this.status.scanStarted(this));
	}

	private void scanFinished() {
		long scanTime;

		synchronized (this) {
			scanTime = this.scanTimeNanos = System.nanoTime() - this.scanStartedNanos;
		}

		LOG.notice("Finished scanning ''{0}'' (scan took: {1} ms)", this.result.name(), scanTime / 1000000l);

		callStatus(() -> this.status.scanFinished(this));
		synchronized (this) {
			notifyAll();
		}
	}

	private void callStatus(Runnable runnable) {
		try {
			runnable.run();
		} catch (RuntimeException e) {
			LOG.warning(e, "Status callback failed with exception");
		}
	}

}
