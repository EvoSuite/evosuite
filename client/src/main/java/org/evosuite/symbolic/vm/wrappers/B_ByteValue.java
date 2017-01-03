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

public final class B_ByteValue extends SymbolicFunction {

	private static final String BYTE_VALUE = "byteValue";

	public B_ByteValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, BYTE_VALUE, Types.TO_BYTE);
	}

	@Override
	public Object executeFunction() {
		ReferenceConstant symb_byte = this.getSymbReceiver();
		Byte conc_byte = (Byte) this.getConcReceiver();

		int conc_byte_value = this.getConcByteRetVal();

		IntegerValue symb_byte_value = env.heap
				.getField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE,
						conc_byte, symb_byte, conc_byte_value);

		return symb_byte_value;
	}

}
