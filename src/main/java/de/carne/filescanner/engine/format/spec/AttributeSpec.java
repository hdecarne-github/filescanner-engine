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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.FileScannerResultContext;
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
public abstract class AttributeSpec<T> implements FormatSpec, Supplier<T> {

	private final Class<T> type;
	private final Supplier<String> name;
	private AttributeFormatter<T> format = Object::toString;
	private final List<AttributeValidator<T>> validators = new ArrayList<>();
	private final List<AttributeRenderer<T>> renderers = new ArrayList<>();
	private AttributeBindMode bindMode = AttributeBindMode.NONE;
	@Nullable
	private CompositeSpec bindScope = null;

	/**
	 * Constructs a new {@linkplain AttributeSpec} instance.
	 *
	 * @param type the attribute's type.
	 * @param name the attribute's name.
	 */
	protected AttributeSpec(Class<T> type, Supplier<String> name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * Constructs a new {@linkplain AttributeSpec} instance.
	 *
	 * @param type the attribute's type.
	 * @param name The attribute's name.
	 */
	protected AttributeSpec(Class<T> type, String name) {
		this.type = type;
		this.name = FinalSupplier.of(name);
	}

	/**
	 * Gets the attribute's type.
	 *
	 * @return the attribute's type.
	 */
	public Class<T> type() {
		return this.type;
	}

	/**
	 * Gets the attribute's name.
	 *
	 * @return the attribute's name.
	 */
	public String name() {
		return this.name.get();
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
		return format(value -> String.format(formatter, value));
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
		return validate(value::equals);
	}

	/**
	 * Adds an attribute validator for a set of fixed values.
	 *
	 * @param values the valid values.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> validate(Set<T> values) {
		return validate(values::contains);
	}

	/**
	 * Adds an attribute validator for a set of fixed values.
	 *
	 * @param values the valid values.
	 * @return the updated {@linkplain AttributeSpec} instance for chaining.
	 */
	public AttributeSpec<T> validate(Map<T, ?> values) {
		return validate(values.keySet()::contains);
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
	protected abstract T decodeValue(FileScannerResultInputContext context) throws IOException;

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
		T value = decodeValue(context);

		if (!validateValue(value)) {
			throw new UnexpectedDataException(value);
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
		T value = decodeValue(context);

		switch (this.bindMode) {
		case NONE:
			break;
		case CONTEXT:
			context.bindContextValue(this, value);
			break;
		case RESULT:
			break;
		}

		String actualName = this.name.get();

		if (Strings.notEmpty(actualName)) {
			out.setStyle(RenderStyle.NORMAL).write(actualName);
			out.setStyle(RenderStyle.OPERATOR).write(" = ");
		}
		out.setStyle(RenderStyle.VALUE).write(this.format.format(value));
		for (AttributeRenderer<T> renderer : this.renderers) {
			renderer.render(out, value);
		}
		out.writeln();
	}

	@Override
	public T get() {
		return FileScannerResultContext.get().getValue(this);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append('(');
		formatType(buffer, this.type);
		buffer.append(")'");
		buffer.append(this.name);
		buffer.append("'");
		return buffer.toString();
	}

	private static void formatType(StringBuilder buffer, Class<?> type) {
		Class<?> componenentType = type.getComponentType();

		if (componenentType != null) {
			formatType(buffer, componenentType);
			buffer.append("[]");
		} else {
			buffer.append(type.getName());
		}
	}

}
