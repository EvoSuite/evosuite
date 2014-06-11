package org.evosuite.symbolic.vm.string.buffer;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class StringBuffer_Append extends SymbolicFunction {

	private static final String APPEND = "append";

	public StringBuffer_Append(SymbolicEnvironment env, String desc) {
		super(env, Types.JAVA_LANG_STRING_BUFFER, APPEND, desc);
	}

	protected String stringValBeforeExecution;

	@Override
	public void beforeExecuteFunction() {
		StringBuffer conc_str_buffer = (StringBuffer) this.getConcReceiver();
		if (conc_str_buffer != null) {
			stringValBeforeExecution = conc_str_buffer.toString();
		} else {
			stringValBeforeExecution = null;
		}
	}

	public static class StringBufferAppend_B extends StringBuffer_Append {

		public StringBufferAppend_B(SymbolicEnvironment env) {
			super(env, Types.Z_TO_STRING_BUFFER);
		}

		@Override
		public Object executeFunction() {

			NonNullReference symb_str_buffer = this.getSymbReceiver();
			StringBuffer conc_str_buffer = (StringBuffer) this
					.getConcReceiver();

			IntegerValue symb_boolean = this.getSymbIntegerArgument(0);

			StringValue leftExpr = this.env.heap.getField(
					Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, stringValBeforeExecution);

			// append string expression
			String conc_value = conc_str_buffer.toString();
			StringValue append_expr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_BOOLEAN, symb_boolean, conc_value);

			// store to symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, append_expr);

			// append returns the StringBuffer
			return symb_str_buffer;
		}
	}

	public static class StringBufferAppend_C extends StringBuffer_Append {

		public StringBufferAppend_C(SymbolicEnvironment env) {
			super(env, Types.C_TO_STRING_BUFFER);
		}

		@Override
		public Object executeFunction() {

			NonNullReference symb_str_buffer = this.getSymbReceiver();
			StringBuffer conc_str_buffer = (StringBuffer) this
					.getConcReceiver();

			IntegerValue symb_char = this.getSymbIntegerArgument(0);

			StringValue leftExpr = this.env.heap.getField(
					Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, stringValBeforeExecution);

			// append string expression
			String conc_value = conc_str_buffer.toString();
			StringValue append_expr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_CHAR, symb_char, conc_value);

			// store to symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, append_expr);

			// append returns the StringBuffer
			return symb_str_buffer;
		}
	}

	public static class StringBufferAppend_I extends StringBuffer_Append {

		public StringBufferAppend_I(SymbolicEnvironment env) {
			super(env, Types.I_TO_STRING_BUFFER);
		}

		@Override
		public Object executeFunction() {

			NonNullReference symb_str_buffer = this.getSymbReceiver();
			StringBuffer conc_str_buffer = (StringBuffer) this
					.getConcReceiver();

			IntegerValue symb_int = this.getSymbIntegerArgument(0);

			StringValue leftExpr = this.env.heap.getField(
					Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, stringValBeforeExecution);

			// append string expression
			String conc_value = conc_str_buffer.toString();
			StringValue append_expr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_INTEGER, symb_int, conc_value);

			// store to symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, append_expr);

			// append returns the StringBuffer
			return symb_str_buffer;
		}
	}

	public static class StringBufferAppend_L extends StringBuffer_Append {

		public StringBufferAppend_L(SymbolicEnvironment env) {
			super(env, Types.L_TO_STRING_BUFFER);
		}

		@Override
		public Object executeFunction() {

			NonNullReference symb_str_buffer = this.getSymbReceiver();
			StringBuffer conc_str_buffer = (StringBuffer) this
					.getConcReceiver();

			IntegerValue symb_long = this.getSymbIntegerArgument(0);

			StringValue leftExpr = this.env.heap.getField(
					Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, stringValBeforeExecution);

			// append string expression
			String conc_value = conc_str_buffer.toString();
			StringValue append_expr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_INTEGER, symb_long, conc_value);

			// store to symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, append_expr);

			// append returns the StringBuffer
			return symb_str_buffer;
		}
	}

	public static class StringBufferAppend_F extends StringBuffer_Append {

		public StringBufferAppend_F(SymbolicEnvironment env) {
			super(env, Types.F_TO_STRING_BUFFER);
		}

		@Override
		public Object executeFunction() {

			NonNullReference symb_str_buffer = this.getSymbReceiver();
			StringBuffer conc_str_buffer = (StringBuffer) this
					.getConcReceiver();

			RealValue symb_float = this.getSymbRealArgument(0);

			StringValue leftExpr = this.env.heap.getField(
					Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, stringValBeforeExecution);

			// append string expression
			String conc_value = conc_str_buffer.toString();
			StringValue append_expr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_REAL, symb_float, conc_value);

			// store to symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, append_expr);

			// append returns the StringBuffer
			return symb_str_buffer;
		}
	}

	public static class StringBufferAppend_D extends StringBuffer_Append {

		public StringBufferAppend_D(SymbolicEnvironment env) {
			super(env, Types.D_TO_STRING_BUFFER);
		}

		@Override
		public Object executeFunction() {

			NonNullReference symb_str_buffer = this.getSymbReceiver();
			StringBuffer conc_str_buffer = (StringBuffer) this
					.getConcReceiver();

			RealValue symb_double = this.getSymbRealArgument(0);

			StringValue leftExpr = this.env.heap.getField(
					Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, stringValBeforeExecution);

			// append string expression
			String conc_value = conc_str_buffer.toString();
			StringValue append_expr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_REAL, symb_double, conc_value);

			// store to symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, append_expr);

			// append returns the StringBuffer
			return symb_str_buffer;
		}
	}

	public static class StringBufferAppend_STR extends StringBuffer_Append {

		private static final String NULL_STRING = "null";

		public StringBufferAppend_STR(SymbolicEnvironment env) {
			super(env, Types.STR_TO_STRING_BUFFER);
		}

		@Override
		public Object executeFunction() {

			NonNullReference symb_str_buffer = this.getSymbReceiver();
			StringBuffer conc_str_buffer = (StringBuffer) this
					.getConcReceiver();

			StringValue leftExpr = this.env.heap.getField(
					Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, stringValBeforeExecution);

			Reference symb_str = this.getSymbArgument(0);
			String conc_str = (String) this.getConcArgument(0);

			StringValue symb_str_value;
			if (conc_str == null) {
				symb_str_value = ExpressionFactory
						.buildNewStringConstant(NULL_STRING);
			} else {
				NonNullReference symb_non_null_str = (NonNullReference) symb_str;
				symb_str_value = env.heap.getField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_str,
						symb_non_null_str, conc_str);
			}

			// append string expression
			String conc_value = conc_str_buffer.toString();
			StringValue append_expr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_STRING, symb_str_value, conc_value);

			// store to symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
					symb_str_buffer, append_expr);

			// append returns the StringBuffer
			return symb_str_buffer;
		}
	}
}
