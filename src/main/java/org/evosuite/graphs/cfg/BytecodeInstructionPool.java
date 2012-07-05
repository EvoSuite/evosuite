/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.graphs.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.branch.BranchPool;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>BytecodeInstructionPool class.</p>
 *
 * @author Andre Mis
 */
public class BytecodeInstructionPool {

	private static Logger logger = LoggerFactory.getLogger(BytecodeInstructionPool.class);

	// maps className -> method inside that class -> list of
	// BytecodeInstructions
	private static Map<String, Map<String, List<BytecodeInstruction>>> instructionMap = new HashMap<String, Map<String, List<BytecodeInstruction>>>();

	private static List<MethodNode> knownMethodNodes = new ArrayList<MethodNode>();

	// fill the pool

	/**
	 * Called by each CFGGenerator for it's corresponding method.
	 *
	 * The MethodNode contains all instructions within a method. A call to
	 * registerMethodNode() fills the instructionMap of the
	 * BytecodeInstructionPool with the instructions in that method and returns
	 * a List containing the BytecodeInstructions within that method.
	 *
	 * While registering all instructions the lineNumber of each
	 * BytecodeInstruction is set.
	 *
	 * @param node a {@link org.objectweb.asm.tree.MethodNode} object.
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<BytecodeInstruction> registerMethodNode(MethodNode node,
	        String className, String methodName) {
		registerMethodNode(node);

		int lastLineNumber = -1;
		int jpfId = 0;

		for (int instructionId = 0; instructionId < node.instructions.size(); instructionId++) {
			AbstractInsnNode instructionNode = node.instructions.get(instructionId);

			BytecodeInstruction instruction = BytecodeInstructionFactory.createBytecodeInstruction(className,
			                                                                                       methodName,
			                                                                                       instructionId,
			                                                                                       jpfId,
			                                                                                       instructionNode);

			if (instruction.isLineNumber())
				lastLineNumber = instruction.getLineNumber();
			else if (lastLineNumber != -1)
				instruction.setLineNumber(lastLineNumber);

			if (!instruction.isLabel() && !instruction.isLineNumber()
			        && !instruction.isFrame()) {
				jpfId++;
			}

			registerInstruction(instruction);

		}

		List<BytecodeInstruction> r = getInstructionsIn(className, methodName);
		if (r == null || r.size() == 0)
			throw new IllegalStateException(
			        "expect instruction pool to return non-null non-empty list of instructions for a previously registered method "
			                + methodName);

		return r;
	}

	private static void registerMethodNode(MethodNode node) {
		for (MethodNode mn : knownMethodNodes)
			if (mn == node)
				logger.debug("CFGGenerator.analyze() apparently got called for the same MethodNode twice");

		knownMethodNodes.add(node);
	}

	/**
	 * <p>registerInstruction</p>
	 *
	 * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public static void registerInstruction(BytecodeInstruction instruction) {
		String className = instruction.getClassName();
		String methodName = instruction.getMethodName();

		if (!instructionMap.containsKey(className))
			instructionMap.put(className,
			                   new HashMap<String, List<BytecodeInstruction>>());
		if (!instructionMap.get(className).containsKey(methodName))
			instructionMap.get(className).put(methodName,
			                                  new ArrayList<BytecodeInstruction>());
		instructionMap.get(className).get(methodName).add(instruction);

		if (instruction.isActualBranch())
			BranchPool.registerAsBranch(instruction);
	}

	// retrieve data from the pool

	/**
	 * <p>getInstruction</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param instructionId a int.
	 * @param asmNode a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public static BytecodeInstruction getInstruction(String className, String methodName,
	        int instructionId, AbstractInsnNode asmNode) {

		BytecodeInstruction r = getInstruction(className, methodName, instructionId);

		if (r != null)
			r.sanityCheckAbstractInsnNode(asmNode);

		return r;
	}

	/**
	 * <p>getInstruction</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param instructionId a int.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public static BytecodeInstruction getInstruction(String className, String methodName,
	        int instructionId) {

		if (instructionMap.get(className) == null) {
			logger.debug("unknown class: " + className);
			logger.debug(instructionMap.keySet().toString());
			return null;
		}
		if (instructionMap.get(className).get(methodName) == null) {
			logger.debug("unknown method: " + methodName);
			logger.debug(instructionMap.get(className).keySet().toString());
			return null;
		}
		for (BytecodeInstruction instruction : instructionMap.get(className).get(methodName)) {
			if (instruction.getInstructionId() == instructionId)
				return instruction;
		}

		logger.debug("unknown instruction " + instructionId + ", have "
		        + instructionMap.get(className).get(methodName).size());
		for (int i = 0; i < instructionMap.get(className).get(methodName).size(); i++) {
			logger.info(instructionMap.get(className).get(methodName).get(i).toString());
		}

		return null;
	}

	/**
	 * <p>getInstruction</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param node a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public static BytecodeInstruction getInstruction(String className, String methodName,
	        AbstractInsnNode node) {

		if (instructionMap.get(className) == null) {
			logger.debug("unknown class: " + className);
			logger.debug(instructionMap.keySet().toString());
			return null;
		}
		if (instructionMap.get(className).get(methodName) == null) {
			logger.debug("unknown method: " + methodName);
			logger.debug(instructionMap.get(className).keySet().toString());
			return null;
		}
		for (BytecodeInstruction instruction : instructionMap.get(className).get(methodName)) {
			if (instruction.asmNode == node)
				return instruction;
		}

		logger.debug("unknown instruction: " + node + ", have "
		        + instructionMap.get(className).get(methodName).size()
		        + " instructions for this method");
		logger.debug(instructionMap.get(className).get(methodName).toString());

		return null;
	}

	/**
	 * <p>knownClasses</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<String> knownClasses() {
		return new HashSet<String>(instructionMap.keySet());
	}

	/**
	 * <p>knownMethods</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<String> knownMethods(String className) {
		Set<String> r = new HashSet<String>();

		if (instructionMap.get(className) != null)
			r.addAll(instructionMap.get(className).keySet());

		return r;
	}

	/**
	 * <p>getInstructionsIn</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<BytecodeInstruction> getInstructionsIn(String className,
	        String methodName) {
		if (instructionMap.get(className) == null
		        || instructionMap.get(className).get(methodName) == null)
			return null;

		List<BytecodeInstruction> r = new ArrayList<BytecodeInstruction>();
		r.addAll(instructionMap.get(className).get(methodName));

		return r;
	}

	/**
	 * <p>logInstructionsIn</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 */
	public static void logInstructionsIn(String className, String methodName) {

		logger.debug("Printing instructions in " + className + "." + methodName + ":");

		List<BytecodeInstruction> instructions = getInstructionsIn(className, methodName);
		if (instructions == null) {
			logger.debug("..unknown method");
		}

		for (BytecodeInstruction instruction : instructions) {
			logger.debug("\t" + instruction.toString());
		}

	}

	/**
	 * <p>createFakeInstruction</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public static BytecodeInstruction createFakeInstruction(String className,
	        String methodName) {

		AbstractInsnNode fakeNode = new InsnNode(Opcodes.NOP);

		int instructionId = getInstructionsIn(className, methodName).size();

		BytecodeInstruction instruction = new BytecodeInstruction(className, methodName,
		        instructionId, -1, fakeNode);

		registerInstruction(instruction);

		return instruction;

	}

	/**
	 * <p>clear</p>
	 */
	public static void clear() {
		instructionMap.clear();
		knownMethodNodes.clear();
	}

	/**
	 * <p>clear</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 */
	public static void clear(String className) {
		instructionMap.remove(className);
	}

	/**
	 * <p>clear</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 */
	public static void clear(String className, String methodName) {
		if (instructionMap.containsKey(className))
			instructionMap.get(className).remove(methodName);
	}

	/**
	 * <p>forgetInstruction</p>
	 *
	 * @param ins a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public static boolean forgetInstruction(BytecodeInstruction ins) {
		if (!instructionMap.containsKey(ins.getClassName()))
			return false;
		if (!instructionMap.get(ins.getClassName()).containsKey(ins.getMethodName()))
			return false;

		return instructionMap.get(ins.getClassName()).get(ins.getMethodName()).remove(ins);
	}
}
