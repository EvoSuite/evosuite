package edu.uta.cse.dsc.vm2.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class ToLowerCase extends StringFunction {

	private static final String TO_LOWER_CASE = "toLowerCase";

	public ToLowerCase(SymbolicEnvironment env) {
		super(env, TO_LOWER_CASE, Types.TO_STR_DESCRIPTOR);
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
					stringReceiverExpr, Operator.TOLOWERCASE, (String) res);
			replaceStrRefTop(strUnExpr);
		} else {
			// do nothing
		}
	}
}
