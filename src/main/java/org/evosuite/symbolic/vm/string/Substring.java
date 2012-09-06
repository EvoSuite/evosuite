package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Substring extends StringFunction {

	private static final String SUBSTRING = "substring";

	private IntegerValue beginIndexExpr;
	private IntegerValue endIndexExpr;

	public Substring(SymbolicEnvironment env) {
		super(env, SUBSTRING, Types.INT_INT_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.endIndexExpr = bv32(it.next());
		this.beginIndexExpr = bv32(it.next());
		this.stringReceiverExpr = getStringExpression(it.next(), receiver);
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (res != null) {
			StringMultipleExpression symb_value = new StringMultipleExpression(
					stringReceiverExpr, Operator.SUBSTRING, beginIndexExpr,
					new ArrayList<Expression<?>>(Collections
							.singletonList(endIndexExpr)),
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
