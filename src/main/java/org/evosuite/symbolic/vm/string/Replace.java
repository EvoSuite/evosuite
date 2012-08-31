package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class Replace extends StringFunction {

	private static final String REPLACE = "replace";

	public Replace(SymbolicEnvironment env, String desc) {
		super(env, REPLACE, desc);
	}

	public static final class Replace_C extends Replace {

		private IntegerValue oldCharExpr;
		private IntegerValue newCharExpr;

		public Replace_C(SymbolicEnvironment env) {
			super(env, Types.CHAR_CHAR_TO_STR_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.newCharExpr = bv32(it.next());
			this.oldCharExpr = bv32(it.next());
			this.stringReceiverExpr = getStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(Object res) {
			if (res != null) {

				StringMultipleExpression symb_value = new StringMultipleExpression(
						stringReceiverExpr, Operator.REPLACEC, oldCharExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(newCharExpr)),
						(String) res);

				NonNullReference symb_receiver = (NonNullReference) env
						.topFrame().operandStack.peekRef();
				String conc_receiver = (String) res;
				env.heap.putField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_receiver,
						symb_receiver, symb_value);

			}
		}
	}

	public static final class Replace_CS extends Replace {

		private StringValue oldStringExpr;
		private StringValue newStringExpr;

		public Replace_CS(SymbolicEnvironment env) {
			super(env, Types.CHARSEQ_CHARSEQ_TO_STR_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.newStringExpr = getStringExpression(it.next());
			this.oldStringExpr = getStringExpression(it.next());
			this.stringReceiverExpr = getStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(Object res) {
			if (oldStringExpr != null && newStringExpr != null) {
				if (res != null) {

					StringMultipleExpression symb_value = new StringMultipleExpression(
							stringReceiverExpr, Operator.REPLACECS,
							oldStringExpr, new ArrayList<Expression<?>>(
									Collections.singletonList(newStringExpr)),
							(String) res);

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

}
