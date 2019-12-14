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
package de.carne.filescanner.engine.transfer;

/**
 * The possible data transfer data types.
 */
public enum TransferType {

	/**
	 * Raw binary data.
	 */
	APPLICATION_OCTET_STREAM("application/octet-stream"),

	/**
	 * Windows Bitmap image data.
	 */
	IMAGE_BMP("image/bmp"),

	/**
	 * Gif image data.
	 */
	IMAGE_GIF("image/gif"),

	/**
	 * JPEG image data.
	 */
	IMAGE_JPEG("image/jpeg"),

	/**
	 * PNG image data.
	 */
	IMAGE_PNG("image/png"),

	/**
	 * TIFF image data.
	 */
	IMAGE_TIFF("image/tiff"),

	/**
	 * Plain text data.
	 */
	TEXT_PLAIN("text/plain"),

	/**
	 * XML text data.
	 */
	TEXT_XML("text/xml"),

	/**
	 * PDF data.
	 */
	APPLICATION_PDF("application/pdf");

	private final String mimeType;

	private TransferType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Gets the MIME type corresponding to this transfer data type.
	 *
	 * @return the MIME type corresponding to this transfer data type.
	 */
	public String mimeType() {
		return this.mimeType;
	}

	public boolean isText() {
		return this.mimeType.startsWith("text/");
	}

	public boolean isImage() {
		return this.mimeType.startsWith("image/");
	}

}
