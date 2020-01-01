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
package de.carne.filescanner.engine.util;

import java.io.IOException;
import java.util.Set;

import de.carne.filescanner.engine.transfer.RenderOption;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.transfer.Renderer;
import de.carne.filescanner.engine.transfer.TransferSource;
import de.carne.io.Closeables;

/**
 * {@linkplain Renderer} implementation combining multiple {@linkplain Renderer} instances into one for parallel
 * rendering.
 */
public class CombinedRenderer implements Renderer {

	private final Renderer[] renderers;

	/**
	 * Constructs a new {@linkplain CombinedRenderer} instance.
	 *
	 * @param renderers the {@linkplain Renderer} instances to combine.
	 */
	public CombinedRenderer(Renderer... renderers) {
		this.renderers = renderers;
	}

	@Override
	public void close() throws IOException {
		Closeables.closeAll(this.renderers);
	}

	@Override
	public boolean isStyled() {
		boolean isStyled = false;

		for (Renderer renderer : this.renderers) {
			if (renderer.isStyled()) {
				isStyled = true;
			}
		}
		return isStyled;
	}

	@Override
	public void emitPrologue(Set<RenderOption> options) throws IOException {
		for (Renderer renderer : this.renderers) {
			renderer.emitPrologue(options);
		}
	}

	@Override
	public void emitText(int indent, RenderStyle style, String text, boolean lineBreak) throws IOException {
		for (Renderer renderer : this.renderers) {
			renderer.emitText(indent, style, text, lineBreak);
		}
	}

	@Override
	public void emitText(int indent, RenderStyle style, String text, long href, boolean lineBreak) throws IOException {
		for (Renderer renderer : this.renderers) {
			renderer.emitText(indent, style, text, href, lineBreak);
		}
	}

	@Override
	public void emitMediaData(int indent, RenderStyle style, TransferSource source, boolean lineBreak)
			throws IOException {
		for (Renderer renderer : this.renderers) {
			renderer.emitMediaData(indent, style, source, lineBreak);
		}
	}

	@Override
	public void emitMediaData(int indent, RenderStyle style, TransferSource source, long href, boolean lineBreak)
			throws IOException {
		for (Renderer renderer : this.renderers) {
			renderer.emitMediaData(indent, style, source, href, lineBreak);
		}
	}

	@Override
	public void emitEpilogue() throws IOException {
		for (Renderer renderer : this.renderers) {
			renderer.emitEpilogue();
		}
	}

}
