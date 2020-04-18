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
import java.util.Objects;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.StreamValue;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.filescanner.engine.util.LongHelper;
import de.carne.filescanner.engine.util.SizeRenderer;

/**
 * {@linkplain FormatSpec} defining a generic named byte range.
 * <p>
 * The range size has to be static or has to be defined via a bound attribute of type {@linkplain Number}.
 */
public class RangeAttributeSpec extends AttributeSpec<StreamValue> {

	private Supplier<? extends Number> size = FinalSupplier.of(Integer.valueOf(0));

	/**
	 * Constructs a new {@linkplain RangeAttributeSpec} instance.
	 *
	 * @param name the byte range's name.
	 */
	public RangeAttributeSpec(Supplier<String> name) {
		super(StreamValue.class, Objects::equals, name);
		renderer(this::sizeRenderer);
	}

	/**
	 * Constructs a new {@linkplain RangeAttributeSpec} instance.
	 *
	 * @param name the byte range's name.
	 */
	public RangeAttributeSpec(String name) {
		this(FinalSupplier.of(name));
	}

	/**
	 * Sets the size (in bytes) of this byte range.
	 *
	 * @param sizeSupplier the size (in bytes) of this byte range.
	 * @return the updated {@linkplain RangeAttributeSpec} instance for chaining.
	 */
	public RangeAttributeSpec size(Supplier<? extends Number> sizeSupplier) {
		this.size = sizeSupplier;
		return this;
	}

	/**
	 * Sets the size (in bytes) of this byte range.
	 *
	 * @param sizeValue the size (in bytes) of this byte range.
	 * @return the updated {@linkplain RangeAttributeSpec} instance for chaining.
	 */
	public RangeAttributeSpec size(int sizeValue) {
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
		boolean match = false;

		if (isFixedSize()) {
			int sizeValue = this.size.get().intValue();

			if (sizeValue <= buffer.remaining()) {
				buffer.position(buffer.position() + sizeValue);
				match = true;
			}
		} else {
			match = true;
		}
		return match;
	}

	@Override
	protected StreamValue decodeValue(FileScannerResultInputContext context) throws IOException {
		long sizeValue = LongHelper.toUnsignedLong(this.size.get());

		return context.streamValue(sizeValue);
	}

	private void sizeRenderer(RenderOutput out, StreamValue value) throws IOException {
		SizeRenderer.LONG_RENDERER.render(out, value.size());
	}

}
