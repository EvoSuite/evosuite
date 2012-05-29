/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.ThreadInfo;
import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;

public class IF_ICMPEQ extends gov.nasa.jpf.jvm.bytecode.IF_ICMPEQ {
	public IF_ICMPEQ(int targetPc) {
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
		if (pop1 == pop0) {
			PathConstraint.getInstance().addConstraint(new IntegerConstraint(v1,
			                                                   Comparator.EQ, v0));
			return true;
		} else {
			PathConstraint.getInstance().addConstraint(new IntegerConstraint(v1,
			                                                   Comparator.NE, v0));
			return false;
		}
	}
}
