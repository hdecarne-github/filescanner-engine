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
package de.carne.filescanner.engine.spi;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.format.FormatSpec;

/**
 * Base class for all decodable file formats.
 */
public abstract class Format {

	private final String name;
	private final ByteOrder byteOrder;

	private final List<FormatSpec> headerSpecs = new ArrayList<>();
	private final List<FormatSpec> trailerSpecs = new ArrayList<>();
	private final List<Pattern> inputNamePatterns = new ArrayList<>();

	/**
	 * Constructs a new {@linkplain Format} instance.
	 *
	 * @param name the format's name.
	 * @param byteOrder the format's byte order.
	 */
	protected Format(String name, ByteOrder byteOrder) {
		this.name = name;
		this.byteOrder = byteOrder;
	}

	/**
	 * Gets all registered {@linkplain Format} providers.
	 *
	 * @return the loaded {@linkplain Format} providers.
	 */
	public static Iterable<Format> providers() {
		return ServiceLoader.load(Format.class);
	}

	/**
	 * Registers a header {@linkplain FormatSpec} hinting at this {@linkplain Format} instance.
	 *
	 * @param headerSpec the header {@linkplain FormatSpec} to register.
	 * @return the updated {@linkplain Format} instance for chaining.
	 */
	protected Format registerHeaderSpec(FormatSpec headerSpec) {
		this.headerSpecs.add(headerSpec);
		return this;
	}

	/**
	 * Registers a trailer {@linkplain FormatSpec} hinting at this {@linkplain Format} instance.
	 *
	 * @param trailerSpec the trailer {@linkplain FormatSpec} to register.
	 * @return the updated {@linkplain Format} instance for chaining.
	 */
	protected Format registerTrailerSpec(FormatSpec trailerSpec) {
		this.trailerSpecs.add(trailerSpec);
		return this;
	}

	/**
	 * Registers an input name {@linkplain Pattern} hinting at this {@linkplain Format} instance.
	 *
	 * @param inputNamePattern the input name pattern to register.
	 * @return the updated {@linkplain Format} instance for chaining.
	 */
	protected Format registerInputNamePattern(Pattern inputNamePattern) {
		this.inputNamePatterns.add(inputNamePattern);
		return this;
	}

	/**
	 * Gets this format's name.
	 *
	 * @return this format's name.
	 */
	public final String name() {
		return this.name;
	}

	/**
	 * Gets this format's byte order.
	 *
	 * @return this format's byte order.
	 */
	public final ByteOrder byteOrder() {
		return this.byteOrder;
	}

	/**
	 * Checks whether this {@linkplain Format} instance has any header {@linkplain FormatSpec}s defined.
	 *
	 * @return {@code true} if at least one header {@linkplain FormatSpec} is associated with this {@linkplain Format}
	 *         instance.
	 * @see #registerHeaderSpec(FormatSpec)
	 */
	public boolean hasHeaderSpecs() {
		return !this.headerSpecs.isEmpty();
	}

	/**
	 * Gets the header {@linkplain FormatSpec}s hinting at this {@linkplain Format} instance.
	 *
	 * @return the header {@linkplain FormatSpec}s hinting at this {@linkplain Format} instance.
	 * @see #registerHeaderSpec(FormatSpec)
	 */
	public List<FormatSpec> headerSpecs() {
		return Collections.unmodifiableList(this.headerSpecs);
	}

	/**
	 * Checks whether this {@linkplain Format} instance has any trailer {@linkplain FormatSpec}s defined.
	 *
	 * @return {@code true} if at least one trailer {@linkplain FormatSpec} is associated with this {@linkplain Format}
	 *         instance.
	 * @see #registerTrailerSpec(FormatSpec)
	 */
	public boolean hasTrailerSpecs() {
		return !this.trailerSpecs.isEmpty();
	}

	/**
	 * Gets the trailer {@linkplain FormatSpec}s hinting at this {@linkplain Format} instance.
	 *
	 * @return the trailer {@linkplain FormatSpec}s hinting at this {@linkplain Format} instance.
	 * @see #registerTrailerSpec(FormatSpec)
	 */
	public List<FormatSpec> trailerSpecs() {
		return Collections.unmodifiableList(this.trailerSpecs);
	}

	/**
	 * Checks whether this {@linkplain Format} instance has any input name {@linkplain Pattern}s defined.
	 *
	 * @return {@code true} if at least one input name {@linkplain Pattern} is associated with this {@linkplain Format}
	 *         instance.
	 * @see #registerInputNamePattern(Pattern)
	 */
	public boolean hasInputNamePatterns() {
		return !this.inputNamePatterns.isEmpty();
	}

	/**
	 * Gets the input name {@linkplain Pattern}s hinting at this {@linkplain Format} instance.
	 *
	 * @return the input name {@linkplain Pattern}s hinting at this {@linkplain Format} instance.
	 * @see #registerInputNamePattern(Pattern)
	 */
	public List<Pattern> inputNamePatterns() {
		return Collections.unmodifiableList(this.inputNamePatterns);
	}

	/**
	 * Decodes the format.
	 *
	 * @param decoder the {@linkplain FileScannerResultDecodeContext} to use for decoding.
	 * @return the decoded {@linkplain FileScannerResult} (may be of size 0).
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread is interrupted.
	 */
	public abstract FileScannerResult decode(FileScannerResultDecodeContext decoder)
			throws IOException, InterruptedException;

}
