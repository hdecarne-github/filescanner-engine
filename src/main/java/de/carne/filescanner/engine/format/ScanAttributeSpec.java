/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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

import org.eclipse.jdt.annotation.NonNull;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.StreamValue;
import de.carne.filescanner.engine.StreamValueDecoder;
import de.carne.filescanner.engine.ValueStreamer;
import de.carne.filescanner.engine.ValueStreamerFactory;
import de.carne.filescanner.engine.ValueStreamerStatus;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.filescanner.engine.util.SizeRenderer;

/**
 * Input data specification used for scan marker based specifications.
 */
public class ScanAttributeSpec extends AttributeSpec<StreamValue> {

	private final ValueStreamerFactory valueStreamerFactory;
	private Supplier<? extends Number> limit = FinalSupplier.of(Integer.MAX_VALUE);
	private Supplier<? extends Number> step = FinalSupplier.of(1);

	/**
	 * Constructs a new {@linkplain ScanAttributeSpec} instance.
	 *
	 * @param name the attribute's name.
	 * @param valueStreamerFactory the {@linkplain ValueStreamerFactory} to use.
	 */
	public ScanAttributeSpec(Supplier<String> name, ValueStreamerFactory valueStreamerFactory) {
		super(StreamValue.class, Objects::equals, name);
		this.valueStreamerFactory = valueStreamerFactory;
		renderer(this::sizeRenderer);
	}

	/**
	 * Constructs a new {@linkplain ScanAttributeSpec} instance.
	 *
	 * @param name the attribute's name.
	 * @param valueStreamerFactory the {@linkplain ValueStreamerFactory} to use.
	 */
	public ScanAttributeSpec(String name, ValueStreamerFactory valueStreamerFactory) {
		this(FinalSupplier.of(name), valueStreamerFactory);
	}

	/**
	 * Sets the scan limit for this attribute.
	 *
	 * @param limitSupplier the scan limit to use.
	 * @return the updated {@linkplain ScanAttributeSpec} instance for chaining.
	 */
	public ScanAttributeSpec limit(Supplier<? extends Number> limitSupplier) {
		this.limit = limitSupplier;
		return this;
	}

	/**
	 * Sets the scan limit for this attribute.
	 *
	 * @param limitValue the scan limit to use.
	 * @return the updated {@linkplain ScanAttributeSpec} instance for chaining.
	 */
	public ScanAttributeSpec limit(int limitValue) {
		return limit(FinalSupplier.of(limitValue));
	}

	/**
	 * Sets the scan step size for this attribute.
	 *
	 * @param stepSupplier the scan step size to use.
	 * @return the updated {@linkplain ScanAttributeSpec} instance for chaining.
	 */
	public ScanAttributeSpec step(Supplier<? extends Number> stepSupplier) {
		this.step = stepSupplier;
		return this;
	}

	/**
	 * Sets the scan step size for this attribute.
	 *
	 * @param stepValue the scan step size to use.
	 * @return the updated {@linkplain ScanAttributeSpec} instance for chaining.
	 */
	public ScanAttributeSpec step(int stepValue) {
		return step(FinalSupplier.of(stepValue));
	}

	@Override
	public boolean isFixedSize() {
		return FormatSpecs.isFixedSize(this.limit);
	}

	@Override
	public int matchSize() {
		return (isFixedSize() ? this.limit.get().intValue() : 0);
	}

	@Override
	public boolean matches(ByteBuffer buffer) {
		boolean match = false;

		if (isFixedSize()) {
			ValueStreamer streamer = this.valueStreamerFactory.getInstance();
			ValueStreamerStatus status = ValueStreamerStatus.STREAMING;

			while (status == ValueStreamerStatus.STREAMING && buffer.hasRemaining()) {
				status = streamer.stream(buffer);
			}
			match = status == ValueStreamerStatus.COMPLETE;
		} else {
			match = true;
		}
		return match;
	}

	@Override
	protected StreamValue decodeValue(FileScannerResultInputContext context) throws IOException {
		ValueStreamer streamer = this.valueStreamerFactory.getInstance();
		long length = context.readValue(this.step.get().intValue(), decoder(streamer)).longValue();

		return context.streamValue(length, false);
	}

	@Override
	protected StreamValue redecodeValue(@NonNull FileScannerResultRenderContext context) throws IOException {
		return context.streamValue(context.result().size(), true);
	}

	private static StreamValueDecoder<Long> decoder(ValueStreamer streamer) {
		return new StreamValueDecoder<>() {

			private long length = 0;

			@Override
			public ValueStreamerStatus stream(ByteBuffer buffer) {
				int decodeStart = buffer.position();
				ValueStreamerStatus status = streamer.stream(buffer);

				this.length += buffer.position() - decodeStart;
				return status;
			}

			@Override
			public Long decode() throws IOException {
				return this.length;
			}

		};
	}

	private void sizeRenderer(RenderOutput out, StreamValue value) throws IOException {
		SizeRenderer.LONG_RENDERER.render(out, value.size());
	}

}
