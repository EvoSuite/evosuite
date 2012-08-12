package edu.uta.cse.dsc.vm2.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleExpression;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class ReplaceFirst extends StringVirtualFunction {

	private StringExpression regexExpr;
	private StringExpression replacementExpr;

	private static final String FUNCTION_NAME = "replaceFirst";

	public ReplaceFirst(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();

		this.replacementExpr = operandToStringRef(it.next());
		this.regexExpr = operandToStringRef(it.next());
		this.stringReceiverExpr = stringRef(it.next());

	}

	@Override
	public void CALL_RESULT(Object res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| regexExpr.containsSymbolicVariable()
				|| replacementExpr.containsSymbolicVariable()) {

			StringMultipleExpression strTExpr = new StringMultipleExpression(
					stringReceiverExpr, Operator.REPLACEFIRST, regexExpr,
					new ArrayList<Expression<?>>(Collections
							.singletonList(replacementExpr)),
					(String) res);
			replaceStrRefTop(strTExpr);
		}
	}
}
