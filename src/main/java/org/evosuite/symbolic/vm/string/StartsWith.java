package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class StartsWith extends StringFunction {

	private StringValue prefixExpr;
	private IntegerValue offsetExpr;

	private static final String STARTS_WITH = "startsWith";

	public StartsWith(SymbolicEnvironment env) {
		super(env, STARTS_WITH, Types.STR_INT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.offsetExpr = bv32(it.next());
		this.prefixExpr = getStringExpression(it.next());
		this.stringReceiverExpr = getStringExpression(it.next());
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


			this.replaceTopBv32(strTExpr);
		} else {
			// do nothing (concrete value only)
		}

	}
}
