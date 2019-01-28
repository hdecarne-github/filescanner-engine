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
package de.carne.filescanner.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.spec.AttributeBindMode;
import de.carne.filescanner.engine.format.spec.AttributeSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpec;
import de.carne.filescanner.engine.transfer.ExportTarget;
import de.carne.filescanner.engine.transfer.FileScannerResultExporter;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Input data processor class used during result rendering and exporting.
 */
public class FileScannerResultRenderContext extends FileScannerResultInputContext {

	private static final Log LOG = new Log();

	private final Map<Object, Object> contextValues = new HashMap<>();
	private final FileScannerResultBuilder result;

	FileScannerResultRenderContext(FileScannerResultBuilder result) throws IOException {
		super(result.input().range(result.start(), result.end()), result.start());
		this.result = result;
	}

	/**
	 * Gets the {@linkplain FileScannerResult} processed by this context.
	 *
	 * @return the {@linkplain FileScannerResult} processed by this context.
	 */
	public FileScannerResult result() {
		return this.result;
	}

	/**
	 * Renders a {@linkplain CompositeSpec}.
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @param formatSpec a {@linkplain CompositeSpec} to render.
	 * @throws IOException if an I/O error occurs.
	 */
	public void render(RenderOutput out, CompositeSpec formatSpec) throws IOException {
		LOG.debug("Rendering format spec ''{0}''...", formatSpec);

		byteOrder(formatSpec.byteOrder());
		run(() -> formatSpec.renderComposite(out, this));
	}

	/**
	 * Renders a {@linkplain EncodedInputSpec}.
	 *
	 * @param out the {@linkplain RenderOutput} to render to.
	 * @param encodedInputSpec a {@linkplain EncodedInputSpec} to render.
	 * @throws IOException if an I/O error occurs.
	 */
	public void render(RenderOutput out, EncodedInputSpec encodedInputSpec) throws IOException {
		LOG.debug("Rendering encoded input spec ''{0}''...", encodedInputSpec);

		run(() -> encodedInputSpec.inputDecoder().get().render(out));
	}

	/**
	 * Binds an attribute value in {@linkplain AttributeBindMode#CONTEXT} mode.
	 *
	 * @param <T> the actual attribute type.
	 * @param attribute the attribute to bind.
	 * @param value the attribute value to bind.
	 */
	public <T> void bindContextValue(AttributeSpec<T> attribute, @NonNull T value) {
		this.contextValues.put(attribute, value);
	}

	@Override
	public <T> T getValue(AttributeSpec<T> attributeSpec) {
		Object value = this.contextValues.get(attributeSpec);

		if (value == null) {
			value = this.result.getValue(attributeSpec, true);
		}
		return Check.isInstanceOf(value, attributeSpec.type());
	}

	/**
	 * Executes the given {@linkplain FileScannerResultExporter} instance.
	 *
	 * @param target the {@linkplain ExportTarget} to export to.
	 * @param exporter the {@linkplain FileScannerResultExporter} instance to execute.
	 * @throws IOException if an I/O error occurs.
	 */
	public void export(ExportTarget target, FileScannerResultExporter exporter) throws IOException {
		LOG.debug("Executing exporter ''{0}''...", exporter);

		run(() -> exporter.export(target, this));
	}

}
