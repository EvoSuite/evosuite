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

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Concat extends SymbolicFunction {

	private static final String CONCAT = "concat";

	public Concat(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, CONCAT, Types.STR_TO_STR_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		String conc_left = (String) this.getConcReceiver();
		ReferenceConstant symb_left = this.getSymbReceiver();

		StringValue left_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_left, symb_left, conc_left);

		String conc_right = (String) this.getConcArgument(0);
		ReferenceConstant symb_right = (ReferenceConstant) this
				.getSymbArgument(0);

		StringValue right_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_right, symb_right, conc_right);

		String res = (String) this.getConcRetVal();
		if (res != null) {
			StringBinaryExpression symb_value = new StringBinaryExpression(
					left_expr, Operator.CONCAT, right_expr, (String) res);

			ReferenceConstant symb_receiver = (ReferenceConstant) env.topFrame().operandStack
					.peekRef();
			String conc_receiver = (String) res;
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					symb_value);
		}
		return this.getSymbRetVal();
	}

}
