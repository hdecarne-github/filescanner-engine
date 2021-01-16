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
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.StreamValueDecoder;
import de.carne.filescanner.engine.ValueStreamerStatus;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.util.SystemProperties;

/**
 * {@linkplain String} based format attribute specification.
 */
public class StringSpec extends StringAttributeSpec {

	private static final int MAX_LENGTH = SystemProperties.intValue(StringSpec.class, ".maxLength", Short.MAX_VALUE);

	/**
	 * Constructs a new {@linkplain StringSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public StringSpec(Supplier<String> name) {
		super(name);
	}

	/**
	 * Constructs a new {@linkplain StringSpec} instance.
	 *
	 * @param name the attribute's name.
	 */
	public StringSpec(String name) {
		this(FinalSupplier.of(name));
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
	protected String decodeValue(FileScannerResultInputContext context) throws IOException {
		CharsetDecoder decoder = charset().newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		int chunkSize = (int) Math.ceil(decoder.maxCharsPerByte());
		int maxLength = MAX_LENGTH;

		return context.readValue(chunkSize, new StreamValueDecoder<String>() {

			private final CharBuffer stringBuffer = CharBuffer.allocate(maxLength);

			@Override
			public ValueStreamerStatus stream(ByteBuffer buffer) {
				ValueStreamerStatus status;

				if (this.stringBuffer.hasRemaining() || this.stringBuffer.limit() < this.stringBuffer.capacity()) {
					this.stringBuffer.limit(this.stringBuffer.position() + 1);

					CoderResult coderResult = decoder.decode(buffer, this.stringBuffer, false);

					if (!coderResult.isError()) {
						int lastCharPosition = this.stringBuffer.position() - 1;

						if (this.stringBuffer.get(lastCharPosition) == '\0') {
							this.stringBuffer.position(lastCharPosition);
							this.stringBuffer.flip();
							status = ValueStreamerStatus.COMPLETE;
						} else {
							status = ValueStreamerStatus.STREAMING;
						}
					} else {
						status = ValueStreamerStatus.FAILED;
					}
				} else {
					status = ValueStreamerStatus.FAILED;
				}
				return status;
			}

			@Override
			public String decode() throws IOException {
				return this.stringBuffer.toString();
			}

		});
	}

}
