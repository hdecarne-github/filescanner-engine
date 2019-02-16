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
package de.carne.filescanner.provider.bzip2;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.AttributeSpecs;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.nio.file.FileUtil;
import de.carne.util.Lazy;

/**
 * See Bzip2.formatspec
 */
final class Bzip2FormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Bzip2.formatspec"));
	}

	private Lazy<CompositeSpec> bzip2FormatSpec = resolveLazy("BZIP2_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> bzip2HeaderSpec = resolveLazy("BZIP2_HEADER", CompositeSpec.class);

	public CompositeSpec formatSpec() {
		return this.bzip2FormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.bzip2HeaderSpec.get();
	}

	protected EncodedInputSpecConfig bzip2EncodedInputConfig() {
		return new EncodedInputSpecConfig("Compressed data").decodedInputName(this::decodedInputName)
				.inputDecoder(Bzip2InputDecoder.DECODER);
	}

	private static final Map<String, String> MANGLED_EXTENSION_MAP = new HashMap<>();

	static {
		MANGLED_EXTENSION_MAP.put("tbz2", ".tar");
	}

	private String decodedInputName() {
		String[] splitInputName = FileUtil.splitPath(AttributeSpecs.INPUT_NAME.get());

		return splitInputName[1] + MANGLED_EXTENSION_MAP.getOrDefault(splitInputName[2], "");
	}

}
