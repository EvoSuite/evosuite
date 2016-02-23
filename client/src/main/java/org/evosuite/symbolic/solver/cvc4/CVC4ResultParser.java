/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.solver.cvc4;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.evosuite.symbolic.solver.ResultParser;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CVC4ResultParser extends ResultParser {

	private final Map<String, Object> initialValues;
	static Logger logger = LoggerFactory.getLogger(CVC4ResultParser.class);

	public CVC4ResultParser(Map<String, Object> initialValues) {
		this.initialValues = initialValues;
	}

	public CVC4ResultParser() {
		this.initialValues = null;
	}

	public SolverResult parse(String cvc4ResultStr)
			throws SolverParseException, SolverErrorException, SolverTimeoutException {
		if (cvc4ResultStr.startsWith("sat")) {
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
		token = tokenizer.nextToken(); // sat
		token = tokenizer.nextToken(); //
		token = tokenizer.nextToken(); // (
		token = tokenizer.nextToken(); // model
		token = tokenizer.nextToken(); // \n

		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken(); // (
			if (token.equals(")")) {
				break;
			}
			token = tokenizer.nextToken(); // define-fun ?
			if (token.equals("define-fun")) {
				token = tokenizer.nextToken(); //
				String fun_name = tokenizer.nextToken();
				token = tokenizer.nextToken(); //
				token = tokenizer.nextToken(); // (
				token = tokenizer.nextToken(); // )
				token = tokenizer.nextToken(); //

				String typeName = tokenizer.nextToken();
				if (typeName.equals("Int")) {
					token = tokenizer.nextToken(); // " "
					token = tokenizer.nextToken(); //
					boolean neg = false;
					String integerValueStr;
					if (token.equals("(")) {
						neg = true;
						token = tokenizer.nextToken(); // -
						token = tokenizer.nextToken(); // " "
						integerValueStr = tokenizer.nextToken();
					} else {
						integerValueStr = token;
					}
					Long value;
					if (neg) {
						String absoluteIntegerValue = integerValueStr;
						value = Long.parseLong("-" + absoluteIntegerValue);
					} else {
						value = Long.parseLong(integerValueStr);
					}
					solution.put(fun_name, value);
					if (neg) {
						token = tokenizer.nextToken(); // )
					}
					token = tokenizer.nextToken(); // )
					token = tokenizer.nextToken(); // \n

				} else if (typeName.equals("Real")) {
					token = tokenizer.nextToken(); // " "
					token = tokenizer.nextToken();
					Double value;
					if (!token.equals("(")) {
						value = Double.parseDouble(token);
					} else {
						token = tokenizer.nextToken();
						if (token.equals("-")) {
							token = tokenizer.nextToken(); // " "
							token = tokenizer.nextToken(); // ?
							if (token.equals("(")) {
								token = tokenizer.nextToken(); // "/"
								token = tokenizer.nextToken(); // " "
								String numeratorStr = tokenizer.nextToken();
								token = tokenizer.nextToken(); // " "
								String denominatorStr = tokenizer.nextToken();

								value = parseRational(true, numeratorStr, denominatorStr);
								token = tokenizer.nextToken(); // ")"
								token = tokenizer.nextToken(); // ")"
							} else {
								String absoluteValueStr = token;
								value = Double.parseDouble("-" + absoluteValueStr);
								token = tokenizer.nextToken(); // )
							}
						} else {

							if (token.equals("/")) {
								token = tokenizer.nextToken(); // " "
								String numeratorStr = tokenizer.nextToken();
								token = tokenizer.nextToken(); // " "
								String denominatorStr = tokenizer.nextToken();
								value = parseRational(false,numeratorStr,denominatorStr);
								token = tokenizer.nextToken(); // )
							} else {

								value = Double.parseDouble(token);
							}
						}
					}
					solution.put(fun_name, value);
					token = tokenizer.nextToken(); // )
					token = tokenizer.nextToken(); // \n

				} else if (typeName.equals("String")) {
					token = tokenizer.nextToken();
					StringBuffer value = new StringBuffer();

					while (!token.startsWith("\"")) { // move until \" is found
						token = tokenizer.nextToken();

					}

					value.append(token);
					if (!token.substring(1).endsWith("\"")) {
						String stringToken;
						do {
							if (!tokenizer.hasMoreTokens()) {
								System.out.println("Error!");
							}
							stringToken = tokenizer.nextToken();
							value.append(stringToken);
						} while (!stringToken.endsWith("\"")); // append until
																// \" is found
					}
					String stringWithQuotes = value.toString();
					String stringWithoutQuotes = stringWithQuotes.substring(1, stringWithQuotes.length() - 1);
					solution.put(fun_name, stringWithoutQuotes);
					token = tokenizer.nextToken(); // )
					token = tokenizer.nextToken(); // \n
				} else {
					// throw new IllegalArgumentException(
					// "Must implement this production");
				}
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

	private static void addMissingValues(Map<String, Object> initialValues, Map<String, Object> solution) {
		for (String otherVarName : initialValues.keySet()) {
			if (!solution.containsKey(otherVarName)) {
				solution.put(otherVarName, initialValues.get(otherVarName));
			}
		}
	}
}
