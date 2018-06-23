/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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

import de.carne.boot.check.Check;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.UnexpectedDataException;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Variable number of {@linkplain FormatSpec}s.
 */
public class VarArraySpec extends CompositeSpec {

	private final FormatSpec elementSpec;
	private final int minOccurrence;
	private final int maxOccurrence;

	/**
	 * Constructs a new {@linkplain VarArraySpec} instance.
	 *
	 * @param elementSpec the array element's {@linkplain FormatSpec}.
	 */
	public VarArraySpec(FormatSpec elementSpec) {
		this(elementSpec, 0, Integer.MAX_VALUE);
	}

	/**
	 * Constructs a new {@linkplain VarArraySpec} instance.
	 *
	 * @param elementSpec the array element's {@linkplain FormatSpec}.
	 * @param minOccurrence the minimum number of array elements.
	 */
	public VarArraySpec(FormatSpec elementSpec, int minOccurrence) {
		this(elementSpec, minOccurrence, Integer.MAX_VALUE);
	}

	/**
	 * Constructs a new {@linkplain VarArraySpec} instance.
	 *
	 * @param elementSpec the array element's {@linkplain FormatSpec}.
	 * @param minOccurrence the minimum number of array elements.
	 * @param maxOccurrence the maximum number of array elements.
	 */
	public VarArraySpec(FormatSpec elementSpec, int minOccurrence, int maxOccurrence) {
		Check.assertTrue(0 <= minOccurrence);
		Check.assertTrue(minOccurrence <= maxOccurrence);

		this.elementSpec = elementSpec;
		this.minOccurrence = minOccurrence;
		this.maxOccurrence = maxOccurrence;
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
		int occurrenceCount = 0;

		while (occurrenceCount <= this.maxOccurrence && context.matchFormat(this.elementSpec)) {
			this.elementSpec.decode(context);
			occurrenceCount++;
		}
		if (occurrenceCount < this.minOccurrence) {
			throw new UnexpectedDataException();
		}
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		while (context.matchFormat(this.elementSpec)) {
			this.elementSpec.render(out, context);
		}
	}

}
