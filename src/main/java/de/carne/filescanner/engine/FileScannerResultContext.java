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
package de.carne.filescanner.engine;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.format.spec.AttributeSpec;

/**
 * Input data processor base class responsible for context specific attribute value access.
 */
public abstract class FileScannerResultContext {

	private static final ThreadLocal<@Nullable FileScannerResultContext> CONTEXT = new ThreadLocal<>();

	/**
	 * Gets the {@linkplain FileScannerResultContext} instance attached to the current decode or render call.
	 *
	 * @return the {@linkplain FileScannerResultContext} instance attached to the current decode or render call.
	 */
	public static FileScannerResultContext get() {
		FileScannerResultContext context = CONTEXT.get();

		if (context == null) {
			throw new IllegalStateException("Context only available during decode or render call");
		}
		return context;
	}

	/**
	 * Checks whether a specific {@linkplain FileScannerResultContext} type is already attached to the current decode or
	 * render call.
	 *
	 * @param contextType the {@linkplain FileScannerResultContext} type to check for.
	 * @return {@code true} if a {@linkplain FileScannerResultContext} instance of the submitted type is already
	 * attached to the current decode or render call.
	 */
	protected boolean inContext(Class<? extends FileScannerResultContext> contextType) {
		FileScannerResultContext context = CONTEXT.get();

		return context != null && contextType.isAssignableFrom(context.getClass());
	}

	/**
	 * Runs the submitted {@linkplain FileScannerRunnable} within this {@linkplain FileScannerResultContext} instance.
	 *
	 * @param runnable the {@linkplain FileScannerRunnable} to run.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void run(FileScannerRunnable runnable) throws IOException {
		FileScannerResultContext previousContext = CONTEXT.get();

		if (equals(previousContext)) {
			runnable.run();
		} else {
			CONTEXT.set(this);
			try {
				runnable.run();
			} finally {
				CONTEXT.set(previousContext);
			}
		}
	}

	/**
	 * Gets a bound attribute value.
	 *
	 * @param <T> the actual attribute type.
	 * @param attribute the {@linkplain AttributeSpec} to retrieve the value of.
	 * @return the attribute value.
	 */
	public abstract <T> T getValue(AttributeSpec<T> attribute);

}
