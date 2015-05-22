package org.evosuite.symbolic.z3str;

abstract class Z3ExprBuilder {

	static String mkNot(String formula) {
		return "(not " + formula + ")";
	}

	static String mkEq(String left, String right) {
		return "(= " + left + " " + right + ")";
	}

	static String mkGe(String left, String right) {
		return "(>= " + left + " " + right + ")";
	}

	static String mkGt(String left, String right) {
		return "(> " + left + " " + right + ")";
	}

	static String mkLe(String left, String right) {
		return "(<= " + left + " " + right + ")";
	}

	static String mkLt(String left, String right) {
		return "(< " + left + " " + right + ")";
	}

	static String mkBVASHR(String bv_left, String bv_right) {
		return "(bvashr " + bv_left + " " + bv_right + ")";
	}

	static String mkBVLSHR(String bv_left, String bv_right) {
		return "(bvlshr " + bv_left + " " + bv_right + ")";
	}

	static String mkBVSHL(String bv_left, String bv_right) {
		return "(bvshl " + bv_left + " " + bv_right + ")";
	}

	static String mkBVXOR(String bv_left, String bv_right) {
		return "(bvxor " + bv_left + " " + bv_right + ")";
	}

	static String mkBVAND(String bv_left, String bv_right) {
		return "(bvand " + bv_left + " " + bv_right + ")";
	}

	static String mkBV2Int(String bv_expr, boolean b) {
		return "(bv2int " + bv_expr + ")";
	}

	static String mkBVOR(String bv_left, String bv_right) {
		return "(bvor " + bv_left + " " + bv_right + ")";
	}

	static String mkInt2BV(int bitwidth, String intExpr) {
		return "((_ int2bv " + bitwidth + ")" + intExpr + ")";
	}

	static String mkRealDiv(String left, String right) {
		return "(/ " + left + " " + right + ")";
	}

	static String mkDiv(String left, String right) {
		return "(div " + left + " " + right + ")";
	}

	static String mkRem(String left, String right) {
		return "(rem " + left + " " + right + ")";
	}

	static String mkAdd(String left, String right) {
		return "(+ " + left + " " + right + ")";
	}

	static String mkSub(String left, String right) {
		return "(- " + left + " " + right + ")";
	}

	static String mkMul(String left, String right) {
		return "(* " + left + " " + right + ")";
	}

	static String mkITE(String condition, String thenExpr, String elseExpr) {
		return "(ite " + condition + " " + thenExpr + " " + elseExpr + ")";
	}

	static String mkToReal(String operand) {
		return "(to_real " + operand + ")";
	}

	static String mkStringLiteral(String str) {
		return "\"" + str + "\"";
	}

	static String mkNeg(String intExpr) {
		return "(- " + intExpr + ")";
	}

	static String mkAssert(String constraintStr) {
		return "(assert " + constraintStr + ")";
	}

	static String mkStringVariable(String varName) {
		return "(declare-const " + varName + " String)";
	}

	static String mkRealVariable(String varName) {
		return "(declare-const " + varName + " Real)";
	}

	static String mkIntVariable(String varName) {
		return "(declare-const " + varName + " Int)";
	}

	static String mkRealConstant(double doubleVal) {
		if (doubleVal < 0) {
			return "(- " + Double.toString(Math.abs(doubleVal)) + ")";
		} else {
			String doubleStr = Double.toString(doubleVal);
			return doubleStr;
		}
	}

	static String mkIntegerConstant(long longVal) {
		if (longVal < 0) {
			return "(- " + Long.toString(Math.abs(longVal)) + ")";
		} else {
			String longStr = Long.toString(longVal);
			return longStr;
		}
	}

	static String mkReal2Int(String operandStr) {
		return "(to_int " + operandStr + ")";
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

	static String encodeString(String str) {
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
