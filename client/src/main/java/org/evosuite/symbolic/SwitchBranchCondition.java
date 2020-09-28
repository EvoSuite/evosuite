package org.evosuite.symbolic;

import java.util.List;

import org.evosuite.symbolic.expr.Constraint;

/**
 * Represents a branch condition created from the execution of a
 * <code>switch</code> instruction
 * 
 * @author jgaleotti
 *
 */
public final class SwitchBranchCondition extends BranchCondition {

	/**
	 * Indicates if the current <code>switch</code> branch condition is the default
	 * goal or not (i.e. no specific goal)
	 */
	private final boolean isDefaultGoal;

	/**
	 * If the current switch branch condition is *not* a default goal, this field
	 * contains the goal value
	 */
	private final int goalValue;

	public SwitchBranchCondition(String className, String methodName, int instructionIndex, Constraint<?> constraint,
			List<Constraint<?>> supportingConstraints) {
		super(className, methodName, instructionIndex, constraint, supportingConstraints);
		this.goalValue = 0;
		this.isDefaultGoal = true;
	}

	public SwitchBranchCondition(String className, String methodName, int instructionIndex, Constraint<?> constraint,
			List<Constraint<?>> supportingConstraints, int goalValue) {
		super(className, methodName, instructionIndex, constraint, supportingConstraints);
		this.goalValue = goalValue;
		this.isDefaultGoal = false;
	}

	/**
	 * Indicates if the current switch branch condition is the default branch
	 * condition
	 * 
	 * @return
	 */
	public boolean isDefaultGoal() {
		return isDefaultGoal;
	}

	/**
	 * Indicates if the goal of the switch branch condition. The switch branch
	 * condition needs to be a non-default switch branch condition.
	 * 
	 * @return
	 * @throws IllegalStateException
	 */
	public int getGoalValue() throws IllegalStateException {
		if (!isDefaultGoal()) {
			return goalValue;
		} else {
			throw new IllegalStateException("cannot request goal to a default goal branch condition");
		}
	}

}
