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
package de.carne.filescanner.provider.xar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jdt.annotation.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DecodeAtSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.input.InputDecoder;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.filescanner.engine.util.Bzip2InputDecoder;
import de.carne.filescanner.engine.util.DeflateInputDecoder;
import de.carne.filescanner.engine.util.LocalEntityResolver;
import de.carne.filescanner.engine.util.LzmaInputDecoder;
import de.carne.nio.compression.bzip2.Bzip2DecoderProperties;
import de.carne.nio.compression.bzip2.Bzip2Format;
import de.carne.nio.compression.deflate.DeflateDecoderProperties;
import de.carne.nio.compression.deflate.DeflateFormat;
import de.carne.nio.compression.lzma.LzmaDecoderProperties;
import de.carne.nio.compression.lzma.LzmaFormat;
import de.carne.util.Strings;
import de.carne.util.logging.Log;

class TocHandler extends DefaultHandler {

	private static final Log LOG = new Log();

	private static final DeflateInputDecoder GZIP_INPUT_DECODER;

	static {
		DeflateDecoderProperties properties = DeflateInputDecoder.defaultProperties();

		properties.setFormatProperty(DeflateFormat.ZLIB);
		GZIP_INPUT_DECODER = new DeflateInputDecoder(properties);
	}

	private static final Bzip2InputDecoder BZIP2_INPUT_DECODER;

	static {
		Bzip2DecoderProperties properties = Bzip2InputDecoder.defaultProperties();

		properties.setFormat(Bzip2Format.BZ2LIB);
		BZIP2_INPUT_DECODER = new Bzip2InputDecoder(properties);
	}

	private static final LzmaInputDecoder LZMA_INPUT_DECODER;

	static {
		LzmaDecoderProperties properties = LzmaInputDecoder.defaultProperties();

		properties.setFormat(LzmaFormat.LZMALIB);
		LZMA_INPUT_DECODER = new LzmaInputDecoder(properties);
	}

	private static final String ELEMENT_SUFFIX_FILE = "/file";
	private static final String ELEMENT_SUFFIX_FILE_TYPE = "/file/type";
	private static final String ELEMENT_SUFFIX_FILE_NAME = "/file/name";
	private static final String ELEMENT_SUFFIX_FILE_DATA_LENGTH = "/file/data/length";
	private static final String ELEMENT_SUFFIX_FILE_DATA_OFFSET = "/file/data/offset";
	private static final String ELEMENT_SUFFIX_FILE_DATA_SIZE = "/file/data/size";
	private static final String ELEMENT_SUFFIX_FILE_DATA_ENCODING = "/file/data/encoding";

	private static final String ENCODING_NONE = "application/octet-stream";
	private static final String ENCODING_GZIP = "application/x-gzip";
	private static final String ENCODING_BZIP2 = "application/x-bzip2";
	private static final String ENCODING_LZMA = "application/x-lzma";

	private final Deque<String> elementStack = new LinkedList<>();
	private final Deque<TocEntry> tocEntryStack = new LinkedList<>();
	private final NavigableMap<Long, EncodedInputSpecConfig> tocEntries = new TreeMap<>();

	@SuppressWarnings("squid:S2755")
	public static CompositeSpec parse(InputStream input, long heapOffset)
			throws IOException, ParserConfigurationException, SAXException {
		TocHandler handler = new TocHandler();
		XMLReader reader = SAXParserFactory.newDefaultInstance().newSAXParser().getXMLReader();

		reader.setEntityResolver(LocalEntityResolver.getInstance());
		reader.setContentHandler(handler);
		reader.parse(new InputSource(input));

		StructSpec parsedSpec = new StructSpec();

		for (Map.Entry<Long, EncodedInputSpecConfig> tocEntry : handler.tocEntries.entrySet()) {
			DecodeAtSpec heapEntrySpec = new DecodeAtSpec(new EncodedInputSpec(tocEntry.getValue()));

			heapEntrySpec.position(heapOffset + tocEntry.getKey().longValue());
			parsedSpec.add(heapEntrySpec);
		}
		return parsedSpec;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String element = (this.elementStack.isEmpty() ? qName : this.elementStack.peek() + "/" + qName);

		this.elementStack.push(element);
		if (element.endsWith(ELEMENT_SUFFIX_FILE)) {
			this.tocEntryStack.push(new TocEntry(this.tocEntryStack.peek()));
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_TYPE)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).startSetType();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_NAME)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).startSetName();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_DATA_LENGTH)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).startSetLength();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_DATA_OFFSET)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).startSetOffset();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_DATA_SIZE)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).startSetSize();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_DATA_ENCODING)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).setEncoding(attributes.getValue("style"));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String element = this.elementStack.pop();

		if (element.endsWith(ELEMENT_SUFFIX_FILE)) {
			TocEntry tocEntry = this.tocEntryStack.pop();

			if (tocEntry.isFile()) {
				endFileElement(tocEntry);
			}
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_TYPE)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).endSetType();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_NAME)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).endSetName();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_DATA_LENGTH)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).endSetLength();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_DATA_OFFSET)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).endSetOffset();
		} else if (element.endsWith(ELEMENT_SUFFIX_FILE_DATA_SIZE)) {
			Objects.requireNonNull(this.tocEntryStack.peek()).endSetSize();
		}
	}

	private void endFileElement(TocEntry tocEntry) {
		String name = getTocEntryString("name", tocEntry.getName());
		Long dataLength = getTocEntryLong("data/length", tocEntry.getLength());
		Long dataOffset = getTocEntryLong("data/offset", tocEntry.getOffset());
		Long dataSize = getTocEntryLong("data/size", tocEntry.getSize());
		String dataEncoding = getTocEntryString("data/encoding", tocEntry.getEncoding());

		if (name != null && dataLength != null && dataOffset != null && dataSize != null && dataEncoding != null) {
			String path = tocEntry.path();
			EncodedInputSpecConfig encodedInputSpecConfig = new EncodedInputSpecConfig(
					String.format("heap entry \"%s\"", Strings.encode(name)));

			encodedInputSpecConfig.decodedInputName(path);

			InputDecoder inputDecoder;

			if (ENCODING_NONE.equals(dataEncoding)) {
				inputDecoder = InputDecoders.IDENTITY;
			} else if (ENCODING_GZIP.equals(dataEncoding)) {
				inputDecoder = GZIP_INPUT_DECODER;
			} else if (ENCODING_BZIP2.equals(dataEncoding)) {
				inputDecoder = BZIP2_INPUT_DECODER;
			} else if (ENCODING_LZMA.equals(dataEncoding)) {
				inputDecoder = LZMA_INPUT_DECODER;
			} else {
				inputDecoder = InputDecoders.unsupportedInputDecoder(dataEncoding);
			}

			InputDecoderTable inputDecoderTable = InputDecoderTable.build(inputDecoder, 0, dataLength.longValue(),
					dataSize.longValue());

			encodedInputSpecConfig.inputDecoderTable(inputDecoderTable);
			this.tocEntries.put(dataOffset, encodedInputSpecConfig);
		}
	}

	private @Nullable String getTocEntryString(String name, @Nullable String s) {
		if (s == null) {
			LOG.warning("Missing TOC entry attribute {0}", name);
		}
		return s;
	}

	private @Nullable Long getTocEntryLong(String name, @Nullable String s) {
		String valueString = getTocEntryString(name, s);
		Long value = null;

		if (valueString != null) {
			try {
				value = Long.valueOf(Long.parseUnsignedLong(valueString));
			} catch (NumberFormatException e) {
				LOG.warning(e, "Invalid TOC entry attribute {0}: {1}", name, Strings.encode(valueString));
			}
		}
		return value;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		TocEntry tocEntry = this.tocEntryStack.peek();

		if (tocEntry != null) {
			tocEntry.set(ch, start, length);
		}
	}

	private static class TocEntry {

		@FunctionalInterface
		private interface Setter {

			void set(char[] ch, int start, int length);

		}

		private final @Nullable TocEntry parent;
		private final StringBuilder setBuffer = new StringBuilder();
		private Setter setter = this::setNothing;
		private @Nullable String type = null;
		private @Nullable String name = null;
		private @Nullable String length = null;
		private @Nullable String offset = null;
		private @Nullable String size = null;
		private @Nullable String encoding = null;

		public TocEntry(@Nullable TocEntry parent) {
			this.parent = parent;
		}

		public void set(char[] ch, int start, int length) {
			this.setter.set(ch, start, length);
		}

		public void startSetType() {
			this.setter = this::setType;
		}

		private void setType(char[] ch, int start, int length) {
			this.setBuffer.append(ch, start, length);
		}

		public void endSetType() {
			this.type = this.setBuffer.toString();
			endSet();
		}

		public @Nullable String getName() {
			return this.name;
		}

		public void startSetName() {
			this.setter = this::setName;
		}

		private void setName(char[] ch, int start, int length) {
			this.setBuffer.append(ch, start, length);
		}

		public void endSetName() {
			this.name = this.setBuffer.toString();
			endSet();
		}

		public @Nullable String getLength() {
			return this.length;
		}

		public void startSetLength() {
			this.setter = this::setLength;
		}

		private void setLength(char[] ch, int start, int length) {
			this.setBuffer.append(ch, start, length);
		}

		public void endSetLength() {
			this.length = this.setBuffer.toString();
			endSet();
		}

		public @Nullable String getOffset() {
			return this.offset;
		}

		public void startSetOffset() {
			this.setter = this::setOffset;
		}

		private void setOffset(char[] ch, int start, int length) {
			this.setBuffer.append(ch, start, length);
		}

		public void endSetOffset() {
			this.offset = this.setBuffer.toString();
			endSet();
		}

		public @Nullable String getSize() {
			return this.size;
		}

		public void startSetSize() {
			this.setter = this::setSize;
		}

		private void setSize(char[] ch, int start, int length) {
			this.setBuffer.append(ch, start, length);
		}

		public void endSetSize() {
			this.size = this.setBuffer.toString();
			endSet();
		}

		public @Nullable String getEncoding() {
			return this.encoding;
		}

		public void setEncoding(@Nullable String encoding) {
			this.encoding = encoding;
		}

		private void endSet() {
			this.setBuffer.setLength(0);
			this.setter = this::setNothing;
		}

		@SuppressWarnings("unused")
		private void setNothing(char[] ch, int start, int length) {
			// Nothing to do here
		}

		public boolean isFile() {
			return "file".equals(this.type);
		}

		public String path() {
			StringBuilder path = new StringBuilder();

			buildPath(path);
			return path.toString();
		}

		private void buildPath(StringBuilder path) {
			if (this.parent != null) {
				this.parent.buildPath(path);
				path.append('/');
			}
			path.append(this.name != null ? this.name : "?");
		}

	}

}
