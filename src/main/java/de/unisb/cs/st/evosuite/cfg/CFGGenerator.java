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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.AbstractVisitor;

import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * This class generates a CFG from a method's bytecode
 * 
 * @author Gordon Fraser
 * 
 */
public class CFGGenerator extends Analyzer {

	private static Logger logger = Logger.getLogger(CFGGenerator.class);

	/**
	 * Node of the control flow graph
	 * 
	 * @author Gordon Fraser
	 * 
	 */
	public class CFGVertex {

		AbstractInsnNode node;
		CFGFrame frame;
		int id;
		public int line_no = 0;
		List<Long> mutations = new ArrayList<Long>();
		boolean mutationBranch = false;
		boolean mutatedBranch = false;

		public int defuseId = -1;
		public int useId = -1;
		public int defId = -1;
		public boolean isParameterUse = false; // is set by DefUsePool
		// TODO: every CFGVertex should hold a reference to it's control dependent Branch
		public int branchId = -1;
		public boolean branchExpressionValue = true; // TODO this should be false whenever it is true and visa versa
		public String methodName;
		public String className;

		Map<Long, Integer> mutant_distance = new HashMap<Long, Integer>(); // Calculate distance to each mutation 

		public CFGVertex(int id, AbstractInsnNode node) {
			this.id = id;
			this.node = node;
		}

		public CFGVertex(int id) {
			this.id = id;
			this.node = null;
		}

		public boolean isJump() {
			return (node instanceof JumpInsnNode);
		}

		public boolean isGoto() {
			if (node instanceof JumpInsnNode) {
				return (node.getOpcode() == Opcodes.GOTO);
			}
			return false;
		}

		// TODO shouldn'tthe following the methods be somehow merged
		//		to reflect that all three return true on a "Branch"
		//		in the sense of evosuite.coverage.branch.Branch ?
		
		public boolean isBranch() {
			return isJump() && !isGoto();
		}

		public boolean isTableSwitch() {
			return (node instanceof TableSwitchInsnNode);
		}

		public boolean isLookupSwitch() {
			return (node instanceof LookupSwitchInsnNode);
		}

		public void setMutation(long id) {
			mutations.add(id);
		}

		public boolean hasMutation(long id) {
			return mutations.contains(id);
		}

		public boolean isMutation() {
			return !mutations.isEmpty();
			/*
			 * if(node instanceof LdcInsnNode) {
			 * 
			 * if(((LdcInsnNode)node).cst.toString().contains("mutationId")) {
			 * logger.info("!!!!! Found mutation!"); } } return false;
			 */
		}

		public boolean isMutationBranch() {
			return isBranch() && mutationBranch;
		}

		public void setMutationBranch() {
			mutationBranch = true;
		}

		public void setMutatedBranch() {
			mutatedBranch = true;
		}

		public boolean isMutatedBranch() {
			// Mutated if HOMObserver of MutationObserver are called
			return isBranch() && mutatedBranch;
		}

		public boolean isBranchLabel() {
			if (node instanceof LabelNode
			        && ((LabelNode) node).getLabel().info instanceof Integer) {
				return true;
			}
			return false;
		}

		public boolean isLineNumber() {
			return (node instanceof LineNumberNode);
		}

		public int getBranchId() {
			// return ((Integer)((LabelNode)node).getLabel().info).intValue();
			return line_no;
		}

		public boolean isIfNull() {
			if (node instanceof JumpInsnNode) {
				return (node.getOpcode() == Opcodes.IFNULL);
			}
			return false;
		}

		public boolean isMethodCall() {
			return node instanceof MethodInsnNode;
		}

		public boolean isMethodCall(String methodName) {
			if (node instanceof MethodInsnNode) {
				MethodInsnNode mn = (MethodInsnNode) node;
				return mn.name.equals(methodName);
			}
			return false;
		}

		public int getId() {
			return id;
		}
		
		public boolean isDefUse() {
			return isLocalDU() || isFieldDU();
		}

		public boolean isFieldDU() {
			return isFieldDefinition() || isFieldUse();
		}
		
		public boolean isLocalDU() {
			return isLocalVarDefinition() || isLocalVarUse();
		}		
		
		public boolean isLocalVarDefinition() {
			return node.getOpcode() == Opcodes.ISTORE
			        || node.getOpcode() == Opcodes.LSTORE
			        || node.getOpcode() == Opcodes.FSTORE
			        || node.getOpcode() == Opcodes.DSTORE
			        || node.getOpcode() == Opcodes.ASTORE
			        || node.getOpcode() == Opcodes.IINC;
		}

		public boolean isLocalVarUse() {
			return node.getOpcode() == Opcodes.ILOAD 
					|| node.getOpcode() == Opcodes.LLOAD
			        || node.getOpcode() == Opcodes.FLOAD
			        || node.getOpcode() == Opcodes.DLOAD
			        || node.getOpcode() == Opcodes.ALOAD
			        || node.getOpcode() == Opcodes.IINC;
			// || node.getOpcode() == Opcodes.RET; // TODO ??
		}

		public boolean isDefinition() {
			return isFieldDefinition() || isLocalVarDefinition();
		}

		public boolean isUse() {
			return isFieldUse() || isLocalVarUse();
		}

		public boolean isFieldDefinition() {
			return node.getOpcode() == Opcodes.PUTFIELD
			        || node.getOpcode() == Opcodes.PUTSTATIC;
		}

		public boolean isFieldUse() {
			return node.getOpcode() == Opcodes.GETFIELD
			        || node.getOpcode() == Opcodes.GETSTATIC;
		}

		public boolean isStaticDefUse() {
			return node.getOpcode() == Opcodes.PUTSTATIC
			        || node.getOpcode() == Opcodes.GETSTATIC;
		}

		public boolean isParameterUse() {
			return isParameterUse;
		}
		
		public String getFieldName() {
			return ((FieldInsnNode) node).name;
		}

		public int getLocalVar() {
			if (node instanceof VarInsnNode)
				return ((VarInsnNode) node).var;
			else
				return ((IincInsnNode) node).var;
		}

		public String getLocalVarName() {
			return methodName + "_LV_" + getLocalVar();
		}

		public String getDUVariableName() {
			if (!this.isDefUse())
				throw new IllegalStateException(
				        "You can only call getDUVariableName() on a local variable or field definition/use");
			if (this.isFieldDU())
				return getFieldName();
			else
				return getLocalVarName();
		}

		public String getMethodName() {
			return ((MethodInsnNode) node).name;
		}

		public List<Long> getMutationIds() {
			return mutations;

			// String ids = ((LdcInsnNode)node).cst.toString();
			// return Integer.parseInt(ids.substring(ids.indexOf("_")+1));
		}

		public int getLineNumber() {
			return ((LineNumberNode) node).line;
		}

		public int getDistance(long id) {
			if (mutant_distance.containsKey(id))
				return mutant_distance.get(id);
			return Integer.MAX_VALUE;
		}

		public void setDistance(long id, int distance) {
			mutant_distance.put(id, distance);
		}

		@Override
		public String toString() {

			if (isMutation()) {
				String ids = "Mutations: ";
				for (long l : mutations) {
					ids += " " + l;
				}
				return ids;
			}

			if (isBranch()) {
				return "Branch " + id + " - " + ((JumpInsnNode) node).label.getLabel();
			}

			String type = "";
			String opcode = "";

			if (node.getOpcode() >= 0
			        && node.getOpcode() < AbstractVisitor.OPCODES.length)
				opcode = AbstractVisitor.OPCODES[node.getOpcode()];
			if (node.getType() >= 0 && node.getType() < AbstractVisitor.TYPES.length)
				type = AbstractVisitor.TYPES[node.getType()];

			String stack = "";
			if (frame == null)
				stack = "null";
			else
				for (int i = 0; i < frame.getStackSize(); i++) {
					stack += frame.getStack(i) + ",";
				}

			if (node instanceof LabelNode) {
				return "LABEL " + ((LabelNode) node).getLabel().toString();
			} else if (node instanceof FieldInsnNode)
				return "Field" + " " + node.getOpcode() + " Type=" + type + ", Opcode="
				        + opcode;
			else if (node instanceof FrameNode)
				return "Frame" + " " + node.getOpcode() + " Type=" + type + ", Opcode="
				        + opcode;
			else if (node instanceof IincInsnNode)
				return "IINC " + ((IincInsnNode) node).var + " Type=" + type
				        + ", Opcode=" + opcode;
			else if (node instanceof InsnNode)
				return "" + opcode;
			else if (node instanceof IntInsnNode)
				return "INT " + ((IntInsnNode) node).operand + " Type=" + type
				        + ", Opcode=" + opcode;
			else if (node instanceof MethodInsnNode)
				return opcode + " " + ((MethodInsnNode) node).name;
			else if (node instanceof JumpInsnNode)
				return "JUMP " + ((JumpInsnNode) node).label.getLabel() + " Type=" + type
				        + ", Opcode=" + opcode + ", Stack: " + stack + " - Line: "
				        + line_no;
			else if (node instanceof LdcInsnNode)
				return "LDC " + ((LdcInsnNode) node).cst + " Type=" + type + ", Opcode="
				        + opcode; // cst starts with mutationid if
				                  // this is location of mutation
			else if (node instanceof LineNumberNode)
				return "LINE " + " " + ((LineNumberNode) node).line;
			else if (node instanceof LookupSwitchInsnNode)
				return "LookupSwitchInsnNode" + " " + node.getOpcode() + " Type=" + type
				        + ", Opcode=" + opcode;
			else if (node instanceof MultiANewArrayInsnNode)
				return "MULTIANEWARRAY " + " " + node.getOpcode() + " Type=" + type
				        + ", Opcode=" + opcode;
			else if (node instanceof TableSwitchInsnNode)
				return "TableSwitchInsnNode" + " " + node.getOpcode() + " Type=" + type
				        + ", Opcode=" + opcode;
			else if (node instanceof TypeInsnNode)
				return "NEW " + ((TypeInsnNode) node).desc;
			//				return "TYPE " + " " + node.getOpcode() + " Type=" + type
			//						+ ", Opcode=" + opcode;
			else if (node instanceof VarInsnNode)
				return opcode + " " + ((VarInsnNode) node).var;
			else
				return "Unknown node" + " Type=" + type + ", Opcode=" + opcode;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			// result = prime * result + getOuterType().hashCode();
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass()) // TODO: can Class be compared via == ?
				return false;
			CFGVertex other = (CFGVertex) obj;
			// if (!getOuterType().equals(other.getOuterType()))
			// return false;
			if (id != other.id)
				return false;
			if (methodName != null && !methodName.equals(other.methodName))
				return false;
			if (className != null && !className.equals(other.className))
				return false;
			return true;
		}

		// private CFGGenerator getOuterType() {
		// return CFGGenerator.this;
		// }
	}

	MethodNode current_method = null;
	DefaultDirectedGraph<CFGVertex, DefaultEdge> graph = new DefaultDirectedGraph<CFGVertex, DefaultEdge>(
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

	CFGFrame getCFG(String owner, String method, MethodNode node)
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

	public DefaultDirectedGraph<CFGVertex, DefaultEdge> getGraph() {
		return graph;
	}

	public DirectedMultigraph<CFGVertex, DefaultEdge> getMinimalGraph() {
		DirectedMultigraph<CFGVertex, DefaultEdge> min_graph = new DirectedMultigraph<CFGVertex, DefaultEdge>(
		        DefaultEdge.class);
		CFGVertex current = null;

		for (Mutation m : mutants) {
			if (m.getMethodName().equals(methodName)
			        && m.getClassName().equals(className)) {
				for (CFGVertex v : graph.vertexSet()) {
					if (v.isLineNumber() && v.getLineNumber() == m.getLineNumber()) {
						v.setMutation(m.getId());
						// TODO: What if there are several mutations with the
						// same line number?
					}
				}
			}
		}

		for (CFGVertex v : graph.vertexSet()) {
			if (v.isIfNull()) {
				for (DefaultEdge e : graph.incomingEdgesOf(v)) {
					CFGVertex v2 = graph.getEdgeSource(e);
					if (v2.isMethodCall("getProperty")) {
						v.setMutationBranch();
					}
				}
			} else if (v.isBranch() || v.isTableSwitch() || v.isLookupSwitch()) {
				for (DefaultEdge e : graph.incomingEdgesOf(v)) {
					CFGVertex v2 = graph.getEdgeSource(e);
					if (v2.isMethodCall("touch")) {
						logger.debug("Found mutated branch ");
						v.setMutatedBranch();
					} else {
						if (v2.isMethodCall())
							logger.debug("Edgesource: " + v2.getMethodName());
					}
				}
			}
		}

		for (CFGVertex vertex : graph.vertexSet()) {
			if (current == null)
				current = vertex;

			// Add initial nodes and jump targets
			if (graph.inDegreeOf(vertex) == 0)
				min_graph.addVertex(vertex);
			// Add end nodes
			else if (graph.outDegreeOf(vertex) == 0)
				min_graph.addVertex(vertex);
			else if (vertex.isJump() && !vertex.isGoto()) {
				min_graph.addVertex(vertex);
			} else if (vertex.isTableSwitch() || vertex.isLookupSwitch()) {
				min_graph.addVertex(vertex);
			} else if (vertex.isMutation())
				min_graph.addVertex(vertex);
			/*
			 * else if(vertex.isLineNumber()) { boolean keep = false;
			 * for(Mutation m : mutants) { if(m.getLineNumber() ==
			 * vertex.getLineNumber()) { if(m.getMethodName().equals(methodName)
			 * && m.getClassName().equals(className)) {
			 * vertex.setMutation(m.getId()); keep = true; break; } } } if(keep)
			 * { min_graph.addVertex(vertex);
			 * logger.info("Graph contains mutation "+vertex.getMutationId()); }
			 * }
			 */

		}

		for (CFGVertex vertex : min_graph.vertexSet()) {
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

		CFGVertex v1 = new CFGVertex(src, current_method.instructions.get(src));
		CFGVertex v2 = new CFGVertex(dst, current_method.instructions.get(dst));

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
		CFGVertex v1 = new CFGVertex(src, current_method.instructions.get(src));
		CFGVertex v2 = new CFGVertex(dst, current_method.instructions.get(dst));

		graph.addVertex(v1);
		graph.addVertex(v2);
		graph.addEdge(v1, v2);

		return true;
	}

}
