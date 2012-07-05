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
 */
package org.evosuite.symbolic.bytecode;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerConstraint;

import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

/**
 * Access jump table by index and jump ..., index ...
 *
 * @author Gordon Fraser
 */
public class TABLESWITCH extends gov.nasa.jpf.jvm.bytecode.TABLESWITCH {

	/**
	 * <p>Constructor for TABLESWITCH.</p>
	 *
	 * @param defaultTarget a int.
	 * @param min a int.
	 * @param max a int.
	 */
	public TABLESWITCH(int defaultTarget, int min, int max) {
		super(defaultTarget, min, max);
	}

	/** {@inheritDoc} */
	@Override
	public int getByteCode() {
		return 0xAA;
	}

	/** {@inheritDoc} */
	@Override
	public int getLength() {
		return 13 + 2 * matches.length; // <2do> NOT RIGHT: padding!!
	}

	/** {@inheritDoc} */
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
