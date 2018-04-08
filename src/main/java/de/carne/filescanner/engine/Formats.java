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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.carne.filescanner.engine.spi.Format;

/**
 * Helper class used to access the available {@linkplain Format} provider and configure the active ones for a scan.
 */
public final class Formats implements Iterable<Map.Entry<Format, Boolean>> {

	private final Map<Format, Boolean> formatStatus = new HashMap<>();

	private Formats() {
		Format.providers().forEach(f -> this.formatStatus.put(f, Boolean.TRUE));
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
		this.formatStatus.put(format, Boolean.TRUE);
		return this;
	}

	/**
	 * Disables a specific {@linkplain Format}.
	 *
	 * @param format the {@linkplain Format} to disable.
	 * @return the updated {@linkplain Formats} instance for chaining.
	 */
	public Formats disable(Format format) {
		this.formatStatus.put(format, Boolean.FALSE);
		return this;
	}

	/**
	 * Gets the currently enabled {@linkplain Format} instances.
	 *
	 * @return the currently enabled {@linkplain Format} instances.
	 */
	public Collection<Format> enabledFormats() {
		return this.formatStatus.entrySet().stream().filter(entry -> entry.getValue().booleanValue()).map(Entry::getKey)
				.collect(Collectors.toList());
	}

	@Override
	public Iterator<Entry<Format, Boolean>> iterator() {
		return Collections.unmodifiableMap(this.formatStatus).entrySet().iterator();
	}

}
