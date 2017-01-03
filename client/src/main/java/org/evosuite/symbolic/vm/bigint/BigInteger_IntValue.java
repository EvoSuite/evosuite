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

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class BigInteger_IntValue extends SymbolicFunction {

	private static final String INT_VALUE = "intValue";

	public BigInteger_IntValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_MATH_BIG_INTEGER, INT_VALUE, Types.TO_INT);
	}


	@Override
	public Object executeFunction() {
		BigInteger conc_big_integer = (BigInteger) this.getConcReceiver();
		ReferenceConstant symb_big_integer = this.getSymbReceiver();
		int res =  this.getConcIntRetVal();
		
		IntegerValue integer_expr = this.env.heap.getField(
				Types.JAVA_MATH_BIG_INTEGER,
				SymbolicHeap.$BIG_INTEGER_CONTENTS, conc_big_integer,
				symb_big_integer, res);
		
		return integer_expr;
	}

}
