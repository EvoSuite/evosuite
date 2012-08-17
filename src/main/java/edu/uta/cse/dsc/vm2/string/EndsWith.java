package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class EndsWith extends StringFunction {

	private static final String ENDS_WITH = "endsWith";
	private StringExpression strExpr;

	public EndsWith(SymbolicEnvironment env) {
		super(env, ENDS_WITH, Types.STR_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = operandToStringExpression(it.next());
		this.stringReceiverExpr = operandToStringExpression(it.next());
	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| strExpr.containsSymbolicVariable()) {
			int conV = res ? 1 : 0;
			StringComparison strBExpr = new StringComparison(
					stringReceiverExpr, Operator.ENDSWITH, strExpr, (long) conV);
			StringToIntCast castExpr = new StringToIntCast(strBExpr,
					(long) conV);
			this.replaceTopBv32(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
