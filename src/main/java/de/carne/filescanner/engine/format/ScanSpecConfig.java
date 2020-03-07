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
package de.carne.filescanner.engine.format;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.carne.boot.check.Check;
import de.carne.filescanner.engine.ValueStreamer;

/**
 * Configuration parameters for a {@linkplain ScanSpec}.
 */
public final class ScanSpecConfig {

	private final int matchSize;
	private final ValueStreamer streamer;

	/**
	 * Constructs a new {@linkplain ScanSpecConfig} instance.
	 *
	 * @param matchSize the chunk size to use for matching.
	 * @param streamer the {@linkplain ValueStreamer} to use for matching.
	 */
	public ScanSpecConfig(int matchSize, ValueStreamer streamer) {
		Check.assertTrue(matchSize > 0);

		this.matchSize = matchSize;
		this.streamer = streamer;
	}

	/**
	 * Gets the chunk size to use for matching.
	 *
	 * @return the chunk size to use for matching.
	 */
	public int matchSize() {
		return this.matchSize;
	}

	/**
	 * Matches the submitted data.
	 *
	 * @param buffer the data to match.
	 * @return {@code true} if the submitted data matches. {@code false} otherwise.
	 * @throws IOException if a decode error occurs.
	 */
	public boolean match(ByteBuffer buffer) throws IOException {
		return !this.streamer.stream(buffer);
	}

}