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
package de.carne.filescanner.provider.util;

import java.io.IOException;
import java.util.function.IntConsumer;

import de.carne.boot.Exceptions;
import de.carne.filescanner.engine.format.AttributeFormatter;
import de.carne.filescanner.engine.format.AttributeRenderer;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.util.Strings;
import de.carne.util.function.FunctionException;

/**
 * Utility class providing hexadecimal string related utility functions.
 */
public final class HexStrings {

	private HexStrings() {
		// Prevent instantiation
	}

	/**
	 * Parses an {@code int} value from an octal string.
	 *
	 * @param s the string to parse.
	 * @return the parsed value.
	 * @throws IOException if a parsing error occurs.
	 */
	public static int parseInt(String s) throws IOException {
		IntValueParser parser = new IntValueParser(s);

		try {
			s.chars().forEachOrdered(parser);
		} catch (FunctionException e) {
			throw e.rethrow(IOException.class);
		}
		return parser.value();
	}

	/**
	 * Parses an {@code int} value from an octal string.
	 * <p>
	 * In contrast to {@linkplain #parseInt(String)} this function does not fail on a parse error, but returns the
	 * result parsed so far.
	 * </p>
	 *
	 * @param s the string to parse.
	 * @return the parsed value.
	 */
	public static int safeParseInt(String s) {
		IntValueParser parser = new IntValueParser(s);

		try {
			s.chars().forEachOrdered(parser);
		} catch (FunctionException e) {
			Exceptions.ignore(e.rethrow(IOException.class));
		}
		return parser.value();
	}

	/**
	 * Creates an {@linkplain AttributeRenderer} for an octal string representing an {@code int} value.
	 * <p>
	 * The actual rendering is done by parsing the value and forwarding the render call to the submitted
	 * {@linkplain AttributeRenderer}.
	 * </p>
	 *
	 * @param renderer the {@linkplain AttributeRenderer} to use for actual value rendering.
	 * @return the created {@linkplain AttributeRenderer}.
	 */
	public static AttributeRenderer<String> intRenderer(AttributeRenderer<Integer> renderer) {
		return (out, value) -> renderer.render(out, safeParseInt(value));
	}

	/**
	 * Creates an {@linkplain AttributeRenderer} for an octal string representing an {@code int} value.
	 * <p>
	 * The actual rendering is done by parsing the value and forwarding the render call to the submitted
	 * {@linkplain AttributeFormatter}.
	 * </p>
	 *
	 * @param formatter the {@linkplain AttributeFormatter} to use for actual value formating.
	 * @return the created {@linkplain AttributeRenderer}.
	 */
	public static AttributeRenderer<String> intRenderer(AttributeFormatter<Integer> formatter) {
		return (out, value) -> out.setStyle(RenderStyle.COMMENT).write("// ")
				.write(formatter.format(safeParseInt(value)));
	}

	/**
	 * Parses a {@code long} value from an octal string.
	 *
	 * @param s the string to parse.
	 * @return the parsed value.
	 * @throws IOException if a parsing error occurs.
	 */
	public static long parseLong(String s) throws IOException {
		LongValueParser parser = new LongValueParser(s);

		try {
			s.chars().forEachOrdered(parser);
		} catch (FunctionException e) {
			throw e.rethrow(IOException.class);
		}
		return parser.value();
	}

	/**
	 * Parses a {@code long} value from an octal string.
	 * <p>
	 * In contrast to {@linkplain #parseInt(String)} this function does not fail on a parse error, but returns the
	 * result parsed so far.
	 * </p>
	 *
	 * @param s the string to parse.
	 * @return the parsed value.
	 */
	public static long safeParseLong(String s) {
		LongValueParser parser = new LongValueParser(s);

		try {
			s.chars().forEachOrdered(parser);
		} catch (FunctionException e) {
			Exceptions.ignore(e.rethrow(IOException.class));
		}
		return parser.value();
	}

	/**
	 * Creates an {@linkplain AttributeRenderer} for an octal string representing an {@code long} value.
	 * <p>
	 * The actual rendering is done by parsing the value and forwarding the render call to the submitted
	 * {@linkplain AttributeRenderer}.
	 * </p>
	 *
	 * @param renderer the {@linkplain AttributeRenderer} to use for actual value rendering.
	 * @return the created {@linkplain AttributeRenderer}.
	 */
	public static AttributeRenderer<String> longRenderer(AttributeRenderer<Long> renderer) {
		return (out, value) -> renderer.render(out, safeParseLong(value));
	}

	/**
	 * Creates an {@linkplain AttributeRenderer} for an octal string representing an {@code long} value.
	 * <p>
	 * The actual rendering is done by parsing the value and forwarding the render call to the submitted
	 * {@linkplain AttributeFormatter}.
	 * </p>
	 *
	 * @param formatter the {@linkplain AttributeFormatter} to use for actual value formating.
	 * @return the created {@linkplain AttributeRenderer}.
	 */
	public static AttributeRenderer<String> longRenderer(AttributeFormatter<Long> formatter) {
		return (out, value) -> out.setStyle(RenderStyle.COMMENT).write("// ")
				.write(formatter.format(safeParseLong(value)));
	}

	private abstract static class ValueParser implements IntConsumer {

		protected final String s;
		private int pos = 0;
		private boolean eos = false;

		protected ValueParser(String s) {
			this.s = s;
		}

		@Override
		public void accept(int value) {
			if (value == 0) {
				this.eos = true;
			} else if (this.eos) {
				throw new FunctionException(new IOException(
						"Unexpected trailing char: \"" + Strings.encode(this.s) + "\"[" + this.pos + "]"));
			} else if ('0' <= value && value <= '9') {
				shift(value - '0');
			} else if ('a' <= value && value <= 'f') {
				shift(10 + value - 'a');
			} else if ('A' <= value && value <= 'F') {
				shift(10 + value - 'A');
			} else {
				throw new FunctionException(
						new IOException("Unexpected octal char: \"" + Strings.encode(this.s) + "\"[" + this.pos + "]"));
			}
			this.pos++;
		}

		protected abstract void shift(int value);

	}

	private static class IntValueParser extends ValueParser {

		private int parsedValue = 0;

		IntValueParser(String s) {
			super(s);
		}

		public int value() {
			return this.parsedValue;
		}

		@Override
		protected void shift(int value) {
			if ((this.parsedValue & 0xf0000000) != 0) {
				throw new FunctionException(
						new IOException("Octal string exceeds integer limit: \"" + Strings.encode(this.s) + "\""));
			}
			this.parsedValue = (this.parsedValue << 4 | value);
		}

	}

	private static class LongValueParser extends ValueParser {

		private long parsedValue = 0;

		LongValueParser(String s) {
			super(s);
		}

		public long value() {
			return this.parsedValue;
		}

		@Override
		protected void shift(int value) {
			if ((this.parsedValue & 0xf000000000000000l) != 0) {
				throw new FunctionException(
						new IOException("Octal string exceeds long limit: \"" + Strings.encode(this.s) + "\""));
			}
			this.parsedValue = (this.parsedValue << 4 | value);
		}

	}

}
