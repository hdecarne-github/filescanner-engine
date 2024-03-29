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
package de.carne.filescanner.engine.format;

import java.util.function.Supplier;

/**
 * Word attribute {@linkplain AttributeLinkResolver}.
 */
public class WordAttributeLinkResolver implements AttributeLinkResolver<Short> {

	private final Supplier<? extends Number> linkBase;

	/**
	 * Constructs a new {@linkplain WordAttributeLinkResolver} instance.
	 *
	 * @param linkBase the link base to use for resolving the link position.
	 */
	public WordAttributeLinkResolver(Supplier<? extends Number> linkBase) {
		this.linkBase = linkBase;
	}

	@Override
	public long resolve(Short value) {
		long linkBaseValue = this.linkBase.get().longValue();

		return linkBaseValue + Short.toUnsignedLong(value);
	}

}
