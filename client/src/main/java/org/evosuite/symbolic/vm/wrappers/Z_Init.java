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

public final class Z_Init extends SymbolicFunction {

	public Z_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BOOLEAN, Types.INIT, Types.Z_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		IntegerValue bv32 = this.getSymbIntegerArgument(0);
		ReferenceConstant symb_boolean = this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				null/* conc_boolean */, symb_boolean, bv32);

		// return void
		return null;
	}

}
