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
package de.carne.filescanner.provider.udif;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.StreamValue;
import de.carne.filescanner.engine.format.spec.AttributeSpecs;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.filescanner.engine.format.spec.FormatSpecs;
import de.carne.filescanner.engine.format.spec.RangeSpec;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderer;
import de.carne.filescanner.engine.transfer.StyledTextRenderer;
import de.carne.filescanner.engine.transfer.TransferType;
import de.carne.util.Lazy;

/**
 * See UDIF.formatspec
 */
final class UdifFormatSpecDefinition extends FormatSpecDefinition {

	private static final Log LOG = new Log();

	private static final StyledTextRenderer RESOURCE_FORK_RENDERER = new StyledTextRenderer(TransferType.TEXT_XML);

	private Map<StreamValue, CompositeSpec> dataForkSpecCache = new WeakHashMap<>();

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("UDIF.formatspec"));
	}

	private Lazy<CompositeSpec> udifFormatSpec = resolveLazy("UDIF_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> udifTrailerSpec = resolveLazy("UDIF_TRAILER", CompositeSpec.class);

	private Lazy<RangeSpec> resourceForkSpec = resolveLazy("XML_PLIST", RangeSpec.class);

	public CompositeSpec formatSpec() {
		return this.udifFormatSpec.get();
	}

	public CompositeSpec trailerSpec() {
		return this.udifTrailerSpec.get();
	}

	protected FileScannerResultRenderer resourceForkRenderer() {
		return RESOURCE_FORK_RENDERER;
	}

	protected Long imageDataSize() {
		return AttributeSpecs.INPUT_SIZE.get().longValue() - this.udifTrailerSpec.get().matchSize();
	}

	protected CompositeSpec dataForkSpec() {
		StreamValue resourceFork = this.resourceForkSpec.get().get();

		return this.dataForkSpecCache.computeIfAbsent(resourceFork, this::dataForkSpecHelper);
	}

	private CompositeSpec dataForkSpecHelper(StreamValue resourceFork) {
		CompositeSpec dataForkSpec = FormatSpecs.EMPTY;

		try (InputStream resourceForkInput = resourceFork.stream()) {
			dataForkSpec = ResourceForkHandler.parse(resourceForkInput);
		} catch (Exception e) {
			LOG.warning(e, "Failed to decode UDIF resource fork XML");
		}
		return dataForkSpec;
	}

}
