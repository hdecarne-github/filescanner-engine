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

import de.carne.filescanner.engine.FileScannerResultContextValueSpec;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Union of {@linkplain CompositeSpec}s.
 */
public class UnionSpec extends CompositeSpec {

	private final FileScannerResultContextValueSpec<CompositeSpec> decodedSpec = new FileScannerResultContextValueSpec<>(
			CompositeSpec.class, getClass().getSimpleName() + ".decodedSpec");
	private final List<CompositeSpec> elements = new ArrayList<>();
	private boolean fixedSize = true;
	private int matchSize = 0;

	/**
	 * Adds a format element.
	 *
	 * @param <T> the actual format element type.
	 * @param element the {@linkplain FormatSpec} to add.
	 * @return the added {@linkplain FormatSpec}.
	 */
	public <T extends CompositeSpec> T add(T element) {
		this.elements.add(element);
		this.fixedSize = this.fixedSize && element.isFixedSize();
		this.matchSize = Math.max(this.matchSize, element.matchSize());
		return element;
	}

	@Override
	public boolean isFixedSize() {
		return this.fixedSize;
	}

	@Override
	public int matchSize() {
		return this.matchSize;
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		boolean match = true;

		for (CompositeSpec element : this.elements) {
			ByteBuffer matchBuffer = buffer.duplicate();

			matchBuffer.order(element.byteOrder());
			match = element.matches(matchBuffer);
			if (match) {
				break;
			}
		}
		return match;
	}

	@Override
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException {
		for (CompositeSpec element : this.elements) {
			if (context.matchComposite(element)) {
				element.decode(context);
				context.bindDecodedValue(this.decodedSpec, element);
				break;
			}
		}
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		super.renderComposite(out, context);

		CompositeSpec element = this.decodedSpec.get();

		if (!element.isResult()) {
			element.render(out, context);
		}
	}

}
