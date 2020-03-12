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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.Exceptions;
import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.input.FileScannerInput;
import de.carne.filescanner.engine.input.FileScannerInputRange;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.RawTransferHandler;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.transfer.TransferSource;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.filescanner.provider.util.HexFormat;
import de.carne.filescanner.provider.util.PrettyFormat;
import de.carne.text.MemoryUnitFormat;
import de.carne.util.Strings;

/**
 * {@linkplain FileScannerResultBuilder} implementation with commit and render support.
 */
abstract class FileScannerResultBuilder implements FileScannerResult {

	private static final CommitState UNCOMMITTED = new CommitState("<uncomitted>", -1);

	private final @Nullable FileScannerResultBuilder parent;
	private final Type type;
	private final FileScannerInput input;
	private final long start;
	private final List<FileScannerResultExportHandler> exportHandlers = new ArrayList<>();
	private CommitState committedState = UNCOMMITTED;
	private CommitState currentState;
	private @Nullable Object data = null;

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
			boolean relocated, FileScannerInputRange inputRange, long start) {
		return new FormatResultBuilder(parent, formatSpec, relocated, inputRange, start);
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
	public FileScannerResult inputResult() {
		FileScannerResultBuilder inputResult = this;

		while (inputResult.type != Type.INPUT) {
			inputResult = Objects.requireNonNull(inputResult.parent);
		}
		return inputResult;
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

	private synchronized int childIndex(FileScannerResult child) {
		return this.committedState.getChildren().indexOf(child);
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
		FileScannerResultBuilder checkedParent = this.parent;

		if (checkedParent != null) {
			checkedParent.keyHelper(keyBytes);
			if (checkedParent.type != Type.ENCODED_INPUT) {
				keyBytes.write((int) ((this.start >> 56) & 0xff));
				keyBytes.write((int) ((this.start >> 48) & 0xff));
				keyBytes.write((int) ((this.start >> 40) & 0xff));
				keyBytes.write((int) ((this.start >> 32) & 0xff));
				keyBytes.write((int) ((this.start >> 24) & 0xff));
				keyBytes.write((int) ((this.start >> 16) & 0xff));
				keyBytes.write((int) ((this.start >> 8) & 0xff));
				keyBytes.write((int) (this.start & 0xff));
			} else {
				int inputIndex = checkedParent.childIndex(this);

				keyBytes.write((inputIndex >> 24) & 0xff);
				keyBytes.write((inputIndex >> 16) & 0xff);
				keyBytes.write((inputIndex >> 8) & 0xff);
				keyBytes.write(inputIndex & 0xff);
			}
		}
	}

	protected FileScannerResultBuilder parent() {
		return Objects.requireNonNull(this.parent);
	}

	protected synchronized <T> void bindValue(FileScannerResultContextValueSpec<T> valueSpec, @NonNull T value) {
		modifyState().getValues().put(valueSpec, value);
	}

	public abstract <T> void bindResultValue(CompositeSpec scope, FileScannerResultContextValueSpec<T> valueSpec,
			@NonNull T value);

	public abstract <T> void bindDecodedValue(FileScannerResultContextValueSpec<T> valueSpec, @NonNull T value);

	@Nullable
	protected synchronized <T> T getResultValue(FileScannerResultContextValueSpec<T> valueSpec, boolean committed) {
		Object value = (committed ? this.committedState : this.currentState).getValues().get(valueSpec);

		return (value != null ? Check.isInstanceOf(value, valueSpec.type()) : null);
	}

	public abstract <T> T getValue(FileScannerResultContextValueSpec<T> valueSpec, boolean committed);

	public void resolveExportHandlers(List<Supplier<FileScannerResultExportHandler>> handlers) {
		for (Supplier<FileScannerResultExportHandler> handler : handlers) {
			this.exportHandlers.add(handler.get());
		}
	}

	@Nullable
	public synchronized FileScannerResultBuilder updateAndCommit(long commitPosition, boolean fullCommit) {
		FileScannerResultBuilder commitResult = null;

		if (this.currentState.end() < commitPosition) {
			commitResult = this;
			modifyState().updateEnd(commitPosition);
		}

		FileScannerResultBuilder checkedParent = this.parent;

		if (!this.currentState.equals(this.committedState)
				&& (checkedParent == null || this.start < this.currentState.end())) {
			commitResult = this;

			boolean initialCommit = UNCOMMITTED.equals(this.committedState);

			this.committedState = this.currentState.commit();
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

		handlers[0] = RawTransferHandler.APPLICATION_OCTET_STREAM_TRANSFER;

		int handlerIndex = 0;

		for (FileScannerResultExportHandler handler : this.exportHandlers) {
			handlers[handlerIndex] = handler;
			handlerIndex++;
		}
		return handlers;
	}

	@Override
	public TransferSource export(FileScannerResultExportHandler exportHandler) throws IOException {
		FileScannerResultRenderContext context = new FileScannerResultRenderContext(this);

		return context.export(exportHandler);
	}

	private static final class CommitState {

		private static final Log LOG = new Log();

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
			FileScannerResult.Type commitChildType = commitChild.type();
			long commitChildStart = commitChild.start();
			int addIndex = 0;

			for (FileScannerResult child : this.children) {
				long childStart = child.start();

				if (childStart >= commitChildStart) {
					if (commitChildType != FileScannerResult.Type.INPUT && commitChild.end() > childStart) {
						LOG.warning("Overlapping results ''{0}'' - ''{1}''", commitChild, child);
					}
					break;
				}
				addIndex++;
			}
			this.children.add(addIndex, commitChild);
			return (commitChildType != FileScannerResult.Type.INPUT ? updateEnd(commitChild.end()) : this);
		}

		public List<FileScannerResultBuilder> getChildren() {
			return this.children;
		}

		public Map<Object, Object> getValues() {
			return this.values;
		}

	}

	private static class InputResultBuilder extends FileScannerResultBuilder {

		public InputResultBuilder(@Nullable FileScannerResultBuilder parent, FileScannerInputRange inputRange)
				throws IOException {
			super(parent, FileScannerResult.Type.INPUT, inputRange, FinalSupplier.of(inputRange.name()));
			bindValue(FileScannerResultContextValueSpecs.INPUT_NAME, inputRange.name());
			bindValue(FileScannerResultContextValueSpecs.INPUT_SIZE, inputRange.size());
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
		public <T> void bindResultValue(CompositeSpec scope, FileScannerResultContextValueSpec<T> valueSpec, T value) {
			throw new IllegalStateException("Cannot bind result value to input result '" + this + "'");
		}

		@Override
		public <T> void bindDecodedValue(@NonNull FileScannerResultContextValueSpec<T> valueSpec, @NonNull T value) {
			throw new IllegalStateException("Cannot bind decoded value to input result '" + this + "'");
		}

		@Override
		public <T> T getValue(FileScannerResultContextValueSpec<T> valueSpec, boolean committed) {
			// Always search uncommitted (because input results are never committed and never reverted)
			@Nullable T value = getResultValue(valueSpec, false);

			if (value == null) {
				throw new IllegalStateException("Failed to retrieve context value '" + valueSpec + "'");
			}
			return value;
		}

	}

	private static class FormatResultBuilder extends FileScannerResultBuilder {

		private static final Log LOG = new Log();

		private final CompositeSpec formatSpec;
		private final boolean relocated;

		public FormatResultBuilder(FileScannerResultBuilder parent, CompositeSpec formatSpec, boolean relocated,
				FileScannerInputRange inputRange, long start) {
			super(parent, FileScannerResult.Type.FORMAT, inputRange, formatSpec.resultName(), start, start);
			this.formatSpec = formatSpec;
			this.relocated = relocated;
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
		public <T> void bindResultValue(CompositeSpec scope, FileScannerResultContextValueSpec<T> valueSpec,
				@NonNull T value) {
			if (this.formatSpec.equals(scope)) {
				bindValue(valueSpec, value);
			} else {
				parent().bindResultValue(scope, valueSpec, value);
			}
		}

		@Override
		public <T> void bindDecodedValue(@NonNull FileScannerResultContextValueSpec<T> valueSpec, @NonNull T value) {
			if (this.formatSpec.isResult()) {
				bindValue(valueSpec, value);
			} else {
				parent().bindDecodedValue(valueSpec, value);
			}
		}

		@Override
		public <T> T getValue(FileScannerResultContextValueSpec<T> valueSpec, boolean committed) {
			@Nullable T value = getResultValue(valueSpec, committed);

			if (value == null) {
				value = Objects.requireNonNull(parent().getValue(valueSpec, committed));
				if (this.relocated) {
					LOG.debug("Re-binding relocated context value '':{0}'' = ''{1}''", valueSpec,
							Strings.encode(Objects.toString(value)));

					bindValue(valueSpec, value);
				}
			}
			return value;
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
		public <T> void bindResultValue(CompositeSpec scope, FileScannerResultContextValueSpec<T> valueSpec, T value) {
			throw new IllegalStateException("Cannot bind result value to encoded input result '" + this + "'");
		}

		@Override
		public <T> void bindDecodedValue(@NonNull FileScannerResultContextValueSpec<T> valueSpec, @NonNull T value) {
			throw new IllegalStateException("Cannot bind decoded value to encoded input result '" + this + "'");
		}

		@Override
		public <T> T getValue(FileScannerResultContextValueSpec<T> valueSpec, boolean committed) {
			@Nullable T value = getResultValue(valueSpec, committed);

			return (value != null ? value : parent().getValue(valueSpec, committed));
		}

	}

}
