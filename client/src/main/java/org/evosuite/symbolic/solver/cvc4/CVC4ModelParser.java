package org.evosuite.symbolic.solver.cvc4;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CVC4ModelParser {

	private final Map<String, Object> initialValues;
	static Logger logger = LoggerFactory.getLogger(CVC4ModelParser.class);

	public CVC4ModelParser(Map<String, Object> initialValues) {
		this.initialValues = initialValues;
	}

	public Map<String, Object> parse(String cvc4ResultStr) {
		Map<String, Object> solution = new HashMap<String, Object>();

		Map<String, String> arraysToFuncMap = new HashMap<String,String>();

		StringTokenizer tokenizer = new StringTokenizer(cvc4ResultStr, "() \n\t");
		tokenizer.nextToken(); // sat
		tokenizer.nextToken(); // model

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("define-fun")) {
				String funcName = tokenizer.nextToken();
				if (funcName.equals(CVC4Solver.STR_LENGTH)) {

					tokenizer.nextToken(); // x!1
					tokenizer.nextToken(); // Array
					tokenizer.nextToken(); // Int
					tokenizer.nextToken(); // Int
					tokenizer.nextToken(); // Int

					tokenizer.nextToken(); // value
				} else {
					String typeName = tokenizer.nextToken();
					if (typeName.equals("Int")) {
						String integerValueStr = tokenizer.nextToken();
						Long value;
						if (integerValueStr.equals("-")) {
							String absoluteIntegerValue = tokenizer.nextToken();
							value = Long.parseLong("-" + absoluteIntegerValue);
						} else {
							value = Long.parseLong(integerValueStr);
						}
						solution.put(funcName, value);
					} else if (typeName.equals("Real")) {
						String realValueStr = tokenizer.nextToken();
						Double value;
						if (realValueStr.equals("-")) {
							String absoluteValueStr = tokenizer.nextToken();
							if (absoluteValueStr.equals("/")) {
								String numeratorStr = tokenizer.nextToken();
								String denominatorStr = tokenizer.nextToken();

								double numerator = Double
										.parseDouble(numeratorStr);
								double denominator = Double
										.parseDouble(denominatorStr);

								value = -(numerator / denominator);
							} else {
								value = Double.parseDouble("-"
										+ absoluteValueStr);
							}
						} else {

							if (realValueStr.equals("/")) {
								String numeratorStr = tokenizer.nextToken();
								String denominatorStr = tokenizer.nextToken();

								double numerator = Double
										.parseDouble(numeratorStr);
								double denominator = Double
										.parseDouble(denominatorStr);

								value = (numerator / denominator);
							} else {

								value = Double.parseDouble(realValueStr);
							}
						}
						solution.put(funcName, value);
					} else if (typeName.equals("Array")) {
						tokenizer.nextToken(); //Int
						tokenizer.nextToken(); //Int
						tokenizer.nextToken(); //_
						tokenizer.nextToken(); //as_array
						String arrayFuncName = tokenizer.nextToken();
						arraysToFuncMap .put(arrayFuncName, funcName);
					} else if (typeName.equals("x!1")) {
						
					} else {
//						throw new IllegalArgumentException(
//								"Must implement this production");
					}
				}
			} else {
//				throw new IllegalArgumentException(
//						"Must implement this production");
			}
		}

		if (solution.isEmpty()) {
			logger.warn("The Z3 model has no variables");
		} else {
			logger.debug("Parsed values from Z3 output");
			for (String varName : solution.keySet()) {
				String valueOf = String.valueOf(solution.get(varName));
				logger.debug(varName + ":" + valueOf);
			}
		}

		logger.debug("Adding missing values to Solver solution");
		addMissingValues(initialValues, solution);

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
