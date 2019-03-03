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
package de.carne.filescanner.provider.udif;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.carne.boot.logging.Log;
import de.carne.filescanner.engine.UnexpectedDataException;
import de.carne.filescanner.engine.format.HexFormat;
import de.carne.filescanner.engine.format.spec.CompositeSpec;
import de.carne.filescanner.engine.format.spec.DecodeAtSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpec;
import de.carne.filescanner.engine.format.spec.EncodedInputSpecConfig;
import de.carne.filescanner.engine.format.spec.FormatSpecs;
import de.carne.filescanner.engine.format.spec.StructSpec;
import de.carne.filescanner.engine.input.InputDecoderTable;
import de.carne.filescanner.engine.input.InputDecoders;

class ResourceForkHandler extends DefaultHandler {

	private static final Log LOG = new Log();

	private static final SAXParserFactory PARSER_FACTORY = SAXParserFactory.newInstance();

	private static final String ELEMENT_KEY0 = "plist/dict/key";
	private static final String ELEMENT_KEY1 = "plist/dict/dict/key";
	private static final String ELEMENT_KEY2 = "plist/dict/dict/array/dict/key";

	private static final String KEY_RESOURCEFORK = "resource-fork";
	private static final String KEY_BLKX = "blkx";
	private static final String KEY_DATA = "Data";
	private static final String KEY_NAME = "Name";

	private final Deque<String> elementStack = new LinkedList<>();

	private @NonNull String[] keyPath = new @NonNull String[] { "", "", "" };
	private byte @Nullable [] blkxDataBuffer = null;
	private @Nullable String blkxName = null;
	private SortedMap<Long, EncodedInputSpec> blkxSpecs = new TreeMap<>();

	public static CompositeSpec parse(InputStream input) throws IOException {
		ResourceForkHandler handler = new ResourceForkHandler();

		try {
			SAXParser parser = PARSER_FACTORY.newSAXParser();

			parser.parse(input, handler);
		} catch (SAXException | ParserConfigurationException e) {
			throw new IOException("XML parse failure", e);
		}

		CompositeSpec spec = FormatSpecs.EMPTY;

		if (!handler.blkxSpecs.isEmpty()) {
			StructSpec parsedSpec = new StructSpec();

			for (Map.Entry<Long, EncodedInputSpec> entry : handler.blkxSpecs.entrySet()) {
				long blkxPosition = entry.getKey();
				EncodedInputSpec blkxSpec = entry.getValue();
				StructSpec dataForkSpec = new StructSpec();

				dataForkSpec.result(formatDataForkName(blkxPosition));

				dataForkSpec.add(blkxSpec);

				DecodeAtSpec decodeAtSpec = new DecodeAtSpec(dataForkSpec);

				decodeAtSpec.position(blkxPosition);
				parsedSpec.add(decodeAtSpec);
			}
			spec = parsedSpec;
		}
		return spec;
	}

	private static String formatDataForkName(long blkxPosition) {
		StringBuilder name = new StringBuilder();

		name.append("Data fork [");
		HexFormat.formatLong(name, blkxPosition);
		name.append(']');
		return name.toString();
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
		String characters = new String(ch, start, length);

		if (ELEMENT_KEY0.equals(element)) {
			this.keyPath[0] = characters;
			this.keyPath[1] = "";
			this.keyPath[2] = "";
		} else if (ELEMENT_KEY1.equals(element)) {
			this.keyPath[1] = characters;
			this.keyPath[2] = "";
		} else if (ELEMENT_KEY2.equals(element)) {
			this.keyPath[2] = characters;
		} else if (KEY_RESOURCEFORK.equals(this.keyPath[0]) && KEY_BLKX.equals(this.keyPath[1])) {
			if (KEY_DATA.equals(this.keyPath[2])) {
				feedBlkxDataBuffer(characters);
			} else if (KEY_NAME.equals(this.keyPath[2])) {
				this.blkxName = characters.trim();
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String element = this.elementStack.pop();

		if (ELEMENT_KEY1.equals(element) && this.blkxDataBuffer != null && this.blkxName != null) {
			try {
				ByteBuffer blkxData = ByteBuffer.wrap(this.blkxDataBuffer).order(ByteOrder.BIG_ENDIAN);
				long blkxPosition = decodeBlkxHeader(blkxData);
				InputDecoderTable blkxDecoderTable = decodeBlkxChunks(blkxData);
				EncodedInputSpec blkxSpec = new EncodedInputSpec(
						new EncodedInputSpecConfig(Objects.toString(this.blkxName)).inputDecoderTable(blkxDecoderTable)
								.decodedInputName("image.bin"));

				this.blkxSpecs.put(blkxPosition, blkxSpec);
			} catch (IOException e) {
				LOG.warning(e, "Failed to decode block list ''{0}''", this.blkxName);
			}
			this.blkxDataBuffer = null;
			this.blkxName = null;
		}
	}

	private void feedBlkxDataBuffer(String characters) {
		byte[] data = Base64.getMimeDecoder().decode(characters);
		byte[] oldDataBuffer = this.blkxDataBuffer;

		if (oldDataBuffer != null) {
			byte[] newDataBuffer = new byte[oldDataBuffer.length + data.length];

			System.arraycopy(oldDataBuffer, 0, newDataBuffer, 0, oldDataBuffer.length);
			System.arraycopy(data, 0, newDataBuffer, oldDataBuffer.length, data.length);
			this.blkxDataBuffer = newDataBuffer;
		} else {
			this.blkxDataBuffer = data;
		}
	}

	private long decodeBlkxHeader(ByteBuffer blkxData) throws IOException {
		// Also check for the chunk count needed for chunk decoding
		if (blkxData.remaining() < 204) {
			throw unexpectedData("Insufficient block list size", blkxData.remaining());
		}

		int signature = blkxData.getInt();
		int version = blkxData.getInt();
		/* long sectorNumber = */ blkxData.getLong();
		/* long sectorCount = */ blkxData.getLong();
		long dataOffset = blkxData.getLong();

		if (signature != 0x6d697368 || version != 1 || dataOffset < 0) {
			throw unexpectedData("Unexpeced block list header data", signature, version, dataOffset);
		}
		blkxData.position(blkxData.position() + 42 * 4);
		return dataOffset;
	}

	private InputDecoderTable decodeBlkxChunks(ByteBuffer blkxData) throws IOException {
		InputDecoderTable inputDecoderTable = new InputDecoderTable();
		int numberOfBlockChunks = blkxData.getInt();

		if (blkxData.remaining() != numberOfBlockChunks * 40) {
			throw unexpectedData("Mismatching block list size", numberOfBlockChunks, blkxData.remaining());
		}

		for (int chunkIndex = 0; chunkIndex < numberOfBlockChunks; chunkIndex++) {
			int entryType = blkxData.getInt();
			/* int comment = */ blkxData.getInt();
			/* long chunkSectorNumber = */ blkxData.getLong();
			/* long chunkSectorCount = */ blkxData.getLong();
			long compressedOffset = blkxData.getLong();
			long compressedSize = blkxData.getLong();

			switch (entryType) {
			case 0x00000000:
				inputDecoderTable.add(compressedOffset, InputDecoders.ZERO, compressedSize);
				break;
			case 0x00000001:
				inputDecoderTable.add(compressedOffset, InputDecoders.IDENTITY, compressedSize);
				break;
			case 0x80000004:
				inputDecoderTable.add(compressedOffset,
						InputDecoders.unsupportedInputDecoder("Apple Data Compression (ADC)"), compressedSize);
				break;
			case 0x80000005:
				inputDecoderTable.add(compressedOffset, InputDecoders.unsupportedInputDecoder("zLib data compression"),
						compressedSize);
				break;
			case 0x80000006:
				inputDecoderTable.add(compressedOffset,
						InputDecoders.unsupportedInputDecoder("bz2lib data compression"), compressedSize);
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
		return inputDecoderTable;
	}

	private UnexpectedDataException unexpectedData(String hint, Object... data) {
		return new UnexpectedDataException(hint, -1l, data);
	}

}
