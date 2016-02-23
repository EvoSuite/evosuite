/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
 *
 * @author Gordon Fraser
 */
package org.evosuite.graphs.ccg;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.jgrapht.graph.DefaultEdge;
public class ClassCallEdge extends DefaultEdge {

	private static final long serialVersionUID = 7136724698608115327L;

	private BytecodeInstruction callInstruction;
	
	/**
	 * <p>Constructor for ClassCallEdge.</p>
	 *
	 * @param callInstruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public ClassCallEdge(BytecodeInstruction callInstruction) {
		this.callInstruction = callInstruction;
	}
	
	/**
	 * <p>Getter for the field <code>callInstruction</code>.</p>
	 *
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callInstruction == null) ? 0 : callInstruction.hashCode());
		return result;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return callInstruction.toString();
	}
	
}
