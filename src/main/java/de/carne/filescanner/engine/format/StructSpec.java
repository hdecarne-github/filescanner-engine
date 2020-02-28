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
package de.carne.filescanner.engine.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Struct of {@linkplain FormatSpec}s.
 */
public class StructSpec extends CompositeSpec {

	private final List<FormatSpec> elements = new ArrayList<>();
	private int cachedFixedSize = -1;
	private int cachedMatchSize = -1;

	/**
	 * Adds a format element.
	 *
	 * @param <T> the actual format element type.
	 * @param element the {@linkplain FormatSpec} to add.
	 * @return the added {@linkplain FormatSpec}.
	 */
	public <T extends FormatSpec> T add(T element) {
		this.elements.add(element);
		this.cachedFixedSize = -1;
		this.cachedMatchSize = -1;
		return element;
	}

	@Override
	public boolean isFixedSize() {
		return (this.cachedFixedSize >= 0 ? this.cachedFixedSize != 0 : isFixedSize0());
	}

	private synchronized boolean isFixedSize0() {
		this.cachedFixedSize = 1;
		for (FormatSpec element : this.elements) {
			if (!element.isFixedSize()) {
				this.cachedFixedSize = 0;
				break;
			}
		}
		return this.cachedFixedSize != 0;
	}

	@Override
	public int matchSize() {
		return (this.cachedMatchSize >= 0 ? this.cachedMatchSize : matchSize0());
	}

	private synchronized int matchSize0() {
		this.cachedMatchSize = 0;
		for (FormatSpec spec : this.elements) {
			this.cachedMatchSize += spec.matchSize();
			if (!spec.isFixedSize()) {
				break;
			}
		}
		return this.cachedMatchSize;
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		boolean match = true;

		for (FormatSpec element : this.elements) {
			match = element.matches(buffer);
			if (!match || !element.isFixedSize()) {
				break;
			}
		}
		return match;
	}

	@Override
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException {
		for (FormatSpec element : this.elements) {
			element.decode(context);
		}
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		super.renderComposite(out, context);
		if (!isResult() || out.isEmpty()) {
			for (FormatSpec element : this.elements) {
				element.render(out, context);
			}
		}
	}

}
