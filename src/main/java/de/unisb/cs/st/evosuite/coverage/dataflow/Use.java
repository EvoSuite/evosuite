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
package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Use inside the class under test.
 * 
 * Uses are created by the DefUseFactory via the DefUsePool.
 * 
 * @author Andre Mis
 */

public class Use extends DefUse {

	Use(BytecodeInstruction wrap, int defuseId, int defId, int useId,
			boolean isParameterUse) {

		super(wrap, defuseId, defId, useId, isParameterUse);
		if (!isUse())
			throw new IllegalArgumentException("Vertex of a use expected");
	}
	
//	@Override
//	public boolean equals(Object o) {
//		if(o==null)
//			return false;
//		if(o==this)
//			return true;
//		if(!(o instanceof Use))
//			return super.equals(o);
//		
//		Use other = (Use)o;
//		
//		return useId == other.useId;
//	}

}
