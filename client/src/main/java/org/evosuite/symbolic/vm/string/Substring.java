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
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class Substring extends SymbolicFunction {

	private static final String SUBSTRING = "substring";

	public Substring(SymbolicEnvironment env, String desc) {
		super(env, Types.JAVA_LANG_STRING, SUBSTRING, desc);
	}

	public static final class Substring_II extends Substring {
		public Substring_II(SymbolicEnvironment env) {
			super(env, Types.INT_INT_TO_STR_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			ReferenceConstant symb_receiver = this.getSymbReceiver();
			String conc_receiver = (String) this.getConcReceiver();

			IntegerValue beginIndexExpr = this.getSymbIntegerArgument(0);
			IntegerValue endIndexExpr = this.getSymbIntegerArgument(1);

			StringValue str_expr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					conc_receiver);

			ReferenceConstant symb_ret_val = (ReferenceConstant) this
					.getSymbRetVal();
			String conc_ret_val = (String) this.getConcRetVal();

			StringMultipleExpression symb_value = new StringMultipleExpression(
					str_expr, Operator.SUBSTRING, beginIndexExpr,
					new ArrayList<Expression<?>>(Collections
							.singletonList(endIndexExpr)),
					conc_ret_val);

			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_ret_val, symb_ret_val,
					symb_value);

			return this.getSymbRetVal();
		}
	}

	public static final class Substring_I extends Substring {
		public Substring_I(SymbolicEnvironment env) {
			super(env, Types.INT_TO_STR_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			ReferenceConstant symb_receiver = this.getSymbReceiver();
			String conc_receiver = (String) this.getConcReceiver();

			IntegerValue beginIndexExpr = this.getSymbIntegerArgument(0);

			StringValue str_expr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					conc_receiver);

			ReferenceConstant symb_ret_val = (ReferenceConstant) this
					.getSymbRetVal();
			String conc_ret_val = (String) this.getConcRetVal();

			IntegerValue lengthExpr = new StringUnaryToIntegerExpression(
					str_expr, Operator.LENGTH, (long) conc_receiver.length());

			StringMultipleExpression symb_value = new StringMultipleExpression(
					str_expr, Operator.SUBSTRING, beginIndexExpr,
					new ArrayList<Expression<?>>(Collections
							.singletonList(lengthExpr)),
					conc_ret_val);

			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_ret_val, symb_ret_val,
					symb_value);

			return this.getSymbRetVal();
		}
	}

}
