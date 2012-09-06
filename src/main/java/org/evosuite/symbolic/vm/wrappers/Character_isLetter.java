package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class Character_isLetter extends Function {

	private final static String IS_LETTER = "isLetter";
	private IntegerValue charValueExpr;

	public Character_isLetter(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, IS_LETTER, Types.C_TO_Z);
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value) {
		charValueExpr = this.env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT(boolean res) {

		if (charValueExpr.containsSymbolicVariable()) {

			long conV = res ? 1 : 0;

			IntegerUnaryExpression getNumericValueExpr = new IntegerUnaryExpression(
					charValueExpr, Operator.ISLETTER, conV);
			this.replaceTopBv32(getNumericValueExpr);

		}

	}

}
