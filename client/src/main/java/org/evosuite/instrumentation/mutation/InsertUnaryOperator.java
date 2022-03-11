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

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.instrumentation.BooleanValueInterpreter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * InsertUnaryOperator class.
 * </p>
 *
 * @author fraser
 */
public class InsertUnaryOperator implements MutationOperator {

    private static final Logger logger = LoggerFactory.getLogger(InsertUnaryOperator.class);

    public static final String NAME = "InsertUnaryOp";

    /* (non-Javadoc)
     * @see org.evosuite.cfg.instrumentation.mutation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, org.evosuite.cfg.BytecodeInstruction)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Mutation> apply(MethodNode mn, String className, String methodName,
                                BytecodeInstruction instruction, Frame frame) {
        // TODO - need to keep InsnList in Mutation, not only Instruction?

        // Mutation: Insert an INEG _after_ an iload
        List<Mutation> mutations = new LinkedList<>();
        List<InsnList> mutationCode = new LinkedList<>();
        List<String> descriptions = new LinkedList<>();

        if (instruction.getASMNode() instanceof VarInsnNode) {
            try {
                InsnList mutation = new InsnList();
                VarInsnNode node = (VarInsnNode) instruction.getASMNode();

                // insert mutation into bytecode with conditional
                mutation.add(new VarInsnNode(node.getOpcode(), node.var));
                mutation.add(new InsnNode(getNegation(node.getOpcode())));
                mutationCode.add(mutation);

                if (!mn.localVariables.isEmpty())
                    descriptions.add("Negation of " + getName(mn, node));
                else
                    descriptions.add("Negation");

                if (node.getOpcode() == Opcodes.ILOAD) {
                    if (frame.getStack(frame.getStackSize() - 1) != BooleanValueInterpreter.BOOLEAN_VALUE) {
                        mutation = new InsnList();
                        mutation.add(new IincInsnNode(node.var, 1));
                        mutation.add(new VarInsnNode(node.getOpcode(), node.var));
                        if (!mn.localVariables.isEmpty())
                            descriptions.add("IINC 1 " + getName(mn, node));
                        else
                            descriptions.add("IINC 1");
                        mutationCode.add(mutation);

                        mutation = new InsnList();
                        mutation.add(new IincInsnNode(node.var, -1));
                        mutation.add(new VarInsnNode(node.getOpcode(), node.var));
                        if (!mn.localVariables.isEmpty())
                            descriptions.add("IINC -1 " + getName(mn, node));
                        else
                            descriptions.add("IINC -1");
                        mutationCode.add(mutation);
                    }
                }
            } catch (VariableNotFoundException e) {
                logger.info("Could not find variable: " + e);
                return new ArrayList<>();
            }
        } else {
            InsnList mutation = new InsnList();
            FieldInsnNode node = (FieldInsnNode) instruction.getASMNode();
            Type type = Type.getType(node.desc);
            mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
                    node.desc));
            mutation.add(new InsnNode(getNegation(type)));
            descriptions.add("Negation");
            mutationCode.add(mutation);

            if (type == Type.INT_TYPE) {
                mutation = new InsnList();
                mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
                        node.desc));
                mutation.add(new InsnNode(Opcodes.ICONST_1));
                mutation.add(new InsnNode(Opcodes.IADD));
                descriptions.add("+1");
                mutationCode.add(mutation);

                mutation = new InsnList();
                mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
                        node.desc));
                mutation.add(new InsnNode(Opcodes.ICONST_M1));
                mutation.add(new InsnNode(Opcodes.IADD));
                descriptions.add("-1");
                mutationCode.add(mutation);
            }
        }

        int i = 0;
        for (InsnList mutation : mutationCode) {
            // insert mutation into pool
            Mutation mutationObject = MutationPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).addMutation(className,
                    methodName,
                    NAME + " "
                            + descriptions.get(i++),
                    instruction,
                    mutation,
                    Mutation.getDefaultInfectionDistance());

            mutations.add(mutationObject);
        }
        return mutations;
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
        switch (node.getOpcode()) {
            case Opcodes.ILOAD:
            case Opcodes.LLOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
                return true;
            case Opcodes.GETFIELD:
            case Opcodes.GETSTATIC:
                FieldInsnNode fieldNode = (FieldInsnNode) instruction.getASMNode();
                Type type = Type.getType(fieldNode.desc);
                if (type == Type.BYTE_TYPE || type == Type.SHORT_TYPE
                        || type == Type.LONG_TYPE || type == Type.FLOAT_TYPE
                        || type == Type.DOUBLE_TYPE || type == Type.BOOLEAN_TYPE
                        || type == Type.INT_TYPE) {
                    return true;
                }
            default:
                return false;
        }
    }

    private int getNegation(Type type) {
        if (type.equals(Type.BYTE_TYPE)) {
            return Opcodes.INEG;
        } else if (type == Type.SHORT_TYPE) {
            return Opcodes.INEG;
        } else if (type == Type.LONG_TYPE) {
            return Opcodes.LNEG;
        } else if (type == Type.FLOAT_TYPE) {
            return Opcodes.FNEG;
        } else if (type == Type.DOUBLE_TYPE) {
            return Opcodes.DNEG;
        } else if (type == Type.BOOLEAN_TYPE) {
            return Opcodes.INEG;
        } else if (type == Type.INT_TYPE) {
            return Opcodes.INEG;
        } else {
            throw new RuntimeException("Don't know how to negate type " + type);
        }
    }

    private int getNegation(int opcode) {
        switch (opcode) {
            case Opcodes.ILOAD:
                return Opcodes.INEG;
            case Opcodes.LLOAD:
                return Opcodes.LNEG;
            case Opcodes.FLOAD:
                return Opcodes.FNEG;
            case Opcodes.DLOAD:
                return Opcodes.DNEG;
            default:
                throw new RuntimeException("Invalid opcode for negation: " + opcode);
        }
    }

    private String getName(MethodNode mn, AbstractInsnNode node)
            throws VariableNotFoundException {
        if (node instanceof VarInsnNode) {
            LocalVariableNode var = getLocal(mn, node, ((VarInsnNode) node).var);
            return var.name;
        } else if (node instanceof FieldInsnNode) {
            return ((FieldInsnNode) node).name;
        } else if (node instanceof IincInsnNode) {
            IincInsnNode incNode = (IincInsnNode) node;
            LocalVariableNode var = getLocal(mn, node, incNode.var);
            return var.name;

        } else {
            throw new RuntimeException("Unknown variable node: " + node);
        }
    }

    private LocalVariableNode getLocal(MethodNode mn, AbstractInsnNode node, int index)
            throws VariableNotFoundException {
        int currentId = mn.instructions.indexOf(node);
        for (Object v : mn.localVariables) {
            LocalVariableNode localVar = (LocalVariableNode) v;
            int startId = mn.instructions.indexOf(localVar.start);
            int endId = mn.instructions.indexOf(localVar.end);
            if (currentId >= startId && currentId <= endId && localVar.index == index)
                return localVar;
        }

        throw new VariableNotFoundException("Could not find local variable " + index
                + " at position " + currentId + ", have variables: "
                + mn.localVariables.size());
    }
}
