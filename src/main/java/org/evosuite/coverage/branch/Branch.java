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
package org.evosuite.coverage.branch;

import java.io.Serializable;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.tree.LabelNode;


/**
 * An object of this class corresponds to a Branch inside the class under test.
 * 
 * Branches are created by the CFGMethodAdapter via the BranchPool. Each Branch
 * holds its corresponding BytecodeInstruction from the RawControlFlowGraph and
 * is associated with a unique actualBranchId.
 * 
 * A Branch can either come from a jump instruction, as defined in
 * BytecodeInstruction.isBranch() - which will be called normal branches - or it
 * can be associated with a case: of a switch statement as defined in
 * BytecodeInstruction.isSwitch() - which will be called switch case branches.
 * Only BytecodeInstructions satisfying BytecodeInstruction.isActualbranch() are
 * expected to be associated with a Branch object.
 * 
 * For SWITCH statements each case <key>: block corresponds to a Branch that can
 * be created by constructing a Branch with the SWITCH statement and the <key>
 * as the targetCaseValue. The default: case of switch statement can also be
 * modeled this way - it has the targetCaseValue set to null.
 * 
 * 
 * @author Andre Mis
 */
public class Branch implements Serializable {

	private static final long serialVersionUID = -4732587925060748263L;

	private final int actualBranchId;

	private boolean isSwitch = false;

	// for switch branches this value indicates to which case of the switch this
	// branch belongs. if this value is null and this is in fact a switch this
	// means this branch is the default: case of that switch
	private Integer targetCaseValue = null;

	private LabelNode targetLabel = null;

	private final BytecodeInstruction instruction;

	/** Keep track of branches that were introduced as part of TT */
	private boolean isInstrumented = false;

	/**
	 * Constructor for usual jump instruction Branches, that are not SWITCH
	 * instructions.
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

		this.targetLabel = targetLabel;
		this.targetCaseValue = targetCaseValue;
		this.isSwitch = true;

		if (this.actualBranchId < 1)
			throw new IllegalStateException(
			        "expect branch to have actualBranchId set to positive value");
	}

	public int getActualBranchId() {
		return actualBranchId;
	}

	public boolean isDefaultCase() {
		return isSwitch && targetCaseValue == null;
	}

	public boolean isActualCase() {
		return isSwitch && targetCaseValue != null;
	}

	public Integer getTargetCaseValue() {
		// in order to avoid confusion when targetCaseValue is null
		if (!isSwitch)
			throw new IllegalStateException(
			        "method only allowed to be called on non-switch-Branches");

		return targetCaseValue; // null for default case
	}

	public LabelNode getTargetLabel() {
		if (!isSwitch)
			throw new IllegalStateException("call only allowed on switch instructions");

		return targetLabel;
	}

	public BytecodeInstruction getInstruction() {
		return instruction;
	}

	public String getClassName() {
		return instruction.getClassName();
	}

	public String getMethodName() {
		return instruction.getMethodName();
	}

	public boolean isSwitchCaseBranch() {
		return isSwitch;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actualBranchId;
		result = prime * result
				+ ((instruction == null) ? 0 : instruction.hashCode());
		result = prime * result + (isSwitch ? 1231 : 1237);
		result = prime * result
				+ ((targetCaseValue == null) ? 0 : targetCaseValue.hashCode());
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

	public boolean isInstrumented() {
		return isInstrumented;
	}

	public void setInstrumented(boolean isInstrumented) {
		this.isInstrumented = isInstrumented;
	}
}
