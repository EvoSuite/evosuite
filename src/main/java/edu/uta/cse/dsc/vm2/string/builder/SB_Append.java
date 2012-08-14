package edu.uta.cse.dsc.vm2.string.builder;

import java.util.Iterator;

import org.evosuite.symbolic.expr.IntToStringCast;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealToStringCast;
import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.ExpressionFactory;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.NullReference;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.Reference;
import edu.uta.cse.dsc.vm2.StringReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;
import edu.uta.cse.dsc.vm2.string.StringFunction;

public abstract class SB_Append extends StringBuilderVirtualFunction {

	private static final String FUNCTION_NAME = "append";

	public SB_Append(SymbolicEnvironment env, String desc) {
		super(env, FUNCTION_NAME, desc);
	}

	public static final class Append_C extends SB_Append {

		public Append_C(SymbolicEnvironment env) {
			super(env, StringFunction.CHAR_TO_STRBUILDER_DESCRIPTOR);
		}

		private IntegerExpression integerExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			this.integerExpr = bv32(it.next());

			Reference ref = ref(it.next());
			// the reference can not be null at this point
			strBuilderRef = (NonNullReference) ref;

			// get from symbolic heap (it could be null)
			this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
					.getField(SB_Init.STRING_BUILDER_CONTENTS, desc,
							strBuilderRef, strBuilderRef, integerExpr);

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())
					|| integerExpr.containsSymbolicVariable()) {

				if (this.stringBuilderExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.heap.putField("java.lang.StringBuffer",
							SB_Init.STRING_BUILDER_CONTENTS, res,
							strBuilderRef, stringBuilderExpr);
				}

				stringBuilderExpr.append(new IntToStringCast(integerExpr));

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

	public static final class Append_S extends SB_Append {

		public Append_S(SymbolicEnvironment env) {
			super(env, StringFunction.STR_TO_STRBUILDER_DESCRIPTOR);
		}

		private StringExpression strExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Reference refStrToAppend = ref(it.next());
			strBuilderRef = (NonNullReference) ref(it.next());

			if (refStrToAppend instanceof NullReference) {
				strExpr = ExpressionFactory.buildNewStringConstant("null");
			} else {
				StringReference strRef = (StringReference) refStrToAppend;
				strExpr = strRef.getStringExpression();
			}

			if (strExpr.containsSymbolicVariable()) {
				// get from symbolic heap (it could be null)
				this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
						.getField("java.lang.StringBuilder",
								SB_Init.STRING_BUILDER_CONTENTS, null,
								strBuilderRef, null);
			}

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())
					|| strExpr.containsSymbolicVariable()) {

				if (this.stringBuilderExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.heap.putField("java.lang.StringBuffer",
							SB_Init.STRING_BUILDER_CONTENTS, res,
							strBuilderRef, stringBuilderExpr);
				}

				stringBuilderExpr.append(strExpr);

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

	public static final class Append_I extends SB_Append {

		public Append_I(SymbolicEnvironment env) {
			super(env, StringFunction.INT_TO_STRBUILDER_DESCRIPTOR);
		}

		private IntegerExpression integerExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			this.integerExpr = bv32(it.next());

			Reference ref = ref(it.next());
			// the reference can not be null at this point
			strBuilderRef = (NonNullReference) ref;

			// get from symbolic heap (it could be null)
			this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
					.getField("java.lang.StringBuilder",
							SB_Init.STRING_BUILDER_CONTENTS, null,
							strBuilderRef, null);

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())
					|| integerExpr.containsSymbolicVariable()) {

				if (this.stringBuilderExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.heap.putField("java.lang.StringBuffer",
							SB_Init.STRING_BUILDER_CONTENTS, res,
							strBuilderRef, stringBuilderExpr);
				}

				stringBuilderExpr.append(new IntToStringCast(integerExpr));

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

	public static final class Append_L extends SB_Append {

		public Append_L(SymbolicEnvironment env) {
			super(env, StringFunction.LONG_TO_STRBUILDER_DESCRIPTOR);
		}

		private IntegerExpression integerExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			this.integerExpr = bv64(it.next());

			Reference ref = ref(it.next());
			// the reference can not be null at this point
			strBuilderRef = (NonNullReference) ref;

			// get from symbolic heap (it could be null)
			this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
					.getField("java.lang.StringBuilder",
							SB_Init.STRING_BUILDER_CONTENTS, receiver,
							strBuilderRef, receiver.toString());

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())
					|| integerExpr.containsSymbolicVariable()) {

				if (this.stringBuilderExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.heap.putField("java.lang.StringBuffer",
							SB_Init.STRING_BUILDER_CONTENTS, res,
							strBuilderRef, stringBuilderExpr);
				}

				stringBuilderExpr.append(new IntToStringCast(integerExpr));

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

	public static final class Append_B extends SB_Append {

		public Append_B(SymbolicEnvironment env) {
			super(env, StringFunction.BOOLEAN_TO_STRBUILDER_DESCRIPTOR);
		}

		private IntegerExpression integerExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			this.integerExpr = bv32(it.next());

			Reference ref = ref(it.next());
			// the reference can not be null at this point
			strBuilderRef = (NonNullReference) ref;

			// get from symbolic heap (it could be null)
			this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
					.getField("java.lang.Builder",
							SB_Init.STRING_BUILDER_CONTENTS, receiver,
							strBuilderRef, null);

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())
					|| integerExpr.containsSymbolicVariable()) {

				if (this.stringBuilderExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.heap.putField("java.lang.StringBuffer",
							SB_Init.STRING_BUILDER_CONTENTS, res,
							strBuilderRef, stringBuilderExpr);
				}

				stringBuilderExpr.append(new IntToStringCast(integerExpr));

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

	public static final class Append_F extends SB_Append {

		public Append_F(SymbolicEnvironment env) {
			super(env, StringFunction.FLOAT_TO_STRBUILDER_DESCRIPTOR);
		}

		private RealExpression realExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			this.realExpr = fp32(it.next());

			Reference ref = ref(it.next());
			// the reference can not be null at this point
			strBuilderRef = (NonNullReference) ref;

			// get from symbolic heap (it could be null)
			this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
					.getField("java.lang.StringBuilder",
							SB_Init.STRING_BUILDER_CONTENTS, receiver,
							strBuilderRef, receiver.toString());

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())
					|| realExpr.containsSymbolicVariable()) {

				if (this.stringBuilderExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.heap.putField("java.lang.StringBuffer",
							SB_Init.STRING_BUILDER_CONTENTS, res,
							strBuilderRef, stringBuilderExpr);
				}

				stringBuilderExpr.append(new RealToStringCast(realExpr));

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

	public static final class Append_D extends SB_Append {

		public Append_D(SymbolicEnvironment env) {
			super(env, StringFunction.DOUBLE_TO_STRBUILDER_DESCRIPTOR);
		}

		private RealExpression realExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			this.realExpr = fp64(it.next());

			Reference ref = ref(it.next());
			// the reference can not be null at this point
			strBuilderRef = (NonNullReference) ref;

			// get from symbolic heap (it could be null)
			this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
					.getField("java.lang.StringBuilder",
							SB_Init.STRING_BUILDER_CONTENTS, receiver,
							strBuilderRef, receiver.toString());

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())
					|| realExpr.containsSymbolicVariable()) {

				if (this.stringBuilderExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.heap.putField("java.lang.StringBuffer",
							SB_Init.STRING_BUILDER_CONTENTS, res,
							strBuilderRef, stringBuilderExpr);
				}

				stringBuilderExpr.append(new RealToStringCast(realExpr));

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

	public static final class Append_O extends SB_Append {

		public Append_O(SymbolicEnvironment env) {
			super(env, StringFunction.OBJECT_TO_STRBUILDER_DESCRIPTOR);
		}

		private StringExpression strExpr;
		private NonNullReference strBuilderRef;

		@Override
		protected void INVOKEVIRTUAL(StringBuilder receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Reference refObjToAppend = ref(it.next()); // discard this symbolic
														// reference
			strBuilderRef = (NonNullReference) ref(it.next());

			// get from symbolic heap (it could be null)
			this.stringBuilderExpr = (StringBuilderExpression) this.env.heap
					.getField("java.lang.StringBuilder",
							SB_Init.STRING_BUILDER_CONTENTS, receiver,
							strBuilderRef, receiver.toString());
		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderExpr != null && stringBuilderExpr
					.containsSymbolicVariable())) {

				stringBuilderExpr.append(strExpr);

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}

		@Override
		public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex,
				Object value) {

			strExpr = ExpressionFactory.buildNewStringConstant(String
					.valueOf(value));

		}
	}

}
