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

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.StreamValueDecoder;
import de.carne.filescanner.engine.ValueStreamer;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.SizeRenderer;

/**
 * Input data specification used for scan marker based specifications.
 */
public class ScanSpec extends CompositeSpec {

	private final ScanSpecConfig config;

	/**
	 * Constructs a new {@linkplain ScanSpec} instance.
	 *
	 * @param config the configuration to use.
	 */
	public ScanSpec(ScanSpecConfig config) {
		this.config = config;
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
		context.readValue(this.config.matchSize(), decoder(this.config::match));
	}

	@Override
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		super.renderComposite(out, context);
		if (!isResult() || out.isEmpty()) {
			long length;

			if (isResult()) {
				FileScannerResult result = context.result();

				length = result.end() - result.start();
			} else {
				length = context.readValue(this.config.matchSize(), decoder(this.config::match)).longValue();
			}
			context.skip(length);
			out.setStyle(RenderStyle.VALUE).write(length > 0 ? "{ ... }" : "{ }");
			SizeRenderer.renderLongSize(out, length);
			out.writeln();
		}
	}

	private static StreamValueDecoder<Long> decoder(ValueStreamer streamer) {
		return new StreamValueDecoder<Long>() {

			private long length = 0;

			@Override
			public boolean stream(ByteBuffer buffer) throws IOException {
				int decodeStart = buffer.position();
				boolean status = streamer.stream(buffer);

				this.length += buffer.position() - decodeStart;
				return status;
			}

			@Override
			public Long decode() throws IOException {
				return Long.valueOf(this.length);
			}

		};
	}

}
