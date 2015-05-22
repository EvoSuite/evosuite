package org.evosuite.symbolic.z3str;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

class Z3StrModelParser {

	public Map<String, Object> parse(String z3Model,
			Map<String, Object> initialValues) {

		Map<String, Object> solution = new HashMap<String, Object>();
		if (z3Model.contains("(error"))
			return null;

		if (z3Model.contains("> Error:"))
			return null;

		if (!z3Model.contains(">> SAT"))
			return null;

		String[] lines = z3Model.split("\n");

		for (String line : lines) {
			if (line.trim().equals(""))
				continue;

			if (line.startsWith("_t_"))
				continue;

			if (line.startsWith("unique-value!"))
				continue;

			if (line.startsWith("**************") || line.startsWith(">>")
					|| line.startsWith("--------------"))
				continue;

			if (line.contains(" -> ")) {
				String[] fields = line.split(" -> ");
				String[] varSec = fields[0].split(":");
				String varName = varSec[0].trim();
				String varType = varSec[1].trim();
				String value = fields[1].trim();

				if (varType.equals("string")) {
					String noQuotationMarks = value.substring(1,
							value.length() - 1);
					String valueStr = removeSlashX(noQuotationMarks);
					solution.put(varName, valueStr);
				} else if (varType.equals("real")) {
					Double doubleVal;
					if (value.contains("/")) {
						String[] fraction = value.split("/");
						String numeratorStr = fraction[0];
						String denominatorStr = fraction[1];
						Long numerator = Long.valueOf(numeratorStr);
						Long denominator = Long.valueOf(denominatorStr);
						doubleVal = (double) numerator / (double) denominator;
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

		addMissingValues(initialValues, solution);
		return solution;
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
						int intValue = Integer.parseInt(new String(new char[]{e,f}).toUpperCase(),16);
						char charValue = (char)intValue;
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
		return charValue == '0' || charValue == '1' || charValue == '2'
				|| charValue == '3' || charValue == '4' || charValue == '5'
				|| charValue == '6' || charValue == '7' || charValue == '8'
				|| charValue == '9' || charValue == 'a' || charValue == 'b'
				|| charValue == 'c' || charValue == 'd' || charValue == 'e'
				|| charValue == 'f';
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
