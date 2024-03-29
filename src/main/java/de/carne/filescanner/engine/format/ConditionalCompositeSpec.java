/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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

import de.carne.filescanner.engine.FileScannerResultContextValueSpec;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Conditional {@linkplain CompositeSpec}.
 */
public class ConditionalCompositeSpec extends CompositeSpec {

	private final FileScannerResultContextValueSpec<CompositeSpec> decodedSpec = new FileScannerResultContextValueSpec<>(
			CompositeSpec.class, getClass().getSimpleName() + ".decodedSpec");
	private final Supplier<CompositeSpec> spec;

	/**
	 * Constructs a new {@linkplain ConditionalCompositeSpec} instance.
	 *
	 * @param spec the {@linkplain Supplier} instance used to resolve the actual {@linkplain FormatSpec}.
	 */
	public ConditionalCompositeSpec(Supplier<CompositeSpec> spec) {
		this.spec = spec;
	}

	@Override
	public boolean isFixedSize() {
		return false;
	}

	@Override
	public int matchSize() {
		return 0;
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return true;
	}

	@Override
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException {
		CompositeSpec decodedSpec0 = context.bindDecodedValue(this.decodedSpec, this.spec.get());

		decodedSpec0.decodeComposite(context);
		mergeDecodedExports(decodedSpec0);
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		context.getValue(this.decodedSpec).renderComposite(out, context);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append('(').append(getClass().getTypeName()).append(')');
		return buffer.toString();
	}

}
