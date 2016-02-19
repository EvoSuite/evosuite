/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.graphs.cfg;

import java.util.List;

import org.evosuite.graphs.GraphPool;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * current method. In this step the CFGGenerator asks the
 * BytecodeInstructionPool for the previously created instructions and fills up
 * it's RawControlFlowGraph
 * 
 * After those calls the RawControlFlowGraph of the method at hand is complete
 * It should contain a Vertex for each BytecodeInstruction inside the specified
 * method and an edge for every possible transition between these instructions
 * 
 * @author Andre Mis
 */
public class CFGGenerator {

	private static Logger logger = LoggerFactory.getLogger(CFGGenerator.class);

	private RawControlFlowGraph rawGraph;

	private boolean nodeRegistered = false;
	private MethodNode currentMethod;
	private String className;
	private String methodName;
	private final ClassLoader classLoader;

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
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param node
	 *            a {@link org.objectweb.asm.tree.MethodNode} object.
	 */
	public CFGGenerator(ClassLoader classLoader, String className, String methodName,
	        MethodNode node) {
		this.classLoader = classLoader;
		registerMethodNode(node, className, methodName);
	}

	/**
	 * Adds the RawControlFlowGraph created by this instance to the GraphPool,
	 * computes the resulting ActualControlFlowGraph
	 */
	public void registerCFGs() {

		int removed = getRawGraph().removeIsolatedNodes();
		if (removed > 0)
			logger.info("removed isolated nodes: " + removed + " in " + methodName);

		// non-minimized cfg needed for defuse-coverage and control
		// dependence calculation
		GraphPool.getInstance(classLoader).registerRawCFG(getRawGraph());
		GraphPool.getInstance(classLoader).registerActualCFG(computeActualCFG());
	}

	// build up the graph

	private void registerMethodNode(MethodNode currentMethod, String className,
	        String methodName) {
		if (nodeRegistered)
			throw new IllegalStateException(
			        "registerMethodNode must not be called more than once for each instance of CFGGenerator");
		if (currentMethod == null || methodName == null || className == null)
			throw new IllegalArgumentException("null given");

		this.currentMethod = currentMethod;
		this.className = className;
		this.methodName = methodName;

		this.rawGraph = new RawControlFlowGraph(classLoader, className, methodName,
		        currentMethod.access);

		List<BytecodeInstruction> instructionsInMethod = BytecodeInstructionPool.getInstance(classLoader).registerMethodNode(currentMethod,
		                                                                                                                     className,
		                                                                                                                     methodName);

		// sometimes there is a Label at the very end of a method without a
		// controlFlowEdge to it. In order to keep the graph as connected as
		// possible and since this is just a label we will simply ignore these
		int count = 0;
		for (BytecodeInstruction ins : instructionsInMethod) {
			count++;
			if (!ins.isLabel() || count < instructionsInMethod.size())
				rawGraph.addVertex(ins);
		}

		nodeRegistered = true;
	}

	/**
	 * Internal management of fields and actual building up of the rawGraph
	 * 
	 * Is called by the corresponding BytecodeAnalyzer whenever it detects a
	 * control flow edge
	 * 
	 * @param src
	 *            a int.
	 * @param dst
	 *            a int.
	 * @param frames
	 *            an array of {@link org.objectweb.asm.tree.analysis.Frame}
	 *            objects.
	 * @param isExceptionEdge
	 *            a boolean.
	 */
	public void registerControlFlowEdge(int src, int dst, Frame[] frames,
	        boolean isExceptionEdge) {
		if (!nodeRegistered)
			throw new IllegalStateException(
			        "CFGGenrator.registerControlFlowEdge() cannot be called unless registerMethodNode() was called first");
		if (frames == null)
			throw new IllegalArgumentException("null given");
		CFGFrame srcFrame = (CFGFrame) frames[src];
		Frame dstFrame = frames[dst];

		if (srcFrame == null)
			throw new IllegalArgumentException(
			        "expect given frames to know srcFrame for " + src);

		if (dstFrame == null) {
			// documentation of getFrames() tells us the following:
			// Returns:
			// the symbolic state of the execution stack frame at each bytecode
			// instruction of the method. The size of the returned array is
			// equal to the number of instructions (and labels) of the method. A
			// given frame is null if the corresponding instruction cannot be
			// reached, or if an error occured during the analysis of the
			// method.
			// so let's say we expect the analyzer to return null only if
			// dst is not reachable and if that happens we just suppress the
			// corresponding ControlFlowEdge for now
			// TODO can the CFG become disconnected like that?
			return;
		}

		srcFrame.successors.put(dst, (CFGFrame) dstFrame);

		AbstractInsnNode srcNode = currentMethod.instructions.get(src);
		AbstractInsnNode dstNode = currentMethod.instructions.get(dst);

		// those nodes should have gotten registered by registerMethodNode()
		BytecodeInstruction srcInstruction = BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
		                                                                                                     methodName,
		                                                                                                     src,
		                                                                                                     srcNode);
		BytecodeInstruction dstInstruction = BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
		                                                                                                     methodName,
		                                                                                                     dst,
		                                                                                                     dstNode);

		srcInstruction.setCFGFrame(srcFrame);

		if (dstInstruction == null)
			throw new IllegalStateException(
			        "expect BytecodeInstructionPool to know the instructions in the method of this edge");

		if (null == rawGraph.addEdge(srcInstruction, dstInstruction, isExceptionEdge))
			logger.error("internal error while adding edge");
	}

	/**
	 * Computes the ActualCFG with BasicBlocks rather then BytecodeInstructions
	 * for this RawCFG.
	 * 
	 * See ActualControlFlowGraph and GraphPool for further details.
	 * 
	 * @return a {@link org.evosuite.graphs.cfg.ActualControlFlowGraph} object.
	 */
	public ActualControlFlowGraph computeActualCFG() {
		BytecodeInstructionPool.getInstance(classLoader).logInstructionsIn(className,
		                                                                   methodName);

		ActualControlFlowGraph cfg = new ActualControlFlowGraph(rawGraph);

		return cfg;
	}

	// getter

	/**
	 * <p>
	 * Getter for the field <code>rawGraph</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.graphs.cfg.RawControlFlowGraph} object.
	 */
	protected RawControlFlowGraph getRawGraph() {
		return rawGraph;
	}

	/**
	 * <p>
	 * Getter for the field <code>className</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * <p>
	 * Getter for the field <code>methodName</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethodName() {
		return methodName;
	}

}
