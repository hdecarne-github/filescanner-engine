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
package de.carne.filescanner.engine;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

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
	 * Runs the submitted {@linkplain FileScannerRunnableV} within this {@linkplain FileScannerResultContext} instance.
	 *
	 * @param runnable the {@linkplain FileScannerRunnableV} to run.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void runV(FileScannerRunnableV runnable) throws IOException {
		FileScannerResultContext previousContext = CONTEXT.get();

		if (equals(previousContext)) {
			runnable.run();
		} else {
			CONTEXT.set(this);
			try {
				runnable.run();
			} finally {
				if (previousContext != null) {
					CONTEXT.set(previousContext);
				} else {
					CONTEXT.remove();
				}
			}
		}
	}

	/**
	 * Runs the submitted {@linkplain FileScannerRunnableT} within this {@linkplain FileScannerResultContext} instance.
	 *
	 * @param <T> the actual processing result type.
	 * @param runnable the {@linkplain FileScannerRunnableT} to run.
	 * @return the processing result.
	 * @throws IOException if an I/O error occurs.
	 */
	protected <T> T runT(FileScannerRunnableT<T> runnable) throws IOException {
		FileScannerResultContext previousContext = CONTEXT.get();
		T result;

		if (equals(previousContext)) {
			result = runnable.run();
		} else {
			CONTEXT.set(this);
			try {
				result = runnable.run();
			} finally {
				if (previousContext != null) {
					CONTEXT.set(previousContext);
				} else {
					CONTEXT.remove();
				}
			}
		}
		return result;
	}

	/**
	 * Gets a bound context value.
	 *
	 * @param <T> the actual value type.
	 * @param valueSpec the context value to retrieve.
	 * @return the context value.
	 */
	public abstract <T> T getValue(FileScannerResultContextValueSpec<T> valueSpec);

}
