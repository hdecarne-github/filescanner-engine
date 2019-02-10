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
package de.carne.filescanner.provider.exe;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.WordSpec;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.filescanner.engine.util.ShortHelper;
import de.carne.util.Lazy;

/**
 * See EXE.formatspec
 */
final class ExeFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("EXE.formatspec"));
	}

	private Lazy<CompositeSpec> exeFormatSpec = resolveLazy("EXE_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> exeHeaderSpec = resolveLazy("IMAGE_DOS_HEADER", CompositeSpec.class);

	private Lazy<WordSpec> stubRelocationCount = resolveLazy("STUB_RELOCATION_COUNT", WordSpec.class);
	private Lazy<DWordSpec> stubNextHeaderOffset = resolveLazy("NEXT_HEADER_OFFSET", DWordSpec.class);

	public CompositeSpec formatSpec() {
		return this.exeFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.exeHeaderSpec.get();
	}

	protected Long stubTextSize() {
		int relocationCount = ShortHelper.toUnsignedInt(this.stubRelocationCount.get().get());
		long nextHeaderOffset = IntHelper.toUnsignedLong(this.stubNextHeaderOffset.get().get());

		return nextHeaderOffset - 0x40 - (relocationCount * 4);
	}

}
