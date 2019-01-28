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
package de.carne.filescanner.provider.jpeg;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.spec.ByteSpec;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.FormatSpecDefinition;
import de.carne.util.Lazy;

/**
 * See JPEG.formatspec
 */
final class JpegFormatSpecDefinition extends FormatSpecDefinition {

	private Lazy<CompositeSpec> jpegFormatSpec = resolveLazy("JPEG_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> jpegStartMarkerSpec = resolveLazy("JPEG_START_MARKER", CompositeSpec.class);

	private Lazy<ByteSpec> xThumbnail = resolveLazy("X_THUMBNAIL", ByteSpec.class);
	private Lazy<ByteSpec> yThumbnail = resolveLazy("Y_THUMBNAIL", ByteSpec.class);

	public CompositeSpec getJpegFormatSpec() {
		return this.jpegFormatSpec.get();
	}

	public CompositeSpec getJpegStartMarkerSpec() {
		return this.jpegStartMarkerSpec.get();
	}

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("JPEG.formatspec"));
	}

	protected Integer thumbnailSize() {
		return Byte.toUnsignedInt(this.xThumbnail.get().get().byteValue())
				* Byte.toUnsignedInt(this.yThumbnail.get().get().byteValue());
	}

}
