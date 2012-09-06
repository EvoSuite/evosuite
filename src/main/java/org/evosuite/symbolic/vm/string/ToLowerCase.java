package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.str.StringUnaryExpression;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class ToLowerCase extends StringFunction {

	private static final String TO_LOWER_CASE = "toLowerCase";

	public ToLowerCase(SymbolicEnvironment env) {
		super(env, TO_LOWER_CASE, Types.TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		this.stringReceiverExpr = getStringExpression(env.topFrame().operandStack
				.peekOperand(), receiver);
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (res != null) {
			StringUnaryExpression symb_value = new StringUnaryExpression(
					stringReceiverExpr, Operator.TOLOWERCASE, (String) res);

			NonNullReference symb_receiver = (NonNullReference) env.topFrame().operandStack
					.peekRef();
			String conc_receiver = (String) res;
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					symb_value);
		}
	}
}
