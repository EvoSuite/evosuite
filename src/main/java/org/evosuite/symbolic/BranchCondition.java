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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.symbolic.expr.Constraint;


/**
 * <p>BranchCondition class.</p>
 *
 * @author Gordon Fraser
 */
public class BranchCondition {
	private String className;
	private String methodName;
	private int lineNumber;

	public final Set<Constraint<?>> reachingConstraints;
	public final Set<Constraint<?>> localConstraints;
	private final List<Constraint<?>> listOfLocalConstraints;

	/**
	 * <p>Constructor for BranchCondition.</p>
	 *
	 * @param ins a {@link gov.nasa.jpf.jvm.bytecode.Instruction} object.
	 * @param reachingConstraints a {@link java.util.Set} object.
	 * @param localConstraints a {@link java.util.Set} object.
	 */
	@Deprecated
	public BranchCondition(Instruction ins, Set<Constraint<?>> reachingConstraints,
	        Set<Constraint<?>> localConstraints) {

		this(ins.getMethodInfo().getClassName(), 
		     ins.getMethodInfo().getName(),
		     ins.getInstructionIndex(),
             reachingConstraints,
		     new ArrayList<Constraint<?>>(localConstraints));
	}

	/**
	 * <p>Constructor for BranchCondition.</p>
	 *
	 * @param ins a {@link gov.nasa.jpf.jvm.bytecode.Instruction} object.
	 * @param reachingConstraints a {@link java.util.Set} object.
	 * @param localConstraints a {@link java.util.Set} object.
	 */
	public BranchCondition(String className, String methodName, int lineNumber, Set<Constraint<?>> reachingConstraints,
	        List<Constraint<?>> localConstraints) {
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;

		this.reachingConstraints = reachingConstraints;
		this.localConstraints = new HashSet<Constraint<?>>(localConstraints);
		this.listOfLocalConstraints = localConstraints;
	}
	
	/** {@inheritDoc} */
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

	public String getClassName() {
		return className;
	}

	public int getInstructionIndex() {
		return lineNumber;
	}

	public String getFullName() {
		return className + methodName;
	}
	
	public List<Constraint<?>> listOfLocalConstraints() {
		return listOfLocalConstraints;
	}
}
