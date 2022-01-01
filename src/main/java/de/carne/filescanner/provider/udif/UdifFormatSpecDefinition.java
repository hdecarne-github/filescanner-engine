/*
 * Copyright (c) 2007-2022 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.provider.udif;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.FileScannerResultContextValueSpecs;
import de.carne.filescanner.engine.StreamValue;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.RangeAttributeSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.StyledTextRenderHandler;
import de.carne.util.Lazy;
import de.carne.util.logging.Log;

/**
 * See UDIF.formatspec
 */
final class UdifFormatSpecDefinition extends FormatSpecDefinition {

	private static final Log LOG = new Log();

	private static final StyledTextRenderHandler RESOURCE_FORK_RENDERER = StyledTextRenderHandler.XML_UTF8_RENDER_HANDLER;

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("UDIF.formatspec"));
	}

	private Lazy<CompositeSpec> udifFormatSpec = resolveLazy("UDIF_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> udifTrailerSpec = resolveLazy("UDIF_TRAILER", CompositeSpec.class);

	private Lazy<RangeAttributeSpec> resourceForkSpec = resolveLazy("XML_PLIST", RangeAttributeSpec.class);

	public CompositeSpec formatSpec() {
		return this.udifFormatSpec.get();
	}

	public CompositeSpec trailerSpec() {
		return this.udifTrailerSpec.get();
	}

	public FileScannerResultRenderHandler resourceForkRenderer() {
		return RESOURCE_FORK_RENDERER;
	}

	public Long imageDataSize() {
		return FileScannerResultContextValueSpecs.INPUT_SIZE.get().longValue() - this.udifTrailerSpec.get().matchSize();
	}

	public CompositeSpec dataForkSpec() {
		CompositeSpec dataForkSpec = FormatSpecs.EMPTY;
		StreamValue resourceFork = this.resourceForkSpec.get().get();

		try (InputStream resourceForkInput = resourceFork.stream()) {
			dataForkSpec = ResourceForkHandler.parse(resourceForkInput);
		} catch (Exception e) {
			LOG.warning(e, "Failed to decode UDIF resource fork XML");
		}
		return dataForkSpec;
	}

}
