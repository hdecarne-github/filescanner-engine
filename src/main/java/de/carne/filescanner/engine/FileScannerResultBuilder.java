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
package de.carne.filescanner.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.carne.boot.Exceptions;
import de.carne.boot.check.Check;
import de.carne.boot.check.Nullable;
import de.carne.filescanner.engine.format.AttributeSpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.PrettyFormat;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.transfer.ExportTarget;
import de.carne.filescanner.engine.transfer.RawFileScannerResultExporter;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.text.MemoryUnitFormat;

/**
 * {@linkplain FileScannerResultBuilder} implementation with commit and render support.
 */
abstract class FileScannerResultBuilder implements FileScannerResult {

	private static final CommitState UNCOMMITTED = new CommitState("<uncomitted>", -1);

	@Nullable
	private final FileScannerResultBuilder parent;
	private final Type type;
	private final FileScannerInput input;
	private final long start;
	private final List<FileScannerResultExportHandler> exportHandlers = new ArrayList<>();
	private CommitState committedState = UNCOMMITTED;
	private CommitState currentState;
	@Nullable
	private Object data = null;

	protected FileScannerResultBuilder(@Nullable FileScannerResultBuilder parent, FileScannerResult.Type type,
			FileScannerInputRange inputRange, Supplier<String> name) {
		this(parent, type, inputRange, name, inputRange.start(), inputRange.end());
	}

	protected FileScannerResultBuilder(@Nullable FileScannerResultBuilder parent, FileScannerResult.Type type,
			FileScannerInput input, Supplier<String> name, long start, long end) {
		this.parent = parent;
		this.type = type;
		this.input = input;
		this.start = start;
		this.currentState = new CommitState(name, end);
	}

	public static FileScannerResultBuilder inputResult(FileScannerInput input) throws IOException {
		return new InputResultBuilder(null, input.range(0, input.size()));
	}

	public static FileScannerResultBuilder inputResult(FileScannerResultBuilder parent, FileScannerInput input)
			throws IOException {
		return new InputResultBuilder(parent, input.range(0, input.size()));
	}

	public static FileScannerResultBuilder formatResult(FileScannerResultBuilder parent, CompositeSpec formatSpec,
			FileScannerInputRange inputRange, long start) {
		return new FormatResultBuilder(parent, formatSpec, inputRange, start);
	}

	public static FileScannerResultBuilder encodedInputResult(FileScannerResultBuilder parent,
			EncodedInputSpec encodedInputSpec, FileScannerInputRange inputRange, long start, long end) {
		return new EncodedInputResultBuilder(parent, encodedInputSpec, inputRange, start, end);
	}

	@Override
	public Type type() {
		return this.type;
	}

	@Override
	public FileScannerInput input() {
		return this.input;
	}

	@Override
	public synchronized String name() {
		return this.committedState.name().get();
	}

	@Override
	public long start() {
		return this.start;
	}

	@Override
	public synchronized long end() {
		return (!UNCOMMITTED.equals(this.committedState) ? this.committedState.end() : this.start);
	}

	@Override
	public synchronized long size() {
		return (!UNCOMMITTED.equals(this.committedState) ? this.committedState.end() - this.start : 0);
	}

	@Override
	public synchronized int childrenCount() {
		return this.committedState.getChildren().size();
	}

	@Override
	public synchronized FileScannerResult[] children() {
		List<FileScannerResultBuilder> children = this.committedState.getChildren();

		return children.toArray(new FileScannerResult[children.size()]);
	}

	@Override
	public synchronized void setData(Object data) {
		this.data = data;
	}

	@Override
	@Nullable
	public synchronized <T> T getData(Class<T> dataType) {
		return (this.data != null ? Check.isInstanceOf(this.data, dataType) : null);
	}

	@Override
	public byte[] key() {
		byte[] key;

		try (ByteArrayOutputStream keyBytes = new ByteArrayOutputStream()) {
			keyHelper(keyBytes);
			key = keyBytes.toByteArray();
		} catch (IOException e) {
			throw Exceptions.toRuntime(e);
		}
		return key;
	}

	private void keyHelper(ByteArrayOutputStream keyBytes) {
		if (this.parent != null) {
			this.parent.keyHelper(keyBytes);
			keyBytes.write((int) ((this.start >> 56) & 0xff));
			keyBytes.write((int) ((this.start >> 48) & 0xff));
			keyBytes.write((int) ((this.start >> 40) & 0xff));
			keyBytes.write((int) ((this.start >> 32) & 0xff));
			keyBytes.write((int) ((this.start >> 24) & 0xff));
			keyBytes.write((int) ((this.start >> 16) & 0xff));
			keyBytes.write((int) ((this.start >> 8) & 0xff));
			keyBytes.write((int) (this.start & 0xff));
		}
	}

	protected FileScannerResultBuilder parent() {
		return Check.notNull(this.parent);
	}

	protected synchronized <T> void bindValue(AttributeSpec<T> attribute, T value) {
		modifyState().getValues().put(attribute, value);
	}

	public abstract <T> void bindResultValue(CompositeSpec scope, AttributeSpec<T> attribute, T value);

	@Nullable
	protected synchronized <T> T getResultValue(AttributeSpec<T> attribute, boolean committed) {
		Object value = (committed ? this.committedState : this.currentState).getValues().get(attribute);

		return (value != null ? Check.isInstanceOf(value, attribute.type()) : null);
	}

	public abstract <T> T getValue(AttributeSpec<T> attribute, boolean committed);

	public void resolveExportHandlers(List<Supplier<FileScannerResultExportHandler>> handlers) {
		for (Supplier<FileScannerResultExportHandler> handler : handlers) {
			FileScannerResultExportHandler handlerInstance = handler.get();

			if (handlerInstance != null) {
				this.exportHandlers.add(handlerInstance);
			}
		}
	}

	@Nullable
	public synchronized FileScannerResultBuilder updateAndCommit(long commitPosition, boolean fullCommit) {
		FileScannerResultBuilder commitResult = null;

		if (this.currentState.end() < commitPosition) {
			commitResult = this;
			modifyState().updateEnd(commitPosition);
		}
		if (!this.currentState.equals(this.committedState) && this.start < this.currentState.end()) {
			commitResult = this;

			boolean initialCommit = UNCOMMITTED.equals(this.committedState);

			this.committedState = this.currentState.commit();

			FileScannerResultBuilder checkedParent = this.parent;

			if (checkedParent != null) {
				if (this.type != Type.INPUT) {
					commitResult = checkedParent.updateAndCommitParent(commitPosition, this, initialCommit, fullCommit);
				} else {
					checkedParent.updateAndCommitParent(-1, this, true, false);
				}
			}
		}
		return commitResult;
	}

	private synchronized FileScannerResultBuilder updateAndCommitParent(long commitPosition,
			FileScannerResultBuilder commitChild, boolean addChild, boolean fullCommit) {
		FileScannerResultBuilder commitResult = commitChild;

		if (addChild) {
			if (this.type == Type.FORMAT) {
				modifyState().updateEnd(commitPosition).addChild(commitChild);
			} else {
				modifyState().addChild(commitChild);
			}
		} else if (this.currentState.end() < commitPosition) {
			modifyState().updateEnd(commitPosition);
		}
		if (fullCommit && !this.currentState.equals(this.committedState)) {
			boolean initialCommit = UNCOMMITTED.equals(this.committedState);

			this.committedState = this.currentState.commit();
			if (this.type != Type.INPUT) {
				commitResult = parent().updateAndCommitParent(commitPosition, this, initialCommit, fullCommit);
			} else {
				commitResult = this;
			}
		}
		return commitResult;
	}

	private CommitState modifyState() {
		if (this.currentState.equals(this.committedState)) {
			this.currentState = new CommitState(this.currentState);
		}
		return this.currentState;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		if (this.currentState.equals(this.committedState)) {
			buffer.append("committed:");
		} else {
			buffer.append("uncomitted:");
		}
		buffer.append(this.type);
		buffer.append(':');
		buffer.append(this.currentState.name());
		buffer.append('[');
		HexFormat.formatLong(buffer, this.start);
		buffer.append('-');
		HexFormat.formatLong(buffer, this.currentState.end());
		buffer.append(']');
		return buffer.toString();
	}

	@Override
	public void render(RenderOutput out) throws IOException {
		out.setStyle(RenderStyle.NORMAL).write("start");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln(HexFormat.formatLong(start()));
		out.setStyle(RenderStyle.NORMAL).write("end");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln(HexFormat.formatLong(end()));
		out.setStyle(RenderStyle.NORMAL).write("size");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).write(PrettyFormat.formatLongNumber(size()));
		out.setStyle(RenderStyle.COMMENT).write(" // ")
				.writeln(MemoryUnitFormat.getMemoryUnitInstance().format(size() * 1.0));
	}

	@Override
	public FileScannerResultExportHandler[] exportHandlers() {
		FileScannerResultExportHandler[] handlers = new FileScannerResultExportHandler[Math.max(1,
				this.exportHandlers.size())];

		handlers[0] = RawFileScannerResultExporter.APPLICATION_OCTET_STREAM_EXPORTER;

		int handlerIndex = 0;

		for (FileScannerResultExportHandler handler : this.exportHandlers) {
			handlers[handlerIndex] = handler;
			handlerIndex++;
		}
		return handlers;
	}

	@Override
	public void export(ExportTarget target, FileScannerResultExporter exporter) throws IOException {
		FileScannerResultRenderContext context = new FileScannerResultRenderContext(this);

		context.export(target, exporter);
	}

	private static final class CommitState {

		private Supplier<String> name;
		private long end;
		private final List<FileScannerResultBuilder> children = new ArrayList<>();
		private final Map<Object, Object> values = new HashMap<>();

		public CommitState(Supplier<String> name, long end) {
			this.name = name;
			this.end = end;
		}

		public CommitState(String name, long end) {
			this(FinalSupplier.of(name), end);
		}

		public CommitState(CommitState state) {
			this.name = state.name;
			this.end = state.end;
			this.children.addAll(state.children);
			this.values.putAll(state.values);
		}

		public CommitState commit() {
			this.name = FinalSupplier.of(this.name.get());
			return this;
		}

		public Supplier<String> name() {
			return this.name;
		}

		public long end() {
			return this.end;
		}

		public CommitState updateEnd(long commitPosition) {
			this.end = Math.max(this.end, commitPosition);
			return this;
		}

		public CommitState addChild(FileScannerResultBuilder commitChild) {
			this.children.add(commitChild);
			return (commitChild.type() != FileScannerResult.Type.INPUT ? updateEnd(commitChild.end()) : this);
		}

		public List<FileScannerResultBuilder> getChildren() {
			return this.children;
		}

		public Map<Object, Object> getValues() {
			return this.values;
		}

	}

	private static class InputResultBuilder extends FileScannerResultBuilder {

		public InputResultBuilder(@Nullable FileScannerResultBuilder parent, FileScannerInputRange inputRange) {
			super(parent, FileScannerResult.Type.INPUT, inputRange, FinalSupplier.of(inputRange.name()));
		}

		@Override
		public void render(RenderOutput out) throws IOException {
			out.setStyle(RenderStyle.NORMAL).write("file");
			out.setStyle(RenderStyle.OPERATOR).write(" = ");
			out.setStyle(RenderStyle.VALUE).writeln(PrettyFormat.formatString(input().name()));
			out.setStyle(RenderStyle.NORMAL).write("size");
			out.setStyle(RenderStyle.OPERATOR).write(" = ");
			out.setStyle(RenderStyle.VALUE).write(PrettyFormat.formatLongNumber(input().size()));
			out.setStyle(RenderStyle.COMMENT).write(" // ")
					.writeln(MemoryUnitFormat.getMemoryUnitInstance().format(input().size() * 1.0));
		}

		@Override
		public <T> void bindResultValue(CompositeSpec scope, AttributeSpec<T> attribute, T value) {
			throw new IllegalStateException("Cannot bind value to input result '" + this + "'");
		}

		@Override
		public <T> T getValue(AttributeSpec<T> attribute, boolean committed) {
			throw new IllegalStateException("Failed to retrieve context attribute '" + attribute + "'");
		}

	}

	private static class FormatResultBuilder extends FileScannerResultBuilder {

		private final CompositeSpec formatSpec;

		public FormatResultBuilder(FileScannerResultBuilder parent, CompositeSpec formatSpec,
				FileScannerInputRange inputRange, long start) {
			super(parent, FileScannerResult.Type.FORMAT, inputRange, formatSpec.resultName(), start, start);
			this.formatSpec = formatSpec;
		}

		@Override
		public void render(RenderOutput out) throws IOException {
			FileScannerResultRenderContext context = new FileScannerResultRenderContext(this);

			this.formatSpec.render(out, context);
			if (out.isEmpty()) {
				super.render(out);
			}
		}

		@Override
		public <T> void bindResultValue(CompositeSpec scope, AttributeSpec<T> attribute, T value) {
			if (this.formatSpec.equals(scope)) {
				bindValue(attribute, value);
			} else {
				parent().bindResultValue(scope, attribute, value);
			}
		}

		@Override
		public <T> T getValue(AttributeSpec<T> attribute, boolean committed) {
			T value = getResultValue(attribute, committed);

			return (value != null ? value : parent().getValue(attribute, committed));
		}

	}

	private static class EncodedInputResultBuilder extends FileScannerResultBuilder {

		private final EncodedInputSpec encodedInputSpec;

		public EncodedInputResultBuilder(FileScannerResultBuilder parent, EncodedInputSpec encodedInputSpec,
				FileScannerInputRange inputRange, long start, long end) {
			super(parent, FileScannerResult.Type.ENCODED_INPUT, inputRange, encodedInputSpec.encodedInputName(), start,
					end);
			this.encodedInputSpec = encodedInputSpec;
		}

		@Override
		public void render(RenderOutput out) throws IOException {
			super.render(out);

			FileScannerResultRenderContext context = new FileScannerResultRenderContext(this);

			context.render(out, this.encodedInputSpec);
		}

		@Override
		public <T> void bindResultValue(CompositeSpec scope, AttributeSpec<T> attribute, T value) {
			throw new IllegalStateException("Cannot bind value to encoded input result '" + this + "'");
		}

		@Override
		public <T> T getValue(AttributeSpec<T> attribute, boolean committed) {
			return parent().getValue(attribute, committed);
		}

	}

}
