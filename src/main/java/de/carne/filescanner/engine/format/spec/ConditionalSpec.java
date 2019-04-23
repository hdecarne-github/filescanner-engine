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

import org.eclipse.jdt.annotation.NonNull;

import de.carne.filescanner.engine.FileScannerResultContextValueSpec;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Conditional {@linkplain FormatSpec}.
 */
public class ConditionalSpec implements FormatSpec {

	private final FileScannerResultContextValueSpec<FormatSpec> resolvedSpec = new FileScannerResultContextValueSpec<>(
			FormatSpec.class, ConditionalSpec.class.getSimpleName() + "#resolvedSpec");
	private final Supplier<FormatSpec> spec;

	/**
	 * Constructs a new {@linkplain ConditionalSpec} instance.
	 *
	 * @param spec the {@linkplain Supplier} instance used to resolve the actual {@linkplain FormatSpec}.
	 */
	public ConditionalSpec(Supplier<FormatSpec> spec) {
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
	public void decode(FileScannerResultDecodeContext context) throws IOException {
		context.bindDecodedValue(this.resolvedSpec, this.spec.get()).decode(context);
	}

	@Override
	public void render(@NonNull RenderOutput out, @NonNull FileScannerResultRenderContext context) throws IOException {
		context.getValue(this.resolvedSpec).render(out, context);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append('(').append(getClass().getTypeName()).append(')');
		return buffer.toString();
	}

}
