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
package org.evosuite.symbolic.solver.z3str2;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.symbolic.solver.ResultParser;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Z3Str2ResultParser extends ResultParser {

	static Logger logger = LoggerFactory.getLogger(Z3Str2ResultParser.class);

	public SolverResult parse(String z3Str2Result) throws SolverErrorException {
		return parse(z3Str2Result, null);
	}

	public SolverResult parse(String z3Str2Result, Map<String, Object> initialValues) throws SolverErrorException {

		if (z3Str2Result.contains("unknown sort")) {
			logger.debug("Z3_str2 output was " + z3Str2Result);
			String errMsg = "Z3_str2 found an unknown";
			throw new SolverErrorException(errMsg);
		}

		if (z3Str2Result.contains("unknown constant")) {
			logger.debug("Z3_str2 output was " + z3Str2Result);
			String errMsg = "Z3_str2 found an unknown constant";
			throw new SolverErrorException(errMsg);
		}

		if (z3Str2Result.contains("invalid expression")) {
			logger.debug("Z3_str2 output was " + z3Str2Result);
			String errMsg = "Z3_str2 found an invalid expression";
			throw new SolverErrorException(errMsg);
		}

		if (z3Str2Result.contains("unexpected input")) {
			logger.debug("Z3_str2 output was " + z3Str2Result);
			String errMsg = "Z3_str2 found an unexpected input";
			throw new SolverErrorException(errMsg);
		}

		if (z3Str2Result.contains("(error")) {
			throw new SolverErrorException("An error occurred in z3str2: " + z3Str2Result);
		}

		if (z3Str2Result.contains("> Error:")) {
			throw new SolverErrorException("An error occurred in z3str2: " + z3Str2Result);
		}

		if (!z3Str2Result.contains(">> SAT")) {
			SolverResult unsatResult = SolverResult.newUNSAT();
			return unsatResult;
		}

		SolverResult solverResult = parseSAT(z3Str2Result, initialValues);

		return solverResult;

	}

	private static SolverResult parseSAT(String z3str2Result, Map<String, Object> initialValues) {

		Map<String, Object> solution = new HashMap<String, Object>();
		String[] lines = z3str2Result.split("\n");

		for (String line : lines) {
			if (line.trim().equals(""))
				continue;

			if (line.startsWith("_t_"))
				continue;

			if (line.startsWith("unique-value!"))
				continue;

			if (line.startsWith("**************") || line.startsWith(">>") || line.startsWith("--------------"))
				continue;

			if (line.contains(" -> ")) {
				String[] fields = line.split(" -> ");
				String[] varSec = fields[0].split(":");
				String varName = varSec[0].trim();
				String varType = varSec[1].trim();
				String value = fields[1].trim();

				if (varName.startsWith("$$_len_")) {
					// ignore $$_len_... string length variables
					continue;
				}
				
				if (varName.startsWith("$$_val_")) {
					// ignore $$_val_... string val variables
					continue;
				}

				if (varName.startsWith("$$_str")) {
					// ignore $$_str... string str variables
					continue;
				}
				
				if (varName.startsWith("$$_bol")) {
					// ignore $$_bol... bool variables
					continue;
				}

				if (varName.startsWith("$$_int_")) {
					// ignore $$_int_... int variables
					continue;
				}

				if (varName.startsWith("$$_xor_")) {
					// ignore $$_xor_... int variables
					continue;
				}
				
				if (varType.equals("string")) {
					String noQuotationMarks = value.substring(1, value.length() - 1);
					String valueStr = removeSlashX(noQuotationMarks);
					solution.put(varName, valueStr);
				} else if (varType.equals("real")) {
					Double doubleVal;
					if (value.contains("/")) {
						String[] fraction = value.split("/");
						String numeratorStr = fraction[0];
						String denominatorStr = fraction[1];
						
						doubleVal = parseRational(false, numeratorStr, denominatorStr);
					} else {
						doubleVal = Double.valueOf(value);
					}
					solution.put(varName, doubleVal);
				} else if (varType.equals("int")) {
					Long longVal = Long.valueOf(value);
					solution.put(varName, longVal);

				}
			}

		}

		if (initialValues != null) {
			addMissingValues(initialValues, solution);
		}

		SolverResult satResult = SolverResult.newSAT(solution);

		return satResult;
	}

	private static String removeSlashX(String str) {
		StringBuffer buff = new StringBuffer();
		char[] charArray = str.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (c == '\\') {
				if (i + 3 < charArray.length) {
					char d = charArray[i + 1];
					char e = charArray[i + 2];
					char f = charArray[i + 3];
					if (d == 'x' && isHexDigit(e) && isHexDigit(f)) {
						int intValue = Integer.parseInt(new String(new char[] { e, f }).toUpperCase(), 16);
						char charValue = (char) intValue;
						buff.append(charValue);
						i += 3;
						continue;
					}
				}
			}
			buff.append(c);
		}

		return buff.toString();
	}

	private static boolean isHexDigit(char charValue) {
		return charValue == '0' || charValue == '1' || charValue == '2' || charValue == '3' || charValue == '4'
				|| charValue == '5' || charValue == '6' || charValue == '7' || charValue == '8' || charValue == '9'
				|| charValue == 'a' || charValue == 'b' || charValue == 'c' || charValue == 'd' || charValue == 'e'
				|| charValue == 'f';
	}

	private static void addMissingValues(Map<String, Object> initialValues, Map<String, Object> solution) {
		for (String otherVarName : initialValues.keySet()) {
			if (!solution.containsKey(otherVarName)) {
				solution.put(otherVarName, initialValues.get(otherVarName));
			}
		}
	}

}
