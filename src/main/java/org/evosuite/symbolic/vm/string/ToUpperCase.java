package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class ToUpperCase extends StringFunction {

	private static final String TO_UPPER_CASE = "toUpperCase";

	public ToUpperCase(SymbolicEnvironment env) {
		super(env, TO_UPPER_CASE, Types.TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		this.stringReceiverExpr = operandToStringExpression(env.topFrame().operandStack
				.peekOperand());
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (stringReceiverExpr.containsSymbolicVariable()) {
			StringUnaryExpression strUnExpr = new StringUnaryExpression(
					stringReceiverExpr, Operator.TOUPPERCASE, (String) res);
			replaceStrRefTop(strUnExpr);
		} else {
			// do nothing
		}
	}
}
