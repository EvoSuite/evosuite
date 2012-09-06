package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class ReplaceFirst extends StringFunction {

	private StringValue regexExpr;
	private StringValue replacementExpr;

	private static final String REPLACE_FIRST = "replaceFirst";

	public ReplaceFirst(SymbolicEnvironment env) {
		super(env, REPLACE_FIRST, Types.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();

		it.next();
		it.next();
		this.stringReceiverExpr = getStringExpression(it.next(), receiver);

	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {

		String string = (String) value;
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		if (nr == 1) {
			this.replacementExpr = getStringExpression(it.next(), string);

		} else if (nr == 0) {
			it.next();
			this.regexExpr = getStringExpression(it.next(), string);
		}
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (res != null) {

			StringMultipleExpression symb_value = new StringMultipleExpression(
					stringReceiverExpr, Operator.REPLACEFIRST, regexExpr,
					new ArrayList<Expression<?>>(Collections
							.singletonList(replacementExpr)),
					(String) res);
			NonNullReference symb_receiver = (NonNullReference) env.topFrame().operandStack
					.peekRef();
			String conc_receiver = (String) res;
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					symb_value);
		}
	}

}
