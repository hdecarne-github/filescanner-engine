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
package de.carne.filescanner.engine;

/**
 * This class provides information about the current scan progress of a {@linkplain FileScanner} instance.
 */
public final class FileScannerProgress {

	private final long scanStartedNanos;
	private final long scanTimeNanos;
	private final long scannedBytes;
	private final long totalInputBytes;

	FileScannerProgress(long scanStartedNanos, long scanTimeNanos, long scannedBytes, long totalInputBytes) {
		this.scanStartedNanos = scanStartedNanos;
		this.scanTimeNanos = scanTimeNanos;
		this.scannedBytes = scannedBytes;
		this.totalInputBytes = totalInputBytes;
	}

	/**
	 * Get the timestamp (in nanoseconds) the scan has been started.
	 *
	 * @return The timestamp (in nanoseconds) the scan has been started.
	 */
	public long scanStartedNanos() {
		return this.scanStartedNanos;
	}

	/**
	 * Get the current scan time (in nanoseconds).
	 *
	 * @return The current scan time (in nanoseconds).
	 */
	public long scanTimeNanos() {
		return this.scanTimeNanos;
	}

	/**
	 * Get the number of scanned bytes.
	 *
	 * @return The number of scanned bytes.
	 */
	public long scannedBytes() {
		return this.scannedBytes;
	}

	/**
	 * Get the total number of input bytes.
	 *
	 * @return The total number of input bytes.
	 */
	public long totalInputBytes() {
		return this.totalInputBytes;
	}

	/**
	 * Get the current scan progress (in percent).
	 *
	 * @return The current scan progress (in percent).
	 */
	public int scanProgress() {
		return (this.totalInputBytes > 0 ? (int) ((this.scannedBytes * 100) / this.totalInputBytes) : 0);
	}

	/**
	 * Get the current scan rate (in bytes per second).
	 *
	 * @return The current scan rate (in bytes per second) or {@code -1} if the scan rate is indetermined.
	 */
	public long scanRate() {
		return (this.scanTimeNanos > 1000000000l ? (this.scannedBytes / (this.scanTimeNanos / 1000000000l)) : -1);
	}

}
