
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.symbolic.bytecode;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.RealToIntegerCast;

import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;
public class D2L extends gov.nasa.jpf.jvm.bytecode.D2L {

	/** {@inheritDoc} */
	@Override
	public Instruction execute(SystemState ss, KernelState ks, ThreadInfo th) {
		StackFrame sf = th.getTopFrame();
		@SuppressWarnings("unchecked")
		Expression<Double> sym_val = (Expression<Double>) sf.getLongOperandAttr();

		if (sym_val == null) {
			return super.execute(ss, ks, th);
		} else {// symbolic
			Instruction result = super.execute(ss, ks, th);
			sf.setLongOperandAttr(new RealToIntegerCast(sym_val, new Long(sf.longPeek())));
			return result;
		}
	}
}
