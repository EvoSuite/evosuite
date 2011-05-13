package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * 
 * Supposed to become the new implementation of a control flow graph inside
 * EvoSuite
 * 
 * The "actual" CFG does not contain single cfg.BytecodeInstructions as nodes,
 * but contains cfg.BasicBlocks - look at that class for more information
 * 
 * Simply put this is a minimized version of the complete/raw CFG the
 * cfg.BytecodeAnalyzer and cfg.CFGGenerator produce - which holds a node for
 * every BytecodeInstruction
 * 
 * Out of the above described raw CFG the following "pin-points" are extracted:
 * - the entryNode (first instruction in the method) - all exitNodes (outDegree
 * 0) - all branches (outDegree>1) - and in a subsequent step all targets of all
 * branches - all joins (inDegree>1) - and in a subsequent step all sources of
 * all joins
 * 
 * All those "pin-points" are put into a big set (some of the above categories
 * may overlap) and for all those single BytecodeInstrucions their corresponding
 * BasicBlock is computed and added to this CFGs vertexSet After that the raw
 * CFG is asked for the parents of the first instruction of each BasicBlock and
 * the parents of that blocks last instruction and the edges to their
 * corresponding BasicBlocks are added to this CFG TODO: verify that this method
 * works :D
 * 
 * 
 * cfg.EvoSuiteGraph is used as the underlying data structure holding the
 * graphical representation of the CFG
 * 
 * WORK IN PROGRESS
 * 
 * TODO implement
 * 
 * @author Andre Mis
 */
public class ActualControlFlowGraph extends EvoSuiteGraph<BasicBlock,ControlFlowEdge> {

	private static Logger logger = Logger.getLogger(ActualControlFlowGraph.class);
	
	private String className;
	private String methodName;
	
	private BytecodeInstruction entryPoint;
	private Set<BytecodeInstruction> exitPoints = new HashSet<BytecodeInstruction>();
	private Set<BytecodeInstruction> branches;
	private Set<BytecodeInstruction> branchTargets;
	private Set<BytecodeInstruction> joins;
	private Set<BytecodeInstruction> joinSources;
	
	
	public ActualControlFlowGraph(CFGGenerator generator) {
		super(new DirectedMultigraph<BasicBlock,ControlFlowEdge>(ControlFlowEdge.class));
		if (generator == null)
			throw new IllegalArgumentException("null given");
		
		this.className = generator.getClassName();
		this.methodName = generator.getMethodName();
		
		fillSets(generator);
		
		computeGraph(generator);
	}
	
	// set field values
	
	private void fillSets(CFGGenerator generator) {

		setEntryPoint(generator.determineEntryPoint());
		setExitPoints(generator.determineExitPoints());
		
		setBranches(generator.determineBranches());
		setBranchTargets(generator);
		setJoins(generator.determineJoins());
		setJoinSources(generator);
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
	
	// computing the actual CFG
	

	// compute actual CFG from maximal one given by CFGGenerator
	
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
		
		logger.info(getNodeCount()+" BasicBlocks");
	}
	
	
	private void addBlock(BasicBlock nodeBlock) {
		if (nodeBlock == null)
			throw new IllegalArgumentException("null given");
		
		logger.debug("Adding block: "+nodeBlock.getName());
		
		if (containsBlock(nodeBlock))
			throw new IllegalArgumentException("block already added before");

		if (!addVertex(nodeBlock))
			throw new IllegalStateException(
					"internal error while addind basic block to CFG");
		
//		for (BasicBlock test : graph.vertexSet()) {
//			logger.debug("experimental self-equals on " + test.getName());
//			if (nodeBlock.equals(test))
//				logger.debug("true");
//			else
//				logger.debug("false");
//			if (!containsBlock(test))
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
		
		if(!containsBlock(nodeBlock))
			throw new IllegalStateException("expect graph to contain the given block on returning of addBlock()");
		
		logger.debug(".. succeeded. nodeCount: "+getNodeCount());
	}
	
	
	private void computeEdges(CFGGenerator generator) {

		for (BasicBlock block : vertexSet()) {
			
			computeIncomingEdgesFor(block, generator);
			computeOutgoingEdgesFor(block, generator);
		}
		
		logger.info(getEdgeCount()+" ControlFlowEdges");
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
		
		if (containsEdge(newEdge)) {
			logger.debug("edge already contained in CFG");
		} else if (!addEdge(src, target, newEdge))
			throw new IllegalStateException(
					"internal error while adding edge to CFG");
	}
	
	// retrieve information about the CFG
	
	
	public BasicBlock getBlockOf(BytecodeInstruction instruction) {
		
		for(BasicBlock block : vertexSet())
			if(block.containsInstruction(instruction))
				return block;
		
		logger.debug("unknown instruction "+instruction.toString());
		
		return null;
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
	
	
	// retrieve information about the CFG
	
	public boolean knowsInstruction(BytecodeInstruction instruction) {
		for(BasicBlock block : vertexSet())
			if(block.containsInstruction(instruction))
				return true;
		
		return false;
	}	
	
	public boolean knowsInstruction(BytecodeInstruction instruction, CFGGenerator generator) {
		for(BasicBlock block : vertexSet())
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
	
	// sanity check
	

	// sanity checks
	
	public void checkSanity() {

		logger.debug("checking sanity of CFG for " + methodName);

		if (isEmpty())
			throw new IllegalStateException(
					"a CFG must contain at least one element");

		for (BytecodeInstruction initInstruction : getInitiallyKnownInstructions()) {
			if (!knowsInstruction(initInstruction))
				throw new IllegalStateException(
						"expect CFG to contain all initially known instructions");
		}

		logger.debug(".. all initInstructions contained");

		checkEdgeSanity();

		logger.debug(".. CFG sanity ensured");
	}
	
	void checkNodeSanity() {
		// ensure graph is connected and isEntry and isExitBlock() work as
		// expected
		for (BasicBlock node : vertexSet()) {

			checkEntryExitPointConstraint(node);

			checkSingleCFGNodeConstraint(node);

			checkNodeMinimalityConstraint(node);
		}
		logger.debug("..all node constraints ensured");
	}

	void checkEntryExitPointConstraint(BasicBlock node) {
		// exit point constraint
		int out = getChildCount(node);
		if (!isExitBlock(node) && out == 0)
			throw new IllegalStateException(
					"expect nodes without outgoing edges to be exitBlocks: "
							+ node.toString());
		// entry point constraint
		int in = getParentCount(node);
		if (!isEntryBlock(node) && in == 0)
			throw new IllegalStateException(
					"expect nodes without incoming edges to be the entryBlock: "
							+ node.toString());
	}

	void checkSingleCFGNodeConstraint(BasicBlock node) {
		int in = getParentCount(node);
		int out = getChildCount(node);
		if (in + out == 0 && getNodeCount() != 1)
			throw new IllegalStateException(
					"node with neither child nor parent only allowed if CFG consists of a single block: "
							+ node.toString());

		if (getNodeCount() == 1 && !(isEntryBlock(node) && isExitBlock(node)))
			throw new IllegalStateException(
					"if a CFG consists of a single basic block that block must be both entry and exitBlock: "
							+ node.toString());
	}

	void checkNodeMinimalityConstraint(BasicBlock node) {

		if (hasNPartentsMChildren(node, 1, 1)) {
			for (BasicBlock child : getChildren(node))
				if (hasNPartentsMChildren(child, 1, 1))
					throw new IllegalStateException(
							"whenever a node has exactly one child and one parent, it is expected that the same is true for either of those");

			for (BasicBlock parent : getParents(node))
				if (hasNPartentsMChildren(parent, 1, 1))
					throw new IllegalStateException(
							"whenever a node has exactly one child and one parent, it is expected that the same is true for either of those");
		}
	}

	void checkEdgeSanity() {

		for(ControlFlowEdge e : edgeSet()) {
			// check edge-references
			if(!e.getSource().equals(getEdgeSource(e)))
				throw new IllegalStateException("source reference of control flow edge not set properly: "+e.toString());
			if(!e.getTarget().equals(getEdgeTarget(e)))
				throw new IllegalStateException("target reference of control flow edge not set properly: "+e.toString());
		}
		logger.debug(".. all edge references sane");
	}


}
