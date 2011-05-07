package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;

import de.unisb.cs.st.evosuite.mutation.HOM.HOMObserver;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class CFGGenerator {

	private static Logger logger = Logger.getLogger(BytecodeAnalyzer.class);
	
	List<Mutation> mutants;
	DefaultDirectedGraph<BytecodeInstruction, DefaultEdge> rawGraph = new DefaultDirectedGraph<BytecodeInstruction, DefaultEdge>(
			DefaultEdge.class);
	
	boolean nodeRegistered = false;
	MethodNode currentMethod;
	String className;
	String methodName;
	

	// initialization
	
	public CFGGenerator(String className, String methodName,
			MethodNode node, List<Mutation> mutants) {
		this.mutants = mutants;
		registerMethodNode(node, className, methodName);
	}
	
	private void registerMethodNode(MethodNode currentMethod, String className, String methodName) {
		if(nodeRegistered)
			throw new IllegalStateException("registerMethodNode must not be called more than once for each instance of CFGGenerator");
		if(currentMethod==null || methodName==null || currentMethod == null)
			throw new IllegalArgumentException("null given");
		
		this.currentMethod = currentMethod;
		this.className = className;
		this.methodName = methodName;
		
		BytecodeInstructionPool.registerMethodNode(currentMethod,className,methodName);
		
		nodeRegistered = true;
	}
	
	
	// build up the graph
	
	/**
	 *  Internal management of fields and actual building up of the rawGraph 
	 */
	public void registerControlFlowEdge(int src, int dst, Frame[] frames) {
		if(!nodeRegistered)
			throw new IllegalStateException("CFGGenrator.registerControlFlowEdge() cannot be called unless registerMethodNode() was called first");
		if(frames == null)
			throw new IllegalArgumentException("null given");
		CFGFrame srcFrame = (CFGFrame) frames[src];
		Frame dstFrame = frames[dst];
		if(srcFrame==null || dstFrame == null)
			throw new IllegalArgumentException("expect expect given frames to know src and dst");
			
		srcFrame.successors.put(dst, (CFGFrame) dstFrame);
		
		AbstractInsnNode srcNode = currentMethod.instructions.get(src);
		AbstractInsnNode dstNode = currentMethod.instructions.get(dst);

		// those nodes should have gotten registered by registerMethodNode() 
		BytecodeInstruction srcInstruction = BytecodeInstructionPool
				.getInstruction(className,methodName,src,srcNode);
		BytecodeInstruction dstInstruction = BytecodeInstructionPool
				.getInstruction(className,methodName,dst,dstNode);
		
		if(srcInstruction==null || dstInstruction==null)
			throw new IllegalStateException("expect BytecodeInstructionPool to know the instructions in the method of this edge");

		rawGraph.addVertex(srcInstruction);
		rawGraph.addVertex(dstInstruction);
		rawGraph.addEdge(srcInstruction, dstInstruction);
	}
	
	// retrieve information about the graph
	
	public DefaultDirectedGraph<BytecodeInstruction, DefaultEdge> getCompleteGraph() {
		return rawGraph;
	}

	/**
	 * TODO supposed to build the final CFG with BasicBlocks as nodes and stuff!
	 * 
	 *  soon
	 */
	public DirectedMultigraph<BytecodeInstruction, DefaultEdge> getMinimalGraph() {

		setMutationIDs();
		setMutationBranches();


		DirectedMultigraph<BytecodeInstruction, DefaultEdge> min_graph = new DirectedMultigraph<BytecodeInstruction, DefaultEdge>(
				DefaultEdge.class);

		//Get minimal cfg vertices
		for (BytecodeInstruction vertex : rawGraph.vertexSet()) {
			// Add initial nodes and jump targets
			if (rawGraph.inDegreeOf(vertex) == 0){
				min_graph.addVertex(vertex);
				// Add end nodes
			}else if (rawGraph.outDegreeOf(vertex) == 0){
				min_graph.addVertex(vertex);
			}else if (vertex.isJump() && !vertex.isGoto()) {
				min_graph.addVertex(vertex);
			} else if (vertex.isTableSwitch() || vertex.isLookupSwitch()) {
				min_graph.addVertex(vertex);
			} else if (vertex.isMutation()){
				min_graph.addVertex(vertex);
			}
		}
		//Get minimal cfg edges
		for (BytecodeInstruction vertex : min_graph.vertexSet()) {
			Set<DefaultEdge> handled = new HashSet<DefaultEdge>();

			Queue<DefaultEdge> queue = new LinkedList<DefaultEdge>();
			queue.addAll(rawGraph.outgoingEdgesOf(vertex));
			while (!queue.isEmpty()) {
				DefaultEdge edge = queue.poll();
				if (handled.contains(edge))
					continue;
				handled.add(edge);
				if (min_graph.containsVertex(rawGraph.getEdgeTarget(edge))) {
					min_graph.addEdge(vertex, rawGraph.getEdgeTarget(edge));
				} else {
					queue.addAll(rawGraph.outgoingEdgesOf(rawGraph.getEdgeTarget(edge)));
				}
			}
		}

		return min_graph;
	}
	
	
	// mark mutations

	/**
	 * Sets the mutation IDs for each node
	 */
	private void setMutationIDs(){
		for (Mutation m : mutants) {
			if (m.getMethodName().equals(methodName)
					&& m.getClassName().equals(className)) {
				for (BytecodeInstruction v : rawGraph.vertexSet()) {
					if (v.isLineNumber() && v.getLineNumber() == m.getLineNumber()) {
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
	private void setMutationBranches(){
		for (BytecodeInstruction v : rawGraph.vertexSet()) {
			if (v.isIfNull()) {
				for (DefaultEdge e : rawGraph.incomingEdgesOf(v)) {
					BytecodeInstruction v2 = rawGraph.getEdgeSource(e);
					//#TODO the magic string "getProperty" should be in some String variable, near the getProperty function declaration (which I couldn't find (steenbuck))
					if (v2.isMethodCall("getProperty")) {
						v.setMutationBranch();
					}
				}
			} else if (v.isBranch() || v.isTableSwitch() || v.isLookupSwitch()) {
				for (DefaultEdge e : rawGraph.incomingEdgesOf(v)) {
					BytecodeInstruction v2 = rawGraph.getEdgeSource(e);
					//#TODO method signature should be used here
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
	
}
