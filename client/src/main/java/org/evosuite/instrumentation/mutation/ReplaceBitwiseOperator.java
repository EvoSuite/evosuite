/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

package org.evosuite.instrumentation.mutation;

import org.evosuite.PackageInfo;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.*;


/**
 * <p>ReplaceBitwiseOperator class.</p>
 *
 * @author Gordon Fraser
 */
public class ReplaceBitwiseOperator implements MutationOperator {

    public static final String NAME = "ReplaceBitwiseOperator";

    private static final Set<Integer> opcodesInt = new HashSet<>();

    private static final Set<Integer> opcodesIntShift = new HashSet<>();

    private static final Set<Integer> opcodesLong = new HashSet<>();

    private static final Set<Integer> opcodesLongShift = new HashSet<>();

    private int numVariable = 0;

    static {
        opcodesInt.addAll(Arrays.asList(Opcodes.IAND, Opcodes.IOR,
                Opcodes.IXOR));

        opcodesIntShift.addAll(Arrays.asList(Opcodes.ISHL, Opcodes.ISHR,
                Opcodes.IUSHR));

        opcodesLong.addAll(Arrays.asList(Opcodes.LAND, Opcodes.LOR,
                Opcodes.LXOR));

        opcodesLongShift.addAll(Arrays.asList(Opcodes.LSHL, Opcodes.LSHR,
                Opcodes.LUSHR));
    }

    /* (non-Javadoc)
     * @see org.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, org.evosuite.cfg.BytecodeInstruction)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Mutation> apply(MethodNode mn, String className, String methodName,
                                BytecodeInstruction instruction, Frame frame) {

        numVariable = ReplaceArithmeticOperator.getNextIndex(mn);

        // TODO: Check if this operator is applicable at all first
        // Should we do this via a method defined in the interface?
        InsnNode node = (InsnNode) instruction.getASMNode();

        List<Mutation> mutations = new LinkedList<>();
        Set<Integer> replacement = new HashSet<>();
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
            Mutation mutationObject = MutationPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).addMutation(className,
                    methodName,
                    NAME + " "
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
                return ">> I";
            case Opcodes.LSHR:
                return ">> L";
            case Opcodes.ISHL:
                return "<< I";
            case Opcodes.LSHL:
                return "<< L";
            case Opcodes.IUSHR:
                return ">>> I";
            case Opcodes.LUSHR:
                return ">>> L";
        }
        throw new RuntimeException("Unknown opcode: " + opcode);
    }

    /**
     * <p>getInfectionDistance</p>
     *
     * @param opcodeOrig a int.
     * @param opcodeNew  a int.
     * @return a {@link org.objectweb.asm.tree.InsnList} object.
     */
    public InsnList getInfectionDistance(int opcodeOrig, int opcodeNew) {
        InsnList distance = new InsnList();

        if (opcodesInt.contains(opcodeOrig)) {
            distance.add(new InsnNode(Opcodes.DUP2));
            distance.add(new LdcInsnNode(opcodeOrig));
            distance.add(new LdcInsnNode(opcodeNew));
            distance.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(ReplaceBitwiseOperator.class),
                    "getInfectionDistanceInt", "(IIII)D", false));
        } else if (opcodesIntShift.contains(opcodeOrig)) {
            distance.add(new InsnNode(Opcodes.DUP2));
            distance.add(new LdcInsnNode(opcodeOrig));
            distance.add(new LdcInsnNode(opcodeNew));
            distance.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(ReplaceBitwiseOperator.class),
                    "getInfectionDistanceInt", "(IIII)D", false));
        } else if (opcodesLong.contains(opcodeOrig)) {

            distance.add(new VarInsnNode(Opcodes.LSTORE, numVariable));
            distance.add(new InsnNode(Opcodes.DUP2));
            distance.add(new VarInsnNode(Opcodes.LLOAD, numVariable));
            distance.add(new InsnNode(Opcodes.DUP2_X2));
            distance.add(new LdcInsnNode(opcodeOrig));
            distance.add(new LdcInsnNode(opcodeNew));
            distance.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(ReplaceBitwiseOperator.class),
                    "getInfectionDistanceLong", "(JJII)D", false));
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
                    PackageInfo.getNameWithSlash(ReplaceBitwiseOperator.class),
                    "getInfectionDistanceLong", "(JIII)D", false));
            numVariable += 1;
        }

        return distance;
    }

    /**
     * <p>getInfectionDistanceInt</p>
     *
     * @param x          a int.
     * @param y          a int.
     * @param opcodeOrig a int.
     * @param opcodeNew  a int.
     * @return a double.
     */
    public static double getInfectionDistanceInt(int x, int y, int opcodeOrig,
                                                 int opcodeNew) {
        if (opcodeOrig == Opcodes.ISHR && opcodeNew == Opcodes.IUSHR) {
            if (x < 0 && y != 0) {
                int origValue = calculate(x, y, opcodeOrig);
                int newValue = calculate(x, y, opcodeNew);
                assert (origValue != newValue);

                return 0.0;
            } else
                // TODO x >= 0?
                return y != 0 && x > 0 ? x + 1.0 : 1.0;
        }
        int origValue = calculate(x, y, opcodeOrig);
        int newValue = calculate(x, y, opcodeNew);
        return origValue == newValue ? 1.0 : 0.0;
    }

    /**
     * <p>getInfectionDistanceLong</p>
     *
     * @param x          a long.
     * @param y          a int.
     * @param opcodeOrig a int.
     * @param opcodeNew  a int.
     * @return a double.
     */
    public static double getInfectionDistanceLong(long x, int y, int opcodeOrig,
                                                  int opcodeNew) {
        if (opcodeOrig == Opcodes.LSHR && opcodeNew == Opcodes.LUSHR) {
            if (x < 0 && y != 0) {
                long origValue = calculate(x, y, opcodeOrig);
                long newValue = calculate(x, y, opcodeNew);
                assert (origValue != newValue);

                return 0.0;
            } else
                return y != 0 && x > 0 ? x + 1.0 : 1.0;
        }
        long origValue = calculate(x, y, opcodeOrig);
        long newValue = calculate(x, y, opcodeNew);
        return origValue == newValue ? 1.0 : 0.0;
    }

    /**
     * <p>getInfectionDistanceLong</p>
     *
     * @param x          a long.
     * @param y          a long.
     * @param opcodeOrig a int.
     * @param opcodeNew  a int.
     * @return a double.
     */
    public static double getInfectionDistanceLong(long x, long y, int opcodeOrig,
                                                  int opcodeNew) {

        long origValue = calculate(x, y, opcodeOrig);
        long newValue = calculate(x, y, opcodeNew);
        return origValue == newValue ? 1.0 : 0.0;
    }

    /**
     * <p>calculate</p>
     *
     * @param x      a int.
     * @param y      a int.
     * @param opcode a int.
     * @return a int.
     */
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

    /**
     * <p>calculate</p>
     *
     * @param x      a long.
     * @param y      a long.
     * @param opcode a int.
     * @return a long.
     */
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
     * @see org.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(org.evosuite.cfg.BytecodeInstruction)
     */

    /**
     * {@inheritDoc}
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
        else return opcodesLongShift.contains(opcode);
    }
}
