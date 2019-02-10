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

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.UnexpectedDataException;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Variable number of {@linkplain FormatSpec}s.
 */
public class SequenceSpec extends CompositeSpec {

	private final FormatSpec elementSpec;
	private @Nullable FormatSpec stopSpec = null;
	private @Nullable Supplier<? extends Number> stopSize = null;

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

	/**
	 * Sets the exact number of sequence elements to expect.
	 *
	 * @param size the exact number of sequence elements to expect.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec size(Supplier<? extends Number> size) {
		this.stopSize = size;
		return this;
	}

	/**
	 * Sets the exact number of sequence elements to expect.
	 *
	 * @param size the exact number of sequence elements to expect.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec size(int size) {
		return size(FinalSupplier.of(size));
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
		int matchCount = 0;
		int matchLimit = (this.stopSize != null ? this.stopSize.get().intValue() : Integer.MAX_VALUE);
		boolean done = false;

		while (!done) {
			FormatSpec checkedStopSpec = this.stopSpec;

			if (checkedStopSpec != null && context.matchFormat(checkedStopSpec)) {
				checkedStopSpec.decode(context);
				done = true;
			} else if (context.matchFormat(this.elementSpec)) {
				this.elementSpec.decode(context);
				matchCount++;
				done = (matchCount >= matchLimit);
			} else {
				done = true;
			}
		}
		if (this.stopSize != null && matchCount < matchLimit) {
			throw new UnexpectedDataException();
		}
	}

}
