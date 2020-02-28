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
package de.carne.filescanner.provider.macho;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.util.Lazy;

/**
 * See MachO.formatspec
 */
final class MachOFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("MachO.formatspec"));
	}

	private Lazy<CompositeSpec> machoFormatSpec = resolveLazy("MACHO_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> machHeaderSpec = resolveLazy("MACH_HEADER_64", CompositeSpec.class);

	private Lazy<DWordSpec> cmdSize = resolveLazy("CMD_SIZE", DWordSpec.class);

	public CompositeSpec formatSpec() {
		return this.machoFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.machHeaderSpec.get();
	}

	public Long cmdSize() {
		return IntHelper.toUnsignedLong(this.cmdSize.get().get()) - 8;
	}

}
