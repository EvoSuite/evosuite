package org.evosuite.symbolic.z3;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

class Z3ModelParser {

	private final Map<String, Object> initialValues;

	public Z3ModelParser(Map<String, Object> initialValues) {
		this.initialValues = initialValues;
	}

	public Map<String, Object> parse(String z3ResultStr) {

		Map<String, Object> solution = new HashMap<String, Object>();

		StringTokenizer tokenizer = new StringTokenizer(z3ResultStr, "() \n\t");
		tokenizer.nextToken(); // sat
		tokenizer.nextToken(); // model

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("define-fun")) {
				String funcName = tokenizer.nextToken();
				if (funcName.equals(Z3Solver.STR_LENGTH)) {

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
					}
				}
			}
		}
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
