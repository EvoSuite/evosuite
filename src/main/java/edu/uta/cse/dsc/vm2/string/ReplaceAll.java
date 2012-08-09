package edu.uta.cse.dsc.vm2.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleExpression;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.ReferenceOperand;
import edu.uta.cse.dsc.vm2.StringReferenceOperand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class ReplaceAll extends StringFunction {

	private StringExpression regexExpr;
	private StringExpression replacementExpr;

	private static final String FUNCTION_NAME = "replaceAll";

	public ReplaceAll(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, StringFunction.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();

		ReferenceOperand replacementOperand = (ReferenceOperand) it.next();
		if (replacementOperand.getReference() == null) {
			throwException(new NullPointerException());
			return;
		}

		ReferenceOperand regexOperand = (ReferenceOperand) it.next();
		if (regexOperand.getReference() == null) {
			throwException(new NullPointerException());
			return;
		}

		this.replacementExpr = ((StringReferenceOperand) replacementOperand)
				.getStringExpression();
		this.regexExpr = ((StringReferenceOperand) regexOperand)
				.getStringExpression();
		this.stringReceiverExpr = stringRef(it.next());

		String regex = (String) regexExpr.getConcreteValue();
		String replacement = (String) replacementExpr.getConcreteValue();
		try {
			receiver.replaceAll(regex, replacement);
		} catch (PatternSyntaxException ex) {
			throwException(ex);
			return;
		}
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
