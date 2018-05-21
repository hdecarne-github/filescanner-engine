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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.carne.boot.check.Check;
import de.carne.boot.check.Nullable;
import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultExportHandler;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.FileScannerResultRenderer;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.util.FinalSupplier;

/**
 * Base class for all result and scope defining format elements.
 */
public abstract class CompositeSpec implements FormatSpec {

	private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	private boolean result = false;
	private Supplier<String> resultName = FinalSupplier.of("<undefined>");
	@Nullable
	private FileScannerResultRenderer customRenderer = null;
	private List<Supplier<FileScannerResultExportHandler>> exportHandlers = new ArrayList<>();

	/**
	 * Sets this {@linkplain CompositeSpec}'s byte order.
	 *
	 * @param order the byte order to set.
	 * @return the updated {@linkplain CompositeSpec} for chaining.
	 */
	public CompositeSpec byteOrder(ByteOrder order) {
		this.byteOrder = order;
		return this;
	}

	/**
	 * Gets this {@linkplain CompositeSpec}'s byte order.
	 *
	 * @return this {@linkplain CompositeSpec}'s byte order.
	 */
	public ByteOrder byteOrder() {
		return this.byteOrder;
	}

	/**
	 * Marks this {@linkplain CompositeSpec} as a result spec.
	 *
	 * @param name the result name to use during decoding.
	 * @return the updated {@linkplain CompositeSpec} for chaining.
	 */
	public CompositeSpec result(Supplier<String> name) {
		this.result = true;
		this.resultName = name;
		return this;
	}

	/**
	 * Marks this {@linkplain CompositeSpec} as a result spec.
	 *
	 * @param name the result name to use during decoding.
	 * @return the updated {@linkplain CompositeSpec} for chaining.
	 */
	public CompositeSpec result(String name) {
		return result(FinalSupplier.of(name));
	}

	/**
	 * Sets a custom {@linkplain FileScannerResultRenderer} for rendering this spec.
	 * 
	 * @param renderer the {@linkplain FileScannerResultRenderer} to use for rendering this spec.
	 * @return the updated {@linkplain CompositeSpec} for chaining.
	 */
	public CompositeSpec render(FileScannerResultRenderer renderer) {
		this.customRenderer = renderer;
		return this;
	}

	/**
	 * Adds a {@linkplain FileScannerResultExportHandler} to this {@linkplain CompositeSpec}.
	 *
	 * @param exportHandler the {@linkplain FileScannerResultExportHandler} to add.
	 * @return the updated {@linkplain CompositeSpec} for chaining.
	 */
	public CompositeSpec export(Supplier<FileScannerResultExportHandler> exportHandler) {
		this.exportHandlers.add(exportHandler);
		return this;
	}

	/**
	 * Adds a {@linkplain FileScannerResultExportHandler} to this {@linkplain CompositeSpec}.
	 *
	 * @param exportHandler the {@linkplain FileScannerResultExportHandler} to add.
	 * @return the updated {@linkplain CompositeSpec} for chaining.
	 */
	public CompositeSpec export(FileScannerResultExportHandler exportHandler) {
		this.exportHandlers.add(FinalSupplier.of(exportHandler));
		return this;
	}

	/**
	 * Checks whether this instance is a result spec.
	 *
	 * @return {@code true} if this instance is a result spec.
	 * @see #result(Supplier)
	 * @see #result(String)
	 */
	public boolean isResult() {
		return this.result;
	}

	/**
	 * Gets this instance's result name.
	 *
	 * @return this instance's result name.
	 */
	public Supplier<String> resultName() {
		Check.assertTrue(this.result);
		return this.resultName;
	}

	/**
	 * Gets this instance's export handlers.
	 *
	 * @return this instance's export handlers.
	 */
	public List<Supplier<FileScannerResultExportHandler>> exportHandlers() {
		return Collections.unmodifiableList(this.exportHandlers);
	}

	@Override
	public final void decode(FileScannerResultDecodeContext context) throws IOException {
		context.decode(this);
	}

	/**
	 * Decodes this {@linkplain CompositeSpec} instance.
	 *
	 * @param context the {@linkplain FileScannerResultDecodeContext} instance to use for decoding.
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract void decodeComposite(FileScannerResultDecodeContext context) throws IOException;

	@Override
	public final void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		context.render(out, this);
	}

	/**
	 * Renders this {@linkplain CompositeSpec} instance.
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @param context the {@linkplain FileScannerResultRenderContext} instance to use for rendering.
	 * @throws IOException if an I/O error occurs.
	 */
	public void renderComposite(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
		FileScannerResultRenderer checkedCustomRenderer = this.customRenderer;

		if (checkedCustomRenderer != null) {
			checkedCustomRenderer.render(out, context);
		}
	}

}
