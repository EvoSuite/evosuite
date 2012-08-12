package edu.uta.cse.dsc.vm2.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleComparison;
import org.evosuite.symbolic.expr.StringToIntCast;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class StartsWith extends StringVirtualFunction {

	private StringExpression prefixExpr;
	private IntegerExpression offsetExpr;

	private static final String FUNCTION_NAME = "startsWith";

	public StartsWith(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.STR_INT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.offsetExpr = bv32(it.next());
		this.prefixExpr = operandToStringExpression(it.next());
		this.stringReceiverExpr = stringRef(it.next());
	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| prefixExpr.containsSymbolicVariable()
				|| offsetExpr.containsSymbolicVariable()) {
			int conV = res ? 1 : 0;

			StringMultipleComparison strTExpr = new StringMultipleComparison(
					stringReceiverExpr, Operator.STARTSWITH, prefixExpr,
					new ArrayList<Expression<?>>(
							Collections.singletonList(offsetExpr)), (long) conV);

			StringToIntCast castExpr = new StringToIntCast(strTExpr,
					(long) conV);
			this.replaceBv32Top(castExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
