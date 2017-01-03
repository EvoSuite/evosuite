/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.vm.string.builder;

import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public abstract class StringBuilder_Append extends SymbolicFunction {

	private static final String APPEND = "append";

	protected static final String NULL_STRING = "null";

	protected String conc_str_builder_to_string_pre;

	protected abstract StringValue appendExpression(StringValue leftExpr,
			StringBuilder res);

	@Override
	public final Object executeFunction() {
		// string builder
		StringBuilder conc_str_builder = (StringBuilder) this.getConcReceiver();
		ReferenceConstant symb_str_builder = (ReferenceConstant) this
				.getSymbReceiver();

		// return value
		StringBuilder res = (StringBuilder) this.getConcRetVal();
		ReferenceConstant symb_res = (ReferenceConstant) this.getSymbRetVal();

		StringValue leftExpr = this.env.heap.getField(
				Types.JAVA_LANG_STRING_BUILDER,
				SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
				symb_str_builder, conc_str_builder_to_string_pre);

		// append string expression
		StringValue newStrExpr = appendExpression(leftExpr, res);

		// store to symbolic heap
		env.heap.putField(Types.JAVA_LANG_STRING_BUILDER,
				SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
				symb_res, newStrExpr);

		return symb_res;
	}

	@Override
	public final IntegerConstraint beforeExecuteFunction() {

		StringBuilder conc_str_builder = (StringBuilder) this.getConcReceiver();
		if (conc_str_builder != null) {
			conc_str_builder_to_string_pre = conc_str_builder.toString();
		} else {
			conc_str_builder_to_string_pre = null;
		}
		return null;
	}

	public StringBuilder_Append(SymbolicEnvironment env, String desc) {
		super(env, Types.JAVA_LANG_STRING_BUILDER, APPEND, desc);
	}

	public static final class Append_C extends StringBuilder_Append {

		public Append_C(SymbolicEnvironment env) {
			super(env, Types.CHAR_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {

			IntegerValue symb_char = this.getSymbIntegerArgument(0);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_CHAR, symb_char, res.toString());

			return newStrExpr;
		}

	}

	public static final class Append_S extends StringBuilder_Append {

		public Append_S(SymbolicEnvironment env) {
			super(env, Types.STR_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {

			String conc_str = (String) this.getConcArgument(0);
			ReferenceExpression symb_str = this.getSymbArgument(0);

			StringValue rightExpr;
			if (conc_str == null) {
				rightExpr = ExpressionFactory
						.buildNewStringConstant(NULL_STRING);
			} else {
				ReferenceConstant symb_string = (ReferenceConstant) symb_str;
				rightExpr = env.heap.getField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_str, symb_string,
						conc_str);

			}

			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_STRING, rightExpr, res.toString());

			return newStrExpr;
		}
	}

	public static final class Append_I extends StringBuilder_Append {

		public Append_I(SymbolicEnvironment env) {
			super(env, Types.INT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {

			IntegerValue symb_integer = this.getSymbIntegerArgument(0);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_INTEGER, symb_integer, res.toString());

			return newStrExpr;
		}

	}

	public static final class Append_L extends StringBuilder_Append {

		public Append_L(SymbolicEnvironment env) {
			super(env, Types.LONG_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {

			IntegerValue symb_long = this.getSymbIntegerArgument(0);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_INTEGER, symb_long, res.toString());

			return newStrExpr;
		}

	}

	public static final class Append_B extends StringBuilder_Append {

		public Append_B(SymbolicEnvironment env) {
			super(env, Types.BOOLEAN_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {
			IntegerValue symb_boolean = this.getSymbIntegerArgument(0);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_BOOLEAN, symb_boolean, res.toString());

			return newStrExpr;
		}
	}

	public static final class Append_F extends StringBuilder_Append {

		public Append_F(SymbolicEnvironment env) {
			super(env, Types.FLOAT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {

			RealValue symb_float = this.getSymbRealArgument(0);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_REAL, symb_float, res.toString());

			return newStrExpr;
		}
	}

	public static final class Append_D extends StringBuilder_Append {

		public Append_D(SymbolicEnvironment env) {
			super(env, Types.DOUBLE_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {

			RealValue symb_double = this.getSymbRealArgument(0);

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_REAL, symb_double, res.toString());

			return newStrExpr;
		}
	}

	public static final class Append_O extends StringBuilder_Append {

		public Append_O(SymbolicEnvironment env) {
			super(env, Types.OBJECT_TO_STRBUILDER_DESCRIPTOR);
		}

		@Override
		protected StringValue appendExpression(StringValue leftExpr,
				StringBuilder res) {

			Object conc_object = this.getConcArgument(0);

			StringValue rightExpr;
			if (conc_object != null && conc_object instanceof StringBuilder) {
				/* TODO: What if value instance of StringBuilder */
				throw new UnsupportedOperationException("Implement Me!");
			} else {
				String valueOf = String.valueOf(conc_object);
				if (valueOf == null) {
					valueOf = NULL_STRING;
				}
				rightExpr = ExpressionFactory.buildNewStringConstant(valueOf);
			}

			// append string expression
			StringValue newStrExpr = new StringBinaryExpression(leftExpr,
					Operator.APPEND_STRING, rightExpr, res.toString());

			return newStrExpr;
		}
	}

}
