/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
package de.unisb.cs.st.evosuite.graphs.ccg;

import org.jgrapht.graph.DefaultEdge;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class ClassCallEdge extends DefaultEdge {

	private static final long serialVersionUID = 7136724698608115327L;

	private BytecodeInstruction callInstruction;
	
	public ClassCallEdge(BytecodeInstruction callInstruction) {
		this.callInstruction = callInstruction;
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
		ClassCallEdge other = (ClassCallEdge) obj;
		if (callInstruction == null) {
			if (other.callInstruction != null)
				return false;
		} else if (!callInstruction.equals(other.callInstruction))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return callInstruction.toString();
	}
	
}
