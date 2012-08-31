package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class Length extends StringFunction {

	private static final String LENGTH = "length";

	public Length(SymbolicEnvironment env) {
		super(env, LENGTH, Types.TO_INT_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Operand operand = env.topFrame().operandStack.peekOperand();
		this.stringReceiverExpr = getStringExpression(operand);
	}

	@Override
	public void CALL_RESULT(int res) {
		if (stringReceiverExpr.containsSymbolicVariable()) {
			StringUnaryToIntegerExpression strUnExpr = new StringUnaryToIntegerExpression(
					stringReceiverExpr, Operator.LENGTH, (long) res);
			replaceTopBv32(strUnExpr);
		} else {
			// do nothing
		}
	}
}
