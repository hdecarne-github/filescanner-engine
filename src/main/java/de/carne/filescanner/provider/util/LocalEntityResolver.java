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
package de.carne.filescanner.provider.util;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jdt.annotation.Nullable;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * {@linkplain EntityResolver} implementation only allowing access to local entities.
 */
public class LocalEntityResolver implements EntityResolver {

	private static final Reader EMPTY_STRING_READER = new Reader() {

		@Override
		public int read(char @Nullable [] cbuf, int off, int len) {
			return -1;
		}

		@Override
		public void close() {
			// Nothing to do here
		}

	};

	private static final LocalEntityResolver INSTANCE = new LocalEntityResolver();

	private LocalEntityResolver() {
		// prevent instantiation
	}

	/**
	 * Gets a {@linkplain LocalEntityResolver} instance.
	 *
	 * @return a {@linkplain LocalEntityResolver} instance.
	 */
	public static LocalEntityResolver getInstance() {
		return INSTANCE;
	}

	@Override
	public InputSource resolveEntity(@Nullable String publicId, String systemId) throws SAXException, IOException {
		return new InputSource(EMPTY_STRING_READER);
	}

}
