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
import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;

/**
 * Access jump table by index and jump ..., index ...
 */
public class TABLESWITCH extends gov.nasa.jpf.jvm.bytecode.TABLESWITCH {

	public TABLESWITCH(int defaultTarget, int min, int max) {
		super(defaultTarget, min, max);
	}

	@Override
	public int getByteCode() {
		return 0xAA;
	}

	@Override
	public int getLength() {
		return 13 + 2 * matches.length; // <2do> NOT RIGHT: padding!!
	}

	@SuppressWarnings("unchecked")
	@Override
	public Instruction execute(SystemState ss, KernelState ks, ThreadInfo ti) {
		StackFrame sf = ti.getTopFrame();
		Expression<Long> sym_v = null;

		sym_v = (Expression<Long>) sf.getOperandAttr();

		if (sym_v == null) {
			return super.execute(ss, ks, ti);
		}

		int value = ti.pop();

		lastIdx = DEFAULT;
		Instruction ret = null;

		for (int i = 0, l = matches.length; i < l; i++) {
			if (value == matches[i]) {
				assert (ret == null) : "Two branches found for switch";
				lastIdx = i;
				ret = mi.getInstructionAt(targets[i]);
				PathConstraint.getInstance().addConstraint(new IntegerConstraint(sym_v,
				                                                   Comparator.EQ,
				                                                   new IntegerConstant(
				                                                           matches[i])));
			} else {
				PathConstraint.getInstance().addConstraint(new IntegerConstraint(sym_v,
				                                                   Comparator.NE,
				                                                   new IntegerConstant(
				                                                           matches[i])));
			}
		}
		if (ret == null) {
			ret = mi.getInstructionAt(target);
		}
		return ret;
	}
}
