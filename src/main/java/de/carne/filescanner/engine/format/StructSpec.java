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
package de.carne.filescanner.engine.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.FileScannerResultOutput;

/**
 * Struct of {@linkplain FormatSpec}s.
 */
public class StructSpec extends CompositeSpec {

	private final List<FormatSpec> elements = new ArrayList<>();

	/**
	 * Adds a {@linkplain FormatSpec}.
	 *
	 * @param element the {@linkplain FormatSpec} to add.
	 * @return the added {@linkplain FormatSpec}.
	 */
	public <T extends FormatSpec> T add(T element) {
		this.elements.add(element);
		return element;
	}

	@Override
	public boolean isFixedSize() {
		boolean fixedSize = !this.elements.isEmpty();

		for (FormatSpec spec : this.elements) {
			fixedSize = spec.isFixedSize();
			if (!fixedSize) {
				break;
			}
		}
		return fixedSize;
	}

	@Override
	public int matchSize() {
		int matchSize = 0;

		for (FormatSpec spec : this.elements) {
			matchSize += spec.matchSize();
			if (!spec.isFixedSize()) {
				break;
			}
		}
		return matchSize;
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		boolean match = !this.elements.isEmpty();

		for (FormatSpec spec : this.elements) {
			match = spec.matches(buffer);
			if (!match || !spec.isFixedSize()) {
				break;
			}
		}
		return match;
	}

	@Override
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException, InterruptedException {
		for (FormatSpec element : this.elements) {
			element.decode(context);
		}
	}

	@Override
	public void render(FileScannerResultOutput out, FileScannerResultRenderContext context)
			throws IOException, InterruptedException {
		for (FormatSpec element : this.elements) {
			element.render(out, context);
		}
	}

}
