package org.evosuite.symbolic.vm.string;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Concat extends StringFunction {

	private static final String CONCAT = "concat";

	private StringExpression strExpr;

	public Concat(SymbolicEnvironment env) {
		super(env, CONCAT, Types.STR_TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		this.strExpr = getStringExpression(it.next());
		this.stringReceiverExpr = getStringExpression(it.next());
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (res != null) {
			StringBinaryExpression symb_value = new StringBinaryExpression(
					stringReceiverExpr, Operator.CONCAT, strExpr, (String) res);

			NonNullReference symb_receiver = (NonNullReference) env.topFrame().operandStack
					.peekRef();
			String conc_receiver = (String) res;
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					symb_value);
		}
	}
}
