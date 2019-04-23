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

import de.carne.boot.check.Check;
import de.carne.filescanner.engine.FileScannerResultContextValueSpec;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.UnexpectedDataException;
import de.carne.filescanner.engine.format.PrettyFormat;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Variable number of {@linkplain FormatSpec}s.
 */
public class SequenceSpec extends CompositeSpec {

	private final FileScannerResultContextValueSpec<Integer> elementCount = new FileScannerResultContextValueSpec<>(
			Integer.class, SequenceSpec.class.getSimpleName() + "#elementCount");
	private final FormatSpec elementSpec;
	private @Nullable FormatSpec stopBeforeSpec = null;
	private @Nullable FormatSpec stopAfterSpec = null;
	private @Nullable Supplier<? extends Number> minSize = null;
	private @Nullable Supplier<? extends Number> maxSize = null;

	/**
	 * Constructs a new {@linkplain SequenceSpec} instance.
	 *
	 * @param elementSpec the sequence element's {@linkplain FormatSpec}.
	 */
	public SequenceSpec(FormatSpec elementSpec) {
		this.elementSpec = elementSpec;
	}

	/**
	 * Sets the {@linkplain FormatSpec} of the last sequence element.
	 *
	 * @param stopSpec the {@linkplain FormatSpec} to stop after.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec stopBefore(FormatSpec stopSpec) {
		this.stopBeforeSpec = stopSpec;
		return this;
	}

	/**
	 * Sets the {@linkplain FormatSpec} following the sequence.
	 *
	 * @param stopSpec the {@linkplain FormatSpec} to stop before.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec stopAfter(FormatSpec stopSpec) {
		this.stopAfterSpec = stopSpec;
		return this;
	}

	/**
	 * Sets the minimum number of sequence elements to expect.
	 *
	 * @param min the minimum number of sequence elements to expect.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec min(Supplier<? extends Number> min) {
		this.minSize = min;
		return this;
	}

	/**
	 * Sets the minimum number of sequence elements to expect.
	 *
	 * @param min the minimum number of sequence elements to expect.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec min(int min) {
		return min(FinalSupplier.of(min));
	}

	/**
	 * Sets the maximum number of sequence elements to expect.
	 *
	 * @param max the maximum number of sequence elements to expect.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec max(Supplier<? extends Number> max) {
		this.maxSize = max;
		return this;
	}

	/**
	 * Sets the maximum number of sequence elements to expect.
	 *
	 * @param max the maximum number of sequence elements to expect.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec miax(int max) {
		return max(FinalSupplier.of(max));
	}

	/**
	 * Sets the exact number of sequence elements to expect.
	 *
	 * @param size the exact number of sequence elements to expect.
	 * @return the updated {@linkplain SequenceSpec}.
	 */
	public SequenceSpec size(Supplier<? extends Number> size) {
		this.minSize = this.maxSize = size;
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
		long decodeStart = context.position();
		int matchCount = 0;
		int minMatchCount = (this.minSize != null ? this.minSize.get().intValue() : 0);
		int maxMatchCount = (this.maxSize != null ? this.maxSize.get().intValue() : Integer.MAX_VALUE);

		Check.assertTrue(minMatchCount >= 0);
		Check.assertTrue(maxMatchCount >= minMatchCount);

		boolean done = false;

		while (!done) {
			FormatSpec checkedStopBeforeSpec = this.stopBeforeSpec;
			FormatSpec checkedStopAfterSpec = this.stopAfterSpec;

			if (checkedStopBeforeSpec != null && context.matchFormat(checkedStopBeforeSpec)) {
				done = true;
			} else if (checkedStopAfterSpec != null && context.matchFormat(checkedStopAfterSpec)) {
				checkedStopAfterSpec.decode(context);
				done = true;
			} else if (context.matchFormat(this.elementSpec)) {
				this.elementSpec.decode(context);
				matchCount++;
				done = (matchCount >= maxMatchCount);
			} else {
				done = true;
			}
		}
		if (matchCount < minMatchCount) {
			throw new UnexpectedDataException("Insufficent sequence length", decodeStart);
		}
		context.bindDecodedValue(this.elementCount, matchCount);
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		super.renderComposite(out, context);
		if (!isResult() || out.isEmpty()) {
			int decodedElementCount = context.getValue(this.elementCount).intValue();

			for (int elementIndex = 0; elementIndex < decodedElementCount; elementIndex++) {
				out.setStyle(RenderStyle.LABEL);
				out.writeln(formatElementLabel(elementIndex));
				this.elementSpec.render(out, context);
			}
		}
	}

	private String formatElementLabel(int elementIndex) {
		StringBuilder buffer = new StringBuilder();

		buffer.append('[').append(PrettyFormat.formatIntNumber(elementIndex)).append("]:");
		return buffer.toString();
	}

}
