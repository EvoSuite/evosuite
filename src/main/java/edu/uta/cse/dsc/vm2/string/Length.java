package edu.uta.cse.dsc.vm2.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.evosuite.symbolic.expr.StringUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class Length extends StringFunction {

	private static final String FUNCTION_NAME = "length";
	public Length(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.TO_INT_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		this.stringReceiverExpr = env.topFrame().operandStack.peekStringRef();
	}

	@Override
	public void CALL_RESULT(int res) {
		if (stringReceiverExpr.containsSymbolicVariable()) {
			StringUnaryExpression strUnExpr = new StringUnaryExpression(
					stringReceiverExpr, Operator.LENGTH, Integer.toString(res));
			StringToIntCast castExpr = new StringToIntCast(strUnExpr,
					(long) res);
			replaceBv32Top(castExpr);
		} else {
			// do nothing
		}
	}
}
