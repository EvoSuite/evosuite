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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.runtime.instrumentation.AnnotatedLabel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * BytecodeInstructionPool class.
 * </p>
 * 
 * @author Andre Mis
 */
public class BytecodeInstructionPool {

	private static Logger logger = LoggerFactory.getLogger(BytecodeInstructionPool.class);

	private static Map<ClassLoader, BytecodeInstructionPool> instanceMap = new HashMap<ClassLoader, BytecodeInstructionPool>();

	private final ClassLoader classLoader;

	private BytecodeInstructionPool(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public static BytecodeInstructionPool getInstance(ClassLoader classLoader) {
		if (!instanceMap.containsKey(classLoader)) {
			instanceMap.put(classLoader, new BytecodeInstructionPool(classLoader));
		}

		return instanceMap.get(classLoader);
	}

	// maps className -> method inside that class -> list of
	// BytecodeInstructions
	private final Map<String, Map<String, List<BytecodeInstruction>>> instructionMap = new HashMap<String, Map<String, List<BytecodeInstruction>>>();

	private final List<MethodNode> knownMethodNodes = new ArrayList<MethodNode>();

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
	 * @param node
	 *            a {@link org.objectweb.asm.tree.MethodNode} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<BytecodeInstruction> registerMethodNode(MethodNode node,
	        String className, String methodName) {
		registerMethodNode(node);

		int lastLineNumber = -1;
		int bytecodeOffset = 0;

		for (int instructionId = 0; instructionId < node.instructions.size(); instructionId++) {
			AbstractInsnNode instructionNode = node.instructions.get(instructionId);

			BytecodeInstruction instruction = BytecodeInstructionFactory.createBytecodeInstruction(classLoader,
			                                                                                       className,
			                                                                                       methodName,
			                                                                                       instructionId,
			                                                                                       bytecodeOffset,
			                                                                                       instructionNode);

			if (instruction.isLineNumber())
				lastLineNumber = instruction.getLineNumber();
			else if (lastLineNumber != -1)
				instruction.setLineNumber(lastLineNumber);

			bytecodeOffset += getBytecodeIncrement(instructionNode);

			if (!instruction.isLabel() && !instruction.isLineNumber()
			        && !instruction.isFrame()) {
				bytecodeOffset++;
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

	/**
	 * Determine how many bytes the current instruction occupies together with
	 * its operands
	 * 
	 * @return
	 */
	private int getBytecodeIncrement(AbstractInsnNode instructionNode) {
		int opcode = instructionNode.getOpcode();
		switch (opcode) {
		case Opcodes.ALOAD: // index
		case Opcodes.ASTORE: // index
		case Opcodes.DLOAD:
		case Opcodes.DSTORE:
		case Opcodes.FLOAD:
		case Opcodes.FSTORE:
		case Opcodes.ILOAD:
		case Opcodes.ISTORE:
		case Opcodes.LLOAD:
		case Opcodes.LSTORE:
			VarInsnNode varNode = (VarInsnNode) instructionNode;
			if (varNode.var > 3)
				return 1;
			else
				return 0;
		case Opcodes.BIPUSH: // byte
		case Opcodes.NEWARRAY:
		case Opcodes.RET:
			return 1;
		case Opcodes.LDC:
			LdcInsnNode ldcNode = (LdcInsnNode)instructionNode;
			if(ldcNode.cst instanceof Double || ldcNode.cst instanceof Long)
				return 2; // LDC2_W
			else
				return 1;
		case 19: //LDC_W
		case 20: //LDC2_W
			return 2;		
		case Opcodes.ANEWARRAY: // indexbyte1, indexbyte2
		case Opcodes.CHECKCAST: // indexbyte1, indexbyte2
		case Opcodes.GETFIELD:
		case Opcodes.GETSTATIC:
		case Opcodes.GOTO:
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IFLE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFNE:
		case Opcodes.IFEQ:
		case Opcodes.IFNONNULL:
		case Opcodes.IFNULL:
		case Opcodes.IINC:
		case Opcodes.INSTANCEOF:
		case Opcodes.INVOKESPECIAL:
		case Opcodes.INVOKESTATIC:
		case Opcodes.INVOKEVIRTUAL:
		case Opcodes.JSR:
		case Opcodes.NEW:
		case Opcodes.PUTFIELD:
		case Opcodes.PUTSTATIC:
		case Opcodes.SIPUSH:
			// case Opcodes.LDC_W
			// case Opcodes.LDC2_W

			return 2;
		case Opcodes.MULTIANEWARRAY:
			return 3;
		case Opcodes.INVOKEDYNAMIC:
		case Opcodes.INVOKEINTERFACE:
			return 4;

		case Opcodes.LOOKUPSWITCH:
		case Opcodes.TABLESWITCH:
			// TODO: Could be more
			return 4;
			// case Opcodes.GOTO_W 
			// case Opcodes.JSR_W
		}
		return 0;
	}

	private void registerMethodNode(MethodNode node) {
		for (MethodNode mn : knownMethodNodes)
			if (mn == node)
				logger.debug("CFGGenerator.analyze() apparently got called for the same MethodNode twice");

		knownMethodNodes.add(node);
	}

	/**
	 * <p>
	 * registerInstruction
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public void registerInstruction(BytecodeInstruction instruction) {
		String className = instruction.getClassName();
		String methodName = instruction.getMethodName();

		if (!instructionMap.containsKey(className))
			instructionMap.put(className,
			                   new HashMap<String, List<BytecodeInstruction>>());
		if (!instructionMap.get(className).containsKey(methodName))
			instructionMap.get(className).put(methodName,
			                                  new ArrayList<BytecodeInstruction>());

		instructionMap.get(className).get(methodName).add(instruction);
		logger.debug("Registering instruction "+instruction);
		List<BytecodeInstruction> instructions = instructionMap.get(className).get(methodName);
		if(instructions.size() > 1) {
			BytecodeInstruction previous = instructions.get(instructions.size() - 2);
			if(previous.isLabel()) {
				LabelNode ln = (LabelNode)previous.asmNode;
				if (ln.getLabel() instanceof AnnotatedLabel) {
					AnnotatedLabel aLabel = (AnnotatedLabel) ln.getLabel();
					if(aLabel.isStartTag()) {
						if(aLabel.shouldIgnore()) {
							logger.debug("Ignoring artificial branch: "+instruction);
							return;
						}
					}
				}
			}
		}
		
		if (instruction.isActualBranch()) {
			BranchPool.getInstance(classLoader).registerAsBranch(instruction);
		}
	}

	// retrieve data from the pool

	/**
	 * <p>
	 * getInstruction
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param instructionId
	 *            a int.
	 * @param asmNode
	 *            a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getInstruction(String className, String methodName,
	        int instructionId, AbstractInsnNode asmNode) {

		BytecodeInstruction r = getInstruction(className, methodName, instructionId);

		if (r != null)
			assert (r.sanityCheckAbstractInsnNode(asmNode));

		return r;
	}

	/**
	 * <p>
	 * getInstruction
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param instructionId
	 *            a int.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getInstruction(String className, String methodName,
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
	 * <p>
	 * getInstruction
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param node
	 *            a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getInstruction(String className, String methodName,
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
	 * <p>
	 * knownClasses
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> knownClasses() {
		return new HashSet<String>(instructionMap.keySet());
	}

	/**
	 * <p>
	 * knownMethods
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> knownMethods(String className) {
		Set<String> r = new HashSet<String>();

		if (instructionMap.get(className) != null)
			r.addAll(instructionMap.get(className).keySet());

		return r;
	}

	public boolean hasMethod(String className, String methodName) {
		if (instructionMap.get(className) != null)
			return instructionMap.get(className).containsKey(methodName);

		return false;
	}

	/**
	 * <p>
	 * getInstructionsIn
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<BytecodeInstruction> getInstructionsIn(String className, String methodName) {
		if (instructionMap.get(className) == null
		        || instructionMap.get(className).get(methodName) == null)
			return null;

		List<BytecodeInstruction> r = new ArrayList<BytecodeInstruction>();
		r.addAll(instructionMap.get(className).get(methodName));

		return r;
	}
	
	public List<BytecodeInstruction> getInstructionsIn(String className) {
		if (instructionMap.get(className) == null)
			return null;

		List<BytecodeInstruction> r = new ArrayList<BytecodeInstruction>();
		Map<String, List<BytecodeInstruction>> methodMap = instructionMap.get(className);
		for(List<BytecodeInstruction> methodInstructions : methodMap.values()) {
			r.addAll(methodInstructions);
		}
		
		return r;
	}
	
	public List<BytecodeInstruction> getAllInstructions() {
		List<BytecodeInstruction> r = new ArrayList<BytecodeInstruction>();
		for(String className : instructionMap.keySet()) {
			Map<String, List<BytecodeInstruction>> methodMap = instructionMap.get(className);
			for(List<BytecodeInstruction> methodInstructions : methodMap.values()) {
				r.addAll(methodInstructions);
			}
		}
		
		return r;
	}

	/**
	 * <p>
	 * logInstructionsIn
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public void logInstructionsIn(String className, String methodName) {

		logger.debug("Printing instructions in " + className + "." + methodName + ":");

		List<BytecodeInstruction> instructions = getInstructionsIn(className, methodName);
		if (instructions == null) {
			logger.debug("..unknown method");
		} else {
			for (BytecodeInstruction instruction : instructions) {
				logger.debug("\t" + instruction);
			}
		}

	}

	/**
	 * <p>
	 * createFakeInstruction
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction createFakeInstruction(String className, String methodName) {

		AbstractInsnNode fakeNode = new InsnNode(Opcodes.NOP);

		int instructionId = getInstructionsIn(className, methodName).size();

		BytecodeInstruction instruction = new BytecodeInstruction(classLoader, className,
		        methodName, instructionId, -1, fakeNode);

		registerInstruction(instruction);

		return instruction;

	}

	/**
	 * <p>
	 * clear
	 * </p>
	 */
	public void clear() {
		instructionMap.clear();
		knownMethodNodes.clear();
	}

	public static void clearAll() {
		BytecodeInstructionPool.instanceMap.clear();
	}

	/**
	 * <p>
	 * clear
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public void clear(String className) {
		instructionMap.remove(className);
	}

	public static void clearAll(String className) {
		for (BytecodeInstructionPool pool : instanceMap.values()) {
			pool.clear(className);
		}
	}

	/**
	 * <p>
	 * clear
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public void clear(String className, String methodName) {
		if (instructionMap.containsKey(className))
			instructionMap.get(className).remove(methodName);
	}

	public static void clearAll(String className, String methodName) {
		for (BytecodeInstructionPool pool : instanceMap.values()) {
			pool.clear(className, methodName);
		}
	}

	/**
	 * <p>
	 * forgetInstruction
	 * </p>
	 * 
	 * @param ins
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean forgetInstruction(BytecodeInstruction ins) {
		if (!instructionMap.containsKey(ins.getClassName()))
			return false;
		if (!instructionMap.get(ins.getClassName()).containsKey(ins.getMethodName()))
			return false;

		return instructionMap.get(ins.getClassName()).get(ins.getMethodName()).remove(ins);
	}

	public int getFirstLineNumberOfMethod(String className, String methodName) {
		if (instructionMap.get(className) == null)
			throw new IllegalArgumentException("unknown class " + className);
		if (instructionMap.get(className).get(methodName) == null)
			throw new IllegalArgumentException("unknown method " + methodName
			        + " in class " + className);
		if (instructionMap.get(className).get(methodName).isEmpty())
			throw new IllegalArgumentException("no instructions in method " + methodName
			        + " in class " + className);

		int r = Integer.MAX_VALUE;
		for (BytecodeInstruction ins : instructionMap.get(className).get(methodName)) {
			if (ins.getLineNumber() < r)
				r = ins.getLineNumber();
		}
		return r;
	}
	
	public BytecodeInstruction getFirstInstructionAtLineNumber(String className, String methodName, int lineNumber) {
		// TODO
		if (instructionMap.get(className) == null)
			return null;
		if (instructionMap.get(className).get(methodName) == null)
			return null;
		if (instructionMap.get(className).get(methodName).isEmpty())
			return null;

		for (BytecodeInstruction ins : instructionMap.get(className).get(methodName)) {
			if (ins.getLineNumber() == lineNumber)
				return ins;
		}
		return null;
	}
}
