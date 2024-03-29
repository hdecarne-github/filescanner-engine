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
package de.carne.filescanner.engine.transfer;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.filescanner.engine.FileScannerResult;
import de.carne.util.Check;

/**
 * Class used to output file scanner results in a generic manner to different kind of outputs.
 */
public final class RenderOutput implements Closeable {

	private final Renderer renderer;
	private final Set<RenderOption> options = new HashSet<>();
	private boolean prepared = false;
	private boolean newLine = true;
	private int currentIndent = 0;
	private RenderStyle currentStyle = RenderStyle.NORMAL;

	/**
	 * Constructs a new {@linkplain RenderOutput} instance.
	 *
	 * @param renderer the {@linkplain Renderer} to use for output generation.
	 */
	public RenderOutput(Renderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Convenience function for rendering the given {@linkplain FileScannerResult} to the given {@linkplain Renderer}.
	 *
	 * @param result the {@linkplain FileScannerResult} to render.
	 * @param renderer the {@linkplain Renderer} to use for output generation.
	 * @param renderHandler the {@linkplain FileScannerResultRenderHandler} to use for rendering. May {@code null} to
	 * use the default handler.
	 * @param offset the offset to start rendering at.
	 * @return the number of decoded bytes.
	 * @throws IOException if an I/O error occurs.
	 */
	public static long render(FileScannerResult result, Renderer renderer,
			@Nullable FileScannerResultRenderHandler renderHandler, long offset) throws IOException {
		long decoded;

		try (RenderOutput out = new RenderOutput(renderer)) {
			decoded = result.render(out, renderHandler, offset);
		}
		return decoded;
	}

	/**
	 * Checks whether no output has yet been generated by this instance.
	 *
	 * @return {@code true} if no output has yet been generated by this instance.
	 */
	public boolean isEmpty() {
		return !this.prepared;
	}

	/**
	 * Checks whether this output's {@linkplain Renderer} supports styling.
	 * <p>
	 * This information may be used to optimize rendering.
	 * </p>
	 *
	 * @return {@code true} if this output's {@linkplain Renderer} supports styling.
	 * @see Renderer#isStyled()
	 */
	public boolean isStyled() {
		return this.renderer.isStyled();
	}

	/**
	 * Enables the given {@linkplain RenderOption}.
	 * <p>
	 * Render options can only be changed before the first output is generated.
	 * </p>
	 *
	 * @param option the {@linkplain RenderOption} to enable.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 */
	public RenderOutput enableOption(RenderOption option) {
		Check.assertTrue(!this.prepared);
		this.options.add(option);
		return this;
	}

	/**
	 * Disables the given {@linkplain RenderOption}.
	 * <p>
	 * Render options can only be changed before the first output is generated.
	 * </p>
	 *
	 * @param option the {@linkplain RenderOption} to disable.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 */
	public RenderOutput disableOption(RenderOption option) {
		Check.assertTrue(!this.prepared);
		this.options.remove(option);
		return this;
	}

	/**
	 * Increases the current indent level.
	 *
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 */
	public RenderOutput increaseIndent() {
		this.currentIndent++;
		return this;
	}

	/**
	 * Decreases the current indent level.
	 *
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 */
	public RenderOutput decreaseIndent() {
		Check.isTrue(this.currentIndent > 0);

		this.currentIndent--;
		return this;
	}

	/**
	 * Sets the {@linkplain RenderStyle} for subsequent write calls.
	 *
	 * @param style the {@linkplain RenderStyle} to set.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 */
	public RenderOutput setStyle(RenderStyle style) {
		this.currentStyle = style;
		return this;
	}

	/**
	 * Writes simple text to the output using the currently selected {@linkplain RenderStyle}.
	 *
	 * @param text the text to write.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 * @throws IOException if an I/O error occurs.
	 */
	public RenderOutput write(String text) throws IOException {
		prepareIfNeeded();
		this.renderer.emitText((this.newLine ? this.currentIndent : -1), this.currentStyle, text, false);
		this.newLine = false;
		return this;
	}

	/**
	 * Writes simple text with link to the output using the currently selected {@linkplain RenderStyle}.
	 *
	 * @param text the text to write.
	 * @param href the position to link to.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 * @throws IOException if an I/O error occurs.
	 */
	public RenderOutput write(String text, long href) throws IOException {
		prepareIfNeeded();
		this.renderer.emitText((this.newLine ? this.currentIndent : -1), this.currentStyle, text, href, false);
		this.newLine = false;
		return this;
	}

	/**
	 * Writes simple text as well as a line break to the output using the currently selected {@linkplain RenderStyle}.
	 *
	 * @param text the text to write.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 * @throws IOException if an I/O error occurs.
	 */
	public RenderOutput writeln(String text) throws IOException {
		prepareIfNeeded();
		this.renderer.emitText((this.newLine ? this.currentIndent : -1), this.currentStyle, text, true);
		this.newLine = true;
		return this;
	}

	/**
	 * Writes simple text with link as well as a line break to the output using the currently selected
	 * {@linkplain RenderStyle}.
	 *
	 * @param text the text to write.
	 * @param href the position to link to.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 * @throws IOException if an I/O error occurs.
	 */
	public RenderOutput writeln(String text, long href) throws IOException {
		prepareIfNeeded();
		this.renderer.emitText((this.newLine ? this.currentIndent : -1), this.currentStyle, text, href, true);
		this.newLine = true;
		return this;
	}

	/**
	 * Writes media data to the output using the currently selected {@linkplain RenderStyle}.
	 *
	 * @param source the media data to write.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 * @throws IOException if an I/O error occurs.
	 */
	public RenderOutput write(TransferSource source) throws IOException {
		prepareIfNeeded();
		this.renderer.emitMediaData((this.newLine ? this.currentIndent : -1), this.currentStyle, source, false);
		this.newLine = false;
		return this;
	}

	/**
	 * Writes media data as well as a line break to the output using the currently selected {@linkplain RenderStyle}.
	 *
	 * @param source the media data to write.
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 * @throws IOException if an I/O error occurs.
	 */
	public RenderOutput writeln(TransferSource source) throws IOException {
		prepareIfNeeded();
		this.renderer.emitMediaData((this.newLine ? this.currentIndent : -1), this.currentStyle, source, true);
		this.newLine = true;
		return this;
	}

	/**
	 * Writes a line break to the output.
	 *
	 * @return the updated {@linkplain RenderOutput} for chaining.
	 * @throws IOException if an I/O error occurs.
	 */
	public RenderOutput writeln() throws IOException {
		return writeln("");
	}

	@Override
	public void close() throws IOException {
		if (this.prepared) {
			this.renderer.emitEpilogue();
		}
		this.renderer.close();
	}

	private void prepareIfNeeded() throws IOException {
		if (!this.prepared) {
			this.renderer.emitPrologue(this.options);
			this.prepared = true;
		}
	}

}
