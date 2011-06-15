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

import gov.nasa.jpf.jvm.ThreadInfo;
import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;

public class IFGT extends gov.nasa.jpf.jvm.bytecode.IFGT {
	public IFGT(int targetPc) {
		super(targetPc);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean popConditionValue(ThreadInfo ti) {
		Expression<Long> sym = (Expression<Long>) ti.getOperandAttr();
		if (sym == null) {
			return super.popConditionValue(ti);
		}
		int pop = ti.pop();
		if (pop > 0) {
			PathConstraint.getInstance().addConstraint(
					new IntegerConstraint(sym, Comparator.GT, new IntegerConstant(0)));
			return true;
		} else {
			PathConstraint.getInstance().addConstraint(
					new IntegerConstraint(sym, Comparator.LE, new IntegerConstant(0)));
			return false;
		}
	}

}
