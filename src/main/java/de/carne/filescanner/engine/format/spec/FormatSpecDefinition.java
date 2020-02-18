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
package de.carne.filescanner.engine.format.spec;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.Exceptions;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.StreamValue;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.PrettyFormat;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarBaseVisitor;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarLexer;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousArraySpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousScanSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousSequenceSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousStructSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousUnionSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ArraySpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AttributeFormatModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AttributeRendererModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AttributeValidateNumberArrayModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AttributeValidateNumberModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AttributeValidateStringModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CharArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CompositeSpecByteOrderModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CompositeSpecExportModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CompositeSpecExpressionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CompositeSpecRendererModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ConditionalCompositeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ConditionalSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DecodeAtSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.EncodedInputSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ExternalReferenceContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatSpecsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatTextContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.NumberArrayValueContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.NumberArrayValueSetContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.NumberExpressionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.NumberValueContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.NumberValueSetContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.RangeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.RawSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.RegexTextContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ScanSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ScopeIdentifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SequenceSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SequenceSpecMaxModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SequenceSpecMinModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SequenceSpecSizeModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SequenceSpecStopAfterModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SequenceSpecStopBeforeModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SimpleTextContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SpecIdentifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SpecReferenceContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.StringAttributeCharsetModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.StringAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.StructSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.StructSpecElementContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SymbolDefinitionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.TextExpressionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.UnionSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ValidationTextContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ValidationTextSetContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordSymbolsContext;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.transfer.FileScannerResultRenderHandler;
import de.carne.filescanner.engine.util.ByteHelper;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.filescanner.engine.util.IntHelper;
import de.carne.filescanner.engine.util.LongHelper;
import de.carne.filescanner.engine.util.ShortHelper;
import de.carne.filescanner.engine.util.StringHelper;
import de.carne.filescanner.provider.util.AppleDateRenderer;
import de.carne.filescanner.provider.util.DosDateRenderer;
import de.carne.filescanner.provider.util.DosTimeRenderer;
import de.carne.util.Lazy;
import de.carne.util.Strings;

/**
 * Base class for all kinds {@linkplain FormatSpec} based format definitions.
 */
public abstract class FormatSpecDefinition {

	private static final Log LOG = new Log();

	private static final String LOG_LOADED_SPEC = "Loaded spec: {0}";
	private static final String LOG_ASSIGNED_SPEC = "Assigned spec {0}: {1}";

	private final Map<String, Set<Byte>> byteSymbolsMap = new HashMap<>();
	private final Map<String, Set<Short>> wordSymbolsMap = new HashMap<>();
	private final Map<String, Set<Integer>> dwordSymbolsMap = new HashMap<>();
	private final Map<String, Set<Long>> qwordSymbolsMap = new HashMap<>();

	private final Map<String, AttributeFormatter<Byte>> byteAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<Short>> wordAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<Integer>> dwordAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<Long>> qwordAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<byte[]>> byteArrayAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<short[]>> wordArrayAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<int[]>> dwordArrayAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<long[]>> qwordArrayAttributeFormatter = new HashMap<>();

	private final Map<String, AttributeRenderer<Byte>> byteAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<Short>> wordAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<Integer>> dwordAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<Long>> qwordAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<byte[]>> byteArrayAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<short[]>> wordArrayAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<int[]>> dwordArrayAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<long[]>> qwordArrayAttributeRenderer = new HashMap<>();

	private final Map<String, AttributeRenderer<StreamValue>> streamValueAttributeRenderer = new HashMap<>();

	private final Map<String, Supplier<FormatSpec>> specs = new HashMap<>();

	private final List<Runnable> lateBindings = new LinkedList<>();

	/**
	 * Constructs a new {@linkplain FormatSpecDefinition} instance.
	 */
	protected FormatSpecDefinition() {
		// @PrettyFormat
		this.byteAttributeFormatter.put("CharFormat", PrettyFormat.BYTE_CHAR_FORMATTER);
		this.byteAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.BYTE_FORMATTER);
		this.wordAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.SHORT_FORMATTER);
		this.dwordAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.INT_FORMATTER);
		this.qwordAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.LONG_FORMATTER);
		this.byteArrayAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.BYTE_ARRAY_FORMATTER);
		this.wordArrayAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.SHORT_ARRAY_FORMATTER);
		this.dwordArrayAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.INT_ARRAY_FORMATTER);
		this.qwordArrayAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.LONG_ARRAY_FORMATTER);
		// @HexFormat
		this.byteAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.BYTE_FORMATTER);
		this.wordAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.SHORT_FORMATTER);
		this.dwordAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.INT_FORMATTER);
		this.qwordAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.LONG_FORMATTER);
		this.byteArrayAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.BYTE_ARRAY_FORMATTER);
		this.wordArrayAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.SHORT_ARRAY_FORMATTER);
		this.dwordArrayAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.INT_ARRAY_FORMATTER);
		this.qwordArrayAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.LONG_ARRAY_FORMATTER);
		// @SizeRenderer
		this.byteAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.BYTE_RENDERER);
		this.wordAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.SHORT_RENDERER);
		this.dwordAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.INT_RENDERER);
		this.qwordAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.LONG_RENDERER);
		// @DosTimeRenderer
		this.wordAttributeRenderer.put(DosTimeRenderer.class.getSimpleName(), DosTimeRenderer.RENDERER);
		// @DosDateRenderer
		this.wordAttributeRenderer.put(DosDateRenderer.class.getSimpleName(), DosDateRenderer.RENDERER);
		// @AppleDateRenderer
		this.dwordAttributeRenderer.put(AppleDateRenderer.class.getSimpleName(), AppleDateRenderer.RENDERER);
	}

	/**
	 * Gets the resource containing the actual format spec definitions (using the grammar defined in
	 * FormatSpecGrammar.g4).
	 *
	 * @return the resource containing the actual format spec definitions.
	 */
	protected abstract URL getFormatSpecResource();

	/**
	 * Adds a byte {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addByteAttributeFormatter(String identifier, AttributeFormatter<Byte> formatter) {
		if (this.byteAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of byte attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a word {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addWordAttributeFormatter(String identifier, AttributeFormatter<Short> formatter) {
		if (this.wordAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of word attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a double word {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addDWordAttributeFormatter(String identifier, AttributeFormatter<Integer> formatter) {
		if (this.dwordAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of dword attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a quad word {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addQWordAttributeFormatter(String identifier, AttributeFormatter<Long> formatter) {
		if (this.qwordAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of qword attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a byte array {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addByteArrayAttributeFormatter(String identifier,
			AttributeFormatter<byte[]> formatter) {
		if (this.byteArrayAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of byte array attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a word array {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addWordArrayAttributeFormatter(String identifier,
			AttributeFormatter<short[]> formatter) {
		if (this.wordArrayAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of word array attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a double word array {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addDWordArrayAttributeFormatter(String identifier,
			AttributeFormatter<int[]> formatter) {
		if (this.dwordArrayAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of dword array attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a quad word array {@linkplain AttributeFormatter}.
	 *
	 * @param identifier the formatter identifier.
	 * @param formatter the {@linkplain AttributeFormatter} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addQWordArrayAttributeFormatter(String identifier,
			AttributeFormatter<long[]> formatter) {
		if (this.qwordArrayAttributeFormatter.put(identifier, formatter) != null) {
			LOG.warning("Redefinition of qword array attribute formatter ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a byte {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addByteAttributeRenderer(String identifier, AttributeRenderer<Byte> renderer) {
		if (this.byteAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of byte attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a word {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addWordAttributeRenderer(String identifier, AttributeRenderer<Short> renderer) {
		if (this.wordAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of byte attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a double word {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addDWordAttributeRenderer(String identifier, AttributeRenderer<Integer> renderer) {
		if (this.dwordAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of dword attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a quad word {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addQWordAttributeRenderer(String identifier, AttributeRenderer<Long> renderer) {
		if (this.qwordAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of qword attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a byte array {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addByteArrayAttributeRenderer(String identifier, AttributeRenderer<byte[]> renderer) {
		if (this.byteArrayAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of byte array attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a word array {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addWordArrayAttributeRenderer(String identifier, AttributeRenderer<short[]> renderer) {
		if (this.wordArrayAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of byte array attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a double word array {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addDWordArrayAttributeRenderer(String identifier, AttributeRenderer<int[]> renderer) {
		if (this.dwordArrayAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of dword array attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a quad word array {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addQWordArrayAttributeRenderer(String identifier, AttributeRenderer<long[]> renderer) {
		if (this.qwordArrayAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of qword array attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a stream value {@linkplain AttributeRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain AttributeRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addStreamValueAttributeRenderer(String identifier,
			AttributeRenderer<StreamValue> renderer) {
		if (this.streamValueAttributeRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of stream value attribute renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Loads and initializes the {@linkplain FormatSpecDefinition}.
	 */
	public void load() {
		URL formatSpecResourceUrl = getFormatSpecResource();

		try (InputStream resourceStream = formatSpecResourceUrl.openStream()) {
			ErrorListener errorListener = new ErrorListener(formatSpecResourceUrl);
			CharStream input = CharStreams.fromStream(resourceStream);
			FormatSpecGrammarLexer lexer = new FormatSpecGrammarLexer(input);

			lexer.removeErrorListeners();
			lexer.addErrorListener(errorListener);

			TokenStream tokens = new CommonTokenStream(lexer);
			FormatSpecGrammarParser parser = new FormatSpecGrammarParser(tokens);

			parser.removeErrorListeners();
			parser.addErrorListener(errorListener);

			Loader loader = new Loader(this::loadHelper);

			loader.visitFormatSpecs(parser.formatSpecs());
		} catch (IOException e) {
			throw Exceptions.toRuntime(e);
		}
		for (Runnable lateBinding : this.lateBindings) {
			lateBinding.run();
		}
		this.lateBindings.clear();
		afterLoad();
	}

	protected void afterLoad() {
		// Default is to do nothing
	}

	private static class ErrorListener extends BaseErrorListener {

		private final URL formatSpecResourceUrl;

		ErrorListener(URL formatSpecResourceUrl) {
			this.formatSpecResourceUrl = formatSpecResourceUrl;
		}

		@Override
		public void syntaxError(@Nullable Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
				int charPositionInLine, @Nullable String msg, @Nullable RecognitionException e) {
			throw new ParseCancellationException(
					this.formatSpecResourceUrl + "[" + line + ":" + charPositionInLine + "] '" + msg, e);
		}

	}

	private static class Loader extends FormatSpecGrammarBaseVisitor<FormatSpecDefinition> {

		private final Function<FormatSpecsContext, FormatSpecDefinition> loadHelper;

		Loader(Function<FormatSpecsContext, FormatSpecDefinition> loadHelper) {
			this.loadHelper = loadHelper;
		}

		@Override
		public FormatSpecDefinition visitFormatSpecs(@Nullable FormatSpecsContext ctx) {
			return this.loadHelper.apply(Objects.requireNonNull(ctx));
		}
	}

	@SuppressWarnings({ "null", "squid:S3776" })
	private FormatSpecDefinition loadHelper(FormatSpecsContext ctx) {
		for (SymbolsContext symbolsCtx : ctx.symbols()) {
			ByteSymbolsContext byteSymbolsCtx = symbolsCtx.byteSymbols();

			if (byteSymbolsCtx != null) {
				String byteSymbolsIdentifier = byteSymbolsCtx.symbolsIdentifier().getText();
				ByteSymbolRenderer byteSymbols = new ByteSymbolRenderer();

				loadSymbolDefinitions(byteSymbols, byteSymbolsCtx.symbolDefinition(), ByteHelper::decodeUnsigned);
				this.byteSymbolsMap.put(byteSymbolsIdentifier, byteSymbols.keySet());
				addByteAttributeRenderer(byteSymbolsIdentifier, byteSymbols);
			}

			WordSymbolsContext wordSymbolsCtx = symbolsCtx.wordSymbols();

			if (wordSymbolsCtx != null) {
				String wordSymbolsIdentifier = wordSymbolsCtx.symbolsIdentifier().getText();
				WordSymbolRenderer wordSymbols = new WordSymbolRenderer();

				loadSymbolDefinitions(wordSymbols, wordSymbolsCtx.symbolDefinition(), ShortHelper::decodeUnsigned);
				this.wordSymbolsMap.put(wordSymbolsIdentifier, wordSymbols.keySet());
				addWordAttributeRenderer(wordSymbolsIdentifier, wordSymbols);
			}

			DwordSymbolsContext dwordSymbolsCtx = symbolsCtx.dwordSymbols();

			if (dwordSymbolsCtx != null) {
				String dwordSymbolsIdentifier = dwordSymbolsCtx.symbolsIdentifier().getText();
				DWordSymbolRenderer dwordSymbols = new DWordSymbolRenderer();

				loadSymbolDefinitions(dwordSymbols, dwordSymbolsCtx.symbolDefinition(), IntHelper::decodeUnsigned);
				this.dwordSymbolsMap.put(dwordSymbolsIdentifier, dwordSymbols.keySet());
				addDWordAttributeRenderer(dwordSymbolsIdentifier, dwordSymbols);
			}

			QwordSymbolsContext qwordSymbolsCtx = symbolsCtx.qwordSymbols();

			if (qwordSymbolsCtx != null) {
				String qwordSymbolsIdentifier = qwordSymbolsCtx.symbolsIdentifier().getText();
				QWordSymbolRenderer qwordSymbols = new QWordSymbolRenderer();

				loadSymbolDefinitions(qwordSymbols, qwordSymbolsCtx.symbolDefinition(), LongHelper::decodeUnsigned);
				this.qwordSymbolsMap.put(qwordSymbolsIdentifier, qwordSymbols.keySet());
				addQWordAttributeRenderer(qwordSymbolsIdentifier, qwordSymbols);
			}
		}
		for (FlagSymbolsContext flagSymbolsCtx : ctx.flagSymbols()) {
			ByteFlagSymbolsContext byteFlagSymbolsCtx = flagSymbolsCtx.byteFlagSymbols();

			if (byteFlagSymbolsCtx != null) {
				String byteFlagSymbolsIdentifier = byteFlagSymbolsCtx.symbolsIdentifier().getText();
				ByteFlagRenderer byteFlagSymbols = new ByteFlagRenderer();

				loadSymbolDefinitions(byteFlagSymbols, byteFlagSymbolsCtx.symbolDefinition(),
						ByteHelper::decodeUnsigned);
				addByteAttributeRenderer(byteFlagSymbolsIdentifier, byteFlagSymbols);
			}

			WordFlagSymbolsContext wordFlagSymbolsCtx = flagSymbolsCtx.wordFlagSymbols();

			if (wordFlagSymbolsCtx != null) {
				String wordFlagSymbolsIdentifier = wordFlagSymbolsCtx.symbolsIdentifier().getText();
				WordFlagRenderer wordFlagSymbols = new WordFlagRenderer();

				loadSymbolDefinitions(wordFlagSymbols, wordFlagSymbolsCtx.symbolDefinition(),
						ShortHelper::decodeUnsigned);
				addWordAttributeRenderer(wordFlagSymbolsIdentifier, wordFlagSymbols);
			}

			DwordFlagSymbolsContext dwordFlagSymbolsCtx = flagSymbolsCtx.dwordFlagSymbols();

			if (dwordFlagSymbolsCtx != null) {
				String dwordFlagSymbolsIdentifier = dwordFlagSymbolsCtx.symbolsIdentifier().getText();
				DWordFlagRenderer dwordFlagSymbols = new DWordFlagRenderer();

				loadSymbolDefinitions(dwordFlagSymbols, dwordFlagSymbolsCtx.symbolDefinition(),
						IntHelper::decodeUnsigned);
				addDWordAttributeRenderer(dwordFlagSymbolsIdentifier, dwordFlagSymbols);
			}

			QwordFlagSymbolsContext qwordFlagSymbolsCtx = flagSymbolsCtx.qwordFlagSymbols();

			if (qwordFlagSymbolsCtx != null) {
				String qwordFlagSymbolsIdentifier = qwordFlagSymbolsCtx.symbolsIdentifier().getText();
				QWordFlagRenderer qwordFlagSymbols = new QWordFlagRenderer();

				loadSymbolDefinitions(qwordFlagSymbols, qwordFlagSymbolsCtx.symbolDefinition(),
						LongHelper::decodeUnsigned);
				addQWordAttributeRenderer(qwordFlagSymbolsIdentifier, qwordFlagSymbols);
			}
		}
		for (FormatSpecContext formatSpecCtx : ctx.formatSpec()) {
			loadFormatSpec(formatSpecCtx, ctx);
		}
		return this;
	}

	@SuppressWarnings("null")
	private <T extends Number> void loadSymbolDefinitions(Map<T, String> symbols,
			List<SymbolDefinitionContext> symbolDefinitions, Function<String, T> parser) {
		for (SymbolDefinitionContext symbolDefinitionCtx : symbolDefinitions) {
			String numberString = symbolDefinitionCtx.symbolValue().getText();
			T number = parser.apply(numberString);
			String symbol = decodeQuotedString(symbolDefinitionCtx.symbol().getText());

			symbols.put(number, symbol);
		}
	}

	@SuppressWarnings("null")
	private void loadFormatSpec(FormatSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = reserveSpecIdentifier(specCtx.specIdentifier());
		StructSpec spec = new StructSpec();
		RawSpecContext rawCtx = specCtx.rawSpec();

		if (rawCtx != null) {
			spec.add(loadRawSpec(rawCtx, rootCtx));
		}
		for (StructSpecElementContext elementCtx : specCtx.structSpecElement()) {
			spec.add(loadStructSpecElement(elementCtx, rootCtx));
		}
		applyResultModifier(spec, specCtx.textExpression());
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyRendererModifier(spec, specCtx.compositeSpecRendererModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());

		LOG.debug(LOG_ASSIGNED_SPEC, specIdentifier, spec);

		this.specs.put(specIdentifier, () -> spec);
	}

	@SuppressWarnings("null")
	private RawSpec loadRawSpec(RawSpecContext specCtx, FormatSpecsContext rootCtx) {
		for (SpecReferenceContext specReferenceCtx : specCtx.specReference()) {
			resolveSpec(rootCtx, specReferenceCtx.referencedSpec().specIdentifier(), CompositeSpec.class);
		}

		RawSpec spec = new RawSpec();

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private StructSpec loadStructSpec(StructSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = reserveSpecIdentifier(specCtx.specIdentifier());
		StructSpec spec = loadAnonymousStructSpec(specCtx.anonymousStructSpec(), rootCtx);

		LOG.debug(LOG_ASSIGNED_SPEC, specIdentifier, spec);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private StructSpec loadAnonymousStructSpec(AnonymousStructSpecContext specCtx, FormatSpecsContext rootCtx) {
		StructSpec spec = new StructSpec();

		for (StructSpecElementContext elementCtx : specCtx.structSpecElement()) {
			spec.add(loadStructSpecElement(elementCtx, rootCtx));
		}
		applyResultModifier(spec, specCtx.textExpression());
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyRendererModifier(spec, specCtx.compositeSpecRendererModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private SequenceSpec loadSequenceSpec(SequenceSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = reserveSpecIdentifier(specCtx.specIdentifier());
		SequenceSpec spec = loadAnonymousSequenceSpec(specCtx.anonymousSequenceSpec(), rootCtx);

		LOG.debug(LOG_ASSIGNED_SPEC, specIdentifier, spec);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private SequenceSpec loadAnonymousSequenceSpec(AnonymousSequenceSpecContext specCtx, FormatSpecsContext rootCtx) {
		FormatSpec elementSpec = loadStructSpecElement(specCtx.structSpecElement(), rootCtx);
		SequenceSpec spec = new SequenceSpec(elementSpec);

		applyStopBeforeModifier(spec, specCtx.sequenceSpecStopBeforeModifier(), rootCtx);
		applyStopAfterModifier(spec, specCtx.sequenceSpecStopAfterModifier(), rootCtx);
		applyMinModifier(spec, specCtx.sequenceSpecMinModifier());
		applyMaxModifier(spec, specCtx.sequenceSpecMaxModifier());
		applySizeModifier(spec, specCtx.sequenceSpecSizeModifier());
		applyResultModifier(spec, specCtx.textExpression());
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyRendererModifier(spec, specCtx.compositeSpecRendererModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private void applyStopBeforeModifier(SequenceSpec spec, List<SequenceSpecStopBeforeModifierContext> modifierCtx,
			FormatSpecsContext rootCtx) {
		for (SequenceSpecStopBeforeModifierContext stopBeforeCtx : modifierCtx) {
			spec.stopBefore(resolveSpec(rootCtx, stopBeforeCtx.specReference().referencedSpec().specIdentifier(),
					FormatSpec.class));
		}
	}

	@SuppressWarnings("null")
	private void applyStopAfterModifier(SequenceSpec spec, List<SequenceSpecStopAfterModifierContext> modifierCtx,
			FormatSpecsContext rootCtx) {
		for (SequenceSpecStopAfterModifierContext stopAfterCtx : modifierCtx) {
			spec.stopAfter(resolveSpec(rootCtx, stopAfterCtx.specReference().referencedSpec().specIdentifier(),
					FormatSpec.class));
		}
	}

	@SuppressWarnings("null")
	private void applyMinModifier(SequenceSpec spec, List<SequenceSpecMinModifierContext> modifierCtx) {
		for (SequenceSpecMinModifierContext minCtx : modifierCtx) {
			spec.min(loadNumberExpression(minCtx.numberExpression()));
		}
	}

	@SuppressWarnings("null")
	private void applyMaxModifier(SequenceSpec spec, List<SequenceSpecMaxModifierContext> modifierCtx) {
		for (SequenceSpecMaxModifierContext maxCtx : modifierCtx) {
			spec.max(loadNumberExpression(maxCtx.numberExpression()));
		}
	}

	@SuppressWarnings("null")
	private void applySizeModifier(SequenceSpec spec, List<SequenceSpecSizeModifierContext> modifierCtx) {
		for (SequenceSpecSizeModifierContext sizeCtx : modifierCtx) {
			spec.size(loadNumberExpression(sizeCtx.numberExpression()));
		}
	}

	@SuppressWarnings("null")
	private ArraySpec loadArraySpec(ArraySpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = reserveSpecIdentifier(specCtx.specIdentifier());
		ArraySpec spec = loadAnonymousArraySpec(specCtx.anonymousArraySpec(), rootCtx);

		LOG.debug(LOG_ASSIGNED_SPEC, specIdentifier, spec);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private ArraySpec loadAnonymousArraySpec(AnonymousArraySpecContext specCtx, FormatSpecsContext rootCtx) {
		ArraySpec spec = new ArraySpec(loadNumberExpression(specCtx.numberExpression()));

		for (AttributeSpecContext attributeSpecCtx : specCtx.attributeSpec()) {
			spec.add(loadAttributeSpec(attributeSpecCtx, rootCtx));
		}
		applyResultModifier(spec, specCtx.textExpression());
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyRendererModifier(spec, specCtx.compositeSpecRendererModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private UnionSpec loadUnionSpec(UnionSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = reserveSpecIdentifier(specCtx.specIdentifier());
		UnionSpec spec = loadAnonymousUnionSpec(specCtx.anonymousUnionSpec(), rootCtx);

		LOG.debug(LOG_ASSIGNED_SPEC, specIdentifier, spec);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private UnionSpec loadAnonymousUnionSpec(AnonymousUnionSpecContext specCtx, FormatSpecsContext rootCtx) {
		UnionSpec spec = new UnionSpec();

		for (CompositeSpecExpressionContext expressionCtx : specCtx.compositeSpecExpression()) {
			spec.add(loadCompositeSpecExpression(expressionCtx, rootCtx));
		}
		applyResultModifier(spec, specCtx.textExpression());
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyRendererModifier(spec, specCtx.compositeSpecRendererModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private ScanSpec loadScanSpec(ScanSpecContext specCtx) {
		String specIdentifier = reserveSpecIdentifier(specCtx.specIdentifier());
		ScanSpec spec = loadAnonymousScanSpec(specCtx.anonymousScanSpec());

		LOG.debug(LOG_ASSIGNED_SPEC, specIdentifier, spec);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private ScanSpec loadAnonymousScanSpec(AnonymousScanSpecContext specCtx) {
		ScanSpec spec = new ScanSpec(resolveExternalReference(specCtx.externalReference(), ScanSpecConfig.class).get());

		applyResultModifier(spec, specCtx.textExpression());
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	private void applyResultModifier(CompositeSpec spec, @Nullable TextExpressionContext modiferCtx) {
		if (modiferCtx != null) {
			spec.result(loadTextExpression(modiferCtx));
		}
	}

	private void applyByteOrderModifier(CompositeSpec spec, List<CompositeSpecByteOrderModifierContext> modifierCtx) {
		for (CompositeSpecByteOrderModifierContext byteOrderCtx : modifierCtx) {
			if (byteOrderCtx.LittleEndian() != null) {
				spec.byteOrder(ByteOrder.LITTLE_ENDIAN);
			} else if (byteOrderCtx.BigEndian() != null) {
				spec.byteOrder(ByteOrder.BIG_ENDIAN);
			} else {
				throw newLoadException(byteOrderCtx, "Unexpected byte order modifier");
			}
		}
	}

	@SuppressWarnings("null")
	private void applyRendererModifier(CompositeSpec spec, List<CompositeSpecRendererModifierContext> modifierCtx) {
		for (CompositeSpecRendererModifierContext rendererCtx : modifierCtx) {
			spec.renderer(
					resolveExternalReference(rendererCtx.externalReference(), FileScannerResultRenderHandler.class));
		}
	}

	@SuppressWarnings("null")
	private void applyExportModifier(CompositeSpec spec, List<CompositeSpecExportModifierContext> modifierCtx) {
		for (CompositeSpecExportModifierContext exportCtx : modifierCtx) {
			spec.export(resolveExternalReference(exportCtx.externalReference(), FileScannerResultExportHandler.class));
		}
	}

	@SuppressWarnings("null")
	private ConditionalSpec loadConditionalSpec(ConditionalSpecContext specCtx, FormatSpecsContext rootCtx) {
		for (SpecReferenceContext specReferenceCtx : specCtx.specReference()) {
			resolveSpec(rootCtx, specReferenceCtx.referencedSpec().specIdentifier(), CompositeSpec.class);
		}

		ConditionalSpec spec = new ConditionalSpec(
				resolveExternalReference(specCtx.externalReference(), FormatSpec.class));

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private ConditionalCompositeSpec loadConditionalCompositeSpec(ConditionalCompositeSpecContext specCtx,
			FormatSpecsContext rootCtx) {
		for (SpecReferenceContext specReferenceCtx : specCtx.specReference()) {
			resolveSpec(rootCtx, specReferenceCtx.referencedSpec().specIdentifier(), CompositeSpec.class);
		}

		ConditionalCompositeSpec spec = new ConditionalCompositeSpec(
				resolveExternalReference(specCtx.externalReference(), CompositeSpec.class));

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private EncodedInputSpec loadEncodedInputSpec(EncodedInputSpecContext specCtx) {
		EncodedInputSpec spec = new EncodedInputSpec(
				resolveExternalReference(specCtx.externalReference(), EncodedInputSpecConfig.class).get());

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private DecodeAtSpec loadDecodeAtSpec(DecodeAtSpecContext specCtx, FormatSpecsContext rootCtx) {
		DecodeAtSpec spec = new DecodeAtSpec(loadCompositeSpecExpression(specCtx.compositeSpecExpression(), rootCtx));

		spec.position(loadNumberExpression(specCtx.numberExpression()));

		NumberValueContext levelCtx = specCtx.numberValue();

		if (levelCtx != null) {
			spec.level(decodeLongValue(levelCtx).intValue());
		}

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	private AttributeSpec<?> loadAttributeSpec(AttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		AttributeSpec<?> spec;
		ByteAttributeSpecContext byteAttributeSpecCtx;
		WordAttributeSpecContext wordAttributeSpecCtx;
		DwordAttributeSpecContext dwordAttributeSpecCtx;
		QwordAttributeSpecContext qwordAttributeSpecCtx;
		ByteArrayAttributeSpecContext byteArrayAttributeSpecCtx;
		WordArrayAttributeSpecContext wordArrayAttributeSpecCtx;
		DwordArrayAttributeSpecContext dwordArrayAttributeSpecCtx;
		QwordArrayAttributeSpecContext qwordArrayAttributeSpecCtx;
		CharArrayAttributeSpecContext charArrayAttributeSpecCtx;
		StringAttributeSpecContext stringAttributeSpecCtx;
		RangeSpecContext rangeSpecCtx;

		if ((byteAttributeSpecCtx = specCtx.byteAttributeSpec()) != null) {
			spec = loadByteSpec(byteAttributeSpecCtx, rootCtx);
		} else if ((wordAttributeSpecCtx = specCtx.wordAttributeSpec()) != null) {
			spec = loadWordSpec(wordAttributeSpecCtx, rootCtx);
		} else if ((dwordAttributeSpecCtx = specCtx.dwordAttributeSpec()) != null) {
			spec = loadDWordSpec(dwordAttributeSpecCtx, rootCtx);
		} else if ((qwordAttributeSpecCtx = specCtx.qwordAttributeSpec()) != null) {
			spec = loadQWordSpec(qwordAttributeSpecCtx, rootCtx);
		} else if ((byteArrayAttributeSpecCtx = specCtx.byteArrayAttributeSpec()) != null) {
			spec = loadByteArraySpec(byteArrayAttributeSpecCtx, rootCtx);
		} else if ((wordArrayAttributeSpecCtx = specCtx.wordArrayAttributeSpec()) != null) {
			spec = loadWordArraySpec(wordArrayAttributeSpecCtx, rootCtx);
		} else if ((dwordArrayAttributeSpecCtx = specCtx.dwordArrayAttributeSpec()) != null) {
			spec = loadDWordArraySpec(dwordArrayAttributeSpecCtx, rootCtx);
		} else if ((qwordArrayAttributeSpecCtx = specCtx.qwordArrayAttributeSpec()) != null) {
			spec = loadQWordArraySpec(qwordArrayAttributeSpecCtx, rootCtx);
		} else if ((charArrayAttributeSpecCtx = specCtx.charArrayAttributeSpec()) != null) {
			spec = loadCharArraySpec(charArrayAttributeSpecCtx, rootCtx);
		} else if ((stringAttributeSpecCtx = specCtx.stringAttributeSpec()) != null) {
			spec = loadStringSpec(stringAttributeSpecCtx, rootCtx);
		} else if ((rangeSpecCtx = specCtx.rangeSpec()) != null) {
			spec = loadRangeSpec(rangeSpecCtx, rootCtx);
		} else {
			throw newLoadException(specCtx, "Unexpected attribute spec");
		}
		return spec;
	}

	@SuppressWarnings("null")
	private ByteSpec loadByteSpec(ByteAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		ByteSpec spec = new ByteSpec(loadTextExpression(specCtx.textExpression()));

		applyValidateNumberModifier(spec, specCtx.attributeValidateNumberModifier(), ByteHelper::decodeUnsigned,
				this.byteSymbolsMap);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.byteAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.byteAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private WordSpec loadWordSpec(WordAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		WordSpec spec = new WordSpec(loadTextExpression(specCtx.textExpression()));

		applyValidateNumberModifier(spec, specCtx.attributeValidateNumberModifier(), ShortHelper::decodeUnsigned,
				this.wordSymbolsMap);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.wordAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.wordAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private DWordSpec loadDWordSpec(DwordAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		DWordSpec spec = new DWordSpec(loadTextExpression(specCtx.textExpression()));

		applyValidateNumberModifier(spec, specCtx.attributeValidateNumberModifier(), IntHelper::decodeUnsigned,
				this.dwordSymbolsMap);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.dwordAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.dwordAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private QWordSpec loadQWordSpec(QwordAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		QWordSpec spec = new QWordSpec(loadTextExpression(specCtx.textExpression()));

		applyValidateNumberModifier(spec, specCtx.attributeValidateNumberModifier(), LongHelper::decodeUnsigned,
				this.qwordSymbolsMap);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.qwordAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.qwordAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private ByteArraySpec loadByteArraySpec(ByteArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		ByteArraySpec spec = new ByteArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		applyValidateNumberArrayModifier(spec, specCtx.attributeValidateNumberArrayModifier(),
				ByteHelper::decodeUnsignedArray);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.byteArrayAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.byteArrayAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private WordArraySpec loadWordArraySpec(WordArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		WordArraySpec spec = new WordArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		applyValidateNumberArrayModifier(spec, specCtx.attributeValidateNumberArrayModifier(),
				ShortHelper::decodeUnsignedArray);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.wordArrayAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.wordArrayAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private DWordArraySpec loadDWordArraySpec(DwordArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		DWordArraySpec spec = new DWordArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		applyValidateNumberArrayModifier(spec, specCtx.attributeValidateNumberArrayModifier(),
				IntHelper::decodeUnsignedArray);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.dwordArrayAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.dwordArrayAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private QWordArraySpec loadQWordArraySpec(QwordArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		QWordArraySpec spec = new QWordArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		applyValidateNumberArrayModifier(spec, specCtx.attributeValidateNumberArrayModifier(),
				LongHelper::decodeUnsignedArray);
		applyFormatModifier(spec, specCtx.attributeFormatModifier(), this.qwordArrayAttributeFormatter);
		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.qwordArrayAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private CharArraySpec loadCharArraySpec(CharArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		CharArraySpec spec = new CharArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		applyCharsetModifier(spec, specCtx.stringAttributeCharsetModifier());
		applyValidateStringModifier(spec, specCtx.attributeValidateStringModifier());
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private StringSpec loadStringSpec(StringAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		StringSpec spec = new StringSpec(loadTextExpression(specCtx.textExpression()));

		applyCharsetModifier(spec, specCtx.stringAttributeCharsetModifier());
		applyValidateStringModifier(spec, specCtx.attributeValidateStringModifier());
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	@SuppressWarnings("null")
	private RangeSpec loadRangeSpec(RangeSpecContext specCtx, FormatSpecsContext rootCtx) {
		RangeSpec spec = new RangeSpec(loadTextExpression(specCtx.textExpression()))
				.size(loadNumberExpression(specCtx.numberExpression()));

		applyRendererModifier(spec, specCtx.attributeRendererModifier(), this.streamValueAttributeRenderer);
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);

		LOG.debug(LOG_LOADED_SPEC, spec);

		return spec;
	}

	private <T extends Number> void applyValidateNumberModifier(NumberAttributeSpec<T> spec,
			List<AttributeValidateNumberModifierContext> modifierCtx, Function<String, T> decode,
			Map<String, Set<T>> symbolsMap) {
		for (AttributeValidateNumberModifierContext validateCtx : modifierCtx) {
			NumberValueSetContext numberValueSetCtx;
			SpecReferenceContext specReferenceCtx;

			if ((numberValueSetCtx = validateCtx.numberValueSet()) != null) {
				spec.validate(decodeNumberValueSet(numberValueSetCtx, decode));
			} else if ((specReferenceCtx = validateCtx.specReference()) != null) {
				String specIdentifier = specReferenceCtx.referencedSpec().getText();
				Set<T> symbols = symbolsMap.get(specIdentifier);

				if (symbols == null) {
					throw newLoadException(specReferenceCtx, "Unknown symbols reference @%s", specIdentifier);
				}
				spec.validate(symbols);
			} else {
				throw newLoadException(validateCtx, "Unexpected validate number modifier");
			}
		}
	}

	@SuppressWarnings("null")
	private <T extends Number> Set<T> decodeNumberValueSet(NumberValueSetContext numberValueSetCtx,
			Function<String, T> decode) {
		Set<T> numberValueSet = new HashSet<>();

		for (NumberValueContext numberValueCtx : numberValueSetCtx.numberValue()) {
			numberValueSet.add(decode.apply(numberValueCtx.getText()));
		}
		return numberValueSet;
	}

	private <T> void applyValidateNumberArrayModifier(AttributeSpec<T> spec,
			List<AttributeValidateNumberArrayModifierContext> modifierCtx, Function<String[], T> decode) {
		for (AttributeValidateNumberArrayModifierContext validateCtx : modifierCtx) {
			NumberArrayValueSetContext numberArrayValueSetCtx;

			if ((numberArrayValueSetCtx = validateCtx.numberArrayValueSet()) != null) {
				spec.validate(decodeNumberArrayValueSet(numberArrayValueSetCtx, decode));
			} else {
				throw newLoadException(validateCtx, "Unexpected validate number array modifier");
			}
		}
	}

	@SuppressWarnings("null")
	private <T> Set<T> decodeNumberArrayValueSet(NumberArrayValueSetContext numberArrayValueSetCtx,
			Function<String[], T> decode) {
		Set<T> numberArrayValueSet = new HashSet<>();

		for (NumberArrayValueContext numberArrayValueCtx : numberArrayValueSetCtx.numberArrayValue()) {
			T numberArrayValue = decodeNumberArrayValue(numberArrayValueCtx.numberValue(), decode);

			numberArrayValueSet.add(numberArrayValue);
		}
		return numberArrayValueSet;
	}

	private <T> T decodeNumberArrayValue(List<NumberValueContext> numberValueCtx, Function<String[], T> decode) {
		String[] numberArrayValueTexts = new String[numberValueCtx.size()];
		int numberArrayValueTextIndex = 0;

		for (NumberValueContext numberValue : numberValueCtx) {
			numberArrayValueTexts[numberArrayValueTextIndex] = numberValue.getText();
			numberArrayValueTextIndex++;
		}
		return decode.apply(numberArrayValueTexts);
	}

	@SuppressWarnings({ "null", "squid:S3776" })
	private void applyValidateStringModifier(AttributeSpec<String> spec,
			List<AttributeValidateStringModifierContext> modifierCtx) {
		for (AttributeValidateStringModifierContext validateCtx : modifierCtx) {
			ValidationTextSetContext validationTextSetCtx;

			if ((validationTextSetCtx = validateCtx.validationTextSet()) != null) {
				Set<String> simpleTextSet = new HashSet<>();
				Set<Pattern> patternSet = new HashSet<>();

				for (ValidationTextContext validationTextCtx : validationTextSetCtx.validationText()) {
					RegexTextContext regexTextCtx;
					SimpleTextContext simpleTextCtx;

					if ((regexTextCtx = validationTextCtx.regexText()) != null) {
						patternSet.add(Pattern.compile(decodeReqexString(regexTextCtx.getText())));
					} else if ((simpleTextCtx = validationTextCtx.simpleText()) != null) {
						simpleTextSet.add(decodeQuotedString(simpleTextCtx.getText()));
					} else {
						throw newLoadException(validationTextCtx, "Unexpected validate string argument");
					}
				}
				if (patternSet.isEmpty()) {
					spec.validate(simpleTextSet);
				} else {
					spec.validate(s -> {
						boolean valid = simpleTextSet.contains(s);

						if (!valid) {
							for (Pattern pattern : patternSet) {
								if (pattern.matcher(s).matches()) {
									valid = true;
									break;
								}
							}
						}
						return valid;
					});
				}
			} else {
				throw newLoadException(validateCtx, "Unexpected validate string modifier");
			}
		}
	}

	@SuppressWarnings("null")
	private <T> void applyFormatModifier(AttributeSpec<T> spec, List<AttributeFormatModifierContext> modifierCtx,
			Map<String, AttributeFormatter<T>> formatters) {
		for (AttributeFormatModifierContext formatCtx : modifierCtx) {
			FormatTextContext formatTextCtx;
			SpecReferenceContext specReferenceCtx;

			if ((formatTextCtx = formatCtx.formatText()) != null) {
				spec.format(decodeQuotedString(formatTextCtx.getText()));
			} else if ((specReferenceCtx = formatCtx.specReference()) != null) {
				String specIdentifier = specReferenceCtx.referencedSpec().getText();
				AttributeFormatter<T> formatter = formatters.get(specIdentifier);

				if (formatter == null) {
					throw newLoadException(specReferenceCtx, "Unknown formatter reference @%s", specIdentifier);
				}
				spec.format(formatter);
			} else {
				throw newLoadException(formatCtx, "Unexpected format modifier");
			}
		}
	}

	private <T> void applyRendererModifier(AttributeSpec<T> spec, List<AttributeRendererModifierContext> modifierCtx,
			Map<String, AttributeRenderer<T>> formatters) {
		for (AttributeRendererModifierContext formatCtx : modifierCtx) {
			SpecReferenceContext specReferenceCtx;

			if ((specReferenceCtx = formatCtx.specReference()) != null) {
				String specIdentifier = specReferenceCtx.referencedSpec().getText();
				AttributeRenderer<T> renderer = formatters.get(specIdentifier);

				if (renderer == null) {
					throw newLoadException(specReferenceCtx, "Unknown renderer reference @%s", specIdentifier);
				}
				spec.renderer(renderer);
			} else {
				throw newLoadException(formatCtx, "Unexpected renderer modifier");
			}
		}
	}

	@SuppressWarnings("null")
	private void applyCharsetModifier(StringAttributeSpec spec,
			List<StringAttributeCharsetModifierContext> modifierCtx) {
		for (StringAttributeCharsetModifierContext charsetCtx : modifierCtx) {
			SimpleTextContext simpleTextCtx;

			if ((simpleTextCtx = charsetCtx.simpleText()) != null) {
				String charsetName = decodeQuotedString(simpleTextCtx.getText());

				spec.charset(Charset.forName(charsetName));
			} else {
				throw newLoadException(charsetCtx, "Unexpected charset modifier");
			}
		}
	}

	@SuppressWarnings("null")
	private void bindAttributeSpecIfNeeded(AttributeSpec<?> spec, @Nullable SpecIdentifierContext specIdentifierCtx,
			@Nullable ScopeIdentifierContext scopeIdentiferCtx, FormatSpecsContext rootCtx) {
		if (specIdentifierCtx != null) {
			String specIdentifier = reserveSpecIdentifier(specIdentifierCtx);

			if (scopeIdentiferCtx != null) {
				this.lateBindings.add(
						() -> spec.bind(resolveSpec(rootCtx, scopeIdentiferCtx.specIdentifier(), CompositeSpec.class)));
			} else {
				spec.bind();
			}

			LOG.debug(LOG_ASSIGNED_SPEC, specIdentifier, spec);

			this.specs.put(specIdentifier, () -> spec);
		}
	}

	@SuppressWarnings("null")
	private FormatSpec loadStructSpecElement(StructSpecElementContext elementCtx, FormatSpecsContext rootCtx) {
		FormatSpec element;
		SpecReferenceContext specReferenceCtx;
		AttributeSpecContext attributeSpecCtx;
		AnonymousStructSpecContext anonymousStructSpecCtx;
		AnonymousArraySpecContext anonymousArraySpecCtx;
		AnonymousSequenceSpecContext anonymousSequenceSpecCtx;
		AnonymousUnionSpecContext anonymousUnionSpecCtx;
		AnonymousScanSpecContext anonymousScanSpecCtx;
		ConditionalSpecContext conditionalSpecCtx;
		EncodedInputSpecContext encodedInputSpecCtx;
		DecodeAtSpecContext decodeAtSpecCtx;

		if ((specReferenceCtx = elementCtx.specReference()) != null) {
			element = resolveSpec(rootCtx, specReferenceCtx.referencedSpec().specIdentifier(), FormatSpec.class);
		} else if ((attributeSpecCtx = elementCtx.attributeSpec()) != null) {
			element = loadAttributeSpec(attributeSpecCtx, rootCtx);
		} else if ((anonymousStructSpecCtx = elementCtx.anonymousStructSpec()) != null) {
			element = loadAnonymousStructSpec(anonymousStructSpecCtx, rootCtx);
		} else if ((anonymousArraySpecCtx = elementCtx.anonymousArraySpec()) != null) {
			element = loadAnonymousArraySpec(anonymousArraySpecCtx, rootCtx);
		} else if ((anonymousSequenceSpecCtx = elementCtx.anonymousSequenceSpec()) != null) {
			element = loadAnonymousSequenceSpec(anonymousSequenceSpecCtx, rootCtx);
		} else if ((anonymousUnionSpecCtx = elementCtx.anonymousUnionSpec()) != null) {
			element = loadAnonymousUnionSpec(anonymousUnionSpecCtx, rootCtx);
		} else if ((anonymousScanSpecCtx = elementCtx.anonymousScanSpec()) != null) {
			element = loadAnonymousScanSpec(anonymousScanSpecCtx);
		} else if ((conditionalSpecCtx = elementCtx.conditionalSpec()) != null) {
			element = loadConditionalSpec(conditionalSpecCtx, rootCtx);
		} else if ((encodedInputSpecCtx = elementCtx.encodedInputSpec()) != null) {
			element = loadEncodedInputSpec(encodedInputSpecCtx);
		} else if ((decodeAtSpecCtx = elementCtx.decodeAtSpec()) != null) {
			element = loadDecodeAtSpec(decodeAtSpecCtx, rootCtx);
		} else {
			throw newLoadException(elementCtx, "Unexpected format spec element");
		}
		return element;
	}

	@SuppressWarnings("null")
	private CompositeSpec loadCompositeSpecExpression(CompositeSpecExpressionContext ctx, FormatSpecsContext rootCtx) {
		CompositeSpec spec;
		SpecReferenceContext specReferenceCtx;
		AnonymousStructSpecContext anonymousStructSpecCtx;
		AnonymousUnionSpecContext anonymousUnionSpecCtx;
		AnonymousSequenceSpecContext anonymousSequenceSpecCtx;
		ConditionalCompositeSpecContext conditionalCompositeSpecCtx;

		if ((specReferenceCtx = ctx.specReference()) != null) {
			spec = resolveSpec(rootCtx, specReferenceCtx.referencedSpec().specIdentifier(), CompositeSpec.class);
		} else if ((anonymousStructSpecCtx = ctx.anonymousStructSpec()) != null) {
			spec = loadAnonymousStructSpec(anonymousStructSpecCtx, rootCtx);
		} else if ((anonymousUnionSpecCtx = ctx.anonymousUnionSpec()) != null) {
			spec = loadAnonymousUnionSpec(anonymousUnionSpecCtx, rootCtx);
		} else if ((anonymousSequenceSpecCtx = ctx.anonymousSequenceSpec()) != null) {
			spec = loadAnonymousSequenceSpec(anonymousSequenceSpecCtx, rootCtx);
		} else if ((conditionalCompositeSpecCtx = ctx.conditionalCompositeSpec()) != null) {
			spec = loadConditionalCompositeSpec(conditionalCompositeSpecCtx, rootCtx);
		} else {
			throw newLoadException(ctx, "Unexpected composite spec element");
		}
		return spec;
	}

	@SuppressWarnings({ "null", "unchecked" })
	private Supplier<? extends Number> loadNumberExpression(NumberExpressionContext ctx) {
		Supplier<? extends Number> numberExpression;
		NumberValueContext numberValueCtx;
		SpecReferenceContext specReferenceCtx;
		ExternalReferenceContext externalReferenceCtx;

		if ((numberValueCtx = ctx.numberValue()) != null) {
			numberExpression = FinalSupplier.of(Long.decode(numberValueCtx.getText()));
		} else if ((specReferenceCtx = ctx.specReference()) != null) {
			numberExpression = resolveSpec(specReferenceCtx.referencedSpec().specIdentifier(),
					NumberAttributeSpec.class);
		} else if ((externalReferenceCtx = ctx.externalReference()) != null) {
			numberExpression = resolveExternalReference(externalReferenceCtx, Number.class);
		} else {
			throw newLoadException(ctx, "Unexpected number expression");
		}
		return numberExpression;
	}

	private Long decodeLongValue(NumberValueContext ctx) {
		Long value;
		try {
			value = Long.decode(ctx.getText());
		} catch (NumberFormatException e) {
			throw newLoadException(e, ctx, "Unexpected number value");
		}
		return value;
	}

	@SuppressWarnings("null")
	private Supplier<String> loadTextExpression(TextExpressionContext ctx) {
		Supplier<String> textExpression;
		SimpleTextContext simpleTextCtx;
		FormatTextContext formatTextCtx;
		ExternalReferenceContext externalReferenceCtx;

		if ((simpleTextCtx = ctx.simpleText()) != null) {
			textExpression = FinalSupplier.of(decodeQuotedString(simpleTextCtx.getText()));
		} else if ((formatTextCtx = ctx.formatText()) != null) {
			String formatText = decodeQuotedString(formatTextCtx.getText());
			List<AttributeSpec<?>> argumentSpecs = new ArrayList<>();

			for (SpecReferenceContext specReferenceCtx : ctx.specReference()) {
				argumentSpecs.add(resolveSpec(specReferenceCtx.referencedSpec().specIdentifier(), AttributeSpec.class));
			}
			textExpression = () -> String.format(formatText, argumentSpecs.stream().map(Supplier::get)
					.map(o -> (o instanceof String ? Strings.encode(StringHelper.strip((String) o)) : o)).toArray());
		} else if ((externalReferenceCtx = ctx.externalReference()) != null) {
			textExpression = resolveExternalReference(externalReferenceCtx, String.class);
		} else {
			throw newLoadException(ctx, "Unexpected text expression");
		}
		return textExpression;
	}

	private String decodeReqexString(String regexString) {
		return Strings.decode(regexString.substring(2, regexString.length() - 1));
	}

	private String decodeQuotedString(String quotedString) {
		return Strings.decode(quotedString.substring(1, quotedString.length() - 1));
	}

	private static final String UNKNOWN_EXTERNAL_REFERENCE = "Unknown reference #%s";
	private static final String INVALID_EXTERNAL_REFERENCE = "Invalid reference #%s (expected type: %s actual type: %s)";

	@SuppressWarnings("squid:S3011")
	private <T> Supplier<T> resolveExternalReference(ExternalReferenceContext externalReferenceCtx, Class<T> type) {
		String methodIdentifier = externalReferenceCtx.referencedExternal().getText();
		Method method;

		try {
			method = getClass().getDeclaredMethod(methodIdentifier);
		} catch (NoSuchMethodException e) {
			throw newLoadException(e, externalReferenceCtx, UNKNOWN_EXTERNAL_REFERENCE, methodIdentifier);
		}

		Class<?> methodType = method.getReturnType();

		if (!type.isAssignableFrom(methodType)) {
			throw newLoadException(externalReferenceCtx, INVALID_EXTERNAL_REFERENCE, methodIdentifier,
					type.getSimpleName(), methodType.getSimpleName());
		}
		method.setAccessible(true);
		return () -> {
			try {
				return type.cast(method.invoke(this));
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();

				throw Exceptions.toRuntime(cause != null ? cause : e);
			} catch (ReflectiveOperationException e) {
				throw Exceptions.toRuntime(e);
			}
		};
	}

	@SuppressWarnings("null")
	private String reserveSpecIdentifier(SpecIdentifierContext specIdentifierCtx) {
		String specIdentifier = specIdentifierCtx.getText();

		if (this.specs.containsKey(specIdentifier)) {
			throw newLoadException(specIdentifierCtx, "Duplicate spec identifier @%s", specIdentifier);
		}
		this.specs.put(specIdentifier, () -> {
			throw newLoadException(specIdentifierCtx, "Cyclic spec reference @%s", specIdentifier);
		});
		return specIdentifier;
	}

	private static final String UNKNOWN_SPEC_REFERENCE = "Unknown reference @%s";
	private static final String INVALID_SPEC_REFERENCE = "Invalid reference @%s (expected type: %s actual type: %s)";

	@SuppressWarnings("squid:S3776")
	private <T extends FormatSpec> T resolveSpec(FormatSpecsContext rootCtx, SpecIdentifierContext specIdentifierCtx,
			Class<T> specType) {
		String specIdentifier = specIdentifierCtx.getText();
		Supplier<FormatSpec> resolvedSpecSupplier = this.specs.get(specIdentifier);
		FormatSpec resolvedSpec = null;

		if (resolvedSpecSupplier == null) {
			for (StructSpecContext structSpecCtx : rootCtx.structSpec()) {
				if (specIdentifier.equals(structSpecCtx.specIdentifier().getText())) {
					resolvedSpec = loadStructSpec(structSpecCtx, rootCtx);
				}
			}
			for (ArraySpecContext arraySpecCtx : rootCtx.arraySpec()) {
				if (specIdentifier.equals(arraySpecCtx.specIdentifier().getText())) {
					resolvedSpec = loadArraySpec(arraySpecCtx, rootCtx);
				}
			}
			for (UnionSpecContext unionSpecCtx : rootCtx.unionSpec()) {
				if (specIdentifier.equals(unionSpecCtx.specIdentifier().getText())) {
					resolvedSpec = loadUnionSpec(unionSpecCtx, rootCtx);
				}
			}
			for (SequenceSpecContext sequenceSpecCtx : rootCtx.sequenceSpec()) {
				if (specIdentifier.equals(sequenceSpecCtx.specIdentifier().getText())) {
					resolvedSpec = loadSequenceSpec(sequenceSpecCtx, rootCtx);
				}
			}
			for (ScanSpecContext scanSpecCtx : rootCtx.scanSpec()) {
				if (specIdentifier.equals(scanSpecCtx.specIdentifier().getText())) {
					resolvedSpec = loadScanSpec(scanSpecCtx);
				}
			}
		} else {
			resolvedSpec = resolvedSpecSupplier.get();
		}
		if (resolvedSpec == null) {
			throw newLoadException(specIdentifierCtx, UNKNOWN_SPEC_REFERENCE, specIdentifier);
		}

		Class<?> resolvedSpecType = resolvedSpec.getClass();

		if (!specType.isAssignableFrom(resolvedSpecType)) {
			throw newLoadException(specIdentifierCtx, INVALID_SPEC_REFERENCE, specIdentifier, specType.getSimpleName(),
					resolvedSpecType.getSimpleName());
		}
		return specType.cast(resolvedSpec);
	}

	private <T extends FormatSpec> T resolveSpec(SpecIdentifierContext specIdentifierCtx, Class<T> specType) {
		String specIdentifier = specIdentifierCtx.getText();
		Supplier<FormatSpec> resolvedSpecSupplier = this.specs.get(specIdentifier);

		if (resolvedSpecSupplier == null) {
			throw newLoadException(specIdentifierCtx, UNKNOWN_SPEC_REFERENCE, specIdentifier);
		}

		FormatSpec resolvedSpec = resolvedSpecSupplier.get();
		Class<?> resolvedSpecType = resolvedSpec.getClass();

		if (!specType.isAssignableFrom(resolvedSpecType)) {
			throw newLoadException(specIdentifierCtx, INVALID_SPEC_REFERENCE, specIdentifier, specType.getSimpleName(),
					resolvedSpecType.getSimpleName());
		}
		return specType.cast(resolvedSpec);
	}

	/**
	 * Resolves a previously loaded {@linkplain FormatSpec} instance.
	 *
	 * @param <T> the actual {@linkplain FormatSpec} type to resolve.
	 * @param specIdentifier the identifier of the {@linkplain FormatSpec} instance to resolve.
	 * @param specType the type of the {@linkplain FormatSpec} instance to resolve.
	 * @return the resolved {@linkplain FormatSpec} instance.
	 */
	protected <T extends FormatSpec> T resolveSpec(String specIdentifier, Class<T> specType) {
		Supplier<FormatSpec> resolvedSpecSupplier = this.specs.get(specIdentifier);

		if (resolvedSpecSupplier == null) {
			throw new IllegalArgumentException(String.format(UNKNOWN_SPEC_REFERENCE, specIdentifier));
		}

		FormatSpec resolvedSpec = resolvedSpecSupplier.get();
		Class<?> resolvedSpecType = resolvedSpec.getClass();

		if (!specType.isAssignableFrom(resolvedSpecType)) {
			throw new IllegalArgumentException(String.format(INVALID_SPEC_REFERENCE, specIdentifier,
					specType.getSimpleName(), resolvedSpecType.getSimpleName()));
		}
		return specType.cast(resolvedSpec);
	}

	/**
	 * Lazily resolves a previously loaded {@linkplain FormatSpec} instance.
	 *
	 * @param <T> the actual {@linkplain FormatSpec} type to resolve.
	 * @param specIdentifier the identifier of the {@linkplain FormatSpec} instance to resolve.
	 * @param specType the type of the {@linkplain FormatSpec} instance to resolve.
	 * @return the resolved {@linkplain FormatSpec} instance.
	 */
	protected <T extends FormatSpec> Lazy<T> resolveLazy(String specIdentifier, Class<T> specType) {
		return new Lazy<>(() -> resolveSpec(specIdentifier, specType));
	}

	private IllegalArgumentException newLoadException(ParserRuleContext ctx, String format, Object... args) {
		return newLoadException(null, ctx, format, args);
	}

	private IllegalArgumentException newLoadException(@Nullable Throwable cause, ParserRuleContext ctx, String format,
			Object... args) {
		StringBuilder message = new StringBuilder();

		Token startToken = ctx.getStart();

		message.append(getFormatSpecResource()).append("[").append(startToken.getLine()).append(":")
				.append(startToken.getCharPositionInLine()).append("] ");
		message.append(String.format(format, args));
		return new IllegalArgumentException(message.toString(), cause);
	}

}
