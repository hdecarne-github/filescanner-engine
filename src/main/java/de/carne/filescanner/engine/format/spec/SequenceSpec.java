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

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Variable number of {@linkplain FormatSpec}s.
 */
public class SequenceSpec extends CompositeSpec {

	private final FormatSpec elementSpec;
	private @Nullable FormatSpec stopSpec = null;

	/**
	 * Constructs a new {@linkplain SequenceSpec} instance.
	 *
	 * @param elementSpec the sequence element's {@linkplain FormatSpec}.
	 */
	public SequenceSpec(FormatSpec elementSpec) {
		this.elementSpec = elementSpec;
	}

	/**
	 * Sets the last sequence element's {@linkplain FormatSpec}.
	 *
	 * @param stopAfterSpec the last sequence element's {@linkplain FormatSpec}.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec stopAfter(FormatSpec stopAfterSpec) {
		this.stopSpec = stopAfterSpec;
		return this;
	}

	@Override
	public boolean isFixedSize() {
		return false;
	}

	@Override
	public int matchSize() {
		return this.elementSpec.matchSize();
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		return this.elementSpec.matches(buffer);
	}

	@Override
	public void decodeComposite(FileScannerResultDecodeContext context) throws IOException {
		boolean done = false;

		while (!done) {
			FormatSpec checkedStopSpec = this.stopSpec;

			if (checkedStopSpec != null && context.matchFormat(checkedStopSpec)) {
				checkedStopSpec.decode(context);
				done = true;
			} else if (context.matchFormat(this.elementSpec)) {
				this.elementSpec.decode(context);
			} else {
				done = true;
			}
		}
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		while (context.matchFormat(this.elementSpec)) {
			this.elementSpec.render(out, context);
		}
	}

}
