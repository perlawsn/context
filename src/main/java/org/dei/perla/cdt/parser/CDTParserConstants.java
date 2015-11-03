/* Generated By:JavaCC: Do not edit this line. CDTParserConstants.java */
package org.dei.perla.cdt.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CDTParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int KEYWORD_CREATE = 8;
  /** RegularExpression Id. */
  int KEYWORD_DIMENSION = 9;
  /** RegularExpression Id. */
  int KEYWORD_CONCEPT = 10;
  /** RegularExpression Id. */
  int KEYWORD_WHEN = 11;
  /** RegularExpression Id. */
  int KEYWORD_CHILD = 12;
  /** RegularExpression Id. */
  int KEYWORD_OF = 13;
  /** RegularExpression Id. */
  int KEYWORD_ADD = 14;
  /** RegularExpression Id. */
  int KEYWORD_ATTRIBUTE = 15;
  /** RegularExpression Id. */
  int KEYWORD_DOLLAR = 16;
  /** RegularExpression Id. */
  int KEYWORD_EVALUATED = 17;
  /** RegularExpression Id. */
  int KEYWORD_ON = 18;
  /** RegularExpression Id. */
  int KEYWORD_WITH = 19;
  /** RegularExpression Id. */
  int KEYWORD_ENABLE = 20;
  /** RegularExpression Id. */
  int KEYWORD_DISABLE = 21;
  /** RegularExpression Id. */
  int KEYWORD_COMPONENT = 22;
  /** RegularExpression Id. */
  int KEYWORD_REFRESH = 23;
  /** RegularExpression Id. */
  int KEYWORD_COLON = 24;
  /** RegularExpression Id. */
  int KEYWORD_IS = 25;
  /** RegularExpression Id. */
  int KEYWORD_BETWEEN = 26;
  /** RegularExpression Id. */
  int KEYWORD_LIKE = 27;
  /** RegularExpression Id. */
  int OPERATOR_MULTIPLY = 28;
  /** RegularExpression Id. */
  int OPERATOR_DIVIDE = 29;
  /** RegularExpression Id. */
  int OPERATOR_MODULO = 30;
  /** RegularExpression Id. */
  int OPERATOR_PLUS = 31;
  /** RegularExpression Id. */
  int OPERATOR_MINUS = 32;
  /** RegularExpression Id. */
  int OPERATOR_NOT = 33;
  /** RegularExpression Id. */
  int OPERATOR_XOR = 34;
  /** RegularExpression Id. */
  int OPERATOR_AND = 35;
  /** RegularExpression Id. */
  int OPERATOR_OR = 36;
  /** RegularExpression Id. */
  int OPERATOR_BITWISE_NOT = 37;
  /** RegularExpression Id. */
  int OPERATOR_BITWISE_LSH = 38;
  /** RegularExpression Id. */
  int OPERATOR_BITWISE_RSH = 39;
  /** RegularExpression Id. */
  int OPERATOR_BITWISE_XOR = 40;
  /** RegularExpression Id. */
  int OPERATOR_BITWISE_AND = 41;
  /** RegularExpression Id. */
  int OPERATOR_BITWISE_OR = 42;
  /** RegularExpression Id. */
  int OPERATOR_GREATER = 43;
  /** RegularExpression Id. */
  int OPERATOR_LESS = 44;
  /** RegularExpression Id. */
  int OPERATOR_GREATER_EQUAL = 45;
  /** RegularExpression Id. */
  int OPERATOR_LESS_EQUAL = 46;
  /** RegularExpression Id. */
  int OPERATOR_EQUAL = 47;
  /** RegularExpression Id. */
  int OPERATOR_NOT_EQUAL = 48;
  /** RegularExpression Id. */
  int FUNCTION_COUNT = 49;
  /** RegularExpression Id. */
  int FUNCTION_AVG = 50;
  /** RegularExpression Id. */
  int FUNCTION_MAX = 51;
  /** RegularExpression Id. */
  int FUNCTION_MIN = 52;
  /** RegularExpression Id. */
  int FUNCTION_SUM = 53;
  /** RegularExpression Id. */
  int TIMEUNIT_S = 54;
  /** RegularExpression Id. */
  int TIMEUNIT_M = 55;
  /** RegularExpression Id. */
  int TIMEUNIT_H = 56;
  /** RegularExpression Id. */
  int TIMEUNIT_MS = 57;
  /** RegularExpression Id. */
  int TIMEUNIT_D = 58;
  /** RegularExpression Id. */
  int TYPE_ID = 59;
  /** RegularExpression Id. */
  int TYPE_TIMESTAMP = 60;
  /** RegularExpression Id. */
  int TYPE_BOOLEAN = 61;
  /** RegularExpression Id. */
  int TYPE_INTEGER = 62;
  /** RegularExpression Id. */
  int TYPE_FLOAT = 63;
  /** RegularExpression Id. */
  int TYPE_STRING = 64;
  /** RegularExpression Id. */
  int TYPE_ANY = 65;
  /** RegularExpression Id. */
  int CONSTANT_NULL = 66;
  /** RegularExpression Id. */
  int CONSTANT_BOOLEAN_TRUE = 67;
  /** RegularExpression Id. */
  int CONSTANT_BOOLEAN_FALSE = 68;
  /** RegularExpression Id. */
  int CONSTANT_BOOLEAN_UNKNOWN = 69;
  /** RegularExpression Id. */
  int CONSTANT_INTEGER_10 = 70;
  /** RegularExpression Id. */
  int CONSTANT_INTEGER_16 = 71;
  /** RegularExpression Id. */
  int CONSTANT_FLOAT = 72;
  /** RegularExpression Id. */
  int CONSTANT_SINGLE_QUOTED_STRING_START = 73;
  /** RegularExpression Id. */
  int CONSTANT_DOUBLE_QUOTED_STRING_START = 74;
  /** RegularExpression Id. */
  int CONSTANT_SINGLE_QUOTED_STRING_VALUE = 75;
  /** RegularExpression Id. */
  int CONSTANT_DOUBLE_QUOTED_STRING_VALUE = 76;
  /** RegularExpression Id. */
  int CONSTANT_SINGLE_QUOTED_STRING_END = 77;
  /** RegularExpression Id. */
  int CONSTANT_DOUBLE_QUOTED_STRING_END = 78;
  /** RegularExpression Id. */
  int DIGIT = 79;
  /** RegularExpression Id. */
  int LITERAL = 80;
  /** RegularExpression Id. */
  int UNDERSCORE = 81;
  /** RegularExpression Id. */
  int HEXADECIMAL = 82;
  /** RegularExpression Id. */
  int IDENTIFIER = 83;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int COMMENT = 1;
  /** Lexical state. */
  int NON_SINGLE_QUOTED_STRING = 2;
  /** Lexical state. */
  int NON_DOUBLE_QUOTED_STRING = 3;
  /** Lexical state. */
  int NON_SINGLE_QUOTED_STRING_END = 4;
  /** Lexical state. */
  int NON_DOUBLE_QUOTED_STRING_END = 5;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\r\"",
    "\"\\t\"",
    "\"\\n\"",
    "\"/*\"",
    "<token of kind 6>",
    "\"*/\"",
    "\"CREATE\"",
    "\"DIMENSION\"",
    "\"CONCEPT\"",
    "\"WHEN\"",
    "\"CHILD\"",
    "\"OF\"",
    "\"ADD\"",
    "\"ATTRIBUTE\"",
    "\"$\"",
    "\"EVALUATED\"",
    "\"ON\"",
    "\"WITH\"",
    "\"ENABLE\"",
    "\"DISABLE\"",
    "\"COMPONENT\"",
    "\"REFRESH\"",
    "\":\"",
    "\"IS\"",
    "\"BETWEEN\"",
    "\"LIKE\"",
    "\"*\"",
    "\"/\"",
    "\"%\"",
    "\"+\"",
    "\"-\"",
    "\"NOT\"",
    "\"XOR\"",
    "\"AND\"",
    "\"OR\"",
    "\"~\"",
    "\"<<\"",
    "\">>\"",
    "\"^\"",
    "\"&\"",
    "\"|\"",
    "\">\"",
    "\"<\"",
    "\">=\"",
    "\"<=\"",
    "\"=\"",
    "<OPERATOR_NOT_EQUAL>",
    "\"COUNT\"",
    "\"AVG\"",
    "\"MAX\"",
    "\"MIN\"",
    "\"SUM\"",
    "<TIMEUNIT_S>",
    "<TIMEUNIT_M>",
    "<TIMEUNIT_H>",
    "<TIMEUNIT_MS>",
    "<TIMEUNIT_D>",
    "\"ID\"",
    "\"TIMESTAMP\"",
    "\"BOOLEAN\"",
    "\"INTEGER\"",
    "\"FLOAT\"",
    "\"STRING\"",
    "\"ANY\"",
    "\"NULL\"",
    "\"TRUE\"",
    "\"FALSE\"",
    "\"UNKNOWN\"",
    "<CONSTANT_INTEGER_10>",
    "<CONSTANT_INTEGER_16>",
    "<CONSTANT_FLOAT>",
    "\"\\\'\"",
    "\"\\\"\"",
    "<CONSTANT_SINGLE_QUOTED_STRING_VALUE>",
    "<CONSTANT_DOUBLE_QUOTED_STRING_VALUE>",
    "\"\\\'\"",
    "\"\\\"\"",
    "<DIGIT>",
    "<LITERAL>",
    "\"_\"",
    "<HEXADECIMAL>",
    "<IDENTIFIER>",
    "\"(\"",
    "\")\"",
  };

}
