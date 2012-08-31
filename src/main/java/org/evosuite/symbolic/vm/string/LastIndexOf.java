package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringMultipleToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class LastIndexOf extends StringFunction {

	private static final String LAST_INDEX_OF = "lastIndexOf";

	public LastIndexOf(SymbolicEnvironment env, String desc) {
		super(env, LAST_INDEX_OF, desc);
	}

	public final static class LastIndexOf_C extends LastIndexOf {

		private IntegerValue charExpr;

		public LastIndexOf_C(SymbolicEnvironment env) {
			super(env, Types.INT_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.charExpr = bv32(it.next());
			this.stringReceiverExpr = getStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| charExpr.containsSymbolicVariable()) {
				StringBinaryToIntegerExpression strBExpr = new StringBinaryToIntegerExpression(
						stringReceiverExpr, Operator.LASTINDEXOFC, charExpr,
						(long) res);
				this.replaceTopBv32(strBExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class LastIndexOf_CI extends LastIndexOf {

		private IntegerValue charExpr;
		private IntegerValue fromIndexExpr;

		public LastIndexOf_CI(SymbolicEnvironment env) {
			super(env, Types.INT_INT_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.fromIndexExpr = bv32(it.next());
			this.charExpr = bv32(it.next());
			this.stringReceiverExpr = getStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| charExpr.containsSymbolicVariable()
					|| fromIndexExpr.containsSymbolicVariable()) {

				StringMultipleToIntegerExpression strTExpr = new StringMultipleToIntegerExpression(
						stringReceiverExpr, Operator.LASTINDEXOFCI, charExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(fromIndexExpr)),
						(long) res);

				this.replaceTopBv32(strTExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class LastIndexOf_S extends LastIndexOf {

		private StringValue strExpr;

		public LastIndexOf_S(SymbolicEnvironment env) {
			super(env, Types.STR_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();

			this.strExpr = getStringExpression(it.next());
			this.stringReceiverExpr = getStringExpression(it.next());

		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| strExpr.containsSymbolicVariable()) {
				StringBinaryToIntegerExpression strBExpr = new StringBinaryToIntegerExpression(
						stringReceiverExpr, Operator.LASTINDEXOFS, strExpr,
						(long) res);

				this.replaceTopBv32(strBExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class LastIndexOf_SI extends LastIndexOf {

		private StringValue strExpr;
		private IntegerValue fromIndexExpr;

		public LastIndexOf_SI(SymbolicEnvironment env) {
			super(env, Types.STR_INT_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.fromIndexExpr = bv32(it.next());
			this.strExpr = getStringExpression(it.next());
			this.stringReceiverExpr = getStringExpression(it.next());

		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| strExpr.containsSymbolicVariable()
					|| fromIndexExpr.containsSymbolicVariable()) {
				StringMultipleToIntegerExpression strTExpr = new StringMultipleToIntegerExpression(
						stringReceiverExpr, Operator.LASTINDEXOFSI, strExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(fromIndexExpr)),
						(long) res);

				this.replaceTopBv32(strTExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

}
