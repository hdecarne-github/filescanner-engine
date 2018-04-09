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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.carne.boot.check.Check;
import de.carne.boot.check.Nullable;
import de.carne.filescanner.engine.format.AttributeSpec;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.PrettyFormat;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.transfer.FileScannerResultOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.util.StringSupplier;

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
	private CommitState committedState = UNCOMMITTED;
	private CommitState currentState;

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

	public static FileScannerResultBuilder formatResult(FileScannerResultBuilder parent, CompositeSpec formatSpec,
			FileScannerInputRange inputRange, long position) {
		return new FormatResult(parent, formatSpec, inputRange, position);
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
	public synchronized List<FileScannerResult> children() {
		return new ArrayList<>(this.committedState.getChildren());
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

	public synchronized FileScannerResultBuilder preCommit(long commitPosition) {
		FileScannerResultBuilder checkedParent = parent();

		// Always update this result's extend
		if (this.currentState.end() < commitPosition) {
			modifyState().updateEnd(commitPosition);
		}
		// Only commit and add to parent if it is not the barrier and if this is the initial commit
		if (UNCOMMITTED.equals(this.committedState)) {
			synchronized (checkedParent) {
				checkedParent.modifyState().addChild(this);
			}
			this.committedState = this.currentState;
		}
		return this;
	}

	public synchronized FileScannerResultBuilder updateAndCommit(long commitPosition, boolean fullCommit) {
		FileScannerResultBuilder commitResult = this;

		if (this.currentState.end() < commitPosition) {
			modifyState().updateEnd(commitPosition);
		}
		if (!this.currentState.equals(this.committedState)) {
			boolean initialCommit = UNCOMMITTED.equals(this.committedState);

			this.committedState = this.currentState;
			if (this.type == Type.FORMAT) {
				commitResult = parent().updateAndCommitParent(commitPosition, this, initialCommit, fullCommit);
			}
		}
		return commitResult;
	}

	private synchronized FileScannerResultBuilder updateAndCommitParent(long commitPosition,
			FileScannerResultBuilder commitChild, boolean addChild, boolean fullCommit) {
		FileScannerResultBuilder commitResult = commitChild;

		if (addChild) {
			modifyState().updateEnd(commitPosition).addChild(commitChild);
		} else if (this.currentState.end() < commitPosition) {
			modifyState().updateEnd(commitPosition);
		}
		if (fullCommit && !this.currentState.equals(this.committedState)) {
			boolean initialCommit = UNCOMMITTED.equals(this.committedState);

			this.committedState = this.currentState;
			if (this.type == Type.FORMAT) {
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

	public void renderInput(FileScannerResultOutput out) throws IOException, InterruptedException {
		out.setStyle(RenderStyle.NORMAL).write("file");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln("'" + input().name() + "'");
		out.setStyle(RenderStyle.NORMAL).write("size");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).write(PrettyFormat.formatLongNumber(input().size())).writeln(" byte(s)");
	}

	public void renderResult(FileScannerResultOutput out) throws IOException, InterruptedException {
		out.setStyle(RenderStyle.NORMAL).write("start");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln(HexFormat.formatLong(start()));
		out.setStyle(RenderStyle.NORMAL).write("end");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).writeln(HexFormat.formatLong(end()));
		out.setStyle(RenderStyle.NORMAL).write("size");
		out.setStyle(RenderStyle.OPERATOR).write(" = ");
		out.setStyle(RenderStyle.VALUE).write(PrettyFormat.formatLongNumber(size())).writeln(" byte(s)");
	}

	private static final class CommitState {

		private final Supplier<String> name;
		private long end;
		private final List<FileScannerResultBuilder> children = new ArrayList<>();
		private final Map<Object, Object> values = new HashMap<>();

		public CommitState(Supplier<String> name, long end) {
			this.name = name;
			this.end = end;
		}

		public CommitState(String name, long end) {
			this(StringSupplier.of(name), end);
		}

		public CommitState(CommitState state) {
			this.name = state.name;
			this.end = state.end;
			this.children.addAll(state.children);
			this.values.putAll(state.values);
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
			return updateEnd(commitChild.end());
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
			super(parent, FileScannerResult.Type.INPUT, inputRange, StringSupplier.of(inputRange.name()));
		}

		@Override
		public void render(FileScannerResultOutput out) throws IOException, InterruptedException {
			renderInput(out);
		}

		@Override
		public <T> void bindResultValue(CompositeSpec scope, AttributeSpec<T> attribute, T value) {
			throw new IllegalStateException("Cannot bind value to input result '" + this + "'");
		}

		@Override
		public <T> T getValue(AttributeSpec<T> attribute, boolean committed) {
			throw new IllegalStateException("Cannot get value from input result '" + this + "'");
		}

	}

	private static class FormatResult extends FileScannerResultBuilder {

		private final CompositeSpec formatSpec;

		public FormatResult(FileScannerResultBuilder parent, CompositeSpec formatSpec, FileScannerInputRange inputRange,
				long position) {
			super(parent, FileScannerResult.Type.FORMAT, inputRange, formatSpec.resultName(), position, position);
			this.formatSpec = formatSpec;
		}

		@Override
		public void render(FileScannerResultOutput out) throws IOException, InterruptedException {
			FileScannerResultRenderContext context = new FileScannerResultRenderContext(input().range(start(), end()));

			context.render(out, this.formatSpec);
			if (out.isEmpty()) {
				renderResult(out);
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

}
