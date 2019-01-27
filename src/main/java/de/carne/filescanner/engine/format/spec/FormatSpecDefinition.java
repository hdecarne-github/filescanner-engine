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
package de.carne.filescanner.engine.format.spec;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.Exceptions;
import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.PrettyFormat;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarBaseVisitor;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarLexer;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousSequenceSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousStructSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AnonymousUnionSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.AttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CharArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CompositeSpecByteOrderModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CompositeSpecElementContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.CompositeSpecExportModifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ConditionalSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.EncodedSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ExternalReferenceContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatSpecElementContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatSpecsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatTextContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.NumberExpressionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.NumberValueContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.QwordSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ScopeIdentifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SequenceSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SimpleTextContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SpecIdentifierContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SpecReferenceContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.StructSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SymbolDefinitionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.TextExpressionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.UnionSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordArrayAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordAttributeSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordSymbolsContext;
import de.carne.filescanner.engine.transfer.FileScannerResultExportHandler;
import de.carne.filescanner.engine.util.FinalSupplier;
import de.carne.util.Strings;

/**
 * Base class for all kinds {@linkplain FormatSpec} based format definitions.
 */
public abstract class FormatSpecDefinition {

	private static final Log LOG = new Log();

	private final Map<String, AttributeFormatter<Byte>> byteAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<Short>> wordAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<Integer>> dwordAttributeFormatter = new HashMap<>();
	private final Map<String, AttributeFormatter<Long>> qwordAttributeFormatter = new HashMap<>();

	private final Map<String, AttributeRenderer<Byte>> byteAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<Short>> wordAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<Integer>> dwordAttributeRenderer = new HashMap<>();
	private final Map<String, AttributeRenderer<Long>> qwordAttributeRenderer = new HashMap<>();

	private final Map<String, FlagRenderer<Byte>> byteFlagRenderer = new HashMap<>();
	private final Map<String, FlagRenderer<Short>> wordFlagRenderer = new HashMap<>();
	private final Map<String, FlagRenderer<Integer>> dwordFlagRenderer = new HashMap<>();
	private final Map<String, FlagRenderer<Long>> qwordFlagRenderer = new HashMap<>();

	private final Map<String, Supplier<FormatSpec>> specs = new HashMap<>();

	private final List<Runnable> lateBindings = new LinkedList<>();

	/**
	 * Constructs a new {@linkplain FormatSpecDefinition} instance.
	 */
	protected FormatSpecDefinition() {
		// @PrettyFormat
		this.byteAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.BYTE_FORMATTER);
		this.wordAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.SHORT_FORMATTER);
		this.dwordAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.INT_FORMATTER);
		this.qwordAttributeFormatter.put(PrettyFormat.class.getSimpleName(), PrettyFormat.LONG_FORMATTER);
		// @HexFormat
		this.byteAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.BYTE_FORMATTER);
		this.wordAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.SHORT_FORMATTER);
		this.dwordAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.INT_FORMATTER);
		this.qwordAttributeFormatter.put(HexFormat.class.getSimpleName(), HexFormat.LONG_FORMATTER);
		// @SizeRenderer
		this.byteAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.BYTE_RENDERER);
		this.wordAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.SHORT_RENDERER);
		this.dwordAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.INT_RENDERER);
		this.qwordAttributeRenderer.put(SizeRenderer.class.getSimpleName(), SizeRenderer.LONG_RENDERER);
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
	 * Adds a byte {@linkplain FlagRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain FlagRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addByteFlagRenderer(String identifier, FlagRenderer<Byte> renderer) {
		if (this.byteFlagRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of byte flag renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a word {@linkplain FlagRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain FlagRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addWordFlagRenderer(String identifier, FlagRenderer<Short> renderer) {
		if (this.wordFlagRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of word flag renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a double word {@linkplain FlagRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain FlagRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addDWordFlagRenderer(String identifier, FlagRenderer<Integer> renderer) {
		if (this.dwordFlagRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of dword flag renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Adds a quad word {@linkplain FlagRenderer}.
	 *
	 * @param identifier the renderer identifier.
	 * @param renderer the {@linkplain FlagRenderer} to add.
	 * @return the update {@linkplain FormatSpecDefinition}.
	 */
	public FormatSpecDefinition addQWordFlagRenderer(String identifier, FlagRenderer<Long> renderer) {
		if (this.qwordFlagRenderer.put(identifier, renderer) != null) {
			LOG.warning("Redefinition of qword flag renderer ''{0}''", identifier);
		}
		return this;
	}

	/**
	 * Loads and initializes the {@linkplain FormatSpecDefinition}.
	 *
	 * @throws IOException if the resource cannot be loaded or parsed.
	 */
	public void load() throws IOException {
		URL formatSpecResourceUrl = getFormatSpecResource();

		try (InputStream resourceStream = formatSpecResourceUrl.openStream()) {
			CharStream input = CharStreams.fromStream(resourceStream);
			FormatSpecGrammarLexer lexer = new FormatSpecGrammarLexer(input);
			TokenStream tokens = new CommonTokenStream(lexer);
			FormatSpecGrammarParser parser = new FormatSpecGrammarParser(tokens);
			Loader loader = new Loader(this::loadHelper);

			loader.visitFormatSpecs(parser.formatSpecs());
		} catch (RuntimeException e) {
			throw new IOException("Failed to load format spec resource: " + formatSpecResourceUrl, e);
		}
		for (Runnable lateBinding : this.lateBindings) {
			lateBinding.run();
		}
		this.lateBindings.clear();
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

				loadSymbolDefinitions(byteSymbols, byteSymbolsCtx.symbolDefinition(), Byte::decode);
				addByteAttributeRenderer(byteSymbolsIdentifier, byteSymbols);
			}

			WordSymbolsContext wordSymbolsCtx = symbolsCtx.wordSymbols();

			if (wordSymbolsCtx != null) {
				String wordSymbolsIdentifier = wordSymbolsCtx.symbolsIdentifier().getText();
				WordSymbolRenderer wordSymbols = new WordSymbolRenderer();

				loadSymbolDefinitions(wordSymbols, wordSymbolsCtx.symbolDefinition(), Short::decode);
				addWordAttributeRenderer(wordSymbolsIdentifier, wordSymbols);
			}

			DwordSymbolsContext dwordSymbolsCtx = symbolsCtx.dwordSymbols();

			if (dwordSymbolsCtx != null) {
				String dwordSymbolsIdentifier = dwordSymbolsCtx.symbolsIdentifier().getText();
				DWordSymbolRenderer dwordSymbols = new DWordSymbolRenderer();

				loadSymbolDefinitions(dwordSymbols, dwordSymbolsCtx.symbolDefinition(), Integer::decode);
				addDWordAttributeRenderer(dwordSymbolsIdentifier, dwordSymbols);
			}

			QwordSymbolsContext qwordSymbolsCtx = symbolsCtx.qwordSymbols();

			if (qwordSymbolsCtx != null) {
				String qwordSymbolsIdentifier = qwordSymbolsCtx.symbolsIdentifier().getText();
				QWordSymbolRenderer qwordSymbols = new QWordSymbolRenderer();

				loadSymbolDefinitions(qwordSymbols, qwordSymbolsCtx.symbolDefinition(), Long::decode);
				addQWordAttributeRenderer(qwordSymbolsIdentifier, qwordSymbols);
			}
		}
		for (FlagSymbolsContext flagSymbolsCtx : ctx.flagSymbols()) {
			ByteFlagSymbolsContext byteFlagSymbolsCtx = flagSymbolsCtx.byteFlagSymbols();

			if (byteFlagSymbolsCtx != null) {
				String byteFlagSymbolsIdentifier = byteFlagSymbolsCtx.symbolsIdentifier().getText();
				ByteFlagRenderer byteFlagSymbols = new ByteFlagRenderer();

				loadSymbolDefinitions(byteFlagSymbols, byteFlagSymbolsCtx.symbolDefinition(), Byte::decode);
				addByteFlagRenderer(byteFlagSymbolsIdentifier, byteFlagSymbols);
			}

			WordFlagSymbolsContext wordFlagSymbolsCtx = flagSymbolsCtx.wordFlagSymbols();

			if (wordFlagSymbolsCtx != null) {
				String wordFlagSymbolsIdentifier = wordFlagSymbolsCtx.symbolsIdentifier().getText();
				WordFlagRenderer wordFlagSymbols = new WordFlagRenderer();

				loadSymbolDefinitions(wordFlagSymbols, wordFlagSymbolsCtx.symbolDefinition(), Short::decode);
				addWordFlagRenderer(wordFlagSymbolsIdentifier, wordFlagSymbols);
			}

			DwordFlagSymbolsContext dwordFlagSymbolsCtx = flagSymbolsCtx.dwordFlagSymbols();

			if (dwordFlagSymbolsCtx != null) {
				String dwordFlagSymbolsIdentifier = dwordFlagSymbolsCtx.symbolsIdentifier().getText();
				DWordFlagRenderer dwordFlagSymbols = new DWordFlagRenderer();

				loadSymbolDefinitions(dwordFlagSymbols, dwordFlagSymbolsCtx.symbolDefinition(), Integer::decode);
				addDWordFlagRenderer(dwordFlagSymbolsIdentifier, dwordFlagSymbols);
			}

			QwordFlagSymbolsContext qwordFlagSymbolsCtx = flagSymbolsCtx.qwordFlagSymbols();

			if (qwordFlagSymbolsCtx != null) {
				String qwordFlagSymbolsIdentifier = qwordFlagSymbolsCtx.symbolsIdentifier().getText();
				QWordFlagRenderer qwordFlagSymbols = new QWordFlagRenderer();

				loadSymbolDefinitions(qwordFlagSymbols, qwordFlagSymbolsCtx.symbolDefinition(), Long::decode);
				addQWordFlagRenderer(qwordFlagSymbolsIdentifier, qwordFlagSymbols);
			}
		}
		for (FormatSpecContext formatSpecCtx : ctx.formatSpec()) {
			loadFormatSpec(formatSpecCtx, ctx);
		}
		return this;
	}

	private static <T extends Number> void loadSymbolDefinitions(Map<T, String> symbols,
			List<SymbolDefinitionContext> symbolDefinitions, Function<String, T> parser) {
		for (SymbolDefinitionContext symbolDefinitionCtx : symbolDefinitions) {
			String numberString = Objects.requireNonNull(symbolDefinitionCtx.symbolValue().getText());
			T number = parser.apply(numberString);
			String symbol = Objects.requireNonNull(symbolDefinitionCtx.symbol().getText());

			symbols.put(number, symbol);
		}
	}

	@SuppressWarnings("null")
	private void loadFormatSpec(FormatSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = specCtx.specIdentifier().getText();

		reserveSpecIdentifier(specIdentifier);

		StructSpec spec = new StructSpec();

		for (FormatSpecElementContext elementCtx : specCtx.formatSpecElement()) {
			spec.add(loadFormatSpecElement(elementCtx, rootCtx));
		}
		spec.result(loadTextExpression(specCtx.textExpression()));
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());
		this.specs.put(specIdentifier, () -> spec);
	}

	@SuppressWarnings("null")
	private StructSpec loadStructSpec(StructSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = specCtx.specIdentifier().getText();

		reserveSpecIdentifier(specIdentifier);

		StructSpec spec = loadAnonymousStructSpec(specCtx.anonymousStructSpec(), rootCtx);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private StructSpec loadAnonymousStructSpec(AnonymousStructSpecContext specCtx, FormatSpecsContext rootCtx) {
		StructSpec spec = new StructSpec();

		for (FormatSpecElementContext elementCtx : specCtx.formatSpecElement()) {
			spec.add(loadFormatSpecElement(elementCtx, rootCtx));
		}

		TextExpressionContext textExpression = specCtx.textExpression();

		if (textExpression != null) {
			spec.result(loadTextExpression(specCtx.textExpression()));
		}
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());
		return spec;
	}

	@SuppressWarnings("null")
	private UnionSpec loadUnionSpec(UnionSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = specCtx.specIdentifier().getText();

		reserveSpecIdentifier(specIdentifier);

		UnionSpec spec = loadAnonymousUnionSpec(specCtx.anonymousUnionSpec(), rootCtx);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private UnionSpec loadAnonymousUnionSpec(AnonymousUnionSpecContext specCtx, FormatSpecsContext rootCtx) {
		UnionSpec spec = new UnionSpec();

		for (CompositeSpecElementContext elementCtx : specCtx.compositeSpecElement()) {
			spec.add(loadCompositeSpecElement(elementCtx, rootCtx));
		}

		TextExpressionContext textExpression = specCtx.textExpression();

		if (textExpression != null) {
			spec.result(loadTextExpression(specCtx.textExpression()));
		}
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());
		return spec;
	}

	@SuppressWarnings("null")
	private SequenceSpec loadSequenceSpec(SequenceSpecContext specCtx, FormatSpecsContext rootCtx) {
		String specIdentifier = specCtx.specIdentifier().getText();

		reserveSpecIdentifier(specIdentifier);

		SequenceSpec spec = loadAnonymousSequenceSpec(specCtx.anonymousSequenceSpec(), rootCtx);

		this.specs.put(specIdentifier, () -> spec);
		return spec;
	}

	@SuppressWarnings("null")
	private SequenceSpec loadAnonymousSequenceSpec(AnonymousSequenceSpecContext specCtx, FormatSpecsContext rootCtx) {
		FormatSpec elementSpec = loadFormatSpecElement(specCtx.formatSpecElement(), rootCtx);
		SequenceSpec spec = new SequenceSpec(elementSpec);
		TextExpressionContext textExpression = specCtx.textExpression();

		if (textExpression != null) {
			spec.result(loadTextExpression(specCtx.textExpression()));
		}
		applyByteOrderModifier(spec, specCtx.compositeSpecByteOrderModifier());
		applyExportModifier(spec, specCtx.compositeSpecExportModifier());
		return spec;
	}

	private void applyByteOrderModifier(CompositeSpec spec, List<CompositeSpecByteOrderModifierContext> modifierCtx) {
		for (CompositeSpecByteOrderModifierContext byteOrderCtx : modifierCtx) {
			if (byteOrderCtx.LittleEndian() != null) {
				spec.byteOrder(ByteOrder.LITTLE_ENDIAN);
			} else {
				spec.byteOrder(ByteOrder.BIG_ENDIAN);
			}
		}
	}

	@SuppressWarnings("null")
	private void applyExportModifier(CompositeSpec spec, List<CompositeSpecExportModifierContext> modifierCtx) {
		for (CompositeSpecExportModifierContext exportCtx : modifierCtx) {
			spec.export(resolveExternalReference(exportCtx.externalReference(), FileScannerResultExportHandler.class));
		}
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
		} else {
			throw Check.fail();
		}
		return spec;
	}

	@SuppressWarnings("null")
	private ByteSpec loadByteSpec(ByteAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		ByteSpec spec = new ByteSpec(loadTextExpression(specCtx.textExpression()));

		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private WordSpec loadWordSpec(WordAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		WordSpec spec = new WordSpec(loadTextExpression(specCtx.textExpression()));

		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private DWordSpec loadDWordSpec(DwordAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		DWordSpec spec = new DWordSpec(loadTextExpression(specCtx.textExpression()));

		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private QWordSpec loadQWordSpec(QwordAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		QWordSpec spec = new QWordSpec(loadTextExpression(specCtx.textExpression()));

		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private ByteArraySpec loadByteArraySpec(ByteArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		ByteArraySpec spec = new ByteArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private WordArraySpec loadWordArraySpec(WordArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		WordArraySpec spec = new WordArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private DWordArraySpec loadDWordArraySpec(DwordArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		DWordArraySpec spec = new DWordArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private QWordArraySpec loadQWordArraySpec(QwordArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {
		QWordArraySpec spec = new QWordArraySpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private FixedStringSpec loadCharArraySpec(CharArrayAttributeSpecContext specCtx, FormatSpecsContext rootCtx) {

		FixedStringSpec spec = new FixedStringSpec(loadTextExpression(specCtx.textExpression()));

		spec.size(loadNumberExpression(specCtx.numberExpression()));
		bindAttributeSpecIfNeeded(spec, specCtx.specIdentifier(), specCtx.scopeIdentifier(), rootCtx);
		return spec;
	}

	@SuppressWarnings("null")
	private void bindAttributeSpecIfNeeded(AttributeSpec<?> spec, @Nullable SpecIdentifierContext specIdentifierCtx,
			@Nullable ScopeIdentifierContext scopeIdentiferCtx, FormatSpecsContext rootCtx) {
		if (specIdentifierCtx != null) {
			String specIdentifier = specIdentifierCtx.getText();

			reserveSpecIdentifier(specIdentifier);
			if (scopeIdentiferCtx != null) {
				String scopeIdentifier = scopeIdentiferCtx.getText();
				this.lateBindings.add(() -> spec.bind(resolveSpec(rootCtx, scopeIdentifier, CompositeSpec.class)));
			} else {
				spec.bind();
			}
			this.specs.put(specIdentifier, () -> spec);
		}
	}

	@SuppressWarnings("null")
	private FormatSpec loadFormatSpecElement(FormatSpecElementContext elementCtx, FormatSpecsContext rootCtx) {
		FormatSpec element;
		SpecReferenceContext specReferenceCtx;
		AttributeSpecContext attributeSpecCtx;
		AnonymousStructSpecContext anonymousStructSpecCtx;
		AnonymousUnionSpecContext anonymousUnionSpecCtx;
		AnonymousSequenceSpecContext anonymousSequenceSpecCtx;
		ConditionalSpecContext conditionalSpecCtx;
		EncodedSpecContext encodedSpecCtx;

		if ((specReferenceCtx = elementCtx.specReference()) != null) {
			String specIdentifier = specReferenceCtx.referencedSpec().getText();

			element = resolveSpec(rootCtx, specIdentifier, FormatSpec.class);
		} else if ((attributeSpecCtx = elementCtx.attributeSpec()) != null) {
			element = loadAttributeSpec(attributeSpecCtx, rootCtx);
		} else if ((anonymousStructSpecCtx = elementCtx.anonymousStructSpec()) != null) {
			element = loadAnonymousStructSpec(anonymousStructSpecCtx, rootCtx);
		} else if ((anonymousUnionSpecCtx = elementCtx.anonymousUnionSpec()) != null) {
			element = loadAnonymousUnionSpec(anonymousUnionSpecCtx, rootCtx);
		} else if ((anonymousSequenceSpecCtx = elementCtx.anonymousSequenceSpec()) != null) {
			element = loadAnonymousSequenceSpec(anonymousSequenceSpecCtx, rootCtx);
		} else if ((conditionalSpecCtx = elementCtx.conditionalSpec()) != null) {
			element = FormatSpecs.EMPTY;
		} else if ((encodedSpecCtx = elementCtx.encodedSpec()) != null) {
			element = FormatSpecs.EMPTY;
		} else {
			throw Check.fail();
		}
		return element;
	}

	@SuppressWarnings("null")
	private CompositeSpec loadCompositeSpecElement(CompositeSpecElementContext elementCtx, FormatSpecsContext rootCtx) {
		CompositeSpec spec;
		SpecReferenceContext specReferenceCtx;
		AnonymousStructSpecContext anonymousStructSpecCtx;
		AnonymousUnionSpecContext anonymousUnionSpecCtx;
		AnonymousSequenceSpecContext anonymousSequenceSpecCtx;
		ConditionalSpecContext conditionalSpecCtx;
		EncodedSpecContext encodedSpecCtx;

		if ((specReferenceCtx = elementCtx.specReference()) != null) {
			String specIdentifier = specReferenceCtx.referencedSpec().getText();

			spec = resolveSpec(rootCtx, specIdentifier, CompositeSpec.class);
		} else if ((anonymousStructSpecCtx = elementCtx.anonymousStructSpec()) != null) {
			spec = loadAnonymousStructSpec(anonymousStructSpecCtx, rootCtx);
		} else if ((anonymousUnionSpecCtx = elementCtx.anonymousUnionSpec()) != null) {
			spec = loadAnonymousUnionSpec(anonymousUnionSpecCtx, rootCtx);
		} else if ((anonymousSequenceSpecCtx = elementCtx.anonymousSequenceSpec()) != null) {
			spec = loadAnonymousSequenceSpec(anonymousSequenceSpecCtx, rootCtx);
		} else if ((conditionalSpecCtx = elementCtx.conditionalSpec()) != null) {
			spec = FormatSpecs.EMPTY;
		} else if ((encodedSpecCtx = elementCtx.encodedSpec()) != null) {
			spec = FormatSpecs.EMPTY;
		} else {
			throw Check.fail();
		}
		return spec;
	}

	@SuppressWarnings("null")
	private Supplier<? extends Number> loadNumberExpression(NumberExpressionContext ctx) {
		Supplier<? extends Number> numberExpression;
		SpecReferenceContext specReferenceCtx;
		NumberValueContext numberValueCtx;

		if ((specReferenceCtx = ctx.specReference()) != null) {
			String specIdentifier = specReferenceCtx.referencedSpec().getText();

			numberExpression = resolveSpec(specIdentifier, NumberAttributeSpec.class);
		} else if ((numberValueCtx = ctx.numberValue()) != null) {
			numberExpression = FinalSupplier.of(Integer.decode(numberValueCtx.getText()));
		} else {
			throw Check.fail();
		}
		return numberExpression;
	}

	@SuppressWarnings("null")
	private Supplier<String> loadTextExpression(TextExpressionContext ctx) {
		Supplier<String> textExpression;
		FormatTextContext formatTextCtx;
		SimpleTextContext simpleTextCtx;

		if ((formatTextCtx = ctx.formatText()) != null) {
			String formatText = decodeQuotedString(formatTextCtx.getText());

			textExpression = () -> String.format(formatText);
		} else if ((simpleTextCtx = ctx.simpleText()) != null) {
			textExpression = FinalSupplier.of(decodeQuotedString(simpleTextCtx.getText()));
		} else {
			throw Check.fail();
		}
		return textExpression;
	}

	private String decodeQuotedString(String quotedString) {
		return Strings.decode(quotedString.substring(1, quotedString.length() - 1));
	}

	private static final String UNKNOWN_EXTERNAL_REFERENCE = "Unknown reference #%s";
	private static final String INVALID_EXTERNAL_REFERENCE = "Invalid reference #%s (expected type: %s actual type: %s)";

	private <T> Supplier<T> resolveExternalReference(ExternalReferenceContext externalReferenceCtx, Class<T> type) {
		String methodIdentifier = externalReferenceCtx.referencedExternal().getText();
		Method method;

		try {
			method = getClass().getDeclaredMethod(methodIdentifier);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(String.format(UNKNOWN_EXTERNAL_REFERENCE, methodIdentifier), e);
		}

		Class<?> methodType = method.getReturnType();

		if (!type.isAssignableFrom(methodType)) {
			throw new IllegalArgumentException(String.format(INVALID_EXTERNAL_REFERENCE, methodIdentifier,
					type.getSimpleName(), methodType.getSimpleName()));
		}
		return () -> {
			try {
				return type.cast(method.invoke(this));
			} catch (ReflectiveOperationException e) {
				throw Exceptions.toRuntime(e);
			}
		};
	}

	private void reserveSpecIdentifier(String specIdentifier) {
		if (this.specs.containsKey(specIdentifier)) {
			throw new IllegalArgumentException("Duplicate spec identifier: " + specIdentifier);
		}
		this.specs.put(specIdentifier, () -> unresolvedSpec(specIdentifier));
	}

	private CompositeSpec unresolvedSpec(String identifier) {
		throw new IllegalArgumentException("Cyclic reference @" + identifier);
	}

	private static final String UNKNOWN_SPEC_REFERENCE = "Unknown reference @%s";
	private static final String INVALID_SPEC_REFERENCE = "Invalid reference @%s (expected type: %s actual type: %s)";

	@SuppressWarnings("squid:S3776")
	private <T extends FormatSpec> T resolveSpec(FormatSpecsContext rootCtx, String specIdentifier, Class<T> specType) {
		Supplier<FormatSpec> resolvedSpecSupplier = this.specs.get(specIdentifier);
		FormatSpec resolvedSpec = null;

		if (resolvedSpecSupplier == null) {
			for (StructSpecContext structSpecCtx : rootCtx.structSpec()) {
				if (specIdentifier.equals(structSpecCtx.specIdentifier().getText())) {
					resolvedSpec = loadStructSpec(structSpecCtx, rootCtx);
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
		} else {
			resolvedSpec = resolvedSpecSupplier.get();
		}
		if (resolvedSpec == null) {
			throw new IllegalArgumentException(String.format(UNKNOWN_SPEC_REFERENCE, specIdentifier));
		}

		Class<?> resolvedSpecType = resolvedSpec.getClass();

		if (!specType.isAssignableFrom(resolvedSpecType)) {
			throw new IllegalArgumentException(String.format(INVALID_SPEC_REFERENCE, specIdentifier,
					specType.getSimpleName(), resolvedSpecType.getSimpleName()));
		}
		return specType.cast(resolvedSpec);
	}

	private <T extends FormatSpec> T resolveSpec(String specIdentifier, Class<T> specType) {
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

}
