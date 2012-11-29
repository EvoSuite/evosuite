package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;

public final class Character_getNumericValue extends SymbolicFunction {

	private final static String GET_NUMERIC_VALUE = "getNumericValue";

	public Character_getNumericValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, GET_NUMERIC_VALUE, Types.C_TO_I);
	}

	@Override
	public Object executeFunction() {

		IntegerValue charValueExpr = this.getSymbIntegerArgument(0);
		int res = this.getConcIntRetVal();

		if (charValueExpr.containsSymbolicVariable()) {

			IntegerUnaryExpression getNumericValueExpr = new IntegerUnaryExpression(
					charValueExpr, Operator.GETNUMERICVALUE, (long) res);
			return getNumericValueExpr;

		} else {
			return this.getSymbIntegerRetVal();
		}
	}

}
