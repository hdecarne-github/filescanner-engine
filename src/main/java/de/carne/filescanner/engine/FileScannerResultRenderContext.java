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
package de.carne.filescanner.engine;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.spec.AttributeBindMode;
import de.carne.filescanner.engine.format.spec.AttributeSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.TransferSource;

/**
 * Input data processor class used during result rendering and exporting.
 */
public class FileScannerResultRenderContext extends FileScannerResultInputContext {

	private static final Log LOG = new Log();

	private final Map<FileScannerResultContextValueSpec<?>, Object> contextValues = new HashMap<>();
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
		if (!inContext(getClass()) || !formatSpec.isResult()) {
			LOG.debug("Rendering format spec ''{0}''...", formatSpec);

			runV(() -> {
				ByteOrder previousByteOrder = byteOrder(formatSpec.byteOrder());

				try {
					formatSpec.renderComposite(out, this);
				} finally {
					byteOrder(previousByteOrder);
				}
			});
		}
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

		runV(() -> encodedInputSpec.inputDecoderTable().get().render(out));
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
	public <T> T getValue(FileScannerResultContextValueSpec<T> valueSpec) {
		Object value = this.contextValues.get(valueSpec);

		if (value == null) {
			value = this.result.getValue(valueSpec, true);
		}
		return Check.isInstanceOf(value, valueSpec.type());
	}

	/**
	 * Executes the given {@linkplain FileScannerResultExportHandler} instance.
	 *
	 * @param exportHandler the {@linkplain FileScannerResultExportHandler} instance to execute.
	 * @return the {@linkplain TransferSource} instance representing the exported data.
	 * @throws IOException if an I/O error occurs.
	 */
	public TransferSource export(FileScannerResultExportHandler exportHandler) throws IOException {
		LOG.debug("Executing export handler ''{0}''...", exportHandler);

		return runT(() -> exportHandler.export(this));
	}

}
