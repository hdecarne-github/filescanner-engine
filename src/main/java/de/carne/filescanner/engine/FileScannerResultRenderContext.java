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
package de.carne.filescanner.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.AttributeBindMode;
import de.carne.filescanner.engine.format.AttributeSpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultOutput;

/**
 * Input data processor base class used during result rendering.
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
	 * Renders a {@linkplain CompositeSpec}.
	 *
	 * @param out the {@linkplain FileScannerResultOutput} to render to.
	 * @param formatSpec a {@linkplain CompositeSpec} to render.
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread has been interrupted.
	 */
	public void render(FileScannerResultOutput out, CompositeSpec formatSpec) throws IOException, InterruptedException {
		LOG.debug("Rendering format spec ''{0}''...", formatSpec);

		run(() -> formatSpec.renderComposite(out, this), false);
	}

	/**
	 * Renders a {@linkplain EncodedInputSpec}.
	 *
	 * @param out the {@linkplain FileScannerResultOutput} to render to.
	 * @param encodedInputSpec a {@linkplain EncodedInputSpec} to render.
	 * @throws IOException if an I/O error occurs.
	 * @throws InterruptedException if the decode thread has been interrupted.
	 */
	public void render(FileScannerResultOutput out, EncodedInputSpec encodedInputSpec)
			throws IOException, InterruptedException {
		LOG.debug("Rendering encoded input spec ''{0}''...", encodedInputSpec);

		run(() -> encodedInputSpec.inputDecoder().get().render(out), false);
	}

	/**
	 * Binds an attribute value in {@linkplain AttributeBindMode#CONTEXT} mode.
	 *
	 * @param <T> the actual attribute type.
	 * @param attribute the attribute to bind.
	 * @param value the attribute value to bind.
	 */
	public <T> void bindContextValue(AttributeSpec<T> attribute, T value) {
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

}
