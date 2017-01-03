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
package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Z_BooleanValue extends SymbolicFunction {

	private static final String BOOLEAN_VALUE = "booleanValue";

	public Z_BooleanValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BOOLEAN, BOOLEAN_VALUE, Types.TO_BOOLEAN);
	}

	@Override
	public Object executeFunction() {
		ReferenceConstant symb_boolean = this.getSymbReceiver();
		Boolean conc_boolean = (Boolean) this.getConcReceiver();
		boolean conc_boolean_value = this.getConcBooleanRetVal();
		IntegerValue symb_boolean_value = env.heap.getField(
				Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				conc_boolean, symb_boolean, conc_boolean_value ? 1 : 0);
		return symb_boolean_value;
	}

}
