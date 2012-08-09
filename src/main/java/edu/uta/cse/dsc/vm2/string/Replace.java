package edu.uta.cse.dsc.vm2.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleExpression;

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.ReferenceOperand;
import edu.uta.cse.dsc.vm2.StringReferenceOperand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class Replace extends StringFunction {

	private static final String FUNCTION_NAME = "replace";

	public Replace(SymbolicEnvironment env, String desc) {
		super(env, FUNCTION_NAME, desc);
	}

	public static final class Replace_C extends Replace {

		private IntegerExpression oldCharExpr;
		private IntegerExpression newCharExpr;

		public Replace_C(SymbolicEnvironment env) {
			super(env, StringFunction.CHAR_CHAR_TO_STR_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.newCharExpr = bv32(it.next());
			this.oldCharExpr = bv32(it.next());
			this.stringReceiverExpr = stringRef(it.next());
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
			super(env, StringFunction.CHARSEQ_CHARSEQ_TO_STR_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();

			ReferenceOperand newCharSeqOperand = (ReferenceOperand) it.next();
			if (newCharSeqOperand.getReference() == null) {
				throwException(new NullPointerException());
				return;
			}

			ReferenceOperand oldCharSeqOperand = (ReferenceOperand) it.next();
			if (oldCharSeqOperand.getReference() == null) {
				throwException(new NullPointerException());
				return;
			}

			if (newCharSeqOperand instanceof StringReferenceOperand) {
				this.newStringExpr = ((StringReferenceOperand) newCharSeqOperand)
						.getStringExpression();
			} else {
				this.newStringExpr = null;
			}

			if (oldCharSeqOperand instanceof StringReferenceOperand) {
				this.oldStringExpr = ((StringReferenceOperand) oldCharSeqOperand)
						.getStringExpression();
			} else {
				this.oldStringExpr = null;
			}

			this.stringReceiverExpr = stringRef(it.next());
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
