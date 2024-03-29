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
package de.carne.filescanner.provider.xar;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.zip.InflaterInputStream;

import de.carne.filescanner.engine.StreamValue;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.format.FormatSpecs;
import de.carne.filescanner.engine.format.QWordSpec;
import de.carne.filescanner.engine.format.RangeAttributeSpec;
import de.carne.filescanner.engine.format.WordSpec;
import de.carne.filescanner.engine.util.LongHelper;
import de.carne.filescanner.engine.util.ShortHelper;
import de.carne.util.Lazy;
import de.carne.util.logging.Log;

/**
 * See Xar.formatspec
 */
final class XarFormatSpecDefinition extends FormatSpecDefinition {

	private static final Log LOG = new Log();

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("Xar.formatspec"));
	}

	private Lazy<CompositeSpec> xarFormatSpec = resolveLazy("XAR_ARCHIVE", CompositeSpec.class);
	private Lazy<CompositeSpec> xarHeaderSpec = resolveLazy("XAR_HEADER", CompositeSpec.class);

	private Lazy<WordSpec> headerSize = resolveLazy("HEADER_SIZE", WordSpec.class);
	private Lazy<QWordSpec> tocLength = resolveLazy("TOC_LENGTH", QWordSpec.class);
	private Lazy<RangeAttributeSpec> tocXml = resolveLazy("TOC_XML", RangeAttributeSpec.class);

	public CompositeSpec formatSpec() {
		return this.xarFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.xarHeaderSpec.get();
	}

	public Integer tocCksumNameSize() {
		int headerSizeValue = ShortHelper.toUnsignedInt(this.headerSize.get().get());

		return Math.max(headerSizeValue - 28, 0);
	}

	public CompositeSpec heapSpec() {
		CompositeSpec heapSpec = FormatSpecs.EMPTY;
		StreamValue tocXmlValue = this.tocXml.get().get();
		long heapOffset = LongHelper.toUnsignedLong(this.headerSize.get().get())
				+ LongHelper.toUnsignedLong(this.tocLength.get().get());

		try (InputStream tocXmlInput = new InflaterInputStream(tocXmlValue.stream())) {
			heapSpec = TocHandler.parse(tocXmlInput, heapOffset);
		} catch (Exception e) {
			LOG.warning(e, "Failed to decode XAR TOC XML");
		}
		return heapSpec;
	}

}
