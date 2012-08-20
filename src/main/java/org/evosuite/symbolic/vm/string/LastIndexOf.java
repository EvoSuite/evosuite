package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleExpression;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class LastIndexOf extends StringFunction {

	private static final String LAST_INDEX_OF = "lastIndexOf";

	public LastIndexOf(SymbolicEnvironment env, String desc) {
		super(env, LAST_INDEX_OF, desc);
	}

	public final static class LastIndexOf_C extends LastIndexOf {

		private IntegerExpression charExpr;

		public LastIndexOf_C(SymbolicEnvironment env) {
			super(env, Types.INT_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.charExpr = bv32(it.next());
			this.stringReceiverExpr = operandToStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| charExpr.containsSymbolicVariable()) {
				StringBinaryExpression strBExpr = new StringBinaryExpression(
						stringReceiverExpr, Operator.LASTINDEXOFC, charExpr,
						Integer.toString(res));
				StringToIntCast castExpr = new StringToIntCast(strBExpr,
						(long) res);
				this.replaceTopBv32(castExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class LastIndexOf_CI extends LastIndexOf {

		private IntegerExpression charExpr;
		private IntegerExpression fromIndexExpr;

		public LastIndexOf_CI(SymbolicEnvironment env) {
			super(env, Types.INT_INT_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.fromIndexExpr = bv32(it.next());
			this.charExpr = bv32(it.next());
			this.stringReceiverExpr = operandToStringExpression(it.next());
		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| charExpr.containsSymbolicVariable()
					|| fromIndexExpr.containsSymbolicVariable()) {

				StringMultipleExpression strTExpr = new StringMultipleExpression(
						stringReceiverExpr, Operator.LASTINDEXOFCI, charExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(fromIndexExpr)),
						Integer.toString(res));

				StringToIntCast castExpr = new StringToIntCast(strTExpr,
						(long) res);
				this.replaceTopBv32(castExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class LastIndexOf_S extends LastIndexOf {

		private StringExpression strExpr;

		public LastIndexOf_S(SymbolicEnvironment env) {
			super(env, Types.STR_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();

			this.strExpr = operandToStringExpression(it.next());
			this.stringReceiverExpr = operandToStringExpression(it.next());

		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| strExpr.containsSymbolicVariable()) {
				StringBinaryExpression strBExpr = new StringBinaryExpression(
						stringReceiverExpr, Operator.LASTINDEXOFS, strExpr,
						Integer.toString(res));
				StringToIntCast castExpr = new StringToIntCast(strBExpr,
						(long) res);
				this.replaceTopBv32(castExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class LastIndexOf_SI extends LastIndexOf {

		private StringExpression strExpr;
		private IntegerExpression fromIndexExpr;

		public LastIndexOf_SI(SymbolicEnvironment env) {
			super(env, Types.STR_INT_TO_INT_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_String(String receiver) {
			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			this.fromIndexExpr = bv32(it.next());
			this.strExpr = operandToStringExpression(it.next());
			this.stringReceiverExpr = operandToStringExpression(it.next());

		}

		@Override
		public void CALL_RESULT(int res) {
			if (stringReceiverExpr.containsSymbolicVariable()
					|| strExpr.containsSymbolicVariable()
					|| fromIndexExpr.containsSymbolicVariable()) {
				StringMultipleExpression strTExpr = new StringMultipleExpression(
						stringReceiverExpr, Operator.LASTINDEXOFSI, strExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(fromIndexExpr)),
						Integer.toString(res));
				StringToIntCast castExpr = new StringToIntCast(strTExpr,
						(long) res);
				this.replaceTopBv32(castExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

}
