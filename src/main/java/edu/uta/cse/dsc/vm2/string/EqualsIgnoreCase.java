package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class EqualsIgnoreCase extends StringFunction {

	private static final String FUNCTION_NAME = "equalsIgnoreCase";
	private StringExpression strExpr;

	public EqualsIgnoreCase(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.STR_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = operandToStringRef(it.next());
		this.stringReceiverExpr = operandToStringRef(it.next());
	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (this.strExpr != null
				&& (stringReceiverExpr.containsSymbolicVariable() || strExpr
						.containsSymbolicVariable())) {
			int conV = res ? 1 : 0;
			StringComparison strBExpr = new StringComparison(
					stringReceiverExpr, Operator.EQUALSIGNORECASE, strExpr,
					(long) conV);
			StringToIntCast castExpr = new StringToIntCast(strBExpr,
					(long) conV);
			this.replaceBv32Top(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
