package org.evosuite.symbolic.vm.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class EndsWith extends StringFunction {

	private static final String ENDS_WITH = "endsWith";
	private StringValue strExpr;

	public EndsWith(SymbolicEnvironment env) {
		super(env, ENDS_WITH, Types.STR_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		it.next(); // discard (for now)
		this.stringReceiverExpr = getStringExpression(it.next(), receiver);
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {
		String string_value = (String) value;
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = getStringExpression(it.next(), string_value);
	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| strExpr.containsSymbolicVariable()) {
			int conV = res ? 1 : 0;
			StringComparison strBExpr = new StringComparison(
					stringReceiverExpr, Operator.ENDSWITH, strExpr, (long) conV);
			this.replaceTopBv32(strBExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
