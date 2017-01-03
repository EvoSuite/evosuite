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
package org.evosuite.symbolic;

import java.util.List;

import org.evosuite.classpath.ResourceList;
import org.evosuite.symbolic.expr.Constraint;

/**
 * <p>
 * BranchCondition class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class BranchCondition {
	private final String className;
	private final String methodName;
	private final int branchIndex;

	private final Constraint<?> constraint;

	private final List<Constraint<?>> supportingConstraints;

	/**
	 * A branch condition is identified by the className, methodName and
	 * branchIndex belonging to the class in the SUT, the target constraint and
	 * all the suporting constraint for that particular branch (zero checks,
	 * etc)
	 * 
	 * @param constraint
	 *            TODO
	 * @param supportingConstraints
	 *            a {@link java.util.Set} object.
	 * @param reachingConstraints
	 *            a {@link java.util.Set} object.
	 * @param ins
	 *            a {@link gov.nasa.jpf.jvm.bytecode.Instruction} object.
	 */
	public BranchCondition(String className, String methodName, int branchIndex, Constraint<?> constraint,
			List<Constraint<?>> supportingConstraints) {

		this.className = ResourceList.getClassNameFromResourcePath(className);
		this.methodName = methodName;
		this.branchIndex = branchIndex;

		this.constraint = constraint;
		this.supportingConstraints = supportingConstraints;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String ret = "";
		for (Constraint<?> c : this.supportingConstraints) {
			ret += " " + c + "\n";
		}

		ret += this.constraint;
		return ret;
	}

	public String getClassName() {
		return className;
	}

	public int getInstructionIndex() {
		return branchIndex;
	}

	public String getFullName() {
		return className + "." + methodName;
	}

	/**
	 * Returns the constraint for actual branch. This constraint has to be
	 * negated to take another path.
	 * 
	 * @return
	 */
	public Constraint<?> getConstraint() {
		return constraint;
	}

	/**
	 * Returns a list of implicit constraints (nullity checks, zero division,
	 * index within bounds, negative size array length, etc.) collected before
	 * the current branch condtion and after the last symbolic branch condition
	 * 
	 * @return
	 */
	public List<Constraint<?>> getSupportingConstraints() {
		return supportingConstraints;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getBranchIndex() {
		return branchIndex;
	}
}
