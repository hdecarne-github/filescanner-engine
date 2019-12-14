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
package de.carne.filescanner.engine.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import de.carne.filescanner.engine.FileScanner;
import de.carne.filescanner.engine.FileScannerResult;
import de.carne.filescanner.engine.ModuleManifestInfos;
import de.carne.filescanner.engine.transfer.RenderOutput;
import de.carne.filescanner.engine.transfer.RenderStyle;
import de.carne.filescanner.engine.transfer.Renderer;
import de.carne.filescanner.engine.transfer.TransferSource;
import de.carne.util.Strings;

/**
 * Utility class generating a HTML based scan result report.
 */
public class HtmlReportGenerator {

	private static final String TEMPLATE_INPUT_NAME = "%INPUT_NAME%";
	private static final String TEMPLATE_ENGINE_VERSION = "%ENGINE_VERSION%";
	private static final String TEMPLATE_REPORT_TIMESTAMP = "%REPORT_TIMESTAMP%";
	private static final String TEMPLATE_RESULT_TREE = "%RESULT_TREE%";

	/**
	 * Write the given scan result tree to a HTML file.
	 *
	 * @param fileScanner the {@linkplain FileScanner} instance that generated the result.
	 * @param out the {@linkplain Writer} to write to.
	 * @throws IOException if an I/O error occurs.
	 */
	public void write(FileScanner fileScanner, Writer out) throws IOException {
		write(fileScanner, fileScanner.result(), out);
	}

	/**
	 * Write the given scan result tree to a HTML file.
	 *
	 * @param fileScanner the {@linkplain FileScanner} instance that generated the result.
	 * @param result the {@linkplain FileScannerResult} root node to write.
	 * @param out the {@linkplain Writer} to write to.
	 * @throws IOException if an I/O error occurs.
	 */
	public void write(FileScanner fileScanner, FileScannerResult result, Writer out) throws IOException {
		try (BufferedReader templateReader = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream(getClass().getSimpleName() + ".html")))) {
			String templateLine = null;

			while ((templateLine = templateReader.readLine()) != null) {
				Map<String, String> templateArguments = new HashMap<>();
				ModuleManifestInfos engineInfos = new ModuleManifestInfos();

				templateArguments.put(TEMPLATE_INPUT_NAME, Strings.encodeHtml(result.input().name()));
				templateArguments.put(TEMPLATE_ENGINE_VERSION, Strings.encodeHtml(engineInfos.toString()));
				templateArguments.put(TEMPLATE_REPORT_TIMESTAMP,
						Strings.encodeHtml(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));

				String outLine = templateLine;

				if (outLine.equals(TEMPLATE_RESULT_TREE)) {
					out.write("<ul id=\"root\">");
					writeResults(result, out, 0);
					out.write("</ul>");
				} else {
					for (Map.Entry<String, String> templateArgumentsEntry : templateArguments.entrySet()) {
						outLine = outLine.replace(templateArgumentsEntry.getKey(), templateArgumentsEntry.getValue());
					}
					out.write(outLine);
				}
			}
		}
	}

	private void writeResults(FileScannerResult result, Writer out, int depth) throws IOException {
		@NonNull FileScannerResult[] resultChildren = result.children();

		out.write("<li><div class=\"title");
		if (resultChildren.length > 0) {
			out.write(" node");
			if (depth == 0) {
				out.write(" node-expanded");
			}
		} else {
			out.write(" leaf");
		}
		out.write("\">");
		out.write(Strings.encodeHtml(result.name()));
		out.write("</div><div class=\"details\">");
		try (RenderOutput renderOutput = new RenderOutput(new Renderer() {

			@Override
			public void close() throws IOException {
				// Nothing to do here
			}

			@Override
			public int emitText(int indent, RenderStyle style, String text, boolean lineBreak) throws IOException {
				out.write("<span class=\"" + style.name().toLowerCase() + "\">");
				out.write(Strings.encodeHtml(text));
				out.write("</span>");
				if (lineBreak) {
					out.write("<br>");
				}
				return text.length();
			}

			@Override
			public int emitMediaData(int indent, RenderStyle style, TransferSource source, boolean lineBreak)
					throws IOException {
				return emitText(indent, style, "[" + source.transferType().mimeType() + ":" + source.name() + "]",
						lineBreak);
			}

		})) {
			result.render(renderOutput);
		}
		out.write("</div>");
		if (resultChildren.length > 0) {
			out.write("<ul class=\"toggle");
			if (depth == 0) {
				out.write(" expanded");
			}
			out.write("\">");

			int childDepth = depth + 1;

			for (FileScannerResult resultChild : resultChildren) {
				writeResults(resultChild, out, childDepth);
			}
			out.write("</ul>");
		}
		out.write("</li>");
	}

}
