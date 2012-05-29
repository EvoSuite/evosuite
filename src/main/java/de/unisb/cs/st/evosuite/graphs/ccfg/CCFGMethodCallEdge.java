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
package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGEdge;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodCallEdge extends CCFGEdge {

	private static final long serialVersionUID = -1638791707105165885L;

	private BytecodeInstruction callInstruction;
	
	private boolean isCallingEdge;
	
	public CCFGMethodCallEdge(BytecodeInstruction callInstruction, boolean isCallingEdge) {
		this.callInstruction = callInstruction;
		this.isCallingEdge = isCallingEdge;
	}
	
	
	/**
	 * Marks whether this is a calling edge or a returning edge 
	 */
	public boolean isCallingEdge() {
		return isCallingEdge;
	}


	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}

	@Override
	public String toString() {
		return (isCallingEdge?"calling ":"returning from ")+callInstruction.getCalledMethod();
	}
}
