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
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Relocated {@linkplain FormatSpec}.
 */
public class DecodeAtSpec implements FormatSpec {

	private Supplier<? extends Number> position = FinalSupplier.of(Integer.valueOf(0));
	private int level = 0;
	private final CompositeSpec spec;

	/**
	 * Constructs a new {@linkplain DecodeAtSpec} instance.
	 *
	 * @param spec the relocated {@linkplain FormatSpec}.
	 */
	public DecodeAtSpec(CompositeSpec spec) {
		if (!spec.isResult()) {
			throw new IllegalArgumentException("Relocated spec is not a result spec: " + spec);
		}
		this.spec = spec;
	}

	/**
	 * Sets the position of this spec.
	 *
	 * @param positionSupplier the position of this spec.
	 * @return the updated {@linkplain DecodeAtSpec} instance for chaining.
	 */
	public DecodeAtSpec position(Supplier<? extends Number> positionSupplier) {
		this.position = positionSupplier;
		return this;
	}

	/**
	 * Sets the position of this spec.
	 *
	 * @param positionValue the position of this spec.
	 * @return the updated {@linkplain DecodeAtSpec} instance for chaining.
	 */
	public DecodeAtSpec position(long positionValue) {
		return position(FinalSupplier.of(positionValue));
	}

	/**
	 * Sets the level of this spec.
	 *
	 * @param levelValue the level of this spec.
	 * @return the updated {@linkplain DecodeAtSpec} instance for chaining.
	 */
	public DecodeAtSpec level(int levelValue) {
		if (levelValue < 0) {
			throw new IllegalArgumentException("Invalid level value: " + levelValue);
		}
		this.level = levelValue;
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return this.spec.isFixedSize();
	}

	@Override
	public int matchSize() {
		return this.spec.matchSize();
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return this.spec.matches(buffer);
	}

	@Override
	public void decode(FileScannerResultDecodeContext context) throws IOException {
		context.decodeComposite(this.spec, this.position.get().longValue(), this.level);
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		this.spec.render(out, context);
	}

	@Override
	public String toString() {
		return this.position + ": " + this.spec;
	}

}
