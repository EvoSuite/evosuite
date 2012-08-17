package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class EqualsIgnoreCase extends StringFunction {

	private static final String EQUALS_IGNORE_CASE = "equalsIgnoreCase";
	private StringExpression strExpr;

	public EqualsIgnoreCase(SymbolicEnvironment env) {
		super(env, EQUALS_IGNORE_CASE, Types.STR_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = operandToStringExpression(it.next());
		this.stringReceiverExpr = operandToStringExpression(it.next());
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
			this.replaceTopBv32(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
