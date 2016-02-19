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
 * @author Andre Mis, Gordon Fraser
 */
package org.evosuite.graphs.ccfg;
public class CCFGMethodEntryNode extends CCFGNode {

	private String method;
	private CCFGCodeNode entryInstruction;
	
	/**
	 * <p>Constructor for CCFGMethodEntryNode.</p>
	 *
	 * @param method a {@link java.lang.String} object.
	 * @param entryInstruction a {@link org.evosuite.graphs.ccfg.CCFGCodeNode} object.
	 */
	public CCFGMethodEntryNode(String method, CCFGCodeNode entryInstruction) {
		this.method = method;
		this.entryInstruction = entryInstruction;
	}
	
	/**
	 * <p>Getter for the field <code>method</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * <p>Getter for the field <code>entryInstruction</code>.</p>
	 *
	 * @return a {@link org.evosuite.graphs.ccfg.CCFGCodeNode} object.
	 */
	public CCFGCodeNode getEntryInstruction() {
		return entryInstruction;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((entryInstruction == null) ? 0 : entryInstruction.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		CCFGMethodEntryNode other = (CCFGMethodEntryNode) obj;
		if (entryInstruction == null) {
			if (other.entryInstruction != null)
				return false;
		} else if (!entryInstruction.equals(other.entryInstruction))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Entry: "+method;
	}
}
