package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

/**
 * @author Andre Mis
 * 
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
				logger.warn("CFGGenerator.analyze() apparently got called for the same MethodNode twice");

		knownMethodNodes.add(node);
	}

	private static void registerInstruction(BytecodeInstruction instruction) {
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

	public static BytecodeInstruction getInstruction(String className, String methodName,
	        int instructionId, AbstractInsnNode asmNode) {

		BytecodeInstruction r = getInstruction(className, methodName, instructionId);
		if (r != null)
			r.sanityCheckAbstractInsnNode(asmNode);

		return r;
	}

	public static BytecodeInstruction getInstruction(String className, String methodName,
	        int instructionId) {

		if (instructionMap.get(className) == null) {
			logger.debug("unknown class");
			return null;
		}
		if (instructionMap.get(className).get(methodName) == null) {
			logger.debug("unknown method");
			return null;
		}
		for (BytecodeInstruction instruction : instructionMap.get(className).get(methodName)) {
			if (instruction.getInstructionId() == instructionId)
				return instruction;
		}

		logger.debug("unknown instruction");

		return null;
	}

	public static List<BytecodeInstruction> getInstructionsIn(String className,
	        String methodName) {
		if (instructionMap.get(className) == null
		        || instructionMap.get(className).get(methodName) == null)
			return null;

		List<BytecodeInstruction> r = new ArrayList<BytecodeInstruction>();
		r.addAll(instructionMap.get(className).get(methodName));

		return r;
	}

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

	public static BytecodeInstruction createFakeInstruction(String className,
	        String methodName) {

		AbstractInsnNode fakeNode = new InsnNode(Opcodes.NOP);

		int instructionId = getInstructionsIn(className, methodName).size();

		BytecodeInstruction instruction = new BytecodeInstruction(className, methodName,
		        instructionId, -1, fakeNode);

		registerInstruction(instruction);

		return instruction;

	}
}
