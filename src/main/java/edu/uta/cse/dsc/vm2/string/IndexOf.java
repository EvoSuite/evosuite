package edu.uta.cse.dsc.vm2.string;

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

import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class IndexOf extends StringFunction {

	private static final String INDEX_OF = "indexOf";

	public IndexOf(SymbolicEnvironment env, String desc) {
		super(env, INDEX_OF, desc);
	}

	public final static class IndexOf_C extends IndexOf {

		private IntegerExpression charExpr;

		public IndexOf_C(SymbolicEnvironment env) {
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
						stringReceiverExpr, Operator.INDEXOFC, charExpr,
						Integer.toString(res));
				StringToIntCast castExpr = new StringToIntCast(strBExpr,
						(long) res);
				this.replaceTopBv32(castExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class IndexOf_CI extends IndexOf {

		private IntegerExpression charExpr;
		private IntegerExpression fromIndexExpr;

		public IndexOf_CI(SymbolicEnvironment env) {
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
						stringReceiverExpr, Operator.INDEXOFCI, charExpr,
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

	public final static class IndexOf_S extends IndexOf {

		private StringExpression strExpr;

		public IndexOf_S(SymbolicEnvironment env) {
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
						stringReceiverExpr, Operator.INDEXOFS, strExpr,
						Integer.toString(res));
				StringToIntCast castExpr = new StringToIntCast(strBExpr,
						(long) res);
				this.replaceTopBv32(castExpr);
			} else {
				// do nothing (concrete value only)
			}

		}
	}

	public final static class IndexOf_SI extends IndexOf {

		private StringExpression strExpr;
		private IntegerExpression fromIndexExpr;

		public IndexOf_SI(SymbolicEnvironment env) {
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
						stringReceiverExpr, Operator.INDEXOFSI, strExpr,
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
