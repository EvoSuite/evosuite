/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
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
public class ReplaceBitwiseOperator implements MutationOperator {

	private static Set<Integer> opcodesInt = new HashSet<Integer>();

	private static Set<Integer> opcodesIntShift = new HashSet<Integer>();

	private static Set<Integer> opcodesLong = new HashSet<Integer>();

	private static Set<Integer> opcodesLongShift = new HashSet<Integer>();

	private int numVariable = 0;

	static {
		opcodesInt.addAll(Arrays.asList(new Integer[] { Opcodes.IAND, Opcodes.IOR,
		        Opcodes.IXOR }));

		opcodesIntShift.addAll(Arrays.asList(new Integer[] { Opcodes.ISHL, Opcodes.ISHR,
		        Opcodes.IUSHR }));

		opcodesLong.addAll(Arrays.asList(new Integer[] { Opcodes.LAND, Opcodes.LOR,
		        Opcodes.LXOR }));

		opcodesLongShift.addAll(Arrays.asList(new Integer[] { Opcodes.LSHL, Opcodes.LSHR,
		        Opcodes.LUSHR }));
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction) {

		numVariable = ReplaceArithmeticOperator.getNextIndex(mn);

		// TODO: Check if this operator is applicable at all first
		// Should we do this via a method defined in the interface?
		InsnNode node = (InsnNode) instruction.getASMNode();

		List<Mutation> mutations = new LinkedList<Mutation>();
		Set<Integer> replacement = new HashSet<Integer>();
		if (opcodesInt.contains(node.getOpcode()))
			replacement.addAll(opcodesInt);
		else if (opcodesIntShift.contains(node.getOpcode()))
			replacement.addAll(opcodesIntShift);
		else if (opcodesLong.contains(node.getOpcode()))
			replacement.addAll(opcodesLong);
		else if (opcodesLongShift.contains(node.getOpcode()))
			replacement.addAll(opcodesLongShift);
		replacement.remove(node.getOpcode());

		for (int opcode : replacement) {

			InsnNode mutation = new InsnNode(opcode);
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(className,
			                                                   methodName,
			                                                   "ReplaceBitwiseOperator "
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

	private String getOp(int opcode) {
		switch (opcode) {
		case Opcodes.IAND:
		case Opcodes.LAND:
			return "&";
		case Opcodes.IOR:
		case Opcodes.LOR:
			return "|";
		case Opcodes.IXOR:
		case Opcodes.LXOR:
			return "^";
		case Opcodes.ISHR:
		case Opcodes.LSHR:
			return ">>";
		case Opcodes.ISHL:
		case Opcodes.LSHL:
			return "<<";
		case Opcodes.IUSHR:
		case Opcodes.LUSHR:
			return ">>>";
		}
		throw new RuntimeException("Unknown opcode: " + opcode);
	}

	public InsnList getInfectionDistance(int opcodeOrig, int opcodeNew) {
		InsnList distance = new InsnList();

		if (opcodesInt.contains(opcodeOrig)) {
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceBitwiseOperator",
			        "getInfectionDistanceInt", "(IIII)D"));
		} else if (opcodesIntShift.contains(opcodeOrig)) {
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceBitwiseOperator",
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
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceBitwiseOperator",
			        "getInfectionDistanceLong", "(JJII)D"));
			numVariable += 2;

		} else if (opcodesLongShift.contains(opcodeOrig)) {
			distance.add(new VarInsnNode(Opcodes.ISTORE, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new VarInsnNode(Opcodes.ILOAD, numVariable));
			distance.add(new InsnNode(Opcodes.DUP_X2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceBitwiseOperator",
			        "getInfectionDistanceLong", "(JIII)D"));
			numVariable += 1;
		}

		return distance;
	}

	public static double getInfectionDistanceInt(int x, int y, int opcodeOrig,
	        int opcodeNew) {
		if (opcodeOrig == Opcodes.ISHR && opcodeNew == Opcodes.IUSHR) {
			if (x < 0 && y != 0) {
				int origValue = calculate(x, y, opcodeOrig);
				int newValue = calculate(x, y, opcodeNew);
				assert (origValue != newValue);

				return 0.0;
			} else
				return y != 0 && x > 0 ? x + 1 : 1.0;
		}
		int origValue = calculate(x, y, opcodeOrig);
		int newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	public static double getInfectionDistanceLong(long x, int y, int opcodeOrig,
	        int opcodeNew) {
		if (opcodeOrig == Opcodes.LSHR && opcodeNew == Opcodes.LUSHR) {
			if (x < 0 && y != 0) {
				long origValue = calculate(x, y, opcodeOrig);
				long newValue = calculate(x, y, opcodeNew);
				assert (origValue != newValue);

				return 0.0;
			} else
				return y != 0 && x > 0 ? x + 1 : 1.0;
		}
		long origValue = calculate(x, y, opcodeOrig);
		long newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	public static double getInfectionDistanceLong(long x, long y, int opcodeOrig,
	        int opcodeNew) {

		long origValue = calculate(x, y, opcodeOrig);
		long newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	public static int calculate(int x, int y, int opcode) {
		switch (opcode) {
		case Opcodes.IAND:
			return x & y;
		case Opcodes.IOR:
			return x | y;
		case Opcodes.IXOR:
			return x ^ y;
		case Opcodes.ISHL:
			return x << y;
		case Opcodes.ISHR:
			return x >> y;
		case Opcodes.IUSHR:
			return x >>> y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	public static long calculate(long x, long y, int opcode) {
		switch (opcode) {
		case Opcodes.LAND:
			return x & y;
		case Opcodes.LOR:
			return x | y;
		case Opcodes.LXOR:
			return x ^ y;
		case Opcodes.LSHL:
			return x << y;
		case Opcodes.LSHR:
			return x >> y;
		case Opcodes.LUSHR:
			return x >>> y;
		}
		throw new RuntimeException("Unknown long opcode: " + opcode);
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
		else if (opcodesIntShift.contains(opcode))
			return true;
		else if (opcodesLong.contains(opcode))
			return true;
		else if (opcodesLongShift.contains(opcode))
			return true;

		return false;
	}
}
