package org.evosuite.symbolic.vm.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class CompareTo extends StringFunction {

	private static final String COMPARE_TO = "compareTo";
	private StringValue strExpr;

	public CompareTo(SymbolicEnvironment env) {
		super(env, COMPARE_TO, Types.STR_TO_INT_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = getStringExpression(it.next());
		this.stringReceiverExpr = getStringExpression(it.next());

	}

	@Override
	public void CALL_RESULT(int res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| strExpr.containsSymbolicVariable()) {
			StringBinaryToIntegerExpression strBExpr = new StringBinaryToIntegerExpression(
					stringReceiverExpr, Operator.COMPARETO, strExpr, (long) res);

			this.replaceTopBv32(strBExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
