/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;

/**
 * @author Gordon Fraser
 * 
 */
public class ReplaceArithmeticOperator implements MutationOperator {

	private static Set<Integer> opcodesInt = new HashSet<Integer>();

	private static Set<Integer> opcodesLong = new HashSet<Integer>();

	private static Set<Integer> opcodesFloat = new HashSet<Integer>();

	private static Set<Integer> opcodesDouble = new HashSet<Integer>();

	private int numVariable = 0;

	static {
		opcodesInt.addAll(Arrays.asList(new Integer[] { Opcodes.IADD, Opcodes.ISUB,
		        Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM }));

		opcodesLong.addAll(Arrays.asList(new Integer[] { Opcodes.LADD, Opcodes.LSUB,
		        Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM }));

		opcodesFloat.addAll(Arrays.asList(new Integer[] { Opcodes.FADD, Opcodes.FSUB,
		        Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM }));

		opcodesDouble.addAll(Arrays.asList(new Integer[] { Opcodes.DADD, Opcodes.DSUB,
		        Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM }));
	}

	private String getOp(int opcode) {
		switch (opcode) {
		case Opcodes.IADD:
		case Opcodes.LADD:
		case Opcodes.FADD:
		case Opcodes.DADD:
			return "+";
		case Opcodes.ISUB:
		case Opcodes.LSUB:
		case Opcodes.FSUB:
		case Opcodes.DSUB:
			return "-";
		case Opcodes.IMUL:
		case Opcodes.LMUL:
		case Opcodes.FMUL:
		case Opcodes.DMUL:
			return "*";
		case Opcodes.IDIV:
		case Opcodes.LDIV:
		case Opcodes.FDIV:
		case Opcodes.DDIV:
			return "/";
		case Opcodes.IREM:
		case Opcodes.LREM:
		case Opcodes.FREM:
		case Opcodes.DREM:
			return "%";
		}
		throw new RuntimeException("Unknown opcode: " + opcode);
	}

	@SuppressWarnings("rawtypes")
	public static int getNextIndex(MethodNode mn) {
		Iterator it = mn.localVariables.iterator();
		int max = 0;
		int next = 0;
		while (it.hasNext()) {
			LocalVariableNode var = (LocalVariableNode) it.next();
			int index = var.index;
			if (index >= max) {
				max = index;
				next = max + Type.getType(var.desc).getSize();
			}
		}
		return next;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction) {

		numVariable = getNextIndex(mn);
		List<Mutation> mutations = new LinkedList<Mutation>();

		InsnNode node = (InsnNode) instruction.getASMNode();

		for (int opcode : getMutations(node.getOpcode())) {

			InsnNode mutation = new InsnNode(opcode);
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(className,
			                                                   methodName,
			                                                   "ReplaceArithmeticOperator "
			                                                           + getOp(node.getOpcode())
			                                                           + " -> "
			                                                           + getOp(opcode),
			                                                   instruction,
			                                                   mutation,
			                                                   getInfectionDistance(node.getOpcode(),
			                                                                        opcode));
			mutations.add(mutationObject);
		}

		return mutations;
	}

	private Set<Integer> getMutations(int opcode) {
		Set<Integer> replacement = new HashSet<Integer>();
		if (opcodesInt.contains(opcode))
			replacement.addAll(opcodesInt);
		else if (opcodesLong.contains(opcode))
			replacement.addAll(opcodesLong);
		else if (opcodesFloat.contains(opcode))
			replacement.addAll(opcodesFloat);
		else if (opcodesDouble.contains(opcode))
			replacement.addAll(opcodesDouble);

		replacement.remove(opcode);
		return replacement;
	}

	public InsnList getInfectionDistance(int opcodeOrig, int opcodeNew) {
		InsnList distance = new InsnList();

		if (opcodesInt.contains(opcodeOrig)) {
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceArithmeticOperator",
			        "getInfectionDistanceInt", "(IIII)D"));
		} else if (opcodesLong.contains(opcodeOrig)) {
			distance.add(new VarInsnNode(Opcodes.LSTORE, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new VarInsnNode(Opcodes.LLOAD, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2_X2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceArithmeticOperator",
			        "getInfectionDistanceLong", "(JJII)D"));
			numVariable += 2;
		} else if (opcodesFloat.contains(opcodeOrig)) {
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceArithmeticOperator",
			        "getInfectionDistanceFloat", "(FFII)D"));
		} else if (opcodesDouble.contains(opcodeOrig)) {
			distance.add(new VarInsnNode(Opcodes.DSTORE, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new VarInsnNode(Opcodes.DLOAD, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2_X2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceArithmeticOperator",
			        "getInfectionDistanceDouble", "(DDII)D"));
			numVariable += 2;
		}

		return distance;
	}

	private static boolean hasDivZeroError(int opcode) {
		switch (opcode) {
		case Opcodes.IDIV:
		case Opcodes.IREM:
		case Opcodes.FDIV:
		case Opcodes.FREM:
		case Opcodes.LDIV:
		case Opcodes.LREM:
		case Opcodes.DDIV:
		case Opcodes.DREM:
			return true;
		default:
			return false;
		}
	}

	public static double getInfectionDistanceInt(int x, int y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		int origValue = calculate(x, y, opcodeOrig);
		int newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	public static double getInfectionDistanceLong(long x, long y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0L) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		long origValue = calculate(x, y, opcodeOrig);
		long newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	public static double getInfectionDistanceFloat(float x, float y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0.0F) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		float origValue = calculate(x, y, opcodeOrig);
		float newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	public static double getInfectionDistanceDouble(double x, double y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0.0) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		double origValue = calculate(x, y, opcodeOrig);
		double newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	public static int calculate(int x, int y, int opcode) {
		switch (opcode) {
		case Opcodes.IADD:
			return x + y;
		case Opcodes.ISUB:
			return x - y;
		case Opcodes.IMUL:
			return x * y;
		case Opcodes.IDIV:
			return x / y;
		case Opcodes.IREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	public static long calculate(long x, long y, int opcode) {
		switch (opcode) {
		case Opcodes.LADD:
			return x + y;
		case Opcodes.LSUB:
			return x - y;
		case Opcodes.LMUL:
			return x * y;
		case Opcodes.LDIV:
			return x / y;
		case Opcodes.LREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	public static float calculate(float x, float y, int opcode) {
		switch (opcode) {
		case Opcodes.FADD:
			return x + y;
		case Opcodes.FSUB:
			return x - y;
		case Opcodes.FMUL:
			return x * y;
		case Opcodes.FDIV:
			return x / y;
		case Opcodes.FREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	public static double calculate(double x, double y, int opcode) {
		switch (opcode) {
		case Opcodes.DADD:
			return x + y;
		case Opcodes.DSUB:
			return x - y;
		case Opcodes.DMUL:
			return x * y;
		case Opcodes.DDIV:
			return x / y;
		case Opcodes.DREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		AbstractInsnNode node = instruction.getASMNode();
		int opcode = node.getOpcode();
		if (opcodesInt.contains(opcode))
			return true;
		else if (opcodesLong.contains(opcode))
			return true;
		else if (opcodesFloat.contains(opcode))
			return true;
		else if (opcodesDouble.contains(opcode))
			return true;

		return false;
	}
}
