package de.unisb.cs.st.evosuite.cfg;

import java.util.Set;

import org.jgrapht.graph.DirectedMultigraph;

/**
 * 
 * Supposed to become the new implementation of a control flow graph inside EvoSuite
 * 
 * WORK IN PROGRESS
 * 
 * TODO implement
 * 
 * @author Andre Mis
 */
public class ActualControlFlowGraph {

	private DirectedMultigraph<BasicBlock, ControlFlowEdge> graph = new DirectedMultigraph<BasicBlock, ControlFlowEdge>(
			ControlFlowEdge.class);
	
	private String className;
	private String methodName;
	
	private BytecodeInstruction entryPoint;
	private Set<BytecodeInstruction> exitPoints;
	
	
	public ActualControlFlowGraph(String className, String methodName,
			BytecodeInstruction entryPoint, Set<BytecodeInstruction> exitPoints) {
		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");
		
		this.className = className;
		this.methodName = methodName;
		setEntryPoint(entryPoint);
		setExitPoints(exitPoints);
	}
	
	public boolean belongsToMethod(BytecodeInstruction instruction) {
		if(instruction==null)
			throw new IllegalArgumentException("null given");
		
		if(!className.equals(instruction.getClassName()))
			return false;
		if(!methodName.equals(instruction.getMethodName()))
			return false;
		
		return true;
	}

	private void setEntryPoint(BytecodeInstruction entryPoint) {
		if (entryPoint == null)
			throw new IllegalArgumentException("null given");
		if(!belongsToMethod(entryPoint))
			throw new IllegalArgumentException("entry point does not belong to this CFGs method");
		this.entryPoint=entryPoint;
	}
	
	private void setExitPoints(Set<BytecodeInstruction> exitPoints) {
		if (exitPoints == null)
			throw new IllegalArgumentException("null given");

		
		for (BytecodeInstruction exitPoint : exitPoints) {
			if (!belongsToMethod(exitPoint))
				throw new IllegalArgumentException(
						"exit point does not belong to this CFGs method");
			if (!exitPoint.canReturnFromMethod())
				throw new IllegalArgumentException(
						"unexpected exitPoint byteCode instruction type: "
								+ exitPoint.getOpcodeType());
			
			exitPoints.add(exitPoint);
		}
	}



}
