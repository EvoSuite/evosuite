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
package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.ControlFlowEdge;

public class CCFGCodeEdge extends CCFGEdge{

	private static final long serialVersionUID = 4200786738903617164L;
	
	private ControlFlowEdge cfgEdge;
	
	public CCFGCodeEdge(ControlFlowEdge cfgEdge) {
		this.cfgEdge = cfgEdge;
	}

	public ControlFlowEdge getCfgEdge() {
		return cfgEdge;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cfgEdge == null) ? 0 : cfgEdge.hashCode());
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
		CCFGCodeEdge other = (CCFGCodeEdge) obj;
		if (cfgEdge == null) {
			if (other.cfgEdge != null)
				return false;
		} else if (!cfgEdge.equals(other.cfgEdge))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return cfgEdge.toString();
	}
}
