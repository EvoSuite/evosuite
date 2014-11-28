package org.evosuite.symbolic.solver.z3;

import java.text.DecimalFormat;

import org.evosuite.symbolic.solver.SmtLibExprBuilder;

public abstract class Z3ExprBuilder extends SmtLibExprBuilder {

	public static String mkStringVariable(String varName) {
		return "(declare-const " + varName + " (Array (Int) (Int)))";
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

	public static String mkIntegerConstant(long longVal) {
		if (longVal < 0) {
			return "(- " + Long.toString(Math.abs(longVal)) + ")";
		} else {
			String longStr = Long.toString(longVal);
			return longStr;
		}
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

	public static String mkIntSort() {
		return "Int";
	}


	public static String mkImplies(String left, String right) {
		return "(implies " + left + " " + right + ")";
	}

	public static String mkForall(String[] variables, String[] sorts,
			String formula) {
		String quantifier = "forall";
		return mkQuantifier(variables, sorts, formula, quantifier);
	}

	private static String mkQuantifier(String[] variables, String[] sorts,
			String formula, String quantifier) {
		StringBuffer buff = new StringBuffer();
		buff.append("(" + quantifier + " ");

		buff.append("(");
		for (int i = 0; i < variables.length; i++) {
			buff.append("(");
			buff.append(variables[i]);
			buff.append(" ");
			buff.append(sorts[i]);
			buff.append(")");
		}
		buff.append(")");

		buff.append(" ");
		buff.append(formula);
		buff.append(")");
		return buff.toString();
	}

	public static String mkExists(String[] variables, String[] sorts,
			String formula) {
		String quantifier = "exists";
		return mkQuantifier(variables, sorts, formula, quantifier);
	}

	public static String mkFuncDecl(String name, String domainSort,
			String rangeSort) {
		return "(declare-fun " + name + " (" + domainSort + ") (" + rangeSort
				+ "))";
	}
}
