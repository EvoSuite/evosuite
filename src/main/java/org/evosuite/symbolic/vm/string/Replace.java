package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleExpression;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class Replace extends StringFunction {

	private static final String REPLACE = "replace";

	public Replace(SymbolicEnvironment env, String desc) {
		super(env, REPLACE, desc);
	}

	public static final class Replace_C extends Replace {

		private IntegerExpression oldCharExpr;
		private IntegerExpression newCharExpr;

		public Replace_C(SymbolicEnvironment env) {
			super(env, Types.CHAR_CHAR_TO_STR_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.newCharExpr = bv32(it.next());
			this.oldCharExpr = bv32(it.next());
			this.stringReceiverExpr = operandToStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(Object res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| oldCharExpr.containsSymbolicVariable()
					|| newCharExpr.containsSymbolicVariable()) {

				StringMultipleExpression strTExpr = new StringMultipleExpression(
						stringReceiverExpr, Operator.REPLACEC, oldCharExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(newCharExpr)),
						(String) res);
				replaceStrRefTop(strTExpr);
			} else {
				// do nothing
			}
		}
	}

	public static final class Replace_CS extends Replace {

		private StringExpression oldStringExpr;
		private StringExpression newStringExpr;

		public Replace_CS(SymbolicEnvironment env) {
			super(env, Types.CHARSEQ_CHARSEQ_TO_STR_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.newStringExpr = operandToStringExpression(it.next());
			this.oldStringExpr = operandToStringExpression(it.next());
			this.stringReceiverExpr = operandToStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(Object res) {
			if (oldStringExpr != null && newStringExpr != null) {
				if (stringReceiverExpr.containsSymbolicVariable()
						|| oldStringExpr.containsSymbolicVariable()
						|| newStringExpr.containsSymbolicVariable()) {

					StringMultipleExpression strTExpr = new StringMultipleExpression(
							stringReceiverExpr, Operator.REPLACECS,
							oldStringExpr, new ArrayList<Expression<?>>(
									Collections.singletonList(newStringExpr)),
							(String) res);
					replaceStrRefTop(strTExpr);
				}
			}
		}
	}

}
