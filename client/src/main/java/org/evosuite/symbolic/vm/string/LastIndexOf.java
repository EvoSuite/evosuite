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
package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringMultipleToIntegerExpression;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class LastIndexOf extends SymbolicFunction {

	private static final String LAST_INDEX_OF = "lastIndexOf";

	public LastIndexOf(SymbolicEnvironment env, String desc) {
		super(env, Types.JAVA_LANG_STRING, LAST_INDEX_OF, desc);
	}

	public final static class LastIndexOf_C extends LastIndexOf {

		public LastIndexOf_C(SymbolicEnvironment env) {
			super(env, Types.INT_TO_INT_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			String conc_left = (String) this.getConcReceiver();
			ReferenceConstant symb_left = this.getSymbReceiver();

			StringValue left_expr = env.heap
					.getField(Types.JAVA_LANG_STRING,
							SymbolicHeap.$STRING_VALUE, conc_left, symb_left,
							conc_left);

			IntegerValue right_expr = this.getSymbIntegerArgument(0);
			int res = this.getConcIntRetVal();
			if (left_expr.containsSymbolicVariable()
					|| right_expr.containsSymbolicVariable()) {
				StringBinaryToIntegerExpression strBExpr = new StringBinaryToIntegerExpression(
						left_expr, Operator.LASTINDEXOFC, right_expr, (long) res);

				return strBExpr;
			}

			return this.getSymbIntegerRetVal();
		}
	}

	public final static class LastIndexOf_CI extends LastIndexOf {

		public LastIndexOf_CI(SymbolicEnvironment env) {
			super(env, Types.INT_INT_TO_INT_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			String conc_left = (String) this.getConcReceiver();
			ReferenceConstant symb_left = this.getSymbReceiver();

			StringValue left_expr = env.heap
					.getField(Types.JAVA_LANG_STRING,
							SymbolicHeap.$STRING_VALUE, conc_left, symb_left,
							conc_left);

			IntegerValue right_expr = this.getSymbIntegerArgument(0);
			IntegerValue fromIndexExpr = this.getSymbIntegerArgument(1);

			int res = this.getConcIntRetVal();
			if (left_expr.containsSymbolicVariable()
					|| right_expr.containsSymbolicVariable()
					|| fromIndexExpr.containsSymbolicVariable()) {
				StringMultipleToIntegerExpression strBExpr = new StringMultipleToIntegerExpression(
						left_expr, Operator.LASTINDEXOFCI, right_expr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(fromIndexExpr)),
						(long) res);

				return strBExpr;
			}

			return this.getSymbIntegerRetVal();
		}
	}

	public final static class LastIndexOf_S extends LastIndexOf {

		public LastIndexOf_S(SymbolicEnvironment env) {
			super(env, Types.STR_TO_INT_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			String conc_left = (String) this.getConcReceiver();
			ReferenceConstant symb_left = this.getSymbReceiver();

			StringValue left_expr = env.heap
					.getField(Types.JAVA_LANG_STRING,
							SymbolicHeap.$STRING_VALUE, conc_left, symb_left,
							conc_left);

			String conc_right = (String) this.getConcArgument(0);
			ReferenceConstant symb_right = (ReferenceConstant) this
					.getSymbArgument(0);

			StringValue right_expr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_right, symb_right,
					conc_right);

			int res = this.getConcIntRetVal();
			if (left_expr.containsSymbolicVariable()
					|| right_expr.containsSymbolicVariable()) {
				StringBinaryToIntegerExpression strBExpr = new StringBinaryToIntegerExpression(
						left_expr, Operator.LASTINDEXOFS, right_expr, (long) res);

				return strBExpr;
			}

			return this.getSymbIntegerRetVal();
		}

	}

	public final static class LastIndexOf_SI extends LastIndexOf {

		public LastIndexOf_SI(SymbolicEnvironment env) {
			super(env, Types.STR_INT_TO_INT_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			String conc_left = (String) this.getConcReceiver();
			ReferenceConstant symb_left = this.getSymbReceiver();

			StringValue left_expr = env.heap
					.getField(Types.JAVA_LANG_STRING,
							SymbolicHeap.$STRING_VALUE, conc_left, symb_left,
							conc_left);

			String conc_right = (String) this.getConcArgument(0);
			ReferenceExpression symb_right = this.getSymbArgument(0);
			IntegerValue fromIndexExpr = this.getSymbIntegerArgument(1);

			int res = this.getConcIntRetVal();

			if (symb_right instanceof ReferenceConstant) {
				ReferenceConstant symb_non_null_right = (ReferenceConstant) symb_right;
				StringValue right_expr = env.heap.getField(
						Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
						conc_right, symb_non_null_right, conc_right);

				if (left_expr.containsSymbolicVariable()
						|| right_expr.containsSymbolicVariable()
						|| fromIndexExpr.containsSymbolicVariable()) {

					StringMultipleToIntegerExpression strBExpr = new StringMultipleToIntegerExpression(
							left_expr, Operator.LASTINDEXOFSI, right_expr,
							new ArrayList<Expression<?>>(Collections
									.singletonList(fromIndexExpr)),
							(long) res);

					return strBExpr;
				}
			}

			return this.getSymbIntegerRetVal();
		}

	}

}
