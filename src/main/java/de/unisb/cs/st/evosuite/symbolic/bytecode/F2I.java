/*
 * Copyright (C) 2005,2006 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration (NASA).
 * All Rights Reserved.
 * 
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of EvoSuite, but based on the SymbC extension of JPF
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealToIntegerCast;

public class F2I extends gov.nasa.jpf.jvm.bytecode.F2I {
	@SuppressWarnings("unchecked")
	@Override
	public Instruction execute(SystemState ss, KernelState ks, ThreadInfo th) {
		StackFrame sf = th.getTopFrame();
		Expression<Double> sym_val = (Expression<Double>) sf.getOperandAttr();

		if (sym_val == null) {
			return super.execute(ss, ks, th);
		} else {// symbolic
			Instruction result = super.execute(ss, ks, th);
			sf.setOperandAttr(new RealToIntegerCast(sym_val, new Long(sf.peek())));
			return result;
		}
	}
}
