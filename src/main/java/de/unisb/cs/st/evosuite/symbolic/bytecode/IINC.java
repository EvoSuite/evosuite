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
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;

public class IINC extends gov.nasa.jpf.jvm.bytecode.IINC {
	public IINC(int localVarIndex, int increment) {
		super(localVarIndex, increment);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Instruction execute(SystemState ss, KernelState ks, ThreadInfo th) {
		Instruction ret = super.execute(ss, ks, th);
		Expression<Long> expr = (Expression<Long>) th.getLocalAttr(index);
		if (expr != null) {
			th.setLocalAttr(index, new IntegerBinaryExpression(expr, Operator.PLUS,
			        new IntegerConstant(increment), new Long(th.getLocalVariable(index))));
		}
		return ret;

	}
}
