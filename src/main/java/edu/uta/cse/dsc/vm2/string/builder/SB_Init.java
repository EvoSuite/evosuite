package edu.uta.cse.dsc.vm2.string.builder;

import java.util.Iterator;

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
import edu.uta.cse.dsc.vm2.string.StringFunctionCallVM;
import edu.uta.cse.dsc.vm2.string.SpecialFunction;

public abstract class SB_Init extends SpecialFunction {

	public static final String STRING_BUILDER_CONTENTS = "$stringBuilder_contents";

	public SB_Init(SymbolicEnvironment env, String desc) {
		super(env, StringFunctionCallVM.JAVA_LANG_STRING_BUILDER, "<init>",
				desc);
	}

	public static final class StringBuilderInit_S extends SB_Init {

		public StringBuilderInit_S(SymbolicEnvironment env) {
			super(env, StringFunction.STR_TO_VOID_DESCRIPTOR);

		}

		private StringExpression strExpr;
		private NonNullReference stringBuilderRef;

		@Override
		public void INVOKESPECIAL() {
			/**
			 * Gather operands for symbolic execution
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Operand operand = it.next();
			Reference ref = ref(it.next());
			if (ref instanceof NullReference) {
				// An exception will be thrown
				this.stringBuilderRef = null;
				this.strExpr = null;
			} else {
				// normal behaviour
				this.stringBuilderRef = (NonNullReference) ref;
				this.strExpr = this.operandToStringExpression(operand);
			}
		}

		@Override
		public void CALL_RESULT() {
			/**
			 * Symbolic execution
			 */
			if (strExpr.containsSymbolicVariable()) {
				String fieldName = STRING_BUILDER_CONTENTS;
				StringBuilderExpression strBuilderExpr = new StringBuilderExpression(
						strExpr);
				// update symbolic heap
				this.env.updateHeap(fieldName, stringBuilderRef, strBuilderExpr);
			} else {
				// no update to symbolic heap since all values all concrete
			}
		}
	}

	public static final class StringBuilderInit_CS extends SB_Init {

		public StringBuilderInit_CS(SymbolicEnvironment env) {
			super(env, StringFunction.CHARSEQ_TO_VOID_DESCRIPTOR);

		}

		private StringExpression charSeqExpr;
		private NonNullReference stringBuilderRef;

		@Override
		public void INVOKESPECIAL() {
			/**
			 * Gather operands for symbolic execution
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Reference charSeqRef = ref(it.next()); // argument (CharSequence)
			Reference ref = ref(it.next()); // receiver
			if (ref instanceof NullReference) {
				// An exception will be thrown
				this.stringBuilderRef = null;
				this.charSeqExpr = null;
			} else {
				// normal behaviour
				this.stringBuilderRef = (NonNullReference) ref;
				this.charSeqExpr = null;
			}
		}

		@Override
		public void CALL_RESULT() {
			/**
			 * Symbolic execution
			 */

			if (charSeqExpr.containsSymbolicVariable()) {
				String fieldName = STRING_BUILDER_CONTENTS;
				StringBuilderExpression strBuilderExpr = new StringBuilderExpression(
						charSeqExpr);
				// update symbolic heap
				this.env.updateHeap(fieldName, stringBuilderRef, strBuilderExpr);
			} else {
				// no update to symbolic heap since all values all concrete
			}
		}

		@Override
		public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex,
				Object value) {
			if (charSeqExpr == null) {
				// if no symbolic expression, use concrete value
				CharSequence concreteCharSeq = (CharSequence) value;
				if (concreteCharSeq != null) {
					charSeqExpr = ExpressionFactory
							.buildNewStringConstant(concreteCharSeq.toString());
				}
			}
		}
	}

}
