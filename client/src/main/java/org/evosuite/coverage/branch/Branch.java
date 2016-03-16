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
package org.evosuite.coverage.branch;

import java.io.Serializable;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.tree.LabelNode;

/**
 * An object of this class corresponds to a Branch inside the class under test.
 * 
 * <p>
 * Branches are created by the {@code CFGMethodAdapter} via the {@code BranchPool}. Each Branch
 * holds its corresponding {@code BytecodeInstruction} from the {@code RawControlFlowGraph} and
 * is associated with a unique {@code actualBranchId}.
 * 
 * <p>
 * A Branch can either come from a jump instruction, as defined in
 * {@code BytecodeInstruction.isBranch()} - which will be called normal branches - or it
 * can be associated with a case: of a switch statement as defined in
 * {@code BytecodeInstruction.isSwitch()} - which will be called switch case branches.
 * Only {@code BytecodeInstructions} satisfying {@code BytecodeInstruction.isActualbranch()} are
 * expected to be associated with a {@code Branch} object.
 * 
 * <p>
 * For SWITCH statements each case <key>: block corresponds to a {@code Branch} that can
 * be created by constructing a {@code Branch} with the SWITCH statement and the <key>
 * as the targetCaseValue. The default: case of switch statement can also be
 * modeled this way - it has the {@code targetCaseValue} set to {@code null}.
 * 
 * @author Andre Mis
 */
public class Branch implements Serializable, Comparable<Branch> {

	private static final long serialVersionUID = -4732587925060748263L;

	private final int actualBranchId;

	private boolean isSwitch = false;

	// for switch branches this value indicates to which case of the switch this
	// branch belongs. if this value is null and this is in fact a switch this
	// means this branch is the default: case of that switch
	private Integer targetCaseValue = null;

	private final BytecodeInstruction instruction;

	/** Keep track of branches that were introduced as part of TT */
	private boolean isInstrumented = false;

	/**
	 * Constructor for usual jump instruction Branches, that are not SWITCH
	 * instructions.
	 * 
	 * @param branchInstruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @param actualBranchId
	 *            a int.
	 */
	public Branch(BytecodeInstruction branchInstruction, int actualBranchId) {
		if (!branchInstruction.isBranch())
			throw new IllegalArgumentException("only branch instructions are accepted");

		this.instruction = branchInstruction;
		this.actualBranchId = actualBranchId;

		if (this.actualBranchId < 1)
			throw new IllegalStateException(
			        "expect branch to have actualBranchId set to positive value");
	}

	/**
	 * Constructor for switch case branches
	 * 
	 * @param switchInstruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @param targetCaseValue
	 *            a {@link java.lang.Integer} object.
	 * @param targetLabel
	 *            a {@link org.objectweb.asm.tree.LabelNode} object.
	 * @param actualBranchId
	 *            a int.
	 */
	public Branch(BytecodeInstruction switchInstruction, Integer targetCaseValue,
	        LabelNode targetLabel, int actualBranchId) {
		if (!switchInstruction.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");
		if (targetLabel == null)
			throw new IllegalArgumentException(
			        "expect targetLabel to not be null for case branches");

		this.instruction = switchInstruction;
		this.actualBranchId = actualBranchId;

		// this.targetLabel = targetLabel;
		this.targetCaseValue = targetCaseValue;
		this.isSwitch = true;

		if (this.actualBranchId < 1)
			throw new IllegalStateException(
			        "expect branch to have actualBranchId set to positive value");
	}

	/**
	 * <p>
	 * Getter for the field <code>actualBranchId</code>.
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getActualBranchId() {
		return actualBranchId;
	}

	/**
	 * <p>
	 * isDefaultCase
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isDefaultCase() {
		return isSwitch && targetCaseValue == null;
	}

	/**
	 * <p>
	 * isActualCase
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isActualCase() {
		return isSwitch && targetCaseValue != null;
	}

	/**
	 * <p>
	 * Getter for the field <code>targetCaseValue</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getTargetCaseValue() {
		// in order to avoid confusion when targetCaseValue is null
		if (!isSwitch)
			throw new IllegalStateException(
			        "method only allowed to be called on non-switch-Branches");

		return targetCaseValue; // null for default case
	}

	/**
	 * <p>
	 * Getter for the field <code>instruction</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getInstruction() {
		return instruction;
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return instruction.getClassName();
	}

	/**
	 * <p>
	 * getMethodName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethodName() {
		return instruction.getMethodName();
	}

	/**
	 * <p>
	 * isSwitchCaseBranch
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isSwitchCaseBranch() {
		return isSwitch;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actualBranchId;
		result = prime * result + ((instruction == null) ? 0 : instruction.hashCode());
		result = prime * result + (isSwitch ? 1231 : 1237);
		result = prime * result
		        + ((targetCaseValue == null) ? 0 : targetCaseValue.hashCode());
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
		Branch other = (Branch) obj;
		if (actualBranchId != other.actualBranchId)
			return false;
		if (instruction == null) {
			if (other.instruction != null)
				return false;
		} else if (!instruction.equals(other.instruction))
			return false;
		if (isSwitch != other.isSwitch)
			return false;
		if (targetCaseValue == null) {
			if (other.targetCaseValue != null)
				return false;
		} else if (!targetCaseValue.equals(other.targetCaseValue))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Branch other) {
		return instruction.getLineNumber() - other.getInstruction().getLineNumber();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String r = "I" + instruction.getInstructionId();
		r += " Branch " + getActualBranchId();
		r += " " + instruction.getInstructionType();
		if (isSwitch) {
			r += " L" + instruction.getLineNumber();
			if (targetCaseValue != null)
				r += " Case " + targetCaseValue;
			else
				r += " Default-Case";
		} else
			r += " L" + instruction.getLineNumber();

		return r;
	}

	/**
	 * <p>
	 * isInstrumented
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isInstrumented() {
		return isInstrumented;
	}

	/**
	 * <p>
	 * setInstrumented
	 * </p>
	 * 
	 * @param isInstrumented
	 *            a boolean.
	 */
	public void setInstrumented(boolean isInstrumented) {
		this.isInstrumented = isInstrumented;
	}
}
