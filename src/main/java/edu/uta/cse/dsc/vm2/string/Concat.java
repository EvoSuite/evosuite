package edu.uta.cse.dsc.vm2.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class Concat extends StringVirtualFunction {

	private static final String FUNCTION_NAME = "concat";

	private StringExpression strExpr;

	public Concat(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = operandToStringExpression(it.next());
		this.stringReceiverExpr = operandToStringExpression(it.next());
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| strExpr.containsSymbolicVariable()) {
			StringBinaryExpression strBExpr = new StringBinaryExpression(
					stringReceiverExpr, Operator.CONCAT, strExpr, (String) res);
			replaceStrRefTop(strBExpr);
		} else {
			// do nothing
		}
	}
}
