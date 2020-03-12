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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.Exceptions;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.FormatMatcherBuilder.Matcher;
import de.carne.filescanner.engine.input.BufferedFileChannelInput;
import de.carne.filescanner.engine.input.DecodedInputMapper;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.input.InputDecodeCache;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.spi.Format;
import de.carne.filescanner.provider.util.HexFormat;
import de.carne.util.SystemProperties;

/**
 * FileScanner engine.
 */
public final class FileScanner implements Closeable {

	private static final Log LOG = new Log();

	private static final int THREAD_COUNT = SystemProperties.intValue(FileScanner.class, ".threadCount",
			Runtime.getRuntime().availableProcessors());
	private static final long STOP_TIMEOUT = SystemProperties.longValue(FileScanner.class, ".stopTimeout", 5000);

	private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
	private final FormatMatcherBuilder formatMatcherBuilder;
	private final InputDecodeCache inputDecodeCache;
	private final BufferedFileChannelInput rootInput;
	private final FileScannerResultBuilder rootResult;
	private final FileScannerStatus status;
	private int runningScanTasks = 0;
	private long scanStartedNanos = 0;
	private long scanTimeNanos = 0;
	private long lastProgressTimeNanos = 0;
	private long totalInputBytes = 0;
	private long scannedBytes = 0;

	private FileScanner(BufferedFileChannelInput input, Collection<Format> formats, FileScannerStatus status)
			throws IOException {
		this.formatMatcherBuilder = new FormatMatcherBuilder(formats);
		this.inputDecodeCache = new InputDecodeCache();
		this.rootInput = input;
		this.rootResult = FileScannerResultBuilder.inputResult(this.rootInput);
		this.status = status;
		this.rootResult.updateAndCommit(-1, true);
		queueScanTask(() -> scanRootInput(this.rootResult));
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
		} catch (IOException e) {
			LOG.warning(e, "An exception occurred while scanning input ''{0}''", inputResult.name());

			callStatus(() -> this.status.scanException(this, e));
		}
	}

	@SuppressWarnings("squid:S3776")
	private void scanInputRange(FileScannerResultBuilder parent, FileScannerInput input, long start, long end)
			throws IOException {
		Matcher formatMatcher = this.formatMatcherBuilder.matcher();
		long scanPosition = start;
		FileScannerInputRange scanRange = input.range(scanPosition, end);

		while (scanPosition < end && !this.threadPool.isShutdown()) {
			List<Format> matchingFormats = formatMatcher.match(scanRange, scanPosition);
			FileScannerResult decodeResult = null;
			long decodeResultSize = -1;

			for (Format format : matchingFormats) {
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

	InputDecodeCache.DecodeResult decodeInputs(DecodedInputMapper decodedInputMapper,
			InputDecoderTable inputDecoderTable, FileScannerInput input, long start) throws IOException {
		return this.inputDecodeCache.decodeInputs(decodedInputMapper, inputDecoderTable, input, start);
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
	 * Creates a new {@linkplain FileScanner} instance and scans the submitted file.
	 *
	 * @param file the file to scan.
	 * @param formats the {@linkplain Format}s to scan for.
	 * @param status the callback interface receiving scan status updates.
	 * @return the created {@linkplain FileScanner} instance.
	 * @throws IOException if an I/O error occurs.
	 */
	public static FileScanner scan(Path file, Collection<Format> formats, FileScannerStatus status) throws IOException {
		return new FileScanner(FileScannerInput.open(file), formats, status);
	}

	/**
	 * Stops a currently running scan.
	 * <p>
	 * If the scan has already been completed or stopped this function has no effect.
	 * </p>
	 *
	 * @param now whether to force a stop (see {@link ExecutorService#shutdownNow()}).
	 * @param wait whether to wait for the scan to stop ({@code true}) or to return immediately after the stop has been
	 * requested ({@code false}).
	 */
	public void stop(boolean now, boolean wait) {
		if (!this.threadPool.isTerminated()) {

			if (now) {
				LOG.info("Stopping scan threads immediately...");

				this.threadPool.shutdownNow();
			} else {
				LOG.info("Stopping scan threads gracefully...");

				this.threadPool.shutdown();
			}
			if (wait) {
				try {
					boolean terminated = this.threadPool.awaitTermination(STOP_TIMEOUT, TimeUnit.MILLISECONDS);

					if (!terminated) {
						LOG.warning("Failed to stop all scan threads");
					}
				} catch (InterruptedException e) {
					LOG.warning(e, "Scan stop has been interrupted");

					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * Gets the current scan status of this {@linkplain FileScanner}.
	 *
	 * @return {@code true} if the scanner is currently scanning.
	 */
	public synchronized boolean isScanning() {
		return this.runningScanTasks > 0;
	}

	/**
	 * Gets the current scan progress.
	 *
	 * @return the current scan progress.
	 * @see FileScannerProgress
	 */
	public synchronized FileScannerProgress progress() {
		return new FileScannerProgress(this.scanStartedNanos, this.scanTimeNanos, this.scannedBytes,
				this.totalInputBytes);
	}

	/**
	 * Gets the root {@linkplain FileScannerResult}.
	 *
	 * @return the root {@linkplain FileScannerResult}.
	 */
	public FileScannerResult result() {
		return this.rootResult;
	}

	/**
	 * Gets array of {@linkplain FileScannerResult} instances corresponding to the given {@linkplain FileScannerResult}
	 * key.
	 *
	 * @param resultKey the {@linkplain FileScannerResult} key to resolve.
	 * @return the resolved {@linkplain FileScannerResult} path.
	 */
	public @NonNull FileScannerResult[] getResultPath(byte[] resultKey) {
		StringBuilder resultKeyString = new StringBuilder();
		List<FileScannerResult> results = new ArrayList<>();
		FileScannerResult lastResult = this.rootResult;

		resultKeyString.append(HexFormat.formatLong(0));
		results.add(lastResult);

		int resultKeyIndex = 0;

		while (resultKeyIndex < resultKey.length) {
			FileScannerResult currentResult = null;
			FileScannerResult[] lastResultChildren = lastResult.children();

			if (lastResult.type() != FileScannerResult.Type.ENCODED_INPUT) {
				long resultStart = ((resultKey[resultKeyIndex] & 0xffl) << 56)
						| ((resultKey[resultKeyIndex + 1] & 0xffl) << 48)
						| ((resultKey[resultKeyIndex + 2] & 0xffl) << 40)
						| ((resultKey[resultKeyIndex + 3] & 0xffl) << 32)
						| ((resultKey[resultKeyIndex + 4] & 0xffl) << 24)
						| ((resultKey[resultKeyIndex + 5] & 0xffl) << 16)
						| ((resultKey[resultKeyIndex + 6] & 0xffl) << 8) | (resultKey[resultKeyIndex + 7] & 0xffl);

				resultKeyString.append(", ").append(HexFormat.formatLong(resultStart));
				currentResult = getResultByStart(lastResultChildren, resultStart);
				resultKeyIndex += 8;
			} else {
				int resultIndex = ((resultKey[resultKeyIndex] & 0xff) << 24)
						| ((resultKey[resultKeyIndex + 1] & 0xff) << 16) | ((resultKey[resultKeyIndex + 2] & 0xff) << 8)
						| (resultKey[resultKeyIndex + 3] & 0xff);

				resultKeyString.append(", ").append(HexFormat.formatInt(resultIndex));
				currentResult = lastResultChildren[resultIndex];
				resultKeyIndex += 4;
			}
			if (currentResult == null) {
				throw new IllegalArgumentException("Invalid result key: " + resultKeyString);
			}
			results.add(currentResult);
			lastResult = currentResult;
		}
		return results.toArray(new @NonNull FileScannerResult[results.size()]);
	}

	private @Nullable FileScannerResult getResultByStart(FileScannerResult[] results, long resultStart) {
		int first = 0;
		int last = results.length - 1;
		FileScannerResult result = null;

		while (result == null && first <= last) {
			int median = first + (last - first) / 2;
			FileScannerResult currentResult = results[median];
			long currentResultStart = currentResult.start();

			if (resultStart == currentResultStart) {
				result = currentResult;
			} else if (resultStart < currentResultStart) {
				last = median - 1;
			} else {
				first = median + 1;
			}
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		stop(true, true);
		this.rootInput.close();
		this.inputDecodeCache.close();
	}

	private void queueScanTask(FileScannerRunnableV task) {
		synchronized (this) {
			this.runningScanTasks++;
		}
		try {
			this.threadPool.execute(() -> {
				try {
					if (!this.threadPool.isShutdown()) {
						task.run();
					}
				} catch (Exception e) {
					LOG.warning(e, "Scan thread failed with exception");
				} finally {
					cleanUpScanTask();
				}
			});
		} catch (RejectedExecutionException e) {
			Exceptions.ignore(e);
			cleanUpScanTask();
		}
	}

	private void cleanUpScanTask() {
		boolean scanFinished;

		synchronized (this) {
			this.runningScanTasks--;
			scanFinished = this.runningScanTasks == 0;
		}
		if (scanFinished) {
			scanFinished();
			this.threadPool.shutdown();
		}
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

		LOG.notice("Finished scanning ''{0}'' (scan took: {1} ms)", this.rootResult.name(), scanTime / 1000000l);

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
