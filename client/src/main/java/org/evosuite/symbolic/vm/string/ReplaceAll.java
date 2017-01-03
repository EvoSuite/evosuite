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
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class ReplaceAll extends SymbolicFunction {

	private static final String REPLACE_ALL = "replaceAll";

	public ReplaceAll(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, REPLACE_ALL,
				Types.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// receiver
		ReferenceConstant symb_receiver = this.getSymbReceiver();
		String conc_receiver = (String) this.getConcReceiver();
		// regex argument
		ReferenceExpression symb_regex = this.getSymbArgument(0);
		String conc_regex = (String) this.getConcArgument(0);
		// replacement argument
		ReferenceExpression symb_replacement = this.getSymbArgument(1);
		String conc_replacement = (String) this.getConcArgument(1);
		// return value
		String conc_ret_val = (String) this.getConcRetVal();
		ReferenceExpression symb_ret_val = this.getSymbRetVal();

		StringValue stringReceiverExpr = env.heap.getField(
				Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
				conc_receiver, symb_receiver, conc_receiver);

		if (symb_regex instanceof ReferenceConstant
				&& symb_replacement instanceof ReferenceConstant) {

			ReferenceConstant non_null_symb_regex = (ReferenceConstant) symb_regex;
			StringValue regexExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_regex,
					non_null_symb_regex, conc_regex);

			ReferenceConstant non_null_symb_replacement = (ReferenceConstant) symb_replacement;
			StringValue replacementExpr = env.heap.getField(
					Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
					conc_replacement, non_null_symb_replacement,
					conc_replacement);

			if (symb_ret_val instanceof ReferenceConstant) {
				ReferenceConstant non_null_symb_ret_val = (ReferenceConstant) symb_ret_val;

				StringMultipleExpression symb_value = new StringMultipleExpression(
						stringReceiverExpr, Operator.REPLACEALL, regexExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(replacementExpr)),
						conc_ret_val);

				env.heap.putField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_ret_val,
						non_null_symb_ret_val, symb_value);
			}

		}
		return symb_ret_val;
	}

}
