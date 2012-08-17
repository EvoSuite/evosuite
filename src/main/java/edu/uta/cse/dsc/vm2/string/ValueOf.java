package edu.uta.cse.dsc.vm2.string;

import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

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
			strExpr = operandToStringExpression(op);
		}

		@Override
		public void CALL_RESULT(Object res) {
			if (strExpr != null) {
				replaceStrRefTop(strExpr);
			} else {
				// leave concrete reference
			}
		}
	}
}
