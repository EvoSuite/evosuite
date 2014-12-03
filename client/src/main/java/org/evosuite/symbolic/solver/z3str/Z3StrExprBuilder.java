package org.evosuite.symbolic.solver.z3str;

import java.text.DecimalFormat;

import org.evosuite.symbolic.solver.SmtLibExprBuilder;

public abstract class Z3StrExprBuilder extends SmtLibExprBuilder {

	public static String mkStringLiteral(String str) {
		return "\"" + str + "\"";
	}

	public static String mkStringVariable(String varName) {
		return "(declare-const " + varName + " String)";
	}

	public static String mkRealVariable(String varName) {
		return "(declare-const " + varName + " Real)";
	}

	public static String mkIntVariable(String varName) {
		return "(declare-const " + varName + " Int)";
	}

	private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"################.################");

	public static String mkRealConstant(double doubleVal) {
		if (doubleVal < 0) {
			String magnitudeStr = DECIMAL_FORMAT.format(Math.abs(doubleVal));
			return "(- " + magnitudeStr + ")";
		} else {
			String doubleStr = DECIMAL_FORMAT.format(doubleVal);
			return doubleStr;
		}
	}

	public static String mkStringEndsWith(String left, String right) {
		return "(EndsWith " + left + " " + right + ")";
	}

	public static String mkStringContains(String left, String right) {
		return "(Contains " + left + " " + right + ")";
	}

	public static String mkStringStartsWith(String left, String right) {
		return "(StartsWith " + left + " " + right + ")";
	}

	public static String mkStringConstant(String str) {
		return " " + encodeString(str) + " ";
	}

	public static String mkStringIndexOf(String left, String right) {
		return "(Indexof " + left + " " + right + ")";
	}

	public static String mkStringSubstring(String str, String from, String to) {
		return "(Substring " + str + " " + from + " " + to + ")";
	}

	public static String mkStringReplace(String str, String oldValue,
			String newValue) {
		return "(Substring " + str + " " + oldValue + " " + newValue + ")";
	}

	public static String mkStringLength(String str) {
		return "(Length " + str + ")";
	}

	public static String mkStringConcat(String left, String right) {
		return "(Concat " + left + " " + right + ")";
	}

	public static String encodeString(String str) {
		char[] charArray = str.toCharArray();
		String ret_val = "__cOnStStR_";
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (c >= 0 && c <= 255) {
				ret_val += "_x" + Integer.toHexString(c);
			}
		}
		return ret_val;
	}

}
