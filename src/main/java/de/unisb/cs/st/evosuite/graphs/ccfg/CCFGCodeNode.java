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
package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGCodeNode extends CCFGNode {

	private BytecodeInstruction codeInstruction;
	
	public CCFGCodeNode(BytecodeInstruction code) {
		this.codeInstruction = code;
	}
	
	public String getMethod() {
		return codeInstruction.getMethodName();
	}
	
	public BytecodeInstruction getCodeInstruction() {
		return codeInstruction;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeInstruction == null) ? 0 : codeInstruction.hashCode());
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
		CCFGCodeNode other = (CCFGCodeNode) obj;
		if (codeInstruction == null) {
			if (other.codeInstruction != null)
				return false;
		} else if (!codeInstruction.equals(other.codeInstruction))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if(codeInstruction.isMethodCall())
			return codeInstruction.toString()+" in class "+codeInstruction.getCalledMethodsClass();
		else
			return codeInstruction.toString();
	}
}
