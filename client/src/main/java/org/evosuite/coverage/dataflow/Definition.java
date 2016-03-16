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
package org.evosuite.coverage.dataflow;

import org.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Definition inside the class under
 * test.
 * 
 * Definitions are created by the DefUseFactory via the DefUsePool.
 * 
 * @author Andre Mis
 */
public class Definition extends DefUse {

	private static final long serialVersionUID = 1141846324999759006L;

	Definition(BytecodeInstruction wrap) {
		super(wrap);
		if (!DefUsePool.isKnownAsDefinition(wrap))
			throw new IllegalArgumentException(
			        "Instruction must be known as a Definition by the DefUsePool");
	}

	/**
	 * Determines whether this Definition can be an active definition for the
	 * given instruction.
	 * 
	 * This is the case if instruction constitutes a Use for the same variable
	 * as this Definition
	 * 
	 * Not to be confused with DefUse.canBecomeActiveDefinitionFor, which is
	 * sort of the dual to this method
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean canBeActiveFor(BytecodeInstruction instruction) {
		if (!instruction.isUse())
			return false;
		//		if(!DefUsePool.isKnownAsUse(instruction))
		//			return false;

		//		Use use = DefUseFactory.makeUse(instruction);
		return sharesVariableWith(instruction);
	}
}
