package edu.uta.cse.dsc.vm2.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class ToUpperCase extends StringFunction {

	private static final String FUNCTION_NAME = "toUpperCase";
	public ToUpperCase(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		this.stringReceiverExpr = env.topFrame().operandStack.peekStringRef();
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
