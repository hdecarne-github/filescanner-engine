/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import de.carne.filescanner.engine.spi.Format;

/**
 * Helper class used to access the available {@linkplain Format} provider and configure the active ones for a scan.
 */
public final class Formats implements Iterable<Format> {

	private final Set<Format> allFormats = new HashSet<>();
	private final Set<String> disabledFormats = new HashSet<>();

	private Formats() {
		Format.providers().forEach(this.allFormats::add);
	}

	/**
	 * Gets a {@linkplain Formats} instance with all {@linkplain Format}s enabled.
	 *
	 * @return a {@linkplain Formats} instance with all {@linkplain Format}s enabled.
	 */
	public static Formats all() {
		return new Formats();
	}

	/**
	 * Enables a specific {@linkplain Format}.
	 *
	 * @param format the {@linkplain Format} to enable.
	 * @return the updated {@linkplain Formats} instance for chaining.
	 */
	public Formats enable(Format format) {
		return enable(format.name());
	}

	/**
	 * Enables a specific {@linkplain Format}.
	 *
	 * @param name the name of the {@linkplain Format} to enable.
	 * @return the updated {@linkplain Formats} instance for chaining.
	 */
	public Formats enable(String name) {
		this.disabledFormats.remove(name);
		return this;
	}

	/**
	 * Disables a specific {@linkplain Format}.
	 *
	 * @param format the {@linkplain Format} to disable.
	 * @return the updated {@linkplain Formats} instance for chaining.
	 */
	public Formats disable(Format format) {
		return disable(format.name());
	}

	/**
	 * Disables a specific {@linkplain Format}.
	 *
	 * @param name the name of the {@linkplain Format} to enable.
	 * @return the updated {@linkplain Formats} instance for chaining.
	 */
	public Formats disable(String name) {
		this.disabledFormats.add(name);
		return this;
	}

	/**
	 * Gets the currently enabled {@linkplain Format} instances.
	 *
	 * @return the currently enabled {@linkplain Format} instances.
	 */
	public Set<Format> enabledFormats() {
		return this.allFormats.stream().filter(format -> !this.disabledFormats.contains(format.name()))
				.collect(Collectors.toSet());
	}

	@Override
	public Iterator<Format> iterator() {
		return Collections.unmodifiableSet(this.allFormats).iterator();
	}

}
