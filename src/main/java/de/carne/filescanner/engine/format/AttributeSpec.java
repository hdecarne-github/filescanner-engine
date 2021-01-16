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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.FileScannerResultContextValueSpec;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultInputContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.UnexpectedDataException;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.util.Strings;

/**
 * Base class for bind-able attribute format elements.
 *
 * @param <T> The actual attribute type.
 */
public abstract class AttributeSpec<T> extends FileScannerResultContextValueSpec<T> implements FormatSpec {

	private final BiPredicate<T, T> typeEquals;
	private AttributeFormatter<T> format = Object::toString;
	private final List<AttributeValidator<T>> validators = new ArrayList<>();
	private final List<AttributeRenderer<T>> renderers = new ArrayList<>();
	@Nullable
	private AttributeLinkResolver<T> link = null;
	private AttributeBindMode bindMode = AttributeBindMode.NONE;
	@Nullable
	private CompositeSpec bindScope = null;

	/**
	 * Constructs a new {@linkplain AttributeSpec} instance.
	 *
	 * @param type the attribute's type.
	 * @param typeEquals the attribute's equal function.
	 * @param name the attribute's name.
	 */
	protected AttributeSpec(Class<T> type, BiPredicate<T, T> typeEquals, Supplier<String> name) {
		super(type, name);
		this.typeEquals = typeEquals;
	}

	/**
	 * Constructs a new {@linkplain AttributeSpec} instance.
	 *
	 * @param type the attribute's type.
	 * @param typeEquals the attribute's equal function.
	 * @param name The attribute's name.
	 */
	protected AttributeSpec(Class<T> type, BiPredicate<T, T> typeEquals, String name) {
		this(type, typeEquals, FinalSupplier.of(name));
	}

	/**
	 * Sets the attribute format.
	 *
	 * @param formatter the {@linkplain AttributeFormatter} to set.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> format(AttributeFormatter<T> formatter) {
		this.format = formatter;
		return this;
	}

	/**
	 * Sets the attribute format based upon the {@linkplain String#format(String, Object...)} function.
	 *
	 * @param formatter the format string to use for formatting.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> format(String formatter) {
		AttributeFormatter<T> attributeFormatter;

		if (type().isArray()) {
			attributeFormatter = value -> {
				StringBuilder buffer = new StringBuilder();
				int length = Array.getLength(value);

				buffer.append("{ ");
				for (int index = 0; index < length; index++) {
					if (index > 0) {
						buffer.append(", ");
					}
					buffer.append(String.format(formatter, Array.get(value, index)));
				}
				buffer.append(length > 0 ? " }" : "}");
				return buffer.toString();
			};
		} else {
			attributeFormatter = value -> String.format(formatter, value);
		}
		return format(attributeFormatter);
	}

	/**
	 * Adds an attribute validator.
	 *
	 * @param validator the validator to add.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> validate(AttributeValidator<T> validator) {
		this.validators.add(validator);
		return this;
	}

	/**
	 * Adds an attribute validator for a fixed values.
	 *
	 * @param value the valid value.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> validate(@NonNull T value) {
		return validate(value2 -> this.typeEquals.test(value, value2));
	}

	/**
	 * Adds an attribute validator for a set of fixed values.
	 *
	 * @param values the valid values.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> validate(Set<T> values) {
		if (type().isArray()) {
			validate(value -> values.stream().anyMatch(value2 -> this.typeEquals.test(value, value2)));
		} else {
			validate(values::contains);
		}
		return this;
	}

	/**
	 * Adds an attribute renderer.
	 *
	 * @param renderer the renderer to add.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> renderer(AttributeRenderer<T> renderer) {
		this.renderers.add(renderer);
		return this;
	}

	/**
	 * Adds an attribute link.
	 *
	 * @param linkResolver the link resolver to use.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> link(AttributeLinkResolver<T> linkResolver) {
		this.link = linkResolver;
		return this;
	}

	/**
	 * Binds the attribute in {@linkplain AttributeBindMode#CONTEXT} mode.
	 *
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> bind() {
		this.bindMode = AttributeBindMode.CONTEXT;
		this.bindScope = null;
		return this;
	}

	/**
	 * Binds the attribute in {@linkplain AttributeBindMode#RESULT} mode.
	 *
	 * @param scope the bind scope to use (must be a result spec).
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> bind(CompositeSpec scope) {
		if (!scope.isResult()) {
			throw new IllegalArgumentException("Scope format spec must be a result spec");
		}
		this.bindMode = AttributeBindMode.RESULT;
		this.bindScope = scope;
		return this;
	}

	/**
	 * Decodes the attribute value.
	 *
	 * @param context the {@linkplain FileScannerResultInputContext} to decode the value from.
	 * @return the decoded value.
	 * @throws IOException if an I/O error occurs.
	 */
	protected abstract @NonNull T decodeValue(FileScannerResultInputContext context) throws IOException;

	/**
	 * Re-decodes the attribute value during rendering.
	 *
	 * @param context the {@linkplain FileScannerResultInputContext} to decode the value from.
	 * @return the decoded value.
	 * @throws IOException if an I/O error occurs.
	 */
	protected @NonNull T redecodeValue(FileScannerResultRenderContext context) throws IOException {
		return decodeValue(context);
	}

	/**
	 * Validates the attribute value against any defined validator.
	 *
	 * @param value the attribute value to validate.
	 * @return {@code true} if any defined validator accepted the attribute value.
	 */
	protected boolean validateValue(T value) {
		return this.validators.stream().allMatch(validator -> validator.validate(value));
	}

	@Override
	public void decode(FileScannerResultDecodeContext context) throws IOException {
		long decodeStart = context.position();
		T value = decodeValue(context);

		if (!validateValue(value)) {
			throw new UnexpectedDataException("Unexpected " + this, decodeStart, value);
		}
		switch (this.bindMode) {
		case NONE:
			break;
		case CONTEXT:
			context.bindContextValue(this, value);
			break;
		case RESULT:
			context.bindResultValue(Objects.requireNonNull(this.bindScope), this, value);
			break;
		}
	}

	@Override
	public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		T value = redecodeValue(context);

		switch (this.bindMode) {
		case NONE:
			break;
		case CONTEXT:
			context.bindContextValue(this, value);
			break;
		case RESULT:
			break;
		}

		String name = name();

		if (Strings.notEmpty(name)) {
			out.setStyle(RenderStyle.NORMAL).write(name);
			out.setStyle(RenderStyle.OPERATOR).write(" = ");
		}
		out.setStyle(RenderStyle.VALUE);

		String formattedValue = this.format.format(value);
		long linkPosition = (this.link != null ? this.link.resolve(value) : -1l);

		if (linkPosition >= 0) {
			out.write(formattedValue, linkPosition);
		} else {
			out.write(formattedValue);
		}
		for (AttributeRenderer<T> renderer : this.renderers) {
			renderer.render(out, value);
		}
		out.writeln();
	}

}
