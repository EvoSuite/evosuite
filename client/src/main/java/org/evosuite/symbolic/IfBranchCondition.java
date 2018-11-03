package org.evosuite.symbolic;

import java.util.List;

import org.evosuite.classpath.ResourceList;
import org.evosuite.symbolic.expr.Constraint;

/**
 * Represents a branch condition originated from the execution of an IF
 * instruction at the bytecode level.
 * 
 * @author jgaleotti
 *
 */
public final class IfBranchCondition extends BranchCondition {

	private final boolean isTrueBranch;

	/**
	 * A branch condition is identified by the className, methodName and branchIndex
	 * belonging to the class in the SUT, the target constraint and all the
	 * suporting constraint for that particular branch (zero checks, etc)
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
	public IfBranchCondition(String className, String methodName, int instructionIndex, Constraint<?> constraint,
			List<Constraint<?>> supportingConstraints, boolean isTrueBranch) {

		super(className, methodName, instructionIndex, constraint, supportingConstraints);
		this.isTrueBranch = isTrueBranch;
	}

	public boolean isTrueBranch() {
		return isTrueBranch;
	}

	public boolean isFalseBranch() {
		return !isTrueBranch;
	}
}
