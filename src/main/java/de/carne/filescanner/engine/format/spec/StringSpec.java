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
package de.carne.filescanner.engine.format.spec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.function.Supplier;

import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.StreamValueDecoder;
import de.carne.filescanner.engine.UnexpectedDataException;
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
		long decodeStart = context.position();
		CharsetDecoder decoder = charset().newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?");
		int chunkSize = (int) Math.ceil(decoder.maxCharsPerByte());
		int maxLength = MAX_LENGTH;

		return context.readValue(chunkSize, new StreamValueDecoder<String>() {

			private final CharBuffer stringBuffer = CharBuffer.allocate(maxLength);

			@Override
			public boolean stream(ByteBuffer buffer) throws IOException {
				if (this.stringBuffer.limit() < this.stringBuffer.capacity() || this.stringBuffer.hasRemaining()) {
					this.stringBuffer.limit(this.stringBuffer.position() + 1);
				} else {
					throw new UnexpectedDataException("Excessive string length", decodeStart);
				}

				CoderResult coderResult = decoder.decode(buffer, this.stringBuffer, false);

				if (coderResult.isError()) {
					throw new UnexpectedDataException("String decode failure", decodeStart);
				}

				int lastCharPosition = this.stringBuffer.position() - 1;
				boolean streamStatus = this.stringBuffer.get(lastCharPosition) != '\0';

				if (!streamStatus) {
					this.stringBuffer.position(lastCharPosition);
					this.stringBuffer.flip();
				}
				return streamStatus;
			}

			@Override
			public String decode() throws IOException {
				return this.stringBuffer.toString();
			}

		});
	}

}
