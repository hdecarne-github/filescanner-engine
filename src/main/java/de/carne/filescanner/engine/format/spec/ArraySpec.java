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
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.util.Strings;

/**
 * Fixed number of {@linkplain FormatSpec}s.
 * <p>
 * The array length has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class ArraySpec extends CompositeSpec {

	private final Supplier<String> elementName;
	private final FormatSpec elementSpec;
	private boolean fixedSize = true;
	private Supplier<? extends Number> size = FinalSupplier.of(Integer.valueOf(0));

	/**
	 * Constructs a new {@linkplain ArraySpec} instance.
	 *
	 * @param elementName the array element's name.
	 * @param elementSpec the array element's {@linkplain FormatSpec}.
	 */
	public ArraySpec(Supplier<String> elementName, FormatSpec elementSpec) {
		this.elementName = elementName;
		this.elementSpec = elementSpec;
	}

	/**
	 * Constructs a new {@linkplain ArraySpec} instance.
	 *
	 * @param elementName the array element's name.
	 * @param elementSpec the array element's {@linkplain FormatSpec}.
	 */
	public ArraySpec(String elementName, FormatSpec elementSpec) {
		this(FinalSupplier.of(elementName), elementSpec);
	}

	/**
	 * Sets the size (in number of elements) of this array.
	 *
	 * @param sizeSupplier the size (in number of elements) of this array.
	 * @return the updated {@linkplain CharArraySpec} instance for chaining.
	 */
	public ArraySpec size(Supplier<? extends Number> sizeSupplier) {
		this.fixedSize = false;
		this.size = sizeSupplier;
		return this;
	}

	/**
	 * Sets the size (in number of elements) of this array.
	 *
	 * @param sizeValue the size (in number of elements) of this array.
	 * @return the updated {@linkplain CharArraySpec} instance for chaining.
	 */
	public ArraySpec size(int sizeValue) {
		this.fixedSize = true;
		this.size = Integer.valueOf(sizeValue)::intValue;
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return this.fixedSize && this.elementSpec.isFixedSize();
	}

	@Override
	public int matchSize() {
		return matchElementCount() * this.elementSpec.matchSize();
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		int matchElementCount = matchElementCount();
		boolean match = true;

		for (int elementIndex = 0; elementIndex < matchElementCount; elementIndex++) {
			match = this.elementSpec.matches(buffer);
			if (!match) {
				break;
			}
		}
		return match;
	}

	private int matchElementCount() {
		int matchElementCount;

		if (!this.fixedSize) {
			matchElementCount = 0;
		} else if (!this.elementSpec.isFixedSize()) {
			matchElementCount = 1;
		} else {
			matchElementCount = this.size.get().intValue();
		}
		return matchElementCount;
	}

	@Override
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException {
		int elementCount = this.size.get().intValue();

		for (int elementIndex = 0; elementIndex < elementCount; elementIndex++) {
			this.elementSpec.decode(context);
		}
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		int elementCount = this.size.get().intValue();

		for (int elementIndex = 0; elementIndex < elementCount; elementIndex++) {
			String actualElementName = this.elementName.get();

			if (Strings.notEmpty(actualElementName)) {
				out.setStyle(RenderStyle.NORMAL).write(String.format(actualElementName, elementIndex));
				out.setStyle(RenderStyle.OPERATOR).write(" = ");
			}
			this.elementSpec.render(out, context);
		}
	}

}
