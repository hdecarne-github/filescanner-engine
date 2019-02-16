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
package de.carne.filescanner.provider.tar;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.CharArraySpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.util.Lazy;

/**
 * See Tar.formatspec
 */
final class TarFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Tar.formatspec"));
	}

	private Lazy<CompositeSpec> tarFormatSpec = resolveLazy("TAR_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> tarHeaderSpec = resolveLazy("TAR_HEADER", CompositeSpec.class);

	private Lazy<CharArraySpec> headerName = resolveLazy("HEADER_NAME", CharArraySpec.class);

	public CompositeSpec formatSpec() {
		return this.tarFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.tarHeaderSpec.get();
	}

	protected String tarEntryName() {
		return "tar entry \"" + this.headerName.get().getStripped() + "\"";
	}

}
