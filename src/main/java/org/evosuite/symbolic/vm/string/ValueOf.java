package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class ValueOf {
	private static final String VALUE_OF = "valueOf";

	public static final class ValueOf_O extends StringFunction {
		private StringValue strExpr;

		public ValueOf_O(SymbolicEnvironment env) {
			super(env, VALUE_OF, Types.OBJECT_TO_STR_DESCRIPTOR);
		}

		@Override
		public void INVOKESTATIC() {
			// do nothing
		}

		@Override
		public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex,
				Object value) {
			if (value instanceof String) {
				String string = (String) value;
				Operand op = env.topFrame().operandStack.peekOperand();
				strExpr = getStringExpression(op, string);
			}
		}

		@Override
		public void CALL_RESULT(Object res) {
			if (res != null && strExpr != null) {

				StringValue symb_value = strExpr;
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
