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

import de.carne.filescanner.engine.format.spec.DWordSpec;
import de.carne.filescanner.engine.format.spec.QWordSpec;
import de.carne.filescanner.engine.format.spec.StringSpec;

/**
 * Utility class providing {@linkplain FileScannerResultContextValueSpec} related functions.
 */
public final class FileScannerResultContextValueSpecs {

	private FileScannerResultContextValueSpecs() {
		// Prevent instantiation
	}

	/**
	 * Predefined attribute to access the current input's name.
	 */
	public static final StringSpec INPUT_NAME = new StringSpec("#inputName");

	/**
	 * Predefined attribute to access the current input's size.
	 */
	public static final QWordSpec INPUT_SIZE = new QWordSpec("#inputSize");

	/**
	 * Predefined attribute to access the current sequence's element index.
	 */
	public static final DWordSpec SEQUENCE_ELEMENT_INDEX = new DWordSpec("#sequenceElementIndex");

}
