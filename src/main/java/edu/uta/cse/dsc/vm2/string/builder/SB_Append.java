package edu.uta.cse.dsc.vm2.string.builder;

import java.util.Iterator;

import org.evosuite.symbolic.expr.IntToStringCast;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.ExpressionFactory;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.Reference;
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
			this.stringBuilderReceiverExpr = (StringBuilderExpression) this.env
					.getHeap(SB_Init.STRING_BUILDER_CONTENTS, strBuilderRef);

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderReceiverExpr != null && stringBuilderReceiverExpr
					.containsSymbolicVariable())
					|| integerExpr.containsSymbolicVariable()) {

				if (this.stringBuilderReceiverExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderReceiverExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.updateHeap(SB_Init.STRING_BUILDER_CONTENTS,
							strBuilderRef, stringBuilderReceiverExpr);
				}

				stringBuilderReceiverExpr.append(new IntToStringCast(
						integerExpr));

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
			this.strExpr = operandToStringExpression(it.next());

			if (strExpr != null) {
				Reference ref = ref(it.next());
				// the reference can not be null at this point
				strBuilderRef = (NonNullReference) ref;

				// get from symbolic heap (it could be null)
				this.stringBuilderReceiverExpr = (StringBuilderExpression) this.env
						.getHeap(SB_Init.STRING_BUILDER_CONTENTS, strBuilderRef);
			}

		}

		@Override
		public void CALL_RESULT(Object res) {

			if ((stringBuilderReceiverExpr != null && stringBuilderReceiverExpr
					.containsSymbolicVariable())
					|| strExpr.containsSymbolicVariable()) {

				if (this.stringBuilderReceiverExpr == null) {
					/*
					 * if no symbolic value create a constant symbolic
					 * expression using the concrete value
					 */
					String str = ((StringBuilder) res).toString();
					stringBuilderReceiverExpr = new StringBuilderExpression(
							ExpressionFactory.buildNewStringConstant(str));
					this.env.updateHeap(SB_Init.STRING_BUILDER_CONTENTS,
							strBuilderRef, stringBuilderReceiverExpr);
				}

				stringBuilderReceiverExpr.append(strExpr);

			}

			// append(x) always return the same reference
			// CallVM believes the returned object is fresh, but
			// we knoe it is not.
			this.env.topFrame().operandStack.popRef();
			this.env.topFrame().operandStack.pushRef(strBuilderRef);
		}
	}

}
