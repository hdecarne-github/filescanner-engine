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
grammar FormatSpecGrammar;

@header {
package de.carne.filescanner.engine.format.spec.grammar;
}

// Tokens

ByteSymbols: 'byte_symbols';
WordSymbols: 'word_symbols';
DWordSymbols: 'dword_symbols';
QWordSymbols: 'qword_symbols';

ByteFlagSymbols: 'byte_flag_symbols';
WordFlagSymbols: 'word_flag_symbols';
DWordFlagSymbols: 'dword_flag_symbols';
QWordFlagSymbols: 'qword_flag_symbols';

FormatSpec: 'format_spec';
Struct: 'struct';
Union: 'union';
Sequence: 'sequence';
Conditional: 'conditional';
Encoded: 'encoded';
Validate: 'validate';
Text: 'text';
Format: 'format';
Renderer: 'renderer';
Export: 'export';
LittleEndian: 'littleEndian';
BigEndian: 'bigEndian';
Charset: 'charset';

Byte: 'byte';
Word: 'word';
DWord: 'dword';
QWord: 'qword';
Char: 'char';

Apply: '->';
LBracket: '(';
RBracket: ')';
LCBracket: '{';
RCBracket: '}';
LSBracket: '[';
RSBracket: ']';
Comma: ',';
Colon: ':';
At: '@';
Hash: '#';

Number: DecimalNumber|OctalNumber|HexaDecimalNumber;
fragment DecimalNumber: '0'|[1-9][0-9]*;
fragment OctalNumber: '0'[1-7][0-7]*;
fragment HexaDecimalNumber: '0x'[0-9a-fA-F]+;
NumberArray: LCBracket (Number (Comma Number)* )? RCBracket;

Identifier: [a-zA-Z][a-zA-Z0-9_]*;
QuotedString: '"' (~["\\\r\n]|'\\'[btnfr"\\]|'\\u'[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F])* '"';
QuotedChar: '\'' (~['\\\r\n]|'\\'[btnfr'\\]|'\\u'[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]) '\'';
QutoedCharArray: LCBracket (QuotedChar (Comma QuotedChar)* )? RCBracket;

SingleLineComment: '//' ~[\r\n]* -> skip;
MultiLineComment: '/*' .*? '*/' -> skip;
Whitespace:	[ \t\r\n]+ -> skip;

// Rules

formatSpecs
	: (symbols|flagSymbols|formatSpec|structSpec|unionSpec|sequenceSpec)*
	;

// Symbol rules

symbols
	: (byteSymbols|wordSymbols|dwordSymbols|qwordSymbols)
	;

byteSymbols
	: symbolsIdentifier Colon ByteSymbols LCBracket symbolDefinition+ RCBracket
	;

wordSymbols
	: symbolsIdentifier Colon WordSymbols LCBracket symbolDefinition+ RCBracket
	;

dwordSymbols
	: symbolsIdentifier Colon DWordSymbols LCBracket symbolDefinition+ RCBracket
	;

qwordSymbols
	: symbolsIdentifier Colon QWordSymbols LCBracket symbolDefinition+ RCBracket
	;

flagSymbols
	: (byteFlagSymbols|wordFlagSymbols|dwordFlagSymbols|qwordFlagSymbols)
	;

byteFlagSymbols
	: symbolsIdentifier Colon ByteFlagSymbols LCBracket symbolDefinition+ RCBracket
	;

wordFlagSymbols
	: symbolsIdentifier Colon WordFlagSymbols LCBracket symbolDefinition+ RCBracket
	;

dwordFlagSymbols
	: symbolsIdentifier Colon DWordFlagSymbols LCBracket symbolDefinition+ RCBracket
	;

qwordFlagSymbols
	: symbolsIdentifier Colon QWordFlagSymbols LCBracket symbolDefinition+ RCBracket
	;
	
symbolDefinition
	: symbolValue Colon symbol
	;

symbolsIdentifier
	: Identifier
	;
	
symbolValue
	: Number
	;
	
symbol
	: QuotedString
	;

// Spec rules

formatSpec
	: specIdentifier Colon FormatSpec textExpression LCBracket formatSpecElement+ RCBracket(Apply (compositeSpecByteOrderModifier|compositeSpecExportModifier))*
	;

structSpec
	: specIdentifier Colon anonymousStructSpec
	;
	
anonymousStructSpec
	: Struct textExpression? LCBracket formatSpecElement+ RCBracket (Apply (compositeSpecByteOrderModifier|compositeSpecExportModifier))*
	;
	
sequenceSpec
	: specIdentifier Colon anonymousSequenceSpec
	;
	
anonymousSequenceSpec
	: Sequence textExpression? formatSpecElement (Apply (compositeSpecByteOrderModifier|compositeSpecExportModifier))*
	;

unionSpec
	: specIdentifier Colon anonymousUnionSpec
	;

anonymousUnionSpec
	: Union textExpression? LCBracket compositeSpecElement+ RCBracket (Apply (compositeSpecByteOrderModifier|compositeSpecExportModifier))*
	;
	
compositeSpecByteOrderModifier
	: (LittleEndian|BigEndian) LBracket RBracket
	;
	
compositeSpecExportModifier
	: Export LBracket externalReference RBracket
	;
	
formatSpecElement
	: (specReference|attributeSpec|anonymousStructSpec|anonymousUnionSpec|anonymousSequenceSpec|conditionalSpec|encodedInputSpec)
	;
	
compositeSpecElement
	: (specReference|anonymousStructSpec|anonymousUnionSpec|anonymousSequenceSpec)
	;
	
conditionalSpec
	: Conditional externalReference LCBracket specReference* RCBracket
	;
	
encodedInputSpec
	: Encoded externalReference
	;
	
attributeSpec
	: (byteAttributeSpec|wordAttributeSpec|dwordAttributeSpec|qwordAttributeSpec|byteArrayAttributeSpec|wordArrayAttributeSpec|dwordArrayAttributeSpec|qwordArrayAttributeSpec|charArrayAttributeSpec)
	;

byteAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? Byte textExpression (Apply (attributeFormatModifier|attributeValidateNumberModifier|attributeRendererModifier))*
	;

wordAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? Word textExpression (Apply (attributeFormatModifier|attributeValidateNumberModifier|attributeRendererModifier))*
	;

dwordAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? DWord textExpression (Apply (attributeFormatModifier|attributeValidateNumberModifier|attributeRendererModifier))*
	;

qwordAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? QWord textExpression (Apply (attributeFormatModifier|attributeValidateNumberModifier|attributeRendererModifier))*
	;

byteArrayAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? Byte LSBracket numberExpression RSBracket textExpression (Apply (attributeFormatModifier|attributeValidateNumberArrayModifier|attributeRendererModifier))*
	;

wordArrayAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? Word LSBracket numberExpression RSBracket textExpression (Apply (attributeFormatModifier|attributeValidateNumberArrayModifier|attributeRendererModifier))*
	;

dwordArrayAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? DWord LSBracket numberExpression RSBracket textExpression (Apply (attributeFormatModifier|attributeValidateNumberArrayModifier|attributeRendererModifier))*
	;

qwordArrayAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? QWord LSBracket numberExpression RSBracket textExpression (Apply (attributeFormatModifier|attributeValidateNumberArrayModifier|attributeRendererModifier))*
	;

charArrayAttributeSpec
	: (specIdentifier (At scopeIdentifier)? Colon)? Char LSBracket numberExpression RSBracket textExpression (Apply (attributeFormatModifier|attributeValidateStringModifier|attributeRendererModifier|stringAttributeCharsetModifier))*
	;
	
attributeFormatModifier
	: Format LBracket (formatText|specReference) RBracket
	;
	
attributeValidateNumberModifier
	: Validate LBracket (numberValue|specReference) RBracket
	;
	
attributeValidateNumberArrayModifier
	: Validate LBracket (numberArrayValue|specReference) RBracket
	;

attributeValidateStringModifier
	: Validate LBracket (simpleText|specReference) RBracket
	;
	
attributeRendererModifier
	: Renderer LBracket specReference RBracket
	;
	
stringAttributeCharsetModifier
	: Charset LBracket simpleText RBracket
	;
	
specIdentifier
	: Identifier
	;
	
scopeIdentifier
	: specIdentifier
	;

numberExpression
	: (numberValue|specReference|externalReference)
	;

numberValue
	: Number
	;

numberArrayValue
	: NumberArray
	;

textExpression
	: (simpleText|Text LBracket formatText (Comma (specReference))* RBracket|externalReference)
	;
	
simpleText
	: QuotedString
	;
	
formatText
	: QuotedString
	;

specReference
	: At referencedSpec
	;
	
referencedSpec
	: specIdentifier
	;

externalReference
	: Hash referencedExternal
	;
	
referencedExternal
	: Identifier
	;
