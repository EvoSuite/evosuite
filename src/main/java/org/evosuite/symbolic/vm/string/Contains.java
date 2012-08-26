package org.evosuite.symbolic.vm.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class Contains extends StringFunction {

	private StringExpression strExpr;
	private static final String CONTAINS = "contains";

	public Contains(SymbolicEnvironment env) {
		super(env, CONTAINS, Types.CHARSEQ_TO_BOOL_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = getStringExpression(it.next());
		this.stringReceiverExpr = getStringExpression(it.next());
	}

	@Override
	public void CALL_RESULT(boolean res) {
		if (strExpr != null) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| strExpr.containsSymbolicVariable()) {

				int concrete_value = res ? 1 : 0;

				StringComparison strComp = new StringComparison(
						stringReceiverExpr, Operator.CONTAINS, strExpr,
						(long) concrete_value);
				StringToIntCast castExpr = new StringToIntCast(strComp,
						(long) concrete_value);

				replaceTopBv32(castExpr);
			}
		}
	}
}