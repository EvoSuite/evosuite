package edu.uta.cse.dsc.vm2.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringMultipleExpression;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class Substring extends StringFunction {

	private static final String FUNCTION_NAME = "substring";

	private IntegerExpression beginIndexExpr;
	private IntegerExpression endIndexExpr;

	public Substring(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.INT_INT_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.endIndexExpr = bv32(it.next());
		this.beginIndexExpr = bv32(it.next());
		this.stringReceiverExpr = operandToStringRef(it.next());
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| beginIndexExpr.containsSymbolicVariable()
				|| endIndexExpr.containsSymbolicVariable()) {
			StringMultipleExpression strTExpr = new StringMultipleExpression(
					stringReceiverExpr, Operator.SUBSTRING, beginIndexExpr,
					new ArrayList<Expression<?>>(Collections
							.singletonList(endIndexExpr)),
					(String) res);
			replaceStrRefTop(strTExpr);
		} else {
			// do nothing
		}
	}
}
