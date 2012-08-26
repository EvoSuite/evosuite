package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class ValueOf {
	private static final String VALUE_OF = "valueOf";

	public static final class ValueOf_O extends StringFunction {
		private StringExpression strExpr;

		public ValueOf_O(SymbolicEnvironment env) {
			super(env, VALUE_OF, Types.OBJECT_TO_STR_DESCRIPTOR);
		}

		@Override
		public void INVOKESTATIC() {
			Operand op = env.topFrame().operandStack.peekOperand();
			strExpr = getStringExpression(op);
		}

		@Override
		public void CALL_RESULT(Object res) {
			if (res != null && strExpr != null) {

				StringExpression symb_value = strExpr;
				NonNullReference symb_receiver = (NonNullReference) env
						.topFrame().operandStack.peekRef();
				String conc_receiver = (String) res;
				env.heap.putField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_receiver,
						symb_receiver, symb_value);

			}
		}
	}
}
