package org.evosuite.symbolic.vm.string.builder;

import java.util.Iterator;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public abstract class SB_Append extends StringBuilderFunction {

	private static final String FUNCTION_NAME = "append";
	protected static final String NULL_STRING = "null";
	protected Expression<?> rightExpr;
	protected StringBuilder conc_str_builder;
	protected String conc_str_builder_to_string_pre;

	public SB_Append(SymbolicEnvironment env, String desc) {
		super(env, FUNCTION_NAME, desc);
	}

	public static final class Append_C extends SB_Append {

		public Append_C(SymbolicEnvironment env) {
			super(env, Types.CHAR_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			rightExpr = bv32(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			conc_str_builder = conc_receiver;

			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_CHAR, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}

	}

	public static final class Append_S extends SB_Append {

		public Append_S(SymbolicEnvironment env) {
			super(env, Types.STR_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			ref(it.next());
			symb_receiver = (NonNullReference) ref(it.next());

			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex,
				Object value) {
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();
			Reference refStrToAppend = ref(it.next());
			if (isNullRef(refStrToAppend)) {
				rightExpr = ExpressionFactory
						.buildNewStringConstant(NULL_STRING);
			} else {
				NonNullReference symb_string = (NonNullReference) refStrToAppend;
				String string = (String) value;
				rightExpr = env.heap
						.getField(Types.JAVA_LANG_STRING,
								SymbolicHeap.$STRING_VALUE, string,
								symb_string, string);

			}
		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_STRING, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}
	}

	public static final class Append_I extends SB_Append {

		public Append_I(SymbolicEnvironment env) {
			super(env, Types.INT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			rightExpr = bv32(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_INTEGER, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}
	}

	public static final class Append_L extends SB_Append {

		public Append_L(SymbolicEnvironment env) {
			super(env, Types.LONG_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			rightExpr = bv64(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_INTEGER, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}
	}

	public static final class Append_B extends SB_Append {

		public Append_B(SymbolicEnvironment env) {
			super(env, Types.BOOLEAN_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			rightExpr = bv32(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_BOOLEAN, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}
	}

	public static final class Append_F extends SB_Append {

		public Append_F(SymbolicEnvironment env) {
			super(env, Types.FLOAT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			rightExpr = fp32(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_REAL, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}
	}

	public static final class Append_D extends SB_Append {

		public Append_D(SymbolicEnvironment env) {
			super(env, Types.DOUBLE_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
			/**
			 * Gather symbolic arguments
			 */
			Iterator<Operand> it = this.env.topFrame().operandStack.iterator();

			// get argument from stack
			rightExpr = fp64(it.next());

			// get StringBuilder reference from stack.
			symb_receiver = (NonNullReference) ref(it.next());

			// create parameters for execution
			conc_str_builder = conc_receiver;
			conc_str_builder_to_string_pre = conc_receiver.toString();

		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_REAL, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}
	}

	public static final class Append_O extends SB_Append {

		public Append_O(SymbolicEnvironment env) {
			super(env, Types.OBJECT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
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
				String valueOf = String.valueOf(value);
				if (valueOf == null) {
					valueOf = NULL_STRING;
				}
				rightExpr = ExpressionFactory.buildNewStringConstant(valueOf);
			}
		}

		@Override
		public void CALL_RESULT(Object res) {

			StringValue leftExpr = this.env.heap.getField(
					StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder_to_string_pre);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_STRING, rightExpr, res.toString());

			// store to symbolic heap
			env.heap.putField(StringBuilderFunction.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, newStrExpr);

			this.symb_receiver = null;
			this.conc_str_builder = null;
			this.rightExpr = null;
			this.conc_str_builder_to_string_pre = null;
		}
	}

}
