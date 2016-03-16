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
package org.evosuite.symbolic.solver.z3;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.evosuite.symbolic.solver.ResultParser;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Z3ResultParser extends ResultParser {

	private final Map<String, Object> initialValues;
	static Logger logger = LoggerFactory.getLogger(Z3ResultParser.class);

	public Z3ResultParser(Map<String, Object> initialValues) {
		this.initialValues = initialValues;
	}

	public Z3ResultParser() {
		this.initialValues = null;
	}

	public SolverResult parseResult(String z3ResultStr) throws SolverParseException {

		if (z3ResultStr.startsWith("sat")) {
			logger.debug("Z3 outcome was SAT");
			// parse solution
			Map<String, Object> solution = parseModel(z3ResultStr);
			// return a SAT
			SolverResult satResult = SolverResult.newSAT(solution);
			return satResult;
		} else if (z3ResultStr.startsWith("unsat")) {
			logger.debug("Z3 outcome was UNSAT");
			// return an UNSAT
			SolverResult unsatResult = SolverResult.newUNSAT();
			return unsatResult;
		} else {
			logger.debug("Z3 output was " + z3ResultStr);
			throw new SolverParseException("Z3 output is unknown. We are unable to parse it to a proper solution!",
					z3ResultStr);
		}

	}

	private Map<String, Object> parseModel(String z3ResultStr) {

		Map<String, Object> solution = new HashMap<String, Object>();

		Map<String, String> arraysToFuncMap = new HashMap<String, String>();

		StringTokenizer tokenizer = new StringTokenizer(z3ResultStr, "() \n\t");
		tokenizer.nextToken(); // sat
		tokenizer.nextToken(); // model

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("define-fun")) {
				String funcName = tokenizer.nextToken();
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

							value = parseRational(true, numeratorStr, denominatorStr);
						} else {
							value = Double.parseDouble("-" + absoluteValueStr);
						}
					} else {

						if (realValueStr.equals("/")) {
							String numeratorStr = tokenizer.nextToken();
							String denominatorStr = tokenizer.nextToken();

							value = parseRational(false, numeratorStr, denominatorStr);
						} else {

							value = Double.parseDouble(realValueStr);
						}
					}
					solution.put(funcName, value);
				} else if (typeName.equals("Array")) {
					tokenizer.nextToken(); // Int
					tokenizer.nextToken(); // Int
					tokenizer.nextToken(); // _
					tokenizer.nextToken(); // as_array
					String arrayFuncName = tokenizer.nextToken();
					arraysToFuncMap.put(arrayFuncName, funcName);
				} else if (typeName.equals("x!1")) {

				} else {
					// throw new IllegalArgumentException(
					// "Must implement this production");
				}
			} else {
				// throw new IllegalArgumentException(
				// "Must implement this production");
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

		if (initialValues != null) {
			logger.debug("Adding missing values to Solver solution");
			addMissingValues(initialValues, solution);
		}
		return solution;
	}


	private static void addMissingValues(Map<String, Object> initialValues, Map<String, Object> solution) {
		for (String otherVarName : initialValues.keySet()) {
			if (!solution.containsKey(otherVarName)) {
				solution.put(otherVarName, initialValues.get(otherVarName));
			}
		}
	}
}
