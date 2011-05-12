package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
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
	private Set<BytecodeInstruction> exitPoints = new HashSet<BytecodeInstruction>();
	private Set<BytecodeInstruction> branches = new HashSet<BytecodeInstruction>();
	private Set<BytecodeInstruction> joins = new HashSet<BytecodeInstruction>();
	
	
	public ActualControlFlowGraph(CFGGenerator generator) {
		if (generator == null)
			throw new IllegalArgumentException("null given");
		
		this.className = generator.getClassName();
		this.methodName = generator.getMethodName();
		
		setEntryPoint(generator.determineEntryPoint());
		setExitPoints(generator.determineExitPoints());
		setBranches(generator.determineBranches());
		setJoins(generator.determineJoins());
		
		computeGraph(generator);
	}
	
	private void computeGraph(CFGGenerator generator) {
		Set<BytecodeInstruction> nodes = new HashSet<BytecodeInstruction>();
		
		nodes.add(entryPoint);
		nodes.addAll(exitPoints);
		nodes.addAll(branches);
		nodes.addAll(joins);
		
		for(BytecodeInstruction node : nodes) {
			BasicBlock nodeBlock = generator.determineBasicBlockFor(node);
			// TODO stopped here
		}
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
								+ exitPoint.getInstructionType());
			
			this.exitPoints.add(exitPoint);
		}
	}
	
	private void setJoins(Set<BytecodeInstruction> joins) {
		if (joins == null)
			throw new IllegalArgumentException("null given");

		for (BytecodeInstruction join : joins) {
			if (!belongsToMethod(join))
				throw new IllegalArgumentException(
						"join does not belong to this CFGs method");
			
			this.joins.add(join);
		}
	}
	
	private void setBranches(Set<BytecodeInstruction> branches) {
		if (branches == null)
			throw new IllegalArgumentException("null given");
		
		for (BytecodeInstruction branch : branches) {
			if (!belongsToMethod(branch))
				throw new IllegalArgumentException(
						"branch does not belong to this CFGs method");
			if (!branch.isActualBranch())
				throw new IllegalArgumentException(
						"unexpected branch byteCode instruction type: "
								+ branch.getInstructionType());
			
			// TODO the following doesn't work at this point
			//		 because the BranchPool is not yet filled yet
			// BUT one could fill the pool right now and drop further analysis later
			// way cooler, because then filling of the BranchPool is unrelated to
			//  BranchInstrumentation - then again that instrumentation is needed anyways i guess
//			if (!BranchPool.isKnownAsBranch(instruction))
//				throw new IllegalStateException(
//						"expect BranchPool to know all branching instructions: "
//								+ instruction.toString());
			
			this.branches.add(branch);
		}
	}



}
