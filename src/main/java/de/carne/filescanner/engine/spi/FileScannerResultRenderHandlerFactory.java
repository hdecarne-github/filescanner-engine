/*
 * Copyright (c) 2007-2021 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.spi;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;

/**
 * Factory class for accessing {@linkplain FileScannerResultRenderHandler} instances via the {@linkplain ServiceLoader}
 * mechanism.
 */
public abstract class FileScannerResultRenderHandlerFactory {

	/**
	 * The supported handler groups.
	 */
	public enum HandlerGroup {

		/**
		 * Text based handlers.
		 */
		TEXT("Text"),

		/**
		 * Miscellaneous handlers not assignable to one of the specific groups.
		 */
		MISC("Misc");

		private final String displayName;

		private HandlerGroup(String displayName) {
			this.displayName = displayName;
		}

		/**
		 * Gets this group's display name.
		 *
		 * @return this group's display name.
		 */
		public String displayName() {
			return this.displayName;
		}

	}

	/**
	 * Comparable id object used to identify a handler via it's {@linkplain HandlerGroup} and name.
	 */
	public static final class HandlerId implements Comparable<HandlerId> {

		private final HandlerGroup group;
		private final String name;
		private final String displayName;

		HandlerId(HandlerGroup group, String name, String displayName) {
			this.group = group;
			this.name = name;
			this.displayName = displayName;
		}

		/**
		 * Gets this id's {@linkplain HandlerGroup}.
		 *
		 * @return this id's {@linkplain HandlerGroup}.
		 */
		public HandlerGroup group() {
			return this.group;
		}

		/**
		 * Gets this id's name.
		 *
		 * @return this id's name.
		 */
		public String name() {
			return this.name;
		}

		/**
		 * Gets this id's display name.
		 *
		 * @return this id's display name.
		 */
		public String displayName() {
			return this.displayName;
		}

		@Override
		public int compareTo(HandlerId o) {
			int comparision = this.group.compareTo(o.group);

			if (comparision == 0) {
				comparision = this.name.compareTo(o.name);
			}
			return comparision;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.group, this.name);
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			return obj instanceof HandlerId && (this == obj || compareTo((HandlerId) obj) == 0);
		}

		@Override
		public String toString() {
			return this.group + ":" + this.name;
		}

	}

	/**
	 * Gets all available handlers sorted and accessible by their {@linkplain HandlerId}.
	 *
	 * @return the available handlers sorted and accessible by their {@linkplain HandlerId}.
	 */
	public static SortedMap<HandlerId, FileScannerResultRenderHandler> getHandlers() {
		SortedMap<HandlerId, FileScannerResultRenderHandler> handlers = new TreeMap<>();
		ServiceLoader<FileScannerResultRenderHandlerFactory> handlerFactories = ServiceLoader
				.load(FileScannerResultRenderHandlerFactory.class);

		for (FileScannerResultRenderHandlerFactory handlerFactory : handlerFactories) {
			handlerFactory.addHandlers(handlers);
		}
		return handlers;
	}

	protected final void addHandler(SortedMap<HandlerId, FileScannerResultRenderHandler> handlers, HandlerGroup group,
			String name, FileScannerResultRenderHandler handler) {
		addHandler(handlers, group, name, name, handler);
	}

	protected final void addHandler(SortedMap<HandlerId, FileScannerResultRenderHandler> handlers, HandlerGroup group,
			String name, String displayName, FileScannerResultRenderHandler handler) {
		handlers.put(new HandlerId(group, name, displayName), handler);
	}

	protected abstract void addHandlers(SortedMap<HandlerId, FileScannerResultRenderHandler> handlers);

}
