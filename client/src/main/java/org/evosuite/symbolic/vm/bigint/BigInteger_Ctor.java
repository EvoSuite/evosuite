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
package org.evosuite.symbolic.vm.bigint;

import java.math.BigInteger;

import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class BigInteger_Ctor extends SymbolicFunction {

	public BigInteger_Ctor(SymbolicEnvironment env) {
		super(env, Types.JAVA_MATH_BIG_INTEGER, Types.INIT, Types.STRING_TO_VOID);
	}



	@Override
	public Object executeFunction() {
		String conc_string = (String) this.getConcArgument(0);
		ReferenceConstant str_ref = (ReferenceConstant) this.getSymbArgument(0);

		StringValue symb_string = this.env.heap.getField(
				Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
				conc_string, str_ref, conc_string);

		if (symb_string.containsSymbolicVariable()) {

			ReferenceConstant symb_big_integer = (ReferenceConstant) env
					.topFrame().operandStack.peekRef();

			BigInteger bigInteger = new BigInteger(conc_string);
			long concVal = bigInteger.longValue();

			StringToIntegerCast big_integer_value = new StringToIntegerCast(
					symb_string, concVal);

			env.heap.putField(Types.JAVA_MATH_BIG_INTEGER,
					SymbolicHeap.$BIG_INTEGER_CONTENTS,
					null /* conc_big_integer */, symb_big_integer,
					big_integer_value);
		}

		// return void
		return null;
	}
}
