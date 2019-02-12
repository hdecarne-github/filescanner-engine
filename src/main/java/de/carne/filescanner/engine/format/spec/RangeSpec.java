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
import de.carne.filescanner.engine.util.LongHelper;
import de.carne.util.Strings;

/**
 * {@linkplain FormatSpec} defining a generic named byte range.
 * <p>
 * The range size has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class RangeSpec implements FormatSpec {

	private final Supplier<String> name;
	private Supplier<? extends Number> size = FinalSupplier.of(Integer.valueOf(0));

	/**
	 * Constructs a new {@linkplain RangeSpec} instance.
	 *
	 * @param name the byte range's name.
	 */
	public RangeSpec(Supplier<String> name) {
		this.name = name;
	}

	/**
	 * Constructs a new {@linkplain RangeSpec} instance.
	 *
	 * @param name the byte range's name.
	 */
	public RangeSpec(String name) {
		this.name = FinalSupplier.of(name);
	}

	/**
	 * Sets the size (in bytes) of this byte range.
	 *
	 * @param sizeSupplier the size (in bytes) of this byte range.
	 * @return the updated {@linkplain RangeSpec} instance for chaining.
	 */
	public RangeSpec size(Supplier<? extends Number> sizeSupplier) {
		this.size = sizeSupplier;
		return this;
	}

	/**
	 * Sets the size (in bytes) of this byte range.
	 *
	 * @param sizeValue the size (in bytes) of this byte range.
	 * @return the updated {@linkplain RangeSpec} instance for chaining.
	 */
	public RangeSpec size(int sizeValue) {
		this.size = FinalSupplier.of(sizeValue);
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return (this.size instanceof FinalSupplier);
	}

	@Override
	public int matchSize() {
		return (isFixedSize() ? this.size.get().intValue() : 0);
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return !isFixedSize() || this.size.get().intValue() <= buffer.remaining();
	}

	@Override
	public void decode(FileScannerResultDecodeContext context) throws IOException {
		context.skip(LongHelper.toUnsignedLong(this.size.get()));
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		long sizeValue = LongHelper.toUnsignedLong(this.size.get());

		context.skip(sizeValue);

		String nameValue = this.name.get();

		if (Strings.notEmpty(nameValue)) {
			out.setStyle(RenderStyle.NORMAL).write(nameValue);
			out.setStyle(RenderStyle.OPERATOR).write(" = ");
		}
		out.setStyle(RenderStyle.VALUE).write(sizeValue > 0 ? "{ ... }" : "{ }");
		SizeRenderer.renderLongSize(out, sizeValue);
		out.writeln();
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append("(byte[");
		buffer.append(this.size);
		buffer.append("])'");
		buffer.append(this.name);
		buffer.append("'");
		return buffer.toString();
	}

}
