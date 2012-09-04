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

public final class ReplaceAll extends StringFunction {

	private StringValue regexExpr;
	private StringValue replacementExpr;

	private static final String REPLACE_ALL = "replaceAll";

	public ReplaceAll(SymbolicEnvironment env) {
		super(env, REPLACE_ALL, Types.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();

		Operand replacement_operand = it.next();
		Operand regex_operand =it.next();
		Operand receiver_operand = it.next();

		this.replacementExpr = getStringExpression(replacement_operand);
		this.regexExpr = getStringExpression(regex_operand);
		this.stringReceiverExpr = getStringExpression(receiver_operand);

	}

	@Override
	public void CALL_RESULT(Object res) {
		if (res != null && replacementExpr!=null) {

			StringMultipleExpression symb_value = new StringMultipleExpression(
					stringReceiverExpr, Operator.REPLACEALL, regexExpr,
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
