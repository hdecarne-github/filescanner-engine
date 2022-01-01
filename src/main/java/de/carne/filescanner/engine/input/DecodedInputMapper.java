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
package de.carne.filescanner.engine.input;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Defines at the end of an input decoding how the decoded input is mapped to actual decoding result.
 * <p>
 * The default implementation is simply an identity mapping. A derived class has to be used to define a more specific
 * mapping (e.g. for mapping a file system image to it's files).
 * </p>
 */
public class DecodedInputMapper {

	private final Supplier<String> nameHolder;

	/**
	 * Constructs a new {@linkplain DecodedInputMapper} instance.
	 *
	 * @param name the mapping name. This name is also used to name the decoded input.
	 */
	public DecodedInputMapper(String name) {
		this(FinalSupplier.of(name));
	}

	/**
	 * Constructs a new {@linkplain DecodedInputMapper} instance.
	 *
	 * @param name the mapping name. This name is also used to name the decoded input.
	 */
	public DecodedInputMapper(Supplier<String> name) {
		this.nameHolder = name;
	}

	/**
	 * Gets the mapping name.
	 *
	 * @return the mapping name.
	 */
	public final String name() {
		return this.nameHolder.get();
	}

	/**
	 * Maps the decoded input to the actual inputs defining the decoding result.
	 * <p>
	 * The default implementation performs an identity mapping.
	 * </p>
	 *
	 * @param input the decoded input to map.
	 * @return the resulting inputs.
	 * @throws IOException if the mapping fails.
	 */
	public List<FileScannerInput> map(FileScannerInput input) throws IOException {
		return Arrays.asList(input);
	}

}
