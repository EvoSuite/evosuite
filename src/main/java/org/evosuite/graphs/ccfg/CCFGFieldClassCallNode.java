
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
  
 */
package org.evosuite.graphs.ccfg;

import org.evosuite.graphs.cfg.BytecodeInstruction;
public class CCFGFieldClassCallNode extends CCFGCodeNode {

	private String className;
	private String methodName;
		
//	
	/**
	 * <p>Constructor for CCFGFieldClassCallNode.</p>
	 *
	 * @param code a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public CCFGFieldClassCallNode(BytecodeInstruction code, String className, String methodName) {
		super(code);
		this.className=className;
		this.methodName=methodName;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}
 
}
