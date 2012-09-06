package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class Character_getNumericValue extends Function {

	private final static String GET_NUMERIC_VALUE = "getNumericValue";
	private IntegerValue charValueExpr;

	public Character_getNumericValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, GET_NUMERIC_VALUE, Types.C_TO_I);
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value) {
		charValueExpr = this.env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT(int res) {

		if (charValueExpr.containsSymbolicVariable()) {

			IntegerUnaryExpression getNumericValueExpr = new IntegerUnaryExpression(
					charValueExpr, Operator.GETNUMERICVALUE, (long) res);
			this.replaceTopBv32(getNumericValueExpr);

		}

	}

}
