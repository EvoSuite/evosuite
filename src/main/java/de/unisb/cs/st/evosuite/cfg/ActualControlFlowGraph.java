package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
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

	private static Logger logger = Logger.getLogger(ActualControlFlowGraph.class);
	
	private DirectedMultigraph<BasicBlock, ControlFlowEdge> graph = new DirectedMultigraph<BasicBlock, ControlFlowEdge>(
			ControlFlowEdge.class);
	
	private String className;
	private String methodName;
	
	private BytecodeInstruction entryPoint;
	private Set<BytecodeInstruction> exitPoints = new HashSet<BytecodeInstruction>();
	private Set<BytecodeInstruction> branches;
	private Set<BytecodeInstruction> branchTargets;
	private Set<BytecodeInstruction> joins;
	private Set<BytecodeInstruction> joinSources;
	
	
	public ActualControlFlowGraph(CFGGenerator generator) {
		if (generator == null)
			throw new IllegalArgumentException("null given");
		
		this.className = generator.getClassName();
		this.methodName = generator.getMethodName();
		
		fillSets(generator);
		
		computeGraph(generator);
	}
	
	private void fillSets(CFGGenerator generator) {

		setEntryPoint(generator.determineEntryPoint());
		setExitPoints(generator.determineExitPoints());
		
		setBranches(generator.determineBranches());
		setBranchTargets(generator);
		setJoins(generator.determineJoins());
		setJoinSources(generator);
	}

	private void computeGraph(CFGGenerator generator) {
		
		computeNodes(generator);
		computeEdges(generator);
		
		checkSanity();
	}
	
	private void computeNodes(CFGGenerator generator) {

		Set<BytecodeInstruction> nodes = getInitiallyKnownInstructions();
		
		logger.debug("Computing Basic Blocks");
		
		for(BytecodeInstruction node : nodes) {
			if(knowsInstruction(node, generator))
				continue;
			
			BasicBlock nodeBlock = generator.determineBasicBlockFor(node);
			addBlock(nodeBlock);
		}
		
		logger.info(graph.vertexSet().size()+" BasicBlocks");
	}
	
	private Set<BytecodeInstruction> getInitiallyKnownInstructions() {
		Set<BytecodeInstruction> r = new HashSet<BytecodeInstruction>();
		r.add(entryPoint);
		r.addAll(exitPoints);
		r.addAll(branches);
		r.addAll(branchTargets);
		r.addAll(joins);
		r.addAll(joinSources);
		
		return r;
	}

	private void addBlock(BasicBlock nodeBlock) {
		if (nodeBlock == null)
			throw new IllegalArgumentException("null given");
		
		logger.debug("Adding block: "+nodeBlock.getName());
		
		if (graph.containsVertex(nodeBlock))
			throw new IllegalArgumentException("block already added before");

		if (!graph.addVertex(nodeBlock))
			throw new IllegalStateException(
					"internal error while addind basic block to CFG");
		
//		for (BasicBlock test : graph.vertexSet()) {
//			logger.debug("experimental self-equals on " + test.getName());
//			if (nodeBlock.equals(test))
//				logger.debug("true");
//			else
//				logger.debug("false");
//			if (!graph.containsVertex(test))
//				throw new IllegalStateException("wtf");
//
//			logger.debug("experimental equals on " + test.getName() + " with "
//					+ nodeBlock.getName());
//			if (test.equals(nodeBlock))
//				logger.debug("true");
//			else
//				logger.debug("false");
//
//			logger.debug("experimental dual-equal");
//			if (nodeBlock.equals(test))
//				logger.debug("true");
//			else
//				logger.debug("false");
//
//		}
		
		if(!graph.containsVertex(nodeBlock))
			throw new IllegalStateException("expect graph to contain the given block on returning of addBlock()");
		
		logger.debug(".. succeeded. nodeCount: "+graph.vertexSet().size());
	}
	
	private void computeEdges(CFGGenerator generator) {

		for (BasicBlock block : graph.vertexSet()) {
			
			computeIncomingEdgesFor(block, generator);
			computeOutgoingEdgesFor(block, generator);
		}
		
		logger.info(graph.edgeSet().size()+" ControlFlowEdges");
	}
	
	private void computeIncomingEdgesFor(BasicBlock block,
			CFGGenerator generator) {

		if (isEntryBlock(block))
			return;

		BytecodeInstruction blockStart = block.getFirstInstruction();
		Set<DefaultEdge> rawIncomings = generator.rawGraph
				.incomingEdgesOf(blockStart);
		for (DefaultEdge rawIncoming : rawIncomings) {
			BytecodeInstruction incomingStart = generator.rawGraph
					.getEdgeSource(rawIncoming);
			addEdge(incomingStart, block);
		}
	}
	
	private void computeOutgoingEdgesFor(BasicBlock block,
			CFGGenerator generator) {
		
		if(isExitBlock(block))
			return;
		
		BytecodeInstruction blockEnd = block.getLastInstruction();
		
		Set<DefaultEdge> rawOutgoings = generator.rawGraph
				.outgoingEdgesOf(blockEnd);
		for (DefaultEdge rawOutgoing : rawOutgoings) {
			BytecodeInstruction outgoingEnd = generator.rawGraph
					.getEdgeTarget(rawOutgoing);
			addEdge(block, outgoingEnd);
		}
		
	}
	
	private void addEdge(BytecodeInstruction src, BasicBlock target) {
		BasicBlock srcBlock = getBlockOf(src);
		if (srcBlock == null)
			throw new IllegalStateException(
					"when adding an edge to a CFG it is expected to know both the src- and the target-instruction");

		addEdge(srcBlock, target);
	}

	private void addEdge(BasicBlock src, BytecodeInstruction target) {
		BasicBlock targetBlock = getBlockOf(target);
		if (targetBlock == null)
			throw new IllegalStateException(
					"when adding an edge to a CFG it is expected to know both the src- and the target-instruction");

		addEdge(src, targetBlock);
	}
	
	private void addEdge(BasicBlock src, BasicBlock target) {
		if (src == null || target == null)
			throw new IllegalArgumentException("null given");
		
		ControlFlowEdge newEdge = new ControlFlowEdge(src, target);
		
		if (graph.containsEdge(newEdge)) {
			logger.debug("edge already contained in CFG");
		} else if (!graph.addEdge(src, target, newEdge))
			throw new IllegalStateException(
					"internal error while adding edge to CFG");
	}
	
	public BasicBlock getBlockOf(BytecodeInstruction instruction) {
		
		for(BasicBlock block : graph.vertexSet())
			if(block.containsInstruction(instruction))
				return block;
		
		logger.debug("unknown instruction "+instruction.toString());
		
		return null;
	}
	
	public boolean knowsInstruction(BytecodeInstruction instruction) {
		for(BasicBlock block : graph.vertexSet())
			if(block.containsInstruction(instruction))
				return true;
		
		return false;
	}	
	
	public boolean knowsInstruction(BytecodeInstruction instruction, CFGGenerator generator) {
		for(BasicBlock block : graph.vertexSet())
			if(block.containsInstruction(instruction)) {
				
				// sanity check
				BasicBlock testBlock = generator.determineBasicBlockFor(instruction);
				if (!testBlock.equals(block)) {
					
					logger.error(instruction.toString());
					logger.error(block.toString());
					logger.error(testBlock.toString());
					throw new IllegalStateException(
							"expect CFGGenerator.determineBasicBlockFor() to return an equal BasicBlock for all instructions coming from a basic block in the CFG");
				}
				return true;
			}
		
		return false;
	}

	public boolean isEntryBlock(BasicBlock block) {
		if (block == null)
			throw new IllegalArgumentException("null given");

		if (block.containsInstruction(entryPoint)) {
			// sanity check
			if (!block.getFirstInstruction().equals(entryPoint))
				throw new IllegalStateException(
						"expect entryPoint of a method to be the first instruction from the entryBlock of that method");
			return true;
		}

		return false;
	}
	
	public boolean isExitBlock(BasicBlock block) {
		if (block == null)
			throw new IllegalArgumentException("null given");

		for(BytecodeInstruction exitPoint : exitPoints)
			if (block.containsInstruction(exitPoint)) {
				// sanity check
				if (!block.getLastInstruction().equals(exitPoint))
					throw new IllegalStateException(
							"expect exitPoints of a method to be the last instruction from an exitBlock of that method");
				return true;
			}

		return false;
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
		
		this.joins = new HashSet<BytecodeInstruction>();

		for (BytecodeInstruction join : joins) {
			if (!belongsToMethod(join))
				throw new IllegalArgumentException(
						"join does not belong to this CFGs method");
			
			this.joins.add(join);
		}
	}
	
	private void setJoinSources(CFGGenerator generator) {
		if(joins==null)
			throw new IllegalStateException("expect joins to be set before setting of joinSources");
		if(generator == null)
			throw new IllegalArgumentException("null given");
		
		this.joinSources = new HashSet<BytecodeInstruction>();
		
		for(BytecodeInstruction join : joins)
			for(DefaultEdge joinEdge : generator.rawGraph.incomingEdgesOf(join))
				joinSources.add(generator.rawGraph.getEdgeSource(joinEdge));
	}
	
	private void setBranches(Set<BytecodeInstruction> branches) {
		if (branches == null)
			throw new IllegalArgumentException("null given");
		
		this.branches = new HashSet<BytecodeInstruction>();
		
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

	private void setBranchTargets(CFGGenerator generator) {
		if(branches==null)
			throw new IllegalStateException("expect branches to be set before setting of branchTargets");
		if(generator == null)
			throw new IllegalArgumentException("null given");
		
		this.branchTargets = new HashSet<BytecodeInstruction>();
		
		for(BytecodeInstruction branch : branches)
			for(DefaultEdge branchEdge : generator.rawGraph.outgoingEdgesOf(branch))
				branchTargets.add(generator.rawGraph.getEdgeTarget(branchEdge));
	}

	public void checkSanity() {

		logger.debug("checking sanity of CFG for "+methodName);
		
		if (graph.vertexSet().isEmpty())
			throw new IllegalStateException(
					"a CFG must contain at least one element");

		for(BytecodeInstruction initInstruction : getInitiallyKnownInstructions()) {
			if(!knowsInstruction(initInstruction))
				throw new IllegalStateException("expect CFG to contain all initially known instructions");
		}
		
		logger.debug(".. all initInstructions contained");
		
		// ensure graph is connected and isEntry and isExitBlock() work as
		// expected
		for (BasicBlock node : graph.vertexSet()) {

			int out = graph.outDegreeOf(node);
			if (!isExitBlock(node) && out == 0)
				throw new IllegalStateException(
						"expect nodes without outgoing edges to be exitBlocks: "+node.toString());

			int in = graph.inDegreeOf(node);
			if (!isEntryBlock(node) && in == 0)
				throw new IllegalStateException(
						"expect nodes without incoming edges to be the entryBlock: "+node.toString());

			if (in + out == 0 && graph.vertexSet().size() != 1)
				throw new IllegalStateException(
						"node with neither child nor parent only allowed if CFG consists of a single block: "+node.toString());

			if (graph.vertexSet().size() == 1
					&& !(isEntryBlock(node) && isExitBlock(node)))
				throw new IllegalStateException(
						"if a CFG consists of a single basic block that block must be both entry and exitBlock: "+node.toString());
		}
		
		logger.debug(".. passed");
	}
}
