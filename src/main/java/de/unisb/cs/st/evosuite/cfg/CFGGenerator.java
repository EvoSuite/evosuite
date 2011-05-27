package de.unisb.cs.st.evosuite.cfg;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;

import de.unisb.cs.st.evosuite.mutation.HOM.HOMObserver;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * This classed is used to create the RawControlFlowGraph which can then be used
 * to create the ActualControlFlowGraph
 * 
 * When analyzing a CUT the BytecodeAnalyzer creates an instance of this class
 * for each method contained in it
 * 
 * This class's methods get called in the following order:
 * 
 * - upon constructing, the method at hand is registered via
 * registerMethodNode() which fills the BytecodeInstructionPool with all
 * instructions inside that method
 * 
 * - then registerControlFlowEdge() is called by the BytecodeAnalyzer for each
 * possible transition from one byteCode instruction to another within the
 * current method. In this step the CFGGenerator asks the BytecodeInstructionPool
 * for the previously created instructions and fills up it's RawControlFlowGraph
 * 
 * After those calls the RawControlFlowGraph of the method at hand is complete
 * It should contain a Vertex for each BytecodeInstruction inside the specified
 * method and an edge for every possible transition between these instructions
 * 
 * 
 * @author Andre Mis
 */
public class CFGGenerator {

	private static Logger logger = Logger.getLogger(CFGGenerator.class);

	
	private List<Mutation> mutants;

	private RawControlFlowGraph rawGraph;

	private boolean nodeRegistered = false;
	private MethodNode currentMethod;
	private String className;
	private String methodName;

	
	/**
	 * Initializes this generator to generate the CFG for the method identified
	 * by the given parameters
	 * 
	 * Calls registerMethodNode() which in turn calls
	 * BytecodeInstructionPool.registerMethodNode() leading to the creation of
	 * all BytecodeInstruction instances for the method at hand
	 * 
	 * TODO might not want to give asm.MethodNode to the outside, but rather a
	 * MyMethodNode extended from BytecodeInstruction or something
	 */
	public CFGGenerator(String className, String methodName, MethodNode node,
			List<Mutation> mutants) {
		this.mutants = mutants;
		registerMethodNode(node, className, methodName);
	}

	/**
	 * Adds the RawControlFlowGraph created by this instance to the CFGPool,
	 * computes the resulting ActualControlFlowGraph and also adds it to the
	 * CFGPool
	 */
	public void registerCFGs() {
		// non-minimized cfg needed for defuse-coverage and control
		// dependence calculation
		CFGPool.registerRawCFG(getRawGraph());
		CFGPool.registerActualCFG(getActualGraph());
	}
	
	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	// build up the graph

	private void registerMethodNode(MethodNode currentMethod, String className,
			String methodName) {
		if (nodeRegistered)
			throw new IllegalStateException(
					"registerMethodNode must not be called more than once for each instance of CFGGenerator");
		if (currentMethod == null || methodName == null
				|| className == null)
			throw new IllegalArgumentException("null given");

		this.currentMethod = currentMethod;
		this.className = className;
		this.methodName = methodName;
		
		this.rawGraph = new RawControlFlowGraph(className, methodName);

		BytecodeInstructionPool.registerMethodNode(currentMethod, className,
				methodName);

		nodeRegistered = true;
	}
	
	/**
	 * Internal management of fields and actual building up of the rawGraph
	 */
	public void registerControlFlowEdge(int src, int dst, Frame[] frames) {
		if (!nodeRegistered)
			throw new IllegalStateException(
					"CFGGenrator.registerControlFlowEdge() cannot be called unless registerMethodNode() was called first");
		if (frames == null)
			throw new IllegalArgumentException("null given");
		CFGFrame srcFrame = (CFGFrame) frames[src];
		Frame dstFrame = frames[dst];
		if (srcFrame == null || dstFrame == null)
			throw new IllegalArgumentException(
					"expect expect given frames to know src and dst");

		srcFrame.successors.put(dst, (CFGFrame) dstFrame);

		AbstractInsnNode srcNode = currentMethod.instructions.get(src);
		AbstractInsnNode dstNode = currentMethod.instructions.get(dst);

		// those nodes should have gotten registered by registerMethodNode()
		BytecodeInstruction srcInstruction = BytecodeInstructionPool
				.getInstruction(className, methodName, src, srcNode);
		BytecodeInstruction dstInstruction = BytecodeInstructionPool
				.getInstruction(className, methodName, dst, dstNode);

		if (srcInstruction == null || dstInstruction == null)
			throw new IllegalStateException(
					"expect BytecodeInstructionPool to know the instructions in the method of this edge");

//		if(srcInstruction.isLabel() || dstInstruction.isLabel())
//			System.out.println("LABELEDGE: "+srcInstruction.toString()+" to "+dstInstruction.toString());
		
		rawGraph.addVertex(srcInstruction);
		rawGraph.addVertex(dstInstruction);
		
		if(null == rawGraph.addEdge(srcInstruction, dstInstruction))
			logger.error("internal error while adding edge");
		
		// experiment
		
		// TODO so how exactly should we handle the whole "true/false" stuff.
		// TODO how was previously determined, which edge was the true and which
		// was the false distance?
		// TODO assumption: the first edge is the one that makes the branch jump
		// ("true"?!)
		// TODO implement ControlFlowEdge (again ...) and give it a flag
		// determining whether it's true/false
		
//		Set<BytecodeInstruction> srcChildren = rawGraph.getChildren(srcInstruction);
//		if(srcInstruction.isActualBranch() && srcChildren.size()>1) {
//			logger.info("added second edge for instruction "+srcInstruction.toString());
//			for(BytecodeInstruction srcParent : srcChildren) {
//				logger.info(" to "+srcParent.toString());
//			}
//		}
//		
//		if(srcInstruction.isLabel() || srcInstruction.isLineNumber())
//			logger.info("found edge from "+srcInstruction.toString()+" to "+dstInstruction.toString());
	}

	// retrieve information about the graph

//	public DirectedMultigraph<BytecodeInstruction, DefaultEdge> getMinimalGraph() {
//
//		setMutationIDs();
//		setMutationBranches();
//
//		DirectedMultigraph<BytecodeInstruction, DefaultEdge> min_graph = new DirectedMultigraph<BytecodeInstruction, DefaultEdge>(
//				DefaultEdge.class);
//
//		// Get minimal cfg vertices
//		for (BytecodeInstruction vertex : rawGraph.vertexSet()) {
//			// Add initial nodes and jump targets
//			if (rawGraph.inDegreeOf(vertex) == 0) {
//				min_graph.addVertex(vertex);
//				// Add end nodes
//			} else if (rawGraph.outDegreeOf(vertex) == 0) {
//				min_graph.addVertex(vertex);
//			} else if (vertex.isJump() && !vertex.isGoto()) {
//				min_graph.addVertex(vertex);
//			} else if (vertex.isTableSwitch() || vertex.isLookupSwitch()) {
//				min_graph.addVertex(vertex);
//			} else if (vertex.isMutation()) {
//				min_graph.addVertex(vertex);
//			}
//		}
//		// Get minimal cfg edges
//		for (BytecodeInstruction vertex : min_graph.vertexSet()) {
//			Set<DefaultEdge> handled = new HashSet<DefaultEdge>();
//
//			Queue<DefaultEdge> queue = new LinkedList<DefaultEdge>();
//			queue.addAll(rawGraph.outgoingEdgesOf(vertex));
//			while (!queue.isEmpty()) {
//				DefaultEdge edge = queue.poll();
//				if (handled.contains(edge))
//					continue;
//				handled.add(edge);
//				if (min_graph.containsVertex(rawGraph.getEdgeTarget(edge))) {
//					min_graph.addEdge(vertex, rawGraph.getEdgeTarget(edge));
//				} else {
//					queue.addAll(rawGraph.outgoingEdgesOf(rawGraph
//							.getEdgeTarget(edge)));
//				}
//			}
//		}
//
//		return min_graph;
//	}

	/**
	 * TODO supposed to build the final CFG with BasicBlocks as nodes and stuff!
	 * 
	 * WORK IN PROGRESS
	 * 
	 * soon
	 */
	public ActualControlFlowGraph computeCFG() {

		BytecodeInstructionPool.logInstructionsIn(className,methodName);
		ActualControlFlowGraph cfg = new ActualControlFlowGraph(rawGraph);

		return cfg;
	}

	// mark mutations

	/**
	 * Sets the mutation IDs for each node
	 */
	private void setMutationIDs() {
		for (Mutation m : mutants) {
			if (m.getMethodName().equals(methodName)
					&& m.getClassName().equals(className)) {
				for (BytecodeInstruction v : rawGraph.vertexSet()) {
					if (v.isLineNumber()
							&& v.getLineNumber() == m.getLineNumber()) {
						v.setMutation(m.getId());
						// TODO: What if there are several mutations with the
						// same line number?
					}
				}
			}
		}
	}

	/**
	 * This method sets the mutationBranchAttribute on fields.
	 */
	private void setMutationBranches() {
		for (BytecodeInstruction v : rawGraph.vertexSet()) {
			if (v.isIfNull()) {
				for (ControlFlowEdge e : rawGraph.incomingEdgesOf(v)) {
					BytecodeInstruction v2 = rawGraph.getEdgeSource(e);
					// #TODO the magic string "getProperty" should be in some
					// String variable, near the getProperty function
					// declaration (which I couldn't find (steenbuck))
					if (v2.isMethodCall("getProperty")) {
						v.setMutationBranch();
					}
				}
			} else if (v.isBranch() || v.isTableSwitch() || v.isLookupSwitch()) {
				for (ControlFlowEdge e : rawGraph.incomingEdgesOf(v)) {
					BytecodeInstruction v2 = rawGraph.getEdgeSource(e);
					// #TODO method signature should be used here
					if (v2.isMethodCall(HOMObserver.NAME_OF_TOUCH_METHOD)) {
						logger.debug("Found mutated branch ");
						v.setMutatedBranch();
					} else {
						if (v2.isMethodCall())
							logger.debug("Edgesource: " + v2.getMethodName());
					}
				}
			}
		}
	}

	protected RawControlFlowGraph getRawGraph() {
		return rawGraph;
	}

	protected ActualControlFlowGraph getActualGraph() {

		setMutationIDs();
		setMutationBranches();

		return computeCFG();
	}
}
