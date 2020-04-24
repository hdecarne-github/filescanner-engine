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
package de.carne.filescanner.provider.png;

import java.net.URL;
import java.util.Objects;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DWordSpec;
import de.carne.filescanner.engine.format.FormatSpecDefinition;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.transfer.handler.RawTransferHandler;
import de.carne.util.Lazy;

/**
 * See PNG.formatspec
 */
final class PngFormatSpecDefinition extends FormatSpecDefinition {

	@Override
	protected URL getFormatSpecResource() {
		return Objects.requireNonNull(getClass().getResource("PNG.formatspec"));
	}

	private Lazy<CompositeSpec> pngFormatSpec = resolveLazy("PNG_FORMAT", CompositeSpec.class);
	private Lazy<CompositeSpec> pngHeaderSpec = resolveLazy("PNG_FILE_SIGNATURE", CompositeSpec.class);

	private Lazy<DWordSpec> pngChunkType = resolveLazy("CHUNK_TYPE", DWordSpec.class);

	public CompositeSpec formatSpec() {
		return this.pngFormatSpec.get();
	}

	public CompositeSpec headerSpec() {
		return this.pngHeaderSpec.get();
	}

	public FileScannerResultRenderHandler pngRenderer() {
		return RawTransferHandler.IMAGE_PNG_TRANSFER;
	}

	public FileScannerResultExportHandler pngExporter() {
		return RawTransferHandler.IMAGE_PNG_TRANSFER;
	}

	public String genericChunkName() {
		int typeValue = this.pngChunkType.get().get().intValue();
		StringBuilder typeString = new StringBuilder();

		typeString.append(mapChunkTypeChar((typeValue >>> 24) & 0xff));
		typeString.append(mapChunkTypeChar((typeValue >>> 16) & 0xff));
		typeString.append(mapChunkTypeChar((typeValue >>> 8) & 0xff));
		typeString.append(mapChunkTypeChar(typeValue & 0xff));
		return typeString.toString();
	}

	private char mapChunkTypeChar(int code) {
		return ((65 <= code && code <= 90) || (97 <= code && code <= 122) ? (char) code : '?');
	}

}
