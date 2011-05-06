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
	DefaultDirectedGraph<BytecodeInstruction, DefaultEdge> graph = new DefaultDirectedGraph<BytecodeInstruction, DefaultEdge>(
			DefaultEdge.class);
	List<Mutation> mutants;
	String className;
	String methodName;

	public CFGGenerator(List<Mutation> mutants) {
		super(new BasicInterpreter());
		this.mutants = mutants;
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
		Frame[] frames = getFrames();
		if (frames.length == 0)
			return null;

		return (CFGFrame) getFrames()[0];
	}

	public DefaultDirectedGraph<BytecodeInstruction, DefaultEdge> getCompleteGraph() {
		return graph;
	}

	/**
	 * Sets the mutation IDs for each node
	 */
	private void setMutationIDs(){
		for (Mutation m : mutants) {
			if (m.getMethodName().equals(methodName)
					&& m.getClassName().equals(className)) {
				for (BytecodeInstruction v : graph.vertexSet()) {
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
		for (BytecodeInstruction v : graph.vertexSet()) {
			if (v.isIfNull()) {
				for (DefaultEdge e : graph.incomingEdgesOf(v)) {
					BytecodeInstruction v2 = graph.getEdgeSource(e);
					//#TODO the magic string "getProperty" should be in some String variable, near the getProperty function declaration (which I couldn't find (steenbuck))
					if (v2.isMethodCall("getProperty")) {
						v.setMutationBranch();
					}
				}
			} else if (v.isBranch() || v.isTableSwitch() || v.isLookupSwitch()) {
				for (DefaultEdge e : graph.incomingEdgesOf(v)) {
					BytecodeInstruction v2 = graph.getEdgeSource(e);
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

	public DirectedMultigraph<BytecodeInstruction, DefaultEdge> getMinimalGraph() {

		setMutationIDs();

		setMutationBranches();


		DirectedMultigraph<BytecodeInstruction, DefaultEdge> min_graph = new DirectedMultigraph<BytecodeInstruction, DefaultEdge>(
				DefaultEdge.class);

		//Get minimal cfg vertices
		for (BytecodeInstruction vertex : graph.vertexSet()) {
			// Add initial nodes and jump targets
			if (graph.inDegreeOf(vertex) == 0){
				min_graph.addVertex(vertex);
				// Add end nodes
			}else if (graph.outDegreeOf(vertex) == 0){
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
			queue.addAll(graph.outgoingEdgesOf(vertex));
			while (!queue.isEmpty()) {
				DefaultEdge edge = queue.poll();
				if (handled.contains(edge))
					continue;
				handled.add(edge);
				if (min_graph.containsVertex(graph.getEdgeTarget(edge))) {
					min_graph.addEdge(vertex, graph.getEdgeTarget(edge));
				} else {
					queue.addAll(graph.outgoingEdgesOf(graph.getEdgeTarget(edge)));
				}
			}
		}

		return min_graph;
	}

	/**
	 * Called for each non-exceptional cfg edge
	 */
	@Override
	protected void newControlFlowEdge(int src, int dst) {
		CFGFrame s = (CFGFrame) getFrames()[src];
		s.successors.put(dst, (CFGFrame) getFrames()[dst]);
		if (getFrames()[dst] == null) {
			System.out.println("Control flow edge to null");
			logger.error("Control flow edge to null");
		}

		BytecodeInstruction v1 = new BytecodeInstruction(src, current_method.instructions.get(src));
		BytecodeInstruction v2 = new BytecodeInstruction(dst, current_method.instructions.get(dst));

		graph.addVertex(v1);
		graph.addVertex(v2);
		graph.addEdge(v1, v2);
	}

	/**
	 * We also need to keep track of exceptional edges - they are also branches
	 */
	@Override
	protected boolean newControlFlowExceptionEdge(int src, int dst) {
		CFGFrame s = (CFGFrame) getFrames()[src];
		s.successors.put(dst, (CFGFrame) getFrames()[dst]);

		// TODO: Make use of information that this is an exception edge?
		BytecodeInstruction v1 = new BytecodeInstruction(src, current_method.instructions.get(src));
		BytecodeInstruction v2 = new BytecodeInstruction(dst, current_method.instructions.get(dst));

		graph.addVertex(v1);
		graph.addVertex(v2);
		graph.addEdge(v1, v2);

		return true;
	}



}
