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
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.Types;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.RealBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstant;

public class FSUB extends gov.nasa.jpf.jvm.bytecode.FSUB {
	@SuppressWarnings("unchecked")
	@Override
	public Instruction execute(SystemState ss, KernelState ks, ThreadInfo th) {
		StackFrame sf = th.getTopFrame();
		Expression<Double> v0 = (Expression<Double>) sf.getOperandAttr(0);

		Expression<Double> v1 = (Expression<Double>) sf.getOperandAttr(1);
		if (v0 == null && v1 == null) {
			return super.execute(ss, ks, th);
		}

		if (v0 == null) {
			v0 = new RealConstant(Types.intToFloat(th.peek(0)));
		}
		if (v1 == null) {
			v1 = new RealConstant(Types.intToFloat(th.peek(1)));
		}

		Instruction ret = super.execute(ss, ks, th);
		sf.setOperandAttr(new RealBinaryExpression(v1, Operator.MINUS, v0,
		        (double) Types.intToFloat(sf.peek())));
		return ret;

	}
}
