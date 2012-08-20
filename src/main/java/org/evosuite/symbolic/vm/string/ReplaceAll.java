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


public final class ReplaceAll extends StringFunction {

	private StringExpression regexExpr;
	private StringExpression replacementExpr;

	private static final String REPLACE_ALL = "replaceAll";

	public ReplaceAll(SymbolicEnvironment env) {
		super(env, REPLACE_ALL, Types.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();

		this.replacementExpr = operandToStringExpression(it.next());
		this.regexExpr = operandToStringExpression(it.next());
		this.stringReceiverExpr = operandToStringExpression(it.next());

	}

	@Override
	public void CALL_RESULT(Object res) {
		if (stringReceiverExpr.containsSymbolicVariable()
				|| regexExpr.containsSymbolicVariable()
				|| replacementExpr.containsSymbolicVariable()) {

			StringMultipleExpression strTExpr = new StringMultipleExpression(
					stringReceiverExpr, Operator.REPLACEALL, regexExpr,
					new ArrayList<Expression<?>>(Collections
							.singletonList(replacementExpr)),
					(String) res);
			replaceStrRefTop(strTExpr);
		}
	}
}
