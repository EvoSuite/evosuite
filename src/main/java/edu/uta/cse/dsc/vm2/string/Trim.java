package edu.uta.cse.dsc.vm2.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class Trim extends StringVirtualFunction {

	private static final String FUNCTION_NAME = "trim";

	public Trim(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		this.stringReceiverExpr = operandToStringRef(env.topFrame().operandStack
				.peekOperand());
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (stringReceiverExpr.containsSymbolicVariable()) {
			StringUnaryExpression strUnExpr = new StringUnaryExpression(
					stringReceiverExpr, Operator.TRIM, (String) res);
			replaceStrRefTop(strUnExpr);
		} else {
			// do nothing
		}
	}
}
