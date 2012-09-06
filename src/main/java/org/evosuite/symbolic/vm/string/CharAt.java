package org.evosuite.symbolic.vm.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class CharAt extends StringFunction {

	private static final String CHAR_AT = "charAt";

	private IntegerValue indexExpr;

	public CharAt(SymbolicEnvironment env) {
		super(env, CHAR_AT, Types.INT_TO_CHAR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.indexExpr = bv32(it.next());
		this.stringReceiverExpr = getStringExpression(it.next(), receiver);
	}

	@Override
	public void CALL_RESULT(int res) {

		if (stringReceiverExpr.containsSymbolicVariable()
				|| indexExpr.containsSymbolicVariable()) {

			StringBinaryToIntegerExpression strBExpr = new StringBinaryToIntegerExpression(
					stringReceiverExpr, Operator.CHARAT, indexExpr, (long) res);

			replaceTopBv32(strBExpr);

		} else {
			// do nothing
		}
	}
}
