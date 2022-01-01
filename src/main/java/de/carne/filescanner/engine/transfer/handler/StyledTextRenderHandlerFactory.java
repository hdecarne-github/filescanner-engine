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
package de.carne.filescanner.engine.transfer.handler;

import java.util.SortedMap;

import de.carne.filescanner.engine.spi.FileScannerResultRenderHandlerFactory;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;

/**
 * {@linkplain FileScannerResultRenderHandlerFactory} providing styled text based rendering.
 *
 * @see StyledTextRenderHandler
 */
public class StyledTextRenderHandlerFactory extends FileScannerResultRenderHandlerFactory {

	@Override
	protected void addHandlers(SortedMap<HandlerId, FileScannerResultRenderHandler> handlers) {
		addHandler(handlers, HandlerGroup.TEXT, "ASCII", StyledTextRenderHandler.PLAIN_ASCII_RENDER_HANDLER);
		addHandler(handlers, HandlerGroup.TEXT, "ISO-8859-1", StyledTextRenderHandler.PLAIN_ISO8859_RENDER_HANDLER);
		addHandler(handlers, HandlerGroup.TEXT, "UTF@1", "UTF-8", StyledTextRenderHandler.PLAIN_UTF8_RENDER_HANDLER);
		addHandler(handlers, HandlerGroup.TEXT, "UTF@2", "UTF-16LE",
				StyledTextRenderHandler.PLAIN_UTF16LE_RENDER_HANDLER);
		addHandler(handlers, HandlerGroup.TEXT, "UTF@3", "UTF-16BE",
				StyledTextRenderHandler.PLAIN_UTF16BE_RENDER_HANDLER);
		addHandler(handlers, HandlerGroup.TEXT, "XML", StyledTextRenderHandler.XML_UTF8_RENDER_HANDLER);
	}

}
