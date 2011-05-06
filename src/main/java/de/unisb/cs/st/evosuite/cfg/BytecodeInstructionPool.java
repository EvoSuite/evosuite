package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;

/**
 * TODO goal: unite Branch/DefUse-Pools
 * @author Andre Mis
 *
 */
public class BytecodeInstructionPool {

	private static Logger logger = Logger.getLogger(BytecodeInstruction.class);
	
	// maps className -> method inside that class -> list of BytecodeInstructions
	private static Map<String, Map<String, List<BytecodeInstruction>>> instructionMap = new HashMap<String, Map<String, List<BytecodeInstruction>>>();

	private static List<MethodNode> knownMethodNodes = new ArrayList<MethodNode>();
	
	public static void registerMethodNode(MethodNode node, String className, String methodName) {
		
		registerMethodNode(node);
		BytecodeInstructionFactory factory = BytecodeInstructionFactory
				.getInstance();

		for(int instructionId=0;instructionId<node.instructions.size();instructionId++) {
			AbstractInsnNode instructionNode = node.instructions.get(instructionId);
			
			BytecodeInstruction instruction = factory.createBytecodeInstruction(className, methodName, instructionId, instructionNode);
			addInstructionToMap(instruction);
			
		}
	}
	
	private static void registerMethodNode(MethodNode node) {
		// TODO
		for (MethodNode mn : knownMethodNodes)
			if (mn == node)
				logger.warn("TODO CFGGenerator.analyze() apparently got called for the same MethodNode twice");
		knownMethodNodes.add(node);
	}
	
	private static void addInstructionToMap(BytecodeInstruction instruction) {
		String className = instruction.getClassName();
		String methodName = instruction.getMethodName();
		
		if (!instructionMap.containsKey(className))
			instructionMap.put(className, new HashMap<String, List<BytecodeInstruction>>());
		if (!instructionMap.get(className).containsKey(methodName))
			instructionMap.get(className).put(methodName, new ArrayList<BytecodeInstruction>());
		instructionMap.get(className).get(methodName).add(instruction);
	}

	public static BytecodeInstruction getInstruction(int src,
			AbstractInsnNode node1) {
		// TODO Auto-generated method stub
		return null;
	}
}
