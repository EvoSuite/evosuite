/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.solver.smt;

import org.evosuite.testcase.DefaultValueChecker;
import org.evosuite.utils.TypeUtil;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public final class SmtExprPrinter implements SmtExprVisitor<String, Void> {

	@Override
	public String visit(SmtIntConstant n, Void arg) {
		long longValue = n.getConstantValue();
		return buildIntegerString(longValue);
	}

	private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("################0.0################");

	@Override
	public String visit(SmtRealConstant n, Void arg) {
		double doubleVal = n.getConstantValue();
		return buildRealValueString(doubleVal);
	}

	@Override
	public String visit(SmtStringConstant n, Void arg) {
		String str = encodeString(n.getConstantValue());
		return "\"" + str + "\"";
	}

	@Override
	public String visit(SmtIntVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtRealVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtStringVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtOperation n, Void arg) {
		List<String> retValues = new LinkedList<String>();
		for (SmtExpr argument : n.getArguments()) {
			String retValue = argument.accept(this, null);
			retValues.add(retValue);
		}

		StringBuffer result = new StringBuffer();
		if (!retValues.isEmpty()) {
			result.append("(");
		}
		result.append(n.getOperator().toString());
		for (String retValue : retValues) {
			result.append(" ");
			result.append(retValue);
		}
		if (!retValues.isEmpty()) {
			result.append(")");
		}
		return result.toString();
	}

	@Override
	public String visit(SmtArrayVariable.SmtIntegerArrayVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtArrayVariable.SmtRealArrayVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtArrayVariable.SmtStringArrayVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtArrayVariable.SmtReferenceArrayVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtArrayConstant.SmtIntegerArrayConstant n, Void arg) {
		StringBuilder back  = new StringBuilder();
		StringBuilder front = new StringBuilder();

		back.append("((as const (Array Int Int)) 0)");

		Object arr = n.getConstantValue();
		int length = Array.getLength(arr);

		for (int index=0; index < length; index++) {
			Object element = Array.get(arr, index);

			if (!DefaultValueChecker.isDefaultValue(element)) {
				front.append("(store");
				back
					.append(" ")
					.append(index)
					.append(" ")
					.append(buildIntegerArrayValue(element))
					.append(")");
			}
		}

		return front.toString() + back.toString();
	}

	@Override
	public String visit(SmtArrayConstant.SmtRealArrayConstant n, Void arg) {
		StringBuilder back  = new StringBuilder();
		StringBuilder front = new StringBuilder();

		back.append("((as const (Array Int Real)) 0.0)");

		Object arr = n.getConstantValue();
		int length = Array.getLength(arr);

		for (int index=0; index < length; index++) {
			Object element = Array.get(arr, index);

			if (!DefaultValueChecker.isDefaultValue(element)) {
				front.append("(store");
				back
					.append(" ")
					.append(index)
					.append(" ")
					.append(buildRealArrayValue(element))
					.append(")");
			}
		}

		back.append(")");
		return front.toString() + back.toString();
	}

	@Override
	public String visit(SmtArrayConstant.SmtStringArrayConstant n, Void arg) {
		StringBuilder back  = new StringBuilder();
		StringBuilder front = new StringBuilder();

		// TODO: Ver que esta parte este bien
		back.append("((as const (Array Int String)) \"\")");

		Object arr = n.getConstantValue();
		int length = Array.getLength(arr);

		for (int index=0; index < length; index++) {
			Object element = Array.get(arr, index);

			if (!DefaultValueChecker.isDefaultValue(element)) {
				front.append("(store");
				back
					.append(" ")
					.append(index)
					.append(" ")
					.append(buildRealArrayValue(element))
					.append(")");
			}
		}

		back.append(")");
		return front.toString() + back.toString();
	}

	@Override
	public String visit(SmtArrayConstant.SmtReferenceArrayConstant n, Void arg) {
		throw new UnsupportedOperationException(
			"We shouldn't be using constant arrays as this point, they must be reduced to concrete values during execution.");
	}

	@Override
	public String visit(SmtBooleanConstant n, Void arg) {
		if (n.booleanValue() == true) {
			return "true";
		} else {
			return "false";
		}
	}

	public static String encodeString(String str) {
		char[] charArray = str.toCharArray();
		String ret_val = "";
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (Character.isISOControl(c)) {
				if (Integer.toHexString(c).length() == 1) {
					// padding
					ret_val += "_x0" + Integer.toHexString(c);
				} else {
					ret_val += "_x" + Integer.toHexString(c);
				}
			} else {
				ret_val += c;
			}
		}
		return ret_val;
	}

	private String buildRealValueString(double doubleVal) {
		if (doubleVal < 0) {
			String magnitudeStr = DECIMAL_FORMAT.format(Math.abs(doubleVal));
			return "(- " + magnitudeStr + ")";
		} else {
			String doubleStr = DECIMAL_FORMAT.format(doubleVal);
			return doubleStr;
		}
	}

	private String buildIntegerString(long longValue) {
		if (longValue == Long.MIN_VALUE) {
			return "(- " + String.valueOf(Long.MIN_VALUE).replace("-", "") + ")";
		} else if (longValue < 0) {
			long absoluteValue = Math.abs(longValue);
			return "(- " + String.valueOf(absoluteValue) + ")";
		} else {
			return String.valueOf(longValue);
		}
	}

	private String buildIntegerArrayValue(Object element) {
		long value = (long) TypeUtil.unboxIntegerPrimitiveValue(element);
		return buildIntegerString(value);
	}

	private String buildRealArrayValue(Object element) {
		double value = (double) TypeUtil.unboxRealPrimitiveValue(element);
		return buildRealValueString(value);
	}
}
