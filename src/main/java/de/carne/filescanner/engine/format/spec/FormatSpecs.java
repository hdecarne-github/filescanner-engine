/*
 * Copyright (c) 2007-2018 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.filescanner.engine.format.spec;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.carne.filescanner.engine.FileScannerResultDecodeContext;
import de.carne.filescanner.engine.FileScannerResultRenderContext;
import de.carne.filescanner.engine.transfer.RenderOutput;

/**
 * Utility class providing {@linkplain FormatSpec} related functions.
 */
public final class FormatSpecs {

	private FormatSpecs() {
		// Prevent instantiation
	}

	/**
	 * Empty {@linkplain FormatSpec} with no decode result.
	 */
	public static final FormatSpec EMPTY = new FormatSpec() {

		@Override
		public boolean isFixedSize() {
			return true;
		}

		@Override
		public int matchSize() {
			return 0;
		}

		@Override
		public boolean matches(ByteBuffer buffer) {
			return true;
		}

		@Override
		public void decode(FileScannerResultDecodeContext context) throws IOException {
			// Nothing to do here
		}

		@Override
		public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
			// Nothing to do here
		}

	};

	/**
	 * Empty {@linkplain FormatSpec} used to commit the current decode result.
	 */
	public static final FormatSpec COMMIT = new FormatSpec() {

		@Override
		public boolean isFixedSize() {
			return true;
		}

		@Override
		public int matchSize() {
			return 0;
		}

		@Override
		public boolean matches(ByteBuffer buffer) {
			return true;
		}

		@Override
		public void decode(FileScannerResultDecodeContext context) throws IOException {
			context.commit();
		}

		@Override
		public void render(RenderOutput out, FileScannerResultRenderContext context) throws IOException {
			// Nothing to do here
		}

	};

}
