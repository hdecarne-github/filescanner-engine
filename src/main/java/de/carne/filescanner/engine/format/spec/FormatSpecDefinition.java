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
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarBaseVisitor;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarLexer;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.ByteSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.DwordSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatSpecContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.FormatSpecsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SymbolDefinitionContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.SymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordFlagSymbolsContext;
import de.carne.filescanner.engine.format.spec.grammar.FormatSpecGrammarParser.WordSymbolsContext;

/**
 *
 */
public final class FormatSpecDefinition {

	private static final Log LOG = new Log();

	private FormatSpecDefinition() {

	}

	/**
	 * Loads a {@linkplain FormatSpecDefinition} instance from a resource.
	 *
	 * @param resourceUrl the resource {@linkplain URL} to read from.
	 * @return the loaded {@linkplain FormatSpecDefinition}.
	 * @throws IOException if the resource cannot be loaded or parsed.
	 */
	public static FormatSpecDefinition load(URL resourceUrl) throws IOException {
		FormatSpecDefinition formatSpecDefinition;

		try (InputStream resourceStream = resourceUrl.openStream()) {
			CharStream input = CharStreams.fromStream(resourceStream);
			FormatSpecGrammarLexer lexer = new FormatSpecGrammarLexer(input);
			TokenStream tokens = new CommonTokenStream(lexer);
			FormatSpecGrammarParser parser = new FormatSpecGrammarParser(tokens);

			formatSpecDefinition = new LoadHelper(FormatSpecDefinition::loadHelper)
					.visitFormatSpecs(parser.formatSpecs());
		}
		return formatSpecDefinition;
	}

	private static class LoadHelper extends FormatSpecGrammarBaseVisitor<FormatSpecDefinition> {

		private final Function<FormatSpecsContext, FormatSpecDefinition> loader;

		LoadHelper(Function<FormatSpecsContext, FormatSpecDefinition> loader) {
			this.loader = loader;
		}

		@Override
		public FormatSpecDefinition visitFormatSpecs(@Nullable FormatSpecsContext ctx) {
			return this.loader.apply(Objects.requireNonNull(ctx));
		}
	}

	private static FormatSpecDefinition loadHelper(FormatSpecsContext ctx) {
		FormatSpecDefinition formatSpecDefintion = new FormatSpecDefinition();

		for (SymbolsContext symbolsCtx : ctx.symbols()) {
			ByteSymbolsContext byteSymbolsCtx = symbolsCtx.byteSymbols();

			if (byteSymbolsCtx != null) {
				String byteSymbolsName = Objects.requireNonNull(byteSymbolsCtx.symbolsIdentifier().getText());
				ByteSymbolRenderer byteSymbols = new ByteSymbolRenderer();

				loadSymbolDefinitions(byteSymbols, byteSymbolsCtx.symbolDefinition(), Byte::decode);
				formatSpecDefintion.addByteSymbols(byteSymbolsName, byteSymbols);
			}

			WordSymbolsContext wordSymbolsCtx = symbolsCtx.wordSymbols();

			if (wordSymbolsCtx != null) {
				String wordSymbolsName = Objects.requireNonNull(wordSymbolsCtx.symbolsIdentifier().getText());
				WordSymbolRenderer wordSymbols = new WordSymbolRenderer();

				loadSymbolDefinitions(wordSymbols, wordSymbolsCtx.symbolDefinition(), Short::decode);
				formatSpecDefintion.addWordSymbols(wordSymbolsName, wordSymbols);
			}

			DwordSymbolsContext dwordSymbolsCtx = symbolsCtx.dwordSymbols();

			if (dwordSymbolsCtx != null) {
				String dwordSymbolsName = Objects.requireNonNull(dwordSymbolsCtx.symbolsIdentifier().getText());
				DWordSymbolRenderer dwordSymbols = new DWordSymbolRenderer();

				loadSymbolDefinitions(dwordSymbols, dwordSymbolsCtx.symbolDefinition(), Integer::decode);
				formatSpecDefintion.addDWordSymbols(dwordSymbolsName, dwordSymbols);
			}
		}
		for (FlagSymbolsContext flagSymbolsCtx : ctx.flagSymbols()) {
			ByteFlagSymbolsContext byteFlagSymbolsCtx = flagSymbolsCtx.byteFlagSymbols();

			if (byteFlagSymbolsCtx != null) {
				String byteFlagSymbolsName = Objects.requireNonNull(byteFlagSymbolsCtx.symbolsIdentifier().getText());
				ByteFlagRenderer byteFlagSymbols = new ByteFlagRenderer();

				loadSymbolDefinitions(byteFlagSymbols, byteFlagSymbolsCtx.symbolDefinition(), Byte::decode);
				formatSpecDefintion.addByteFlagSymbols(byteFlagSymbolsName, byteFlagSymbols);
			}

			WordFlagSymbolsContext wordFlagSymbolsCtx = flagSymbolsCtx.wordFlagSymbols();

			if (wordFlagSymbolsCtx != null) {
				String wordFlagSymbolsName = Objects.requireNonNull(wordFlagSymbolsCtx.symbolsIdentifier().getText());
				WordFlagRenderer wordFlagSymbols = new WordFlagRenderer();

				loadSymbolDefinitions(wordFlagSymbols, wordFlagSymbolsCtx.symbolDefinition(), Short::decode);
				formatSpecDefintion.addWordFlagSymbols(wordFlagSymbolsName, wordFlagSymbols);
			}

			DwordFlagSymbolsContext dwordFlagSymbolsCtx = flagSymbolsCtx.dwordFlagSymbols();

			if (dwordFlagSymbolsCtx != null) {
				String dwordFlagSymbolsName = Objects.requireNonNull(dwordFlagSymbolsCtx.symbolsIdentifier().getText());
				DWordFlagRenderer dwordFlagSymbols = new DWordFlagRenderer();

				loadSymbolDefinitions(dwordFlagSymbols, dwordFlagSymbolsCtx.symbolDefinition(), Integer::decode);
				formatSpecDefintion.addDWordFlagSymbols(dwordFlagSymbolsName, dwordFlagSymbols);
			}
		}
		for (FormatSpecContext formatSpecCtx : ctx.formatSpec()) {
			String formatSpecName = Objects.requireNonNull(formatSpecCtx.specIdentifier().getText());

			System.out.println(formatSpecName);
		}
		return formatSpecDefintion;
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

	private void addByteSymbols(String name, ByteSymbolRenderer byteSymbols) {

	}

	private void addWordSymbols(String name, WordSymbolRenderer wordSymbols) {

	}

	private void addDWordSymbols(String name, DWordSymbolRenderer dwordSymbols) {

	}

	private void addByteFlagSymbols(String name, ByteFlagRenderer byteFlagSymbols) {

	}

	private void addWordFlagSymbols(String name, WordFlagRenderer wordFlagSymbols) {

	}

	private void addDWordFlagSymbols(String name, DWordFlagRenderer dwordFlagSymbols) {

	}

}
