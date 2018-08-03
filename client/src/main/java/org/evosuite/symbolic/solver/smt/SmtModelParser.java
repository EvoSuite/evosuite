/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.smt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.text.StringEscapeUtils;
import org.evosuite.symbolic.solver.ResultParser;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SmtModelParser extends ResultParser {

	private static final String MODEL_TOKEN = "model";
	private static final String SAT_TOKEN = "sat";
	private static final String BLANK_SPACE_TOKEN = " ";
	private static final String REAL_TOKEN = "Real";
	private static final String QUOTE_TOKEN = "\"";
	private static final String STRING_TOKEN = "String";
	private static final String SLASH_TOKEN = "/";
	private static final String MINUS_TOKEN = "-";
	private static final String INT_TOKEN = "Int";
	private static final String DEFINE_FUN_TOKEN = "define-fun";
	private static final String RIGHT_PARENTHESIS_TOKEN = ")";
	private static final String LEFT_PARENTHESIS_TOKEN = "(";
	private static final String NEW_LINE_TOKEN = "\n";
	private final Map<String, Object> initialValues;
	static Logger logger = LoggerFactory.getLogger(SmtModelParser.class);

	public SmtModelParser(Map<String, Object> initialValues) {
		this.initialValues = initialValues;
	}

	public SmtModelParser() {
		this.initialValues = null;
	}

	public SolverResult parse(String cvc4ResultStr)
			throws SolverParseException, SolverErrorException, SolverTimeoutException {
		if (cvc4ResultStr.startsWith(SAT_TOKEN)) {
			logger.debug("CVC4 outcome was SAT");
			SolverResult satResult = parseModel(cvc4ResultStr);
			return satResult;
		} else if (cvc4ResultStr.startsWith("unsat")) {
			logger.debug("CVC4 outcome was UNSAT");
			SolverResult unsatResult = SolverResult.newUNSAT();
			return unsatResult;
		} else if (cvc4ResultStr.startsWith("unknown")) {
			logger.debug("CVC4 outcome was UNKNOWN (probably due to timeout)");
			throw new SolverTimeoutException();
		} else if (cvc4ResultStr.startsWith("(error")) {
			logger.debug("CVC4 output was the following " + cvc4ResultStr);
			throw new SolverErrorException("An error (probably an invalid input) occurred while executing CVC4");
		} else {
			logger.debug("The following CVC4 output could not be parsed " + cvc4ResultStr);
			throw new SolverParseException("CVC4 output is unknown. We are unable to parse it to a proper solution!",
					cvc4ResultStr);
		}

	}

	private SolverResult parseModel(String cvc4ResultStr) {
		Map<String, Object> solution = new HashMap<String, Object>();

		String token;
		StringTokenizer tokenizer = new StringTokenizer(cvc4ResultStr, "() \n\t", true);
		token = tokenizer.nextToken();
		checkExpectedToken(SAT_TOKEN, token);

		token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
		checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

		token = tokenizer.nextToken();
		checkExpectedToken(MODEL_TOKEN, token);

		token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

		while (token != null) {
			if (token.equals(RIGHT_PARENTHESIS_TOKEN)) {
				break;
			}

			checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

			token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

			if (token.equals(DEFINE_FUN_TOKEN)) {
				token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

				String fun_name = token;

				token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
				checkExpectedToken(LEFT_PARENTHESIS_TOKEN, token);

				token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
				checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

				token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

				Object value;
				if (token.equals(INT_TOKEN)) {
					value = parseIntegerValue(tokenizer);

				} else if (token.equals(REAL_TOKEN)) {
					value = parseRealValue(tokenizer);

				} else if (token.equals(STRING_TOKEN)) {
					value = parseStringValue(tokenizer);

				} else {
					throw new IllegalArgumentException("Unknown data type " + token);
				}
				solution.put(fun_name, value);
				token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
			}
		}

		if (solution.isEmpty()) {
			logger.warn("The CVC4 model has no variables");
			return null;
		} else {
			logger.debug("Parsed values from CVC4 output");
			for (String varName : solution.keySet()) {
				String valueOf = String.valueOf(solution.get(varName));
				logger.debug(varName + ":" + valueOf);
			}
		}

		if (initialValues != null) {
			if (!solution.keySet().equals(initialValues.keySet())) {
				logger.debug("Adding missing values to Solver solution");
				addMissingValues(initialValues, solution);
			}
		}

		SolverResult satResult = SolverResult.newSAT(solution);
		return satResult;
	}

	private static String consumeTokens(StringTokenizer tokenizer, String... tokensToConsume) {
		List<String> tokenList = Arrays.asList(tokensToConsume);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (!tokenList.contains(token)) {
				return token;
			}
		}
		// reached end of string
		return null;
	}

	private String parseStringValue(StringTokenizer tokenizer) {
		String token;
		token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
		StringBuilder strBuilder = new StringBuilder();

		checkExpectedToken(QUOTE_TOKEN, String.valueOf(token.charAt(0)));

		strBuilder.append(token);
		if (!token.substring(1).endsWith(QUOTE_TOKEN)) {
			String stringToken;
			do {
				if (!tokenizer.hasMoreTokens()) {
					System.out.println("Error!");
				}
				stringToken = tokenizer.nextToken();
				strBuilder.append(stringToken);
			} while (!stringToken.endsWith(QUOTE_TOKEN)); // append until
			// \" is found
		}
		String stringWithNoQuotes = removeQuotes(strBuilder.toString());
		String string = decode(stringWithNoQuotes);
		token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
		checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

		return string;
	}

	private static String decode(String encodedString) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < encodedString.length(); i++) {
			char c = encodedString.charAt(i);
			if (c == '\\') {
				if (i < encodedString.length() - 1) {
					switch (encodedString.charAt(i + 1)) {
					case 'b': {
						builder.append('\b');
						i++;
						break;
					}
					case 't': {
						builder.append('\t');
						i++;
						break;
					}
					case 'n': {
						builder.append('\n');
						i++;
						break;
					}
					case '\\': {
						builder.append('\\');
						i++;
						break;
					}
					case 'x': {
						String hexString = encodedString.substring(i + 2, i + 4);
						int decimal = Integer.parseInt(hexString, 16);
						builder.append((char) decimal);
						i = i + 3;
						break;
					}
					default: {
						builder.append(c);
					}
					}
				}

			} else {
				builder.append(c);
			}
		}
		return builder.toString();

	}

	private String removeQuotes(String stringWithQuotes) {
		return stringWithQuotes.substring(1, stringWithQuotes.length() - 1);
	}

	private static Double parseRealValue(StringTokenizer tokenizer) {
		String token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

		Double value;
		if (!token.equals(LEFT_PARENTHESIS_TOKEN)) {
			value = Double.parseDouble(token);

		} else {
			token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
			if (token.equals(MINUS_TOKEN)) {

				token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
				if (token.equals(LEFT_PARENTHESIS_TOKEN)) {
					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
					checkExpectedToken(SLASH_TOKEN, token);

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
					String numeratorStr = token;

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
					String denominatorStr = token;

					value = parseRational(true, numeratorStr, denominatorStr);

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
					checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
					checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
				} else {
					String absoluteValueStr = token;
					value = Double.parseDouble(MINUS_TOKEN + absoluteValueStr);

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
					checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
				}
			} else {

				if (token.equals(SLASH_TOKEN)) {

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

					String numeratorStr;
					boolean neg;
					if (token.equals(LEFT_PARENTHESIS_TOKEN)) {

						token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
						checkExpectedToken(MINUS_TOKEN, token);

						neg = true;

						token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
						numeratorStr = token;

						token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
						checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
					} else {
						neg = false;
						numeratorStr = token;
					}

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

					String denominatorStr = token;
					value = parseRational(neg, numeratorStr, denominatorStr);

					token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
					checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
				} else {

					value = Double.parseDouble(token);
				}
			}
		}
		token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
		checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

		return value;
	}

	private static Long parseIntegerValue(StringTokenizer tokenizer) {
		String token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
		boolean neg = false;
		String integerValueStr;
		if (token.equals(LEFT_PARENTHESIS_TOKEN)) {
			neg = true;
			token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

			checkExpectedToken(MINUS_TOKEN, token);
			token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);

			integerValueStr = token;
		} else {
			integerValueStr = token;
		}
		Long value;
		if (neg) {
			String absoluteIntegerValue = integerValueStr;
			value = Long.parseLong(MINUS_TOKEN + absoluteIntegerValue);
		} else {
			value = Long.parseLong(integerValueStr);
		}
		if (neg) {
			token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
			checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);
		}
		token = consumeTokens(tokenizer, NEW_LINE_TOKEN, BLANK_SPACE_TOKEN);
		checkExpectedToken(RIGHT_PARENTHESIS_TOKEN, token);

		return value;
	}

	private static void checkExpectedToken(String expectedToken, String actualToken) {
		if (!actualToken.equals(expectedToken)) {
			throw new IllegalArgumentException(
					"Malformed CVC4 solution. Expected \"" + expectedToken + "\" but found \"" + actualToken + "\"");
		}
	}

	private static void addMissingValues(Map<String, Object> initialValues, Map<String, Object> solution) {
		for (String otherVarName : initialValues.keySet()) {
			if (!solution.containsKey(otherVarName)) {
				solution.put(otherVarName, initialValues.get(otherVarName));
			}
		}
	}
}
