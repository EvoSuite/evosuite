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
package org.evosuite.symbolic;

import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.Set;

import org.evosuite.symbolic.expr.Constraint;


/**
 * @author Gordon Fraser
 * 
 */
public class BranchCondition {
	public Instruction ins;

	public final Set<Constraint<?>> reachingConstraints;
	public final Set<Constraint<?>> localConstraints;

	public BranchCondition(Instruction ins, Set<Constraint<?>> reachingConstraints,
	        Set<Constraint<?>> localConstraints) {
		this.ins = ins;
		this.reachingConstraints = reachingConstraints;
		this.localConstraints = localConstraints;
	}

	@Override
	public String toString() {
		String ret = "Branch condition with " + reachingConstraints.size()
		        + " reaching constraints and " + localConstraints.size()
		        + " local constraints: ";
		for (Constraint<?> c : localConstraints) {
			ret += " " + c;
		}

		return ret;
	}
}
