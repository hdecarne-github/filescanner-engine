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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.spi.Format;

/**
 * Helper class for handling the formats to scan.
 */
final class FormatMatcherBuilder {

	private final Format[] formats;
	private final int matchHeaderBufferSize;
	private final int matchTrailerBufferSize;

	FormatMatcherBuilder(Collection<Format> formats) {
		this.formats = formats.toArray(new Format[formats.size()]);
		this.matchHeaderBufferSize = matchHeaderBufferSize();
		this.matchTrailerBufferSize = matchTrailerBufferSize();
	}

	private int matchHeaderBufferSize() {
		int matchBufferSize = 0;

		for (Format format : this.formats) {
			for (CompositeSpec headerSpec : format.headerSpecs()) {
				matchBufferSize = Math.max(matchBufferSize, headerSpec.matchSize());
			}
		}
		return matchBufferSize;
	}

	private int matchTrailerBufferSize() {
		int matchBufferSize = 0;

		for (Format format : this.formats) {
			for (CompositeSpec trailerSpec : format.trailerSpecs()) {
				matchBufferSize = Math.max(matchBufferSize, trailerSpec.matchSize());
			}
		}
		return matchBufferSize;
	}

	public Matcher matcher() {
		return new Matcher(this.formats, this.matchHeaderBufferSize, this.matchTrailerBufferSize);
	}

	public final class Matcher {

		private final Format[] matcherFormats;
		private final ByteBuffer matchHeaderBuffer;
		private final ByteBuffer matchTrailerBuffer;

		public Matcher(Format[] matcherFormats, int matchHeaderBufferSize, int matchTrailerBufferSize) {
			this.matcherFormats = matcherFormats;
			this.matchHeaderBuffer = ByteBuffer.allocate(matchHeaderBufferSize);
			this.matchTrailerBuffer = ByteBuffer.allocate(matchTrailerBufferSize);
		}

		@SuppressWarnings("squid:S3776")
		public List<Format> match(FileScannerInputRange inputRange, long scanPosition) throws IOException {
			this.matchHeaderBuffer.rewind();
			inputRange.read(this.matchHeaderBuffer, scanPosition);
			this.matchHeaderBuffer.flip();
			if (scanPosition == 0) {
				this.matchTrailerBuffer.rewind();
				inputRange.read(this.matchTrailerBuffer,
						Math.max(scanPosition, inputRange.end() - this.matchTrailerBuffer.capacity()));
				this.matchTrailerBuffer.flip();
			}

			List<Format> matchingFormats = new ArrayList<>(this.matcherFormats.length);
			int trailerMatches = 0;
			int headerMatches = 0;
			int inputNameMatches = 0;

			for (Format format : this.matcherFormats) {
				if (scanPosition == 0 || !format.isAbsolute()) {
					if (format.hasHeaderSpecs()) {
						if (matchHeaderSpecs(format, this.matchHeaderBuffer)) {
							matchingFormats.add(trailerMatches + headerMatches, format);
							headerMatches++;
						}
					} else if (format.hasTrailerSpecs()) {
						if (matchTrailerSpecs(format, this.matchTrailerBuffer)) {
							matchingFormats.add(trailerMatches, format);
							trailerMatches++;
						}
					} else if (format.hasInputNamePatterns()) {
						if (matchInputNamePatterns(format, inputRange.name())) {
							matchingFormats.add(trailerMatches + headerMatches + inputNameMatches, format);
							inputNameMatches++;
						}
					} else {
						matchingFormats.add(format);
					}
				}
			}
			return matchingFormats;
		}

		private boolean matchHeaderSpecs(Format format, ByteBuffer matchBuffer) {
			boolean match = false;

			for (CompositeSpec headerSpec : format.headerSpecs()) {
				matchBuffer.rewind();
				matchBuffer.order(headerSpec.byteOrder());
				if (headerSpec.matches(matchBuffer)) {
					match = true;
					break;
				}
			}
			return match;
		}

		private boolean matchTrailerSpecs(Format format, ByteBuffer matchBuffer) {
			boolean match = false;

			for (CompositeSpec trailerSpec : format.trailerSpecs()) {
				matchBuffer.rewind();
				matchBuffer.order(trailerSpec.byteOrder());

				int matchPosition = matchBuffer.remaining() - trailerSpec.matchSize();

				if (matchPosition >= 0) {
					matchBuffer.position(matchPosition);
					if (trailerSpec.matches(matchBuffer)) {
						match = true;
						break;
					}
				}
			}
			return match;
		}

		private boolean matchInputNamePatterns(Format format, String inputName) {
			boolean match = false;

			for (Pattern inputNamePattern : format.inputNamePatterns()) {
				if (inputNamePattern.matcher(inputName).matches()) {
					match = true;
					break;
				}
			}
			return match;
		}

	}

}
