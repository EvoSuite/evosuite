package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class CompareTo extends StringVirtualFunction {

	private static final String FUNCTION_NAME = "compareTo";
	private StringExpression strExpr;

	public CompareTo(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.STR_TO_INT_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = operandToStringRef(it.next());
		this.stringReceiverExpr = operandToStringRef(it.next());

	}

	@Override
	public void CALL_RESULT(int res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| strExpr.containsSymbolicVariable()) {
			StringBinaryExpression strBExpr = new StringBinaryExpression(
					stringReceiverExpr, Operator.COMPARETO, strExpr,
					Integer.toString(res));
			StringToIntCast castExpr = new StringToIntCast(strBExpr, (long) res);
			this.replaceBv32Top(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
