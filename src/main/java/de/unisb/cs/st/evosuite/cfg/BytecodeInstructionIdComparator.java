package de.unisb.cs.st.evosuite.cfg;

import java.util.Comparator;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
/**
 * Orders CFGVertices according to their id
 * 
 * This is mainly used to put BytecodeInstructions into a PriorityQueue
 * in ControlFlowGraph.getMaximalInitialDistance()
 */
public class BytecodeInstructionIdComparator implements Comparator<BytecodeInstruction> {

	@Override
	public int compare(BytecodeInstruction arg0, BytecodeInstruction arg1) {

		return new Integer(arg0.getInstructionId()).compareTo(arg1.getInstructionId());
	}
}
