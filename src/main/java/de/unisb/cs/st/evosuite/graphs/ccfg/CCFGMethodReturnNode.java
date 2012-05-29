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

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodReturnNode extends CCFGNode {

	private BytecodeInstruction callInstruction;
	
	public CCFGMethodReturnNode(BytecodeInstruction callInstruction) {
		this.callInstruction = callInstruction;
	}
	
	public String getMethod() {
		return callInstruction.getMethodName();
	}
	
	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callInstruction == null) ? 0 : callInstruction.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CCFGMethodReturnNode other = (CCFGMethodReturnNode) obj;
		if (callInstruction == null) {
			if (other.callInstruction != null)
				return false;
		} else if (!callInstruction.equals(other.callInstruction))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RETURN from "+callInstruction.toString();
	}
}
