package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class ToString extends StringFunction {

	private static final String TO_STRING = "toString";

	public ToString(SymbolicEnvironment env) {
		super(env, TO_STRING, Types.TO_STR_DESCRIPTOR);
	}

	@Override
	protected void INVOKEVIRTUAL_String(String receiver) {
		this.stringReceiverExpr = getStringExpression(env.topFrame().operandStack
				.peekOperand());
	}

	@Override
	public void CALL_RESULT(Object res) {
		if (res != null) {

			NonNullReference symb_receiver = (NonNullReference) env.topFrame().operandStack
					.peekRef();
			String conc_receiver = (String) res;
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					stringReceiverExpr);

		}
	}
}
