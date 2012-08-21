package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleExpression;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class ReplaceFirst extends StringFunction {

	private StringExpression regexExpr;
	private StringExpression replacementExpr;

	private static final String REPLACE_FIRST = "replaceFirst";

	public ReplaceFirst(SymbolicEnvironment env) {
		super(env, REPLACE_FIRST, Types.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();

		this.replacementExpr = operandToStringExpression(it.next());
		this.regexExpr = operandToStringExpression(it.next());
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
