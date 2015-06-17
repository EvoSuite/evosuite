package org.evosuite.symbolic.solver.smt;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SmtExprEvaluator implements SmtExprVisitor<Object, Void> {

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
				throw new IllegalArgumentException("Type mismatch left="
						+ left.getClass().getName() + " and right="
						+ right.getClass().getName());
			}
		}
		case CONCAT: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return leftString + rightString;
		}
		case CONTAINS: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return leftString.contains(rightString) ? 1L : 0L;
		}
		case DIV: {
			// this is the integer division
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			Long leftInteger = (Long) left;
			Long rightInteger = (Long) right;
			return (Long) leftInteger.longValue() / rightInteger.longValue();

		}
		case ENDSWITH: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return leftString.endsWith(rightString) ? 1L : 0L;
		}
		case LENGTH: {
			Object expr = retValues.get(0);
			String exprString = (String) expr;
			return new Long(exprString.length());
		}
		case INDEXOF: {
			Object left = retValues.get(0);
			Object right = retValues.get(1);
			String leftString = (String) left;
			String rightString = (String) right;
			return new Long(leftString.indexOf(rightString));
		}
		case SUBSTRING: {
			Object arg0 = retValues.get(0);
			Object arg1 = retValues.get(1);
			Object arg2 = retValues.get(2);
			String str = (String) arg0;
			Long from = (Long) arg1;
			Long to = (Long) arg2;
			return str.substring(from.intValue(), to.intValue());
		}

		case BV2INT:
		case BV2Nat:
		case BVADD:
		case BVAND:
		case BVASHR:
		case BVLSHR:
		case BVOR:
		case BVSHL:
		case BVXOR:
		case CHAR_TO_INT:

		case EQ:
		case GE:
		case GT:
		case INT2BV32:
		case INT2REAL:
		case INT_TO_CHAR:
		case INT_TO_STR:
		case ITE:
		case LE:
		case LT:
		case MINUS:
		case MOD:
		case MUL:
		case NOT:
		case REAL2INT:
		case REG_EXP_ALL_CHAR:
		case REG_EXP_CONCAT:
		case REG_EXP_KLEENE_CROSS:
		case REG_EXP_KLEENE_STAR:
		case REG_EXP_LOOP:
		case REG_EXP_OPTIONAL:
		case REG_EXP_RANGE:
		case REG_EXP_UNION:
		case REM:
		case REPLACE:
		case SLASH:
		case STARTSWITH:
		case STR_AT:
		case STR_CONCAT:
		case STR_CONTAINS:
		case STR_INDEXOF:
		case STR_IN_REG_EXP:
		case STR_LEN:
		case STR_PREFIXOF:
		case STR_REPLACE:
		case STR_SUBSTR:
		case STR_SUFFIXOF:
		case STR_TO_INT:
		case STR_TO_REG_EXP:
		default:
			throw new IllegalStateException(
					"The following operator must be implemented "
							+ n.getOperator());

		}
	}

	private static boolean isReal(Object left, Object right) {
		return (left instanceof Double) && (right instanceof Double);
	}

	private static boolean isInteger(Object left, Object right) {
		return (left instanceof Long) && (right instanceof Long);
	}

	@Override
	public Boolean visit(SmtBooleanConstant n, Void arg) {
		return n.booleanValue();
	}

}
