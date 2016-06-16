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
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class StartsWith extends SymbolicFunction {

	private static final String STARTS_WITH = "startsWith";

	public StartsWith(SymbolicEnvironment env, String desc) {
		super(env, Types.JAVA_LANG_STRING, STARTS_WITH, desc);
	}

	public static final class StartsWith_S extends StartsWith {

		public StartsWith_S(SymbolicEnvironment env) {
			super(env, Types.STR_TO_BOOL_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			// receiver
			ReferenceConstant symb_receiver = this.getSymbReceiver();
			String conc_receiver = (String) this.getConcReceiver();
			// prefix argument
			ReferenceExpression symb_prefix = this.getSymbArgument(0);
			String conc_prefix = (String) this.getConcArgument(0);

			// return value
			boolean res = this.getConcBooleanRetVal();

			StringValue stringReceiverExpr = env.heap.getField(
					Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
					conc_receiver, symb_receiver, conc_receiver);

			if (symb_prefix instanceof ReferenceConstant) {
				ReferenceConstant non_null_symb_prefix = (ReferenceConstant) symb_prefix;

				StringValue prefixExpr = env.heap.getField(
						Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
						conc_prefix, non_null_symb_prefix, conc_prefix);

				if (stringReceiverExpr.containsSymbolicVariable()
						|| prefixExpr.containsSymbolicVariable()) {
					int conV = res ? 1 : 0;

					StringMultipleComparison strTExpr = new StringMultipleComparison(
							stringReceiverExpr, Operator.STARTSWITH,
							prefixExpr, new ArrayList<Expression<?>>(
									Collections
											.singletonList(new IntegerConstant(
													0))), (long) conV);

					return strTExpr;
				}

			}
			return this.getSymbIntegerRetVal();
		}

	}

	public static final class StartsWith_SI extends StartsWith {

		public StartsWith_SI(SymbolicEnvironment env) {
			super(env, Types.STR_INT_TO_BOOL_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			// receiver
			ReferenceConstant symb_receiver = this.getSymbReceiver();
			String conc_receiver = (String) this.getConcReceiver();
			// prefix argument
			ReferenceExpression symb_prefix = this.getSymbArgument(0);
			String conc_prefix = (String) this.getConcArgument(0);
			// toffset argument
			IntegerValue offsetExpr = this.getSymbIntegerArgument(1);

			// return value
			boolean res = this.getConcBooleanRetVal();

			StringValue stringReceiverExpr = env.heap.getField(
					Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
					conc_receiver, symb_receiver, conc_receiver);

			if (symb_prefix instanceof ReferenceConstant) {
				ReferenceConstant non_null_symb_prefix = (ReferenceConstant) symb_prefix;

				StringValue prefixExpr = env.heap.getField(
						Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
						conc_prefix, non_null_symb_prefix, conc_prefix);

				if (stringReceiverExpr.containsSymbolicVariable()
						|| prefixExpr.containsSymbolicVariable()
						|| offsetExpr.containsSymbolicVariable()) {
					int conV = res ? 1 : 0;

					StringMultipleComparison strTExpr = new StringMultipleComparison(
							stringReceiverExpr, Operator.STARTSWITH,
							prefixExpr, new ArrayList<Expression<?>>(
									Collections.singletonList(offsetExpr)),
							(long) conV);

					return strTExpr;
				}

			}
			return this.getSymbIntegerRetVal();
		}
	}
}
