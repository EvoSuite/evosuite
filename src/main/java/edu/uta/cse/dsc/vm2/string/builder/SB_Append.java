package edu.uta.cse.dsc.vm2.string.builder;

import static edu.uta.cse.dsc.vm2.string.builder.StringBuilderConstants.JAVA_LANG_STRING_BUILDER;
import static edu.uta.cse.dsc.vm2.string.builder.StringBuilderConstants.STRING_BUILDER_CONTENTS;

import java.util.Iterator;

import org.evosuite.symbolic.expr.IntToStringCast;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealToStringCast;
import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringExpression;

import edu.uta.cse.dsc.vm2.ExpressionFactory;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.Reference;
import edu.uta.cse.dsc.vm2.StringReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;
import edu.uta.cse.dsc.vm2.string.StringFunction;

public abstract class SB_Append extends StringBuilderVirtualFunction {

	private static final String FUNCTION_NAME = "append";
	protected static final String NULL_STRING = "null";
	protected StringExpression strExprToAppend;
	protected StringBuilder conc_str_builder;
	public String conc_str_builder_to_string_pre;

	public SB_Append(SymbolicEnvironment env, String desc) {
		super(env, FUNCTION_NAME, desc);
	}

	public static final class Append_C extends SB_Append {

		public Append_C(SymbolicEnvironment env) {
			super(env, StringFunction.CHAR_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			IntegerExpression integerExpr = bv32(it.next());
			char charValue = (char) ((Long) integerExpr.getConcreteValue())
					.intValue();
			String charToAdd = new String(new char[] { charValue });
			strExprToAppend = new IntToStringCast(integerExpr, charToAdd);

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			conc_str_builder = conc_receiver;

			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

	}

	public static final class Append_S extends SB_Append {

		public Append_S(SymbolicEnvironment env) {
			super(env, StringFunction.STR_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Reference refStrToAppend = ref(it.next());
			symb_receiver = (NonNullReference) ref(it.next());

			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

			if (isNullRef(refStrToAppend)) {
				strExprToAppend = ExpressionFactory
						.buildNewStringConstant(NULL_STRING);
			} else {
				StringReference strRef = (StringReference) refStrToAppend;
				strExprToAppend = strRef.getStringExpression();
			}
		}
	}

	public static final class Append_I extends SB_Append {

		public Append_I(SymbolicEnvironment env) {
			super(env, StringFunction.INT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			IntegerExpression integerExpr = bv32(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			strExprToAppend = new IntToStringCast(integerExpr);
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}
	}

	public static final class Append_L extends SB_Append {

		public Append_L(SymbolicEnvironment env) {
			super(env, StringFunction.LONG_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			IntegerExpression integerExpr = bv64(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			strExprToAppend = new IntToStringCast(integerExpr);
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}
	}

	public static final class Append_B extends SB_Append {

		public Append_B(SymbolicEnvironment env) {
			super(env, StringFunction.BOOLEAN_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			IntegerExpression integerExpr = bv32(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			strExprToAppend = new IntToStringCast(integerExpr);
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}
	}

	public static final class Append_F extends SB_Append {

		public Append_F(SymbolicEnvironment env) {
			super(env, StringFunction.FLOAT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			RealExpression realExpr = fp32(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			strExprToAppend = new RealToStringCast(realExpr);
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}
	}

	public static final class Append_D extends SB_Append {

		public Append_D(SymbolicEnvironment env) {
			super(env, StringFunction.DOUBLE_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			RealExpression realExpr = fp64(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			strExprToAppend = new RealToStringCast(realExpr);
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}
	}

	public static final class Append_O extends SB_Append {

		public Append_O(SymbolicEnvironment env) {
			super(env, StringFunction.OBJECT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			ref(it.next()); // discard symbolic argument
			symb_receiver = (NonNullReference) ref(it.next());
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex,
				Object value) {

			if (value != null && value instanceof StringBuilder) {
				/* TODO: What if value instance of StringBuilder */
				throw new UnsupportedOperationException("Implement Me!");
			} else {
				strExprToAppend = ExpressionFactory
						.buildNewStringConstant(String.valueOf(value));
			}
		}
	}

	@Override
	public void CALL_RESULT(Object res) {
		// get from symbolic heap (or create if null)
		StringExpression strExpr = this.env.heap
				.getField(JAVA_LANG_STRING_BUILDER, STRING_BUILDER_CONTENTS,
						conc_str_builder, symb_receiver,
						conc_str_builder_to_string_pre);

		StringBuilderExpression stringBuilderExpr;
		if (!(strExpr instanceof StringBuilderExpression)) {
			stringBuilderExpr = new StringBuilderExpression(strExpr);
		} else {
			stringBuilderExpr = (StringBuilderExpression) strExpr;
		}

		// append string expression
		stringBuilderExpr.append(strExprToAppend);

		// store to symbolic heap
		env.heap.putField(JAVA_LANG_STRING_BUILDER, STRING_BUILDER_CONTENTS,
				conc_str_builder, symb_receiver, stringBuilderExpr);

		this.symb_receiver = null;
		this.conc_str_builder = null;
		this.strExprToAppend = null;
		this.conc_str_builder_to_string_pre = null;
	}

}
