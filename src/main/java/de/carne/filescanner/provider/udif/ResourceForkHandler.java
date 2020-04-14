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
package de.carne.filescanner.provider.udif;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jdt.annotation.NonNull;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.UnexpectedDataException;
import de.carne.filescanner.engine.format.CompositeSpec;
import de.carne.filescanner.engine.format.DecodeAtSpec;
import de.carne.filescanner.engine.format.EncodedInputSpec;
import de.carne.filescanner.engine.format.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.StructSpec;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;
import de.carne.filescanner.provider.util.Bzip2InputDecoder;
import de.carne.filescanner.provider.util.DeflateInputDecoder;
import de.carne.filescanner.provider.util.LocalEntityResolver;
import de.carne.filescanner.provider.util.LzmaInputDecoder;
import de.carne.nio.compression.bzip2.Bzip2DecoderProperties;
import de.carne.nio.compression.bzip2.Bzip2Format;
import de.carne.nio.compression.deflate.DeflateDecoderProperties;
import de.carne.nio.compression.deflate.DeflateFormat;
import de.carne.nio.compression.lzma.LzmaDecoderProperties;
import de.carne.nio.compression.lzma.LzmaFormat;

class ResourceForkHandler extends DefaultHandler {

	private static final Log LOG = new Log();

	private static final DeflateInputDecoder ZLIB_INPUT_DECODER;

	static {
		DeflateDecoderProperties properties = DeflateInputDecoder.defaultProperties();

		properties.setFormatProperty(DeflateFormat.ZLIB);
		ZLIB_INPUT_DECODER = new DeflateInputDecoder(properties);
	}

	private static final Bzip2InputDecoder BZ2LIB_INPUT_DECODER;

	static {
		Bzip2DecoderProperties properties = Bzip2InputDecoder.defaultProperties();

		properties.setFormat(Bzip2Format.BZ2LIB);
		BZ2LIB_INPUT_DECODER = new Bzip2InputDecoder(properties);
	}

	private static final LzmaInputDecoder LZMALIB_INPUT_DECODER;

	static {
		LzmaDecoderProperties properties = LzmaInputDecoder.defaultProperties();

		properties.setFormat(LzmaFormat.LZMALIB);
		LZMALIB_INPUT_DECODER = new LzmaInputDecoder(properties);
	}

	private static final String ELEMENT_KEY0 = "plist/dict/key";
	private static final String ELEMENT_KEY1 = "plist/dict/dict/key";
	private static final String ELEMENT_DICT2 = "plist/dict/dict/array/dict";
	private static final String ELEMENT_KEY2 = "plist/dict/dict/array/dict/key";

	private static final String KEY_RESOURCEFORK = "resource-fork";
	private static final String KEY_BLKX = "blkx";
	private static final String KEY_DATA = "Data";
	private static final String KEY_NAME = "Name";

	private final Deque<String> elementStack = new LinkedList<>();

	private final @NonNull String[] keyPath = new @NonNull String[] { "", "", "" };
	private final BlkxDataDecoder blkxDataDecoder = new BlkxDataDecoder();
	private final StringBuilder blkxName = new StringBuilder();
	private final SortedMap<BlkxDescriptor, EncodedInputSpec> blkxSpecs = new TreeMap<>();

	@SuppressWarnings("squid:S2755")
	public static CompositeSpec parse(InputStream input)
			throws IOException, ParserConfigurationException, SAXException {
		ResourceForkHandler handler = new ResourceForkHandler();
		XMLReader reader = SAXParserFactory.newDefaultInstance().newSAXParser().getXMLReader();

		reader.setEntityResolver(LocalEntityResolver.getInstance());
		reader.setContentHandler(handler);
		reader.parse(new InputSource(input));

		StructSpec parsedSpec = new StructSpec();

		if (!handler.blkxSpecs.isEmpty()) {
			DecodeAtSpec dataForkAtSpec = null;
			StructSpec dataForkSpec = new StructSpec();

			dataForkSpec.result("Data fork");
			for (Map.Entry<BlkxDescriptor, EncodedInputSpec> entry : handler.blkxSpecs.entrySet()) {
				BlkxDescriptor blkxDescriptor = entry.getKey();
				long blkxPosition = blkxDescriptor.blkxPosition();

				if (dataForkAtSpec == null) {
					dataForkAtSpec = new DecodeAtSpec(dataForkSpec).position(blkxPosition);
					parsedSpec.add(dataForkAtSpec);
				}

				DecodeAtSpec entryAtSpec = new DecodeAtSpec(entry.getValue());

				entryAtSpec.position(blkxPosition);
				dataForkSpec.add(entryAtSpec);
			}
		}
		return parsedSpec;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (this.elementStack.isEmpty()) {
			this.elementStack.push(qName);
		} else {
			this.elementStack.push(this.elementStack.peek() + "/" + qName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String element = this.elementStack.peek();

		if (ELEMENT_KEY0.equals(element)) {
			this.keyPath[0] = new String(ch, start, length);
			this.keyPath[1] = "";
			this.keyPath[2] = "";
		} else if (ELEMENT_KEY1.equals(element)) {
			if (KEY_RESOURCEFORK.equals(this.keyPath[0])) {
				this.keyPath[1] = new String(ch, start, length);
				this.keyPath[2] = "";
			}
		} else if (ELEMENT_KEY2.equals(element)) {
			if (KEY_BLKX.equals(this.keyPath[1])) {
				this.keyPath[2] = new String(ch, start, length);
			}
		} else if (KEY_RESOURCEFORK.equals(this.keyPath[0]) && KEY_BLKX.equals(this.keyPath[1])) {
			if (KEY_DATA.equals(this.keyPath[2])) {
				this.blkxDataDecoder.feed(ch, start, length);
			} else if (KEY_NAME.equals(this.keyPath[2])) {
				this.blkxName.append(ch, start, length);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String element = this.elementStack.pop();

		if (ELEMENT_DICT2.equals(element) && KEY_RESOURCEFORK.equals(this.keyPath[0])
				&& KEY_BLKX.equals(this.keyPath[1])) {
			if (!this.blkxDataDecoder.isEmpty() && this.blkxName.length() > 0) {
				String trimmedBlkxName = this.blkxName.toString().trim();

				try {
					ByteBuffer blkxData = this.blkxDataDecoder.getResult().order(ByteOrder.BIG_ENDIAN);
					InputDecoderTable blkxDecoderTable = new InputDecoderTable();
					BlkxDescriptor blkxDescriptor = decodeBlkxChunks(blkxDecoderTable, blkxData,
							decodeBlkxHeader(blkxData));

					if (blkxDescriptor.dataChunkCount() > 0) {
						EncodedInputSpecConfig blkxSpecConfig = new EncodedInputSpecConfig(trimmedBlkxName)
								.inputDecoderTable(blkxDecoderTable);
						EncodedInputSpec blkxSpec = new EncodedInputSpec(blkxSpecConfig);

						this.blkxSpecs.put(blkxDescriptor, blkxSpec);
					}
				} catch (IOException e) {
					LOG.warning(e, "Failed to decode block list ''{0}''", trimmedBlkxName);
				}
			}
			this.blkxDataDecoder.reset();
			this.blkxName.setLength(0);
		}
	}

	private BlkxDescriptor decodeBlkxHeader(ByteBuffer blkxData) throws IOException {
		// Check for header as well as the the chunk count needed for chunk decoding
		if (blkxData.remaining() < (200 + 4)) {
			throw unexpectedData("Insufficient block list size", blkxData.remaining());
		}

		int signature = blkxData.getInt();
		int version = blkxData.getInt();
		long sectorNumber = blkxData.getLong();
		long sectorCount = blkxData.getLong();
		long dataOffset = blkxData.getLong();

		if (signature != 0x6d697368 || version != 1 || dataOffset < 0) {
			throw unexpectedData("Unexpected block list header data", signature, version, dataOffset);
		}
		blkxData.position(blkxData.position() + 42 * 4);
		return new BlkxDescriptor(dataOffset, 0, sectorNumber, sectorNumber + sectorCount);
	}

	private BlkxDescriptor decodeBlkxChunks(InputDecoderTable inputDecoderTable, ByteBuffer blkxData,
			BlkxDescriptor blkxDescriptor) throws IOException {
		int chunkCount = blkxData.getInt();

		if (blkxData.remaining() < chunkCount * 40) {
			throw unexpectedData("Insufficient block list entry size", blkxData.remaining());
		}

		long blkxPosition = blkxDescriptor.blkxPosition();
		int dataChunkCount = 0;

		for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
			int entryType = blkxData.getInt();
			/* int comment = */ blkxData.getInt();
			/* long chunkSectorNumber = */ blkxData.getLong();
			long chunkSectorCount = blkxData.getLong();
			long compressedOffset = blkxData.getLong();
			long compressedSize = blkxData.getLong();

			if (chunkIndex == 0) {
				blkxPosition += compressedOffset;
			}
			compressedOffset -= blkxPosition;
			switch (entryType) {
			case 0x00000000:
			case 0x00000002:
				inputDecoderTable.add(InputDecoders.ZERO, -1l, -1l, chunkSectorCount * 512);
				break;
			case 0x00000001:
				inputDecoderTable.add(InputDecoders.IDENTITY, compressedOffset, compressedSize, -1l);
				dataChunkCount++;
				break;
			case 0x80000004:
				inputDecoderTable.add(InputDecoders.unsupportedInputDecoder("Apple Data Compression (ADC)"),
						compressedOffset, compressedSize, -1l);
				dataChunkCount++;
				break;
			case 0x80000005:
				inputDecoderTable.add(ZLIB_INPUT_DECODER, compressedOffset, compressedSize, -1l);
				dataChunkCount++;
				break;
			case 0x80000006:
				inputDecoderTable.add(BZ2LIB_INPUT_DECODER, compressedOffset, compressedSize, -1l);
				dataChunkCount++;
				break;
			case 0x80000007:
				inputDecoderTable.add(LZMALIB_INPUT_DECODER, compressedOffset, compressedSize, -1l);
				dataChunkCount++;
				break;
			case 0x7ffffffe:
				// Comment block
				break;
			case 0xffffffff:
				// Terminator block
				break;
			default:
				throw unexpectedData("Unexpected entry type", entryType);
			}
		}
		return new BlkxDescriptor(blkxPosition, dataChunkCount, blkxDescriptor.sectorStart(),
				blkxDescriptor.sectorEnd());
	}

	private UnexpectedDataException unexpectedData(String hint, Object... data) {
		return new UnexpectedDataException(hint, -1l, data);
	}

}
