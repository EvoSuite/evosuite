package org.evosuite.symbolic.solver.cvc4;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CVC4ModelParser {

	private final Map<String, Object> initialValues;
	static Logger logger = LoggerFactory.getLogger(CVC4ModelParser.class);

	public CVC4ModelParser(Map<String, Object> initialValues) {
		this.initialValues = initialValues;
	}

	public CVC4ModelParser() {
		this.initialValues = null;
	}

	public Map<String, Object> parse(String cvc4ResultStr) {
		Map<String, Object> solution = new HashMap<String, Object>();

		String token;
		StringTokenizer tokenizer = new StringTokenizer(cvc4ResultStr,
				"() \n\t", true);
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

								double numerator = Double
										.parseDouble(numeratorStr);
								double denominator = Double
										.parseDouble(denominatorStr);

								value = -(numerator / denominator);
								token = tokenizer.nextToken(); // ")"
								token = tokenizer.nextToken(); // ")"
							} else {
								String absoluteValueStr = token;
								value = Double.parseDouble("-"
										+ absoluteValueStr);
								token = tokenizer.nextToken(); // )
							}
						} else {

							if (token.equals("/")) {
								token = tokenizer.nextToken(); // " "
								String numeratorStr = tokenizer.nextToken();
								token = tokenizer.nextToken(); // " "
								String denominatorStr = tokenizer.nextToken();

								double numerator = Double
										.parseDouble(numeratorStr);
								double denominator = Double
										.parseDouble(denominatorStr);

								value = (numerator / denominator);
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

					String stringToken;
					do {
						stringToken = tokenizer.nextToken();
						value.append(stringToken);
					} while (!stringToken.endsWith("\""));

					String stringWithQuotes = value.toString();
					String stringWithoutQuotes = stringWithQuotes.substring(1,
							stringWithQuotes.length() - 1);
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

		return solution;
	}

	private static void addMissingValues(Map<String, Object> initialValues,
			Map<String, Object> solution) {
		for (String otherVarName : initialValues.keySet()) {
			if (!solution.containsKey(otherVarName)) {
				solution.put(otherVarName, initialValues.get(otherVarName));
			}
		}
	}
}
