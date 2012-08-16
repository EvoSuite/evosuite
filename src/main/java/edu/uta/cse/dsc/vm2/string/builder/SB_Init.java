package edu.uta.cse.dsc.vm2.string.builder;

import java.util.Iterator;

import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringConstant;
import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.ExpressionFactory;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.NullReference;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.Reference;
import edu.uta.cse.dsc.vm2.StringReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;
import edu.uta.cse.dsc.vm2.string.SpecialFunction;
import edu.uta.cse.dsc.vm2.string.StringFunction;

import static edu.uta.cse.dsc.vm2.string.builder.StringBuilderConstants.JAVA_LANG_STRING_BUILDER;
import static edu.uta.cse.dsc.vm2.string.builder.StringBuilderConstants.STRING_BUILDER_CONTENTS;

public abstract class SB_Init extends SpecialFunction {

	private static final String FUNCTION_NAME = "<init>";

	public SB_Init(SymbolicEnvironment env, String desc) {
		super(env, JAVA_LANG_STRING_BUILDER, FUNCTION_NAME, desc);
	}

	public static final class StringBuilderInit_S extends SB_Init {

		public StringBuilderInit_S(SymbolicEnvironment env) {
			super(env, StringFunction.STR_TO_VOID_DESCRIPTOR);

		}

		private StringExpression strExpr;
		private NonNullReference symb_str_builder;

		@Override
		public void INVOKESPECIAL() {
			/**
			 * Gather operands for symbolic execution
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Reference str_ref = ref(it.next());
			Reference str_builder_ref = ref(it.next());
			if (isNullRef(str_ref) || isNullRef(str_builder_ref)) {
				// An exception will be thrown
				this.symb_str_builder = null;
				this.strExpr = null;
			} else {
				// normal behaviour
				this.symb_str_builder = (NonNullReference) str_builder_ref;
				this.strExpr = ((StringReference) str_ref)
						.getStringExpression();
			}
		}

		@Override
		public void CALL_RESULT() {
			/**
			 * Symbolic execution
			 */
			StringBuilderExpression strBuilderExpr = new StringBuilderExpression(
					strExpr);

			// update symbolic heap
			this.env.heap.putField(JAVA_LANG_STRING_BUILDER,
					STRING_BUILDER_CONTENTS, null, symb_str_builder,
					strBuilderExpr);
		}

	}

	public static final class StringBuilderInit_CS extends SB_Init {

		public StringBuilderInit_CS(SymbolicEnvironment env) {
			super(env, StringFunction.CHARSEQ_TO_VOID_DESCRIPTOR);

		}

		@Override
		public void INVOKESPECIAL() {
			/* do nothing */
		}

		@Override
		public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex,
				Object conc_value) {
			/* do nothing */

		}

		@Override
		public void CALL_RESULT() {
			/* do nothing */
		}

	}

}
