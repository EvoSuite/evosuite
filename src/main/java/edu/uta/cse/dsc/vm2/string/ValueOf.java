package edu.uta.cse.dsc.vm2.string;

import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class ValueOf extends StaticFunction {
	private static final String FUNCTION_NAME = "valueOf";

	public ValueOf(SymbolicEnvironment env, String desc) {
		super(env, StringFunctionCallVM.JAVA_LANG_STRING, FUNCTION_NAME,
				OBJECT_TO_STR_DESCRIPTOR);
	}

	public static final class ValueOf_O extends ValueOf {
		private StringExpression strExpr;

		public ValueOf_O(SymbolicEnvironment env) {
			super(env, OBJECT_TO_STR_DESCRIPTOR);
		}

		@Override
		public void INVOKESTATIC() {
			Operand op = env.topFrame().operandStack.peekOperand();
			strExpr = this.operandToStringExpression(op);
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
