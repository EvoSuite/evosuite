package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;

public class ControlDependenceGraph extends
		EvoSuiteGraph<BasicBlock, ControlFlowEdge> {

	private static Logger logger = Logger
			.getLogger(ControlDependenceGraph.class);

	private ActualControlFlowGraph cfg;

	private String className;
	private String methodName;

	public ControlDependenceGraph(ActualControlFlowGraph cfg) {
		super(ControlFlowEdge.class);

		this.cfg = cfg;
		this.className = cfg.getClassName();
		this.methodName = cfg.getMethodName();

		computeGraph();
	}

	public Set<Branch> getControlDependentBranches(BytecodeInstruction ins) {
		
		BasicBlock insBlock = getBlockOf(ins);

		return getControlDependentBranches(insBlock);
	}
	
	public Set<Branch> getControlDependentBranches(BasicBlock insBlock) {
		
		Set<Branch> r = new HashSet<Branch>();

		for (ControlFlowEdge e : incomingEdgesOf(insBlock)) {
			Branch b = e.getBranchInstruction();
			if (b != null)
				r.add(b);
		}

		// sanity check
		if (r.isEmpty()) {
			Set<BasicBlock> insParents = getParents(insBlock);
			if (insParents.size() != 1) {

				for (BasicBlock b : insParents)
					logger.error(b.toString());

				throw new IllegalStateException(
						"expect instruction dependent on root branch to have exactly one parent in it's CDG namely the EntryBlock: "
								+ insBlock.toString());
			}

			for (BasicBlock b : insParents)
				if (!b.isEntryBlock() && !getControlDependentBranches(b).isEmpty())
					throw new IllegalStateException(
							"expect instruction dependent on root branch to have exactly one parent in it's CDG namely the EntryBlock"
									+ insBlock.toString() + methodName);
		}

		return r;
	}

	public Set<Integer> getControlDependentBranchIds(BytecodeInstruction ins) {

		Set<Branch> dependentbranches = getControlDependentBranches(ins);

		Set<Integer> r = new HashSet<Integer>();

		for (Branch b : dependentbranches) {
			if (b == null)
				throw new IllegalStateException(
						"expect set returned by getControlDependentBranches() not to contain null");

			r.add(b.getActualBranchId());
		}
		if (r.isEmpty())
			r.add(-1); // to indicate this is only dependent on root branch,
		// meaning entering the method

		// sanity check
		if (r.contains(-1)) {
			if (r.size() > 1)
				throw new IllegalStateException(
						"expect instruction dependent on root branch to have no other dependencies");
		}

		return r;
	}

	/**
	 * Determines whether the given Branch has to be evaluated to true or to
	 * false in order to reach the given BytecodeInstruction - given the
	 * instruction is directly control dependent on the given Branch
	 * 
	 * In other words this method checks whether there is an incoming
	 * ControlFlowEdge to the given instruction's BasicBlock containing the
	 * given Branch as it's BranchInstruction and if so, that edges
	 * branchExpressionValue is returned. If the given instruction is directly
	 * control dependent on the given branch such a ControlFlowEdge must exist.
	 * Should this assumption be violated an IllegalStateException is thrown
	 * 
	 * If the given instruction is not known to this CDG or not directly control
	 * dependent on the given Branch an IllegalArgumentException is thrown
	 */
	public boolean getBranchExpressionValue(BytecodeInstruction ins, Branch b) {
		
		BasicBlock insBlock = getBlockOf(ins);

		if (b == null)
			return true; // root branch special case

		for (ControlFlowEdge e : incomingEdgesOf(insBlock)) {
			Branch current = e.getBranchInstruction();
			if (current == null)
				throw new IllegalStateException(
						"expect ControlFlowEdges whithin the CDG that don't come from EntryBlock to have branchInstructions set");

			if (current.equals(b))
				return e.getBranchExpressionValue();
		}

		throw new IllegalStateException(
				"expect CDG to contain an incoming edge to the given instructions basic block containing the given branch if isControlDependent() returned true on those two");
	}

	// initialization

	/**
	 * Determines whether the given BytecodeInstruction is directly control
	 * dependent on the given Branch. Meaning within this CDG there is an
	 * incoming ControlFlowEdge to this instructions BasicBlock holding the
	 * given Branch as it's branchInstruction
	 * 
	 * If the given instruction is not known to this CDG an
	 * IllegalArgumentException is thrown
	 */
	public boolean isDirectlyControlDependentOn(BytecodeInstruction ins, Branch b) {
		
		BasicBlock insBlock = getBlockOf(ins);

		boolean isRootDependent = isDependentOnRootBranch(ins);
		if (b == null)
			return isRootDependent;
		if(isRootDependent && b != null)
			return false;

		for (ControlFlowEdge e : incomingEdgesOf(insBlock)) {
			Branch current = e.getBranchInstruction();
			if (current == null)
				throw new IllegalStateException(
						"expect ControlFlowEdges whithin the CDG that don't come from EntryBlock to have branchInstructions set "
								+ insBlock.toString() + methodName);

			if (current.equals(b))
				return true;
		}
		
		
		return false;
	}

	/**
	 * Checks whether the given instruction is only dependent on the root branch
	 * of it's method
	 * 
	 * This is the case if the BasicBlock of the given instruction is either directly
	 * adjacent to the Entry Block or if it's parent is
	 * 
	 * TODO check if there can be a root dependent node in the CDG that has no parent that is adjacent to an entry block
	 */
	public boolean isDependentOnRootBranch(BytecodeInstruction ins) {
		
		BasicBlock insBlock = getBlockOf(ins);
		if(isAdjacentToEntryBlock(insBlock))
			return true;
		
		Set<BasicBlock> parents = getParents(insBlock);
		for(BasicBlock parent : parents)
			if(isAdjacentToEntryBlock(parent))
				return true;
		
		return false;
	}

	/**
	 * Returns true if the given BasicBlock has an incoming edge from this CDG's
	 * EntryBlock
	 */
	public boolean isAdjacentToEntryBlock(BasicBlock insBlock) {
		
		Set<BasicBlock> parents = getParents(insBlock);
		for(BasicBlock parent : parents)
			if(parent.isEntryBlock())
				return true;
		
		return false;
	}
	
	public BasicBlock getBlockOf(BytecodeInstruction ins) {
		if (ins == null)
			throw new IllegalArgumentException("null given");
		if (!cfg.knowsInstruction(ins))
			throw new IllegalArgumentException("unknown instruction");

		BasicBlock insBlock = cfg.getBlockOf(ins);
		if (insBlock == null)
			throw new IllegalStateException(
					"expect CFG to return non-null BasicBlock for instruction it knows");
		
		return insBlock;
	}

	private void computeGraph() {

		createGraphNodes();
		computeControlDependence();
	}

	private void createGraphNodes() {
		// copy CFG nodes
		addVertices(cfg);
		
		for(BasicBlock b : vertexSet())
			if(b.isExitBlock() && !graph.removeVertex(b)) // TODO refactor
				throw new IllegalStateException("internal error building up CDG");
				
				
	}

	private void computeControlDependence() {

		ActualControlFlowGraph rcfg = cfg.computeReverseCFG();
		DominatorTree<BasicBlock> dt = new DominatorTree<BasicBlock>(rcfg);

		for (BasicBlock b : rcfg.vertexSet())
			if (!b.isExitBlock()) {

				logger.debug("DFs for: " + b.getName());
				for (BasicBlock cd : dt.getDominatingFrontiers(b)) {
					ControlFlowEdge orig = cfg.getEdge(cd, b);

					if(orig == null && cd.equals(b)) {
						// in for loops for example there it can happen that
						// cd.equals(b) and that the edge containing the
						// information about the branchExpressionValue was on a
						// cyclic path from cd to itself (b)
						
						// TODO this is just for now! unsafe and probably not even correct!
						for(BasicBlock cdChild : cfg.getChildren(cd))
							for(BasicBlock cdGrandChild : cfg.getChildren(cdChild))
								if(cdGrandChild.equals(b))
									orig = cfg.getEdge(cd, cdChild);
					}
					
					if (!addEdge(cd, b, new ControlFlowEdge(orig)))
						throw new IllegalStateException(
								"internal error while adding CD edge");

					logger.debug("  " + cd.getName());
				}
			}
	}

	@Override
	public String getName() {
		return "CDG" + graphId + "_" + methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}
}
