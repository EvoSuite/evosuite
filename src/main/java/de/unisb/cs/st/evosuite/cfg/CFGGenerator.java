/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

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
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;

import de.unisb.cs.st.evosuite.mutation.HOM.HOMObserver;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * This class generates a CFG from a method's bytecode
 * 
 * @author Gordon Fraser
 * 
 */
public class CFGGenerator extends Analyzer {

	private static Logger logger = Logger.getLogger(CFGGenerator.class);

	MethodNode current_method = null;
	DefaultDirectedGraph<BytecodeInstruction, DefaultEdge> rawGraph = new DefaultDirectedGraph<BytecodeInstruction, DefaultEdge>(
			DefaultEdge.class);
	List<Mutation> mutants;
	String className;
	String methodName;

	// Constructor
	
	public CFGGenerator(List<Mutation> mutants) {
		super(new BasicInterpreter());
		this.mutants = mutants;
	}
	
	// build the CFG

	/**
	 * Called for each non-exceptional cfg edge
	 */
	@Override
	protected void newControlFlowEdge(int src, int dst) {

		registerControlFlowEdge(src,dst);
	}

	/**
	 * We also need to keep track of exceptional edges - they are also branches
	 */
	@Override
	protected boolean newControlFlowExceptionEdge(int src, int dst) {
		// TODO: Make use of information that this is an exception edge?
		registerControlFlowEdge(src, dst);
	
		return true;
	}
	
	/**
	 *  Internal management of fields and actual building up of the rawGraph 
	 */
	private void registerControlFlowEdge(int src, int dst) {
		CFGFrame s = (CFGFrame) getFrames()[src];
		Frame dstFrame = getFrames()[dst];
		if (dstFrame == null)
			logger.error("Control flow edge to null");
		
		s.successors.put(dst, (CFGFrame) dstFrame);
		
		AbstractInsnNode srcNode = current_method.instructions.get(src);
		AbstractInsnNode dstNode = current_method.instructions.get(dst);
		// those nodes should have gotten registered by analyze() 
		BytecodeInstruction srcInstruction = BytecodeInstructionPool.getInstruction(src, srcNode);
		BytecodeInstruction dstInstruction = BytecodeInstructionPool.getInstruction(dst, dstNode);

		// fill raw graph
		rawGraph.addVertex(srcInstruction);
		rawGraph.addVertex(dstInstruction);
		rawGraph.addEdge(srcInstruction, dstInstruction);
	}
	
	@Override
	protected Frame newFrame(int nLocals, int nStack) {
		return new CFGFrame(nLocals, nStack);
	}

	@Override
	protected Frame newFrame(Frame src) {
		return new CFGFrame(src);
	}

	CFGFrame analyze(String owner, String method, MethodNode node)
			throws AnalyzerException {
		
		current_method = node;
		className = owner;
		methodName = method;
		this.analyze(owner, node);
		// TODO should this have happened before analyze() ?
		BytecodeInstructionPool.registerMethodNode(node,className,methodName);
		Frame[] frames = getFrames();
		if (frames.length == 0)
			return null;

		return (CFGFrame) getFrames()[0];
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

	// mutations

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
