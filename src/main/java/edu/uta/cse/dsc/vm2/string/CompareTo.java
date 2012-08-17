package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class CompareTo extends StringFunction {

	private static final String COMPARE_TO = "compareTo";
	private StringExpression strExpr;

	public CompareTo(SymbolicEnvironment env) {
		super(env, COMPARE_TO, Types.STR_TO_INT_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = operandToStringExpression(it.next());
		this.stringReceiverExpr = operandToStringExpression(it.next());

	}

	@Override
	public void CALL_RESULT(int res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| strExpr.containsSymbolicVariable()) {
			StringBinaryExpression strBExpr = new StringBinaryExpression(
					stringReceiverExpr, Operator.COMPARETO, strExpr,
					Integer.toString(res));
			StringToIntCast castExpr = new StringToIntCast(strBExpr, (long) res);
			this.replaceTopBv32(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
