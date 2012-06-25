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

import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.ThreadInfo;

public class IF_ICMPNE extends gov.nasa.jpf.jvm.bytecode.IF_ICMPNE {
	public IF_ICMPNE(int targetPc) {
		super(targetPc);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean popConditionValue(ThreadInfo ti) {
		StackFrame sf = ti.getTopFrame();
		Expression<Long> v0 = (Expression<Long>) sf.getOperandAttr(0);

		Expression<Long> v1 = (Expression<Long>) sf.getOperandAttr(1);
		if (v0 == null && v1 == null) {
			return super.popConditionValue(ti);
		}

		if (v0 == null) {
			v0 = new IntegerConstant(ti.peek(0));
		}
		if (v1 == null) {
			v1 = new IntegerConstant(ti.peek(1));
		}
		int pop0 = ti.pop();
		int pop1 = ti.pop();
		if (pop1 != pop0) {
			PathConstraint.getInstance().addConstraint(new IntegerConstraint(v1,
			                                                   Comparator.NE, v0));
			return true;
		} else {
			PathConstraint.getInstance().addConstraint(new IntegerConstraint(v1,
			                                                   Comparator.EQ, v0));
			return false;
		}
	}
}
