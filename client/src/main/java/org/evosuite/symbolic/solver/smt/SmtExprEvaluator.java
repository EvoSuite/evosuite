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
package org.evosuite.symbolic.solver.smt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SmtExprEvaluator implements SmtExprVisitor<Object, Void> {

	private static final double DELTA = 1e-15;

	private final Map<String, Object> solution;

	public SmtExprEvaluator(Map<String, Object> solution) {
		this.solution = solution;
	}

	@Override
	public Long visit(SmtIntConstant n, Void arg) {
		Long longValue = n.getConstantValue();
		return longValue;
	}

	@Override
	public Double visit(SmtRealConstant n, Void arg) {
		Double doubleVal = n.getConstantValue();
		return doubleVal;
	}

	@Override
	public String visit(SmtStringConstant n, Void arg) {
		return n.getConstantValue();
	}

	@Override
	public Long visit(SmtIntVariable n, Void arg) {
		String varName = n.getName();
		if (!solution.containsKey(varName)) {
			throw new IllegalStateException("The variable " + varName
					+ " is not defined in the given solution");
		}

		Object value = solution.get(varName);
		if (value == null) {
			throw new NullPointerException("The value of variable " + varName
					+ " cannot be null");

		}

		if (!(value instanceof Long)) {
			throw new ClassCastException("The value of variable " + varName
					+ " should be Long but found type is "
					+ value.getClass().getName());
		}

		Long retVal = (Long) value;
		return retVal;
	}

	@Override
	public Double visit(SmtRealVariable n, Void arg) {
		String varName = n.getName();
		if (!solution.containsKey(varName)) {
			throw new IllegalStateException("The variable " + varName
					+ " is not defined in the given solution");
		}

		Object value = solution.get(varName);
		if (value == null) {
			throw new NullPointerException("The value of variable " + varName
					+ " cannot be null");

		}

		if (!(value instanceof Double)) {
			throw new ClassCastException("The value of variable " + varName
					+ " should be Double but found type is "
					+ value.getClass().getName());
		}

		Double retVal = (Double) value;
		return retVal;
	}

	@Override
	public String visit(SmtStringVariable n, Void arg) {
		String varName = n.getName();
		if (!solution.containsKey(varName)) {
			throw new IllegalStateException("The variable " + varName
					+ " is not defined in the given solution");
		}

		Object value = solution.get(varName);
		if (value == null) {
			throw new NullPointerException("The value of variable " + varName
					+ " cannot be null");

		}

		if (!(value instanceof String)) {
			throw new ClassCastException("The value of variable " + varName
					+ " should be String but found type is "
					+ value.getClass().getName());
		}

		String retVal = (String) value;
		return retVal;

	}

	@Override
	public Object visit(SmtOperation n, Void arg) {
		List<Object> retValues = new LinkedList<Object>();
		for (SmtExpr argument : n.getArguments()) {
			Object retValue = argument.accept(this, null);
			retValues.add(retValue);
		}

		switch (n.getOperator()) {
		case ABS: {
			Object unaryRetVal = retValues.get(0);
			Long integerOperand = (Long) unaryRetVal;
			long absLong = Math.abs(integerOperand.longValue());
			return (Long) absLong;
		}
		case ADD: {
			// this could be integer or real
			Object left = retValues.get(0);
			Object right = retValues.get(1);

			if (isInteger(left, right)) {
				Long leftInt = (Long) left;
				Long rightInt = (Long) right;
				Long add = (Long) (leftInt.longValue() + rightInt.longValue());
				return add;
			} else if (isReal(left, right)) {
				Double leftReal = (Double) left;
				Double rightReal = (Double) right;
				Double add = (Double) (leftReal.doubleValue() + rightReal
						.doubleValue());
				return add;
			} else {
				throw new IllegalArgumentException("ADD Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}

		case STR_CONCAT:
		case CONCAT: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return leftString + rightString;
		}

		case STR_CONTAINS:
		case CONTAINS: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return (Boolean) leftString.contains(rightString);
		}

		case DIV: {
			// this is the integer division
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) leftInteger.longValue() / rightInteger.longValue();

		}
		case STR_SUFFIXOF: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return (Boolean) rightString.endsWith(leftString);

		}
		case ENDSWITH: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return (Boolean) leftString.endsWith(rightString);
		}
		case STR_LEN:
		case LENGTH: {
			Object expr = retValues.get(0);
			String exprString = (String) expr;
			return new Long(exprString.length());
		}
		case INDEXOF: {
			// this is a string binary operation
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return new Long(leftString.indexOf(rightString));
		}
		case STR_SUBSTR: {
			Object s = retValues.get(0);
			Object startIndex = retValues.get(1);
			Object offset = retValues.get(2);
			String str = (String) s;
			Long startIndexInt = (Long) startIndex;
			Long offSetInt = (Long) offset;
			int start = startIndexInt.intValue();
			int off = offSetInt.intValue();
			return str.substring(start, start + off);
		}
		case SUBSTRING: {
			Object s = retValues.get(0);
			Object startIndex = retValues.get(1);
			Object endIndex = retValues.get(2);
			String str = (String) s;
			Long startIndexInt = (Long) startIndex;
			Long endIndexInt = (Long) endIndex;
			int start = startIndexInt.intValue();
			int end = endIndexInt.intValue();
			return str.substring(start, end);
		}
		case SLASH: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Double leftReal = (Double) left;
			Double rightReal = (Double) right;
			Double div = (Double) (leftReal.doubleValue() / rightReal
					.doubleValue());
			return div;
		}
		case MUL: {
			// this could be integer or real
			Object left = retValues.get(0);
			Object right = retValues.get(1);

			if (isInteger(left, right)) {
				Long leftInt = (Long) left;
				Long rightInt = (Long) right;
				Long mul = (Long) (leftInt.longValue() * rightInt.longValue());
				return mul;
			} else if (isReal(left, right)) {
				Double leftReal = (Double) left;
				Double rightReal = (Double) right;
				Double mul = (Double) (leftReal.doubleValue() * rightReal
						.doubleValue());
				return mul;
			} else {
				throw new IllegalArgumentException("MUL Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}
		case MINUS: {
			// this operation could be binary or unary
			if (retValues.size() == 1) {
				// unary case
				Object operand = retValues.get(0);
				if (isInteger(operand)) {
					Long intOperand = (Long) operand;
					return (Long) (-intOperand.longValue());
				} else if (isReal(operand)) {
					Double realOperand = (Double) operand;
					return (Double) (-realOperand.doubleValue());
				} else {
					throw new IllegalArgumentException(
							"MINUS Type mismatch operand="
									+ operand.getClass().getName());
				}

			} else if (retValues.size() == 2) {
				// binary case
				Object left = retValues.get(0);
				Object right = retValues.get(1);
				if (isInteger(left, right)) {
					Long leftInt = (Long) left;
					Long rightInt = (Long) right;
					Long minus = (Long) (leftInt.longValue() - rightInt
							.longValue());
					return minus;

				} else if (isReal(left, right)) {

					Double leftReal = (Double) left;
					Double rightReal = (Double) right;
					Double minus = (Double) (leftReal.doubleValue() - rightReal
							.doubleValue());

					return minus;
				} else {
					throw new IllegalArgumentException(
							"MINUS Type mismatch left="
									+ left.getClass().getName() + " and right="
									+ right.getClass().getName());
				}
			} else {
				throw new IllegalArgumentException(
						"Invalid number of arguments for MINUS: "
								+ retValues.size());
			}
		}
		case MOD:
		case REM: {
			// this is the integer binary operation
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) leftInteger.longValue() % rightInteger.longValue();

		}
		case GE: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			if (isInteger(left, right)) {
				Long leftInt = (Long) left;
				Long rightInt = (Long) right;
				Boolean ge = (Boolean) (leftInt.longValue() >= rightInt
						.longValue());
				return ge;

			} else if (isReal(left, right)) {

				Double leftReal = (Double) left;
				Double rightReal = (Double) right;
				Boolean ge = (Boolean) (leftReal.doubleValue() >= rightReal
						.doubleValue());

				return ge;
			} else {
				throw new IllegalArgumentException("GE Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}
		case GT: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			if (isInteger(left, right)) {
				Long leftInt = (Long) left;
				Long rightInt = (Long) right;
				Boolean gt = (Boolean) (leftInt.longValue() > rightInt
						.longValue());
				return gt;

			} else if (isReal(left, right)) {

				Double leftReal = (Double) left;
				Double rightReal = (Double) right;
				Boolean gt = (Boolean) (leftReal.doubleValue() > rightReal
						.doubleValue());

				return gt;
			} else {
				throw new IllegalArgumentException("GT Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}
		case LE: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			if (isInteger(left, right)) {
				Long leftInt = (Long) left;
				Long rightInt = (Long) right;
				Boolean le = (Boolean) (leftInt.longValue() <= rightInt
						.longValue());
				return le;

			} else if (isReal(left, right)) {

				Double leftReal = (Double) left;
				Double rightReal = (Double) right;
				Boolean le = (Boolean) (leftReal.doubleValue() <= rightReal
						.doubleValue());

				return le;
			} else {
				throw new IllegalArgumentException("LE Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}

		case LT: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			if (isInteger(left, right)) {
				Long leftInt = (Long) left;
				Long rightInt = (Long) right;
				Boolean lt = (Boolean) (leftInt.longValue() < rightInt
						.longValue());
				return lt;

			} else if (isReal(left, right)) {

				Double leftReal = (Double) left;
				Double rightReal = (Double) right;
				Boolean lt = (Boolean) (leftReal.doubleValue() < rightReal
						.doubleValue());

				return lt;
			} else {
				throw new IllegalArgumentException("LT Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}
		case EQ: {
			// this could be an integer, real or string operation
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			if (isInteger(left, right)) {
				Long leftInt = (Long) left;
				Long rightInt = (Long) right;
				Boolean eq = (Boolean) (leftInt.longValue() == rightInt
						.longValue());
				return eq;

			} else if (isReal(left, right)) {

				Double leftReal = (Double) left;
				Double rightReal = (Double) right;
				Boolean eq = (Boolean) (Math.abs(leftReal.doubleValue()
						- rightReal.doubleValue()) < DELTA);

				return eq;
			} else if (isString(left, right)) {

				String leftString = (String) left;
				String rightString = (String) right;
				Boolean equals = (Boolean) (leftString.equals(rightString));

				return equals;
			} else {
				throw new IllegalArgumentException("EQ Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}
		case NOT: {
			// this is a boolean unary
			Object operand = retValues.get(0);
			Boolean operandBoolean = (Boolean) operand;
			Boolean not = !operandBoolean.booleanValue();
			return not;
		}
		case STR_REPLACE:
		case REPLACE: {
			// this is a string ternary operation
			Object s = retValues.get(0);
			Object target = retValues.get(1);
			Object replacement = retValues.get(2);
			String str = (String) s;
			String targetStr = (String) target;
			String replacementStr = (String) replacement;
			String ret_val = str.replace(targetStr, replacementStr);
			return ret_val;
		}
		case STR_PREFIXOF: {
			// this is a string binary operation
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			Boolean prefixOf = (Boolean) (rightString.startsWith(leftString));
			return prefixOf;

		}
		case STARTSWITH: {
			// this is a string binary operation
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			Boolean startsWith = (Boolean) (leftString.startsWith(rightString));
			return startsWith;
		}
		case ITE: {
			// this is a ternary operation. First argument is boolean
			Object cond = retValues.get(0);
			Object thenObj = retValues.get(1);
			Object elseObj = retValues.get(2);
			Boolean condBoolean = (Boolean) cond;
			if (condBoolean)
				return thenObj;
			else
				return elseObj;
		}

		case BV2INT: {
			// bit vectors are integers
			Object operand = retValues.get(0);
			return operand;
		}
		case BVADD: {
			// bit vectors are integers
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) (leftInteger.longValue() + rightInteger.longValue());
		}
		case BV2Nat: {
			// bit vectors are integers
			Object operand = retValues.get(0);
			return operand;
		}
		case BVAND: {
			// bit vectors are integers
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) (leftInteger.longValue() & rightInteger.longValue());
		}
		case BVASHR: {
			// bit vectors are integers
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) (leftInteger.longValue() >> rightInteger.longValue());
		}
		case BVLSHR: {
			// bit vectors are integers
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) (leftInteger.longValue() >>> rightInteger.longValue());
		}
		case BVOR: {
			// bit vectors are integers
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) (leftInteger.longValue() | rightInteger.longValue());
		}
		case BVSHL: {
			// bit vectors are integers
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) (leftInteger.longValue() << rightInteger.longValue());
		}
		case BVXOR: {
			// bit vectors are integers
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) (leftInteger.longValue() ^ rightInteger.longValue());
		}

		case INT2BV32: {
			// bit vectors are integers
			Object operand = retValues.get(0);
			return operand;
		}
		case INT2REAL: {
			Object operand = retValues.get(0);
			Long operandInt = (Long) operand;
			return new Double(operandInt.longValue());
		}

		case INT_TO_CHAR:
		case INT_TO_STR: {
			Object operand = retValues.get(0);
			Long operandInt = (Long) operand;
			return Long.toString(operandInt);
		}
		case REAL2INT: {
			Object operand = retValues.get(0);
			Double operandReal = (Double) operand;
			return (Long) operandReal.longValue();
		}

		case CHAR_TO_INT: {
			Object operand = retValues.get(0);
			String operandStr = (String) operand;
			if (operandStr.length() != 1) {
				throw new IllegalArgumentException(
						"The following string cannot be transformed into a char "
								+ operandStr);
			}
			char charValue = operandStr.charAt(0);
			return (Long) Long.valueOf(charValue);

		}
		case STR_TO_INT: {
			Object operand = retValues.get(0);
			String operandStr = (String) operand;
			return (Long) Long.parseLong(operandStr);
		}

		case STR_INDEXOF: {
			// this is a string binary operation
			Object s = retValues.get(0);
			Object ch = retValues.get(1);
			Object index = retValues.get(2);

			String str = (String) s;
			String chString = (String) ch;
			Long indexInt = (Long) index;
			int indexOf = str.indexOf(chString, indexInt.intValue());
			return new Long(indexOf);
		}

		case STR_AT: {
			// this is a <string,integer> operation
			Object s = retValues.get(0);
			Object index = retValues.get(1);
			String str = (String) s;
			Long indexInt = (Long) index;
			char charAt = str.charAt(indexInt.intValue());
			return String.valueOf(charAt);
		}

		case REG_EXP_ALL_CHAR:
		case REG_EXP_CONCAT:
		case REG_EXP_KLEENE_CROSS:
		case REG_EXP_KLEENE_STAR:
		case REG_EXP_LOOP:
		case REG_EXP_OPTIONAL:
		case REG_EXP_RANGE:
		case REG_EXP_UNION:
		case STR_IN_REG_EXP:
		case STR_TO_REG_EXP: {
			throw new UnsupportedOperationException("The operation "
					+ n.getOperator() + " should be implemented!");
		}

		default:
			throw new IllegalStateException(
					"The following operator must be implemented "
							+ n.getOperator());

		}
	}

	private static boolean isReal(Object operand) {
		return (operand instanceof Double);
	}

	private static boolean isInteger(Object operand) {
		return (operand instanceof Long);
	}

	private static boolean isReal(Object left, Object right) {
		return (left instanceof Double) && (right instanceof Double);
	}

	private static boolean isInteger(Object left, Object right) {
		return (left instanceof Long) && (right instanceof Long);
	}

	private static boolean isString(Object left, Object right) {
		return (left instanceof String) && (right instanceof String);
	}

	@Override
	public Boolean visit(SmtBooleanConstant n, Void arg) {
		return n.booleanValue();
	}

}
