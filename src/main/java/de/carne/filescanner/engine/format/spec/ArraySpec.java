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
package de.carne.filescanner.engine.format.spec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.format.PrettyFormat;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Fixed number of {@linkplain FormatSpec}s.
 * <p>
 * The array length has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class ArraySpec extends CompositeSpec {

	private final Supplier<? extends Number> size;
	private final List<AttributeSpec<?>> elements = new ArrayList<>();
	private int cachedFixedSize = -1;
	private int cachedMatchSize = -1;

	/**
	 * Constructs a new {@linkplain ArraySpec} instance.
	 *
	 * @param size the array size.
	 */
	public ArraySpec(Supplier<? extends Number> size) {
		this.size = size;
	}

	/**
	 * Constructs a new {@linkplain ArraySpec} instance.
	 *
	 * @param size the array size.
	 */
	public ArraySpec(int size) {
		this(FinalSupplier.of(size));
	}

	/**
	 * Adds an attribute element.
	 *
	 * @param <T> the actual element element type.
	 * @param element the {@linkplain AttributeSpec} to add.
	 * @return the added {@linkplain AttributeSpec}.
	 */
	public <T extends AttributeSpec<?>> T add(T element) {
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
		this.cachedFixedSize = 0;
		if (this.size instanceof FinalSupplier) {
			this.cachedFixedSize = 1;
			for (AttributeSpec<?> element : this.elements) {
				if (!element.isFixedSize()) {
					this.cachedFixedSize = 0;
					break;
				}
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
		if (this.size instanceof FinalSupplier) {
			int matchElementCount = this.size.get().intValue();

			for (AttributeSpec<?> element : this.elements) {
				this.cachedMatchSize += element.matchSize();
				if (!element.isFixedSize()) {
					matchElementCount = Math.min(matchElementCount, 1);
					break;
				}
			}
			this.cachedMatchSize *= matchElementCount;
		}
		return this.cachedMatchSize;
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		boolean match = true;

		if (this.size instanceof FinalSupplier) {
			int totalMatchSize = 0;

			while (match && totalMatchSize < this.cachedMatchSize) {
				for (AttributeSpec<?> element : this.elements) {
					match = element.matches(buffer);
					if (!match || !element.isFixedSize()) {
						break;
					}
					totalMatchSize += element.matchSize();
				}
			}
		}
		return match;
	}

	@Override
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException {
		int elementCount = this.size.get().intValue();

		for (int elementIndex = 0; elementIndex < elementCount; elementIndex++) {
			for (AttributeSpec<?> element : this.elements) {
				element.decode(context);
			}
		}
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		super.renderComposite(out, context);
		if (!isResult() || out.isEmpty()) {
			int elementCount = this.size.get().intValue();

			for (int elementIndex = 0; elementIndex < elementCount; elementIndex++) {
				String arrayLabel = formatArrayLabel(elementIndex);

				for (AttributeSpec<?> element : this.elements) {
					out.setStyle(RenderStyle.LABEL);
					out.write(arrayLabel);
					element.render(out, context);
				}
			}
		}
	}

	private String formatArrayLabel(int elementIndex) {
		StringBuilder buffer = new StringBuilder();

		buffer.append('[').append(PrettyFormat.formatIntNumber(elementIndex)).append("]: ");
		return buffer.toString();
	}

}
