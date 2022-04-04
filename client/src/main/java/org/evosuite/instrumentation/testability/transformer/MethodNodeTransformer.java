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

package org.evosuite.instrumentation.testability.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * <p>MethodNodeTransformer class.</p>
 *
 * @author Gordon Fraser
 */
public class MethodNodeTransformer {

    /**
     * Mapping from old to new local variable indexes. A local variable at index
     * i of size 1 is remapped to 'mapping[2*i]', while a local variable at
     * index i of size 2 is remapped to 'mapping[2*i+1]'.
     */
    protected int[] mapping = new int[40];

    /**
     * Array used to store stack map local variable types after remapping.
     */
    protected Object[] newLocals = new Object[20];

    /**
     * Index of the first local variable, after formal parameters.
     */
    protected int firstLocal;

    /**
     * Index of the next local variable to be created by {@link #newLocal}.
     */
    protected int nextLocal;

    /**
     * Types of the local variables of the method visited by this adapter.
     */
    protected final List<Type> localTypes = new ArrayList<>();

    /**
     * <p>transform</p>
     *
     * @param mn a {@link org.objectweb.asm.tree.MethodNode} object.
     */
    public void transform(MethodNode mn) {

        setupLocals(mn);

        AbstractInsnNode node = mn.instructions.getFirst();

        boolean finished = false;
        while (!finished) {
            if (node instanceof MethodInsnNode) {
                node = transformMethodInsnNode(mn, (MethodInsnNode) node);
            } else if (node instanceof VarInsnNode) {
                node = transformVarInsnNode(mn, (VarInsnNode) node);
            } else if (node instanceof FieldInsnNode) {
                node = transformFieldInsnNode(mn, (FieldInsnNode) node);
            } else if (node instanceof InsnNode) {
                node = transformInsnNode(mn, (InsnNode) node);
            } else if (node instanceof TypeInsnNode) {
                node = transformTypeInsnNode(mn, (TypeInsnNode) node);
            } else if (node instanceof JumpInsnNode) {
                node = transformJumpInsnNode(mn, (JumpInsnNode) node);
            } else if (node instanceof LabelNode) {
                node = transformLabelNode(mn, (LabelNode) node);
            } else if (node instanceof IntInsnNode) {
                node = transformIntInsnNode(mn, (IntInsnNode) node);
            } else if (node instanceof MultiANewArrayInsnNode) {
                node = transformMultiANewArrayInsnNode(mn, (MultiANewArrayInsnNode) node);
            }

            if (node == mn.instructions.getLast()) {
                finished = true;
            } else {
                node = node.getNext();
            }
        }
    }

    /**
     * <p>transformMethodInsnNode</p>
     *
     * @param mn         a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param methodNode a {@link org.objectweb.asm.tree.MethodInsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformMethodInsnNode(MethodNode mn,
                                                       MethodInsnNode methodNode) {
        return methodNode;
    }

    /**
     * <p>transformVarInsnNode</p>
     *
     * @param mn      a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param varNode a {@link org.objectweb.asm.tree.VarInsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
        return varNode;
    }

    /**
     * <p>transformFieldInsnNode</p>
     *
     * @param mn        a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param fieldNode a {@link org.objectweb.asm.tree.FieldInsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
                                                      FieldInsnNode fieldNode) {
        return fieldNode;
    }

    /**
     * <p>transformInsnNode</p>
     *
     * @param mn       a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param insnNode a {@link org.objectweb.asm.tree.InsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
        return insnNode;
    }

    /**
     * <p>transformTypeInsnNode</p>
     *
     * @param mn       a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param typeNode a {@link org.objectweb.asm.tree.TypeInsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformTypeInsnNode(MethodNode mn, TypeInsnNode typeNode) {
        return typeNode;
    }

    /**
     * <p>transformJumpInsnNode</p>
     *
     * @param mn       a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param jumpNode a {@link org.objectweb.asm.tree.JumpInsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformJumpInsnNode(MethodNode mn, JumpInsnNode jumpNode) {
        return jumpNode;
    }

    /**
     * <p>transformLabelNode</p>
     *
     * @param mn        a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param labelNode a {@link org.objectweb.asm.tree.LabelNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformLabelNode(MethodNode mn, LabelNode labelNode) {
        return labelNode;
    }

    /**
     * <p>transformIntInsnNode</p>
     *
     * @param mn          a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param intInsnNode a {@link org.objectweb.asm.tree.IntInsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformIntInsnNode(MethodNode mn, IntInsnNode intInsnNode) {
        return intInsnNode;
    }

    /**
     * <p>transformMultiANewArrayInsnNode</p>
     *
     * @param mn            a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param arrayInsnNode a {@link org.objectweb.asm.tree.MultiANewArrayInsnNode} object.
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    protected AbstractInsnNode transformMultiANewArrayInsnNode(MethodNode mn,
                                                               MultiANewArrayInsnNode arrayInsnNode) {
        return arrayInsnNode;
    }

    protected void setupLocals(MethodNode mn) {
        Type[] args = Type.getArgumentTypes(mn.desc);
        nextLocal = (Opcodes.ACC_STATIC & mn.access) == 0 ? 1 : 0;
        for (final Type arg : args) {
            nextLocal += arg.getSize();
        }
        firstLocal = nextLocal;
    }


    // TODO: Everything from here on needs to be finished, it is just copy&amp;paste for now

    /**
     * Generates the instruction to store the top stack value in the given local
     * variable.
     *
     * @param local a local variable identifier, as returned by
     *              {@link LocalVariablesSorter#newLocal(Type) newLocal()}.
     */
    public void storeLocal(final int local) {
        storeInsn(getLocalType(local), local);
    }

    /**
     * Generates the instruction to load the given local variable on the stack.
     *
     * @param local a local variable identifier, as returned by
     *              {@link LocalVariablesSorter#newLocal(Type) newLocal()}.
     */
    public void loadLocal(final int local) {
        loadInsn(getLocalType(local), local);
    }


    /**
     * Returns the type of the given local variable.
     *
     * @param local a local variable identifier, as returned by
     *              {@link LocalVariablesSorter#newLocal(Type) newLocal()}.
     * @return the type of the given local variable.
     */
    public Type getLocalType(final int local) {
        return localTypes.get(local - firstLocal);
    }

    /**
     * Creates a new local variable of the given type.
     *
     * @param type the type of the local variable to be created.
     * @return the identifier of the newly created local variable.
     */
    public int newLocal(final Type type) {
        Object t;
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                t = Opcodes.INTEGER;
                break;
            case Type.FLOAT:
                t = Opcodes.FLOAT;
                break;
            case Type.LONG:
                t = Opcodes.LONG;
                break;
            case Type.DOUBLE:
                t = Opcodes.DOUBLE;
                break;
            case Type.ARRAY:
                t = type.getDescriptor();
                break;
            // case Type.OBJECT:
            default:
                t = type.getInternalName();
                break;
        }
        int local = newLocalMapping(type);
        setLocalType(local, type);
        setFrameLocal(local, t);
        return local;
    }

    protected int newLocalMapping(final Type type) {
        int local = nextLocal;
        nextLocal += type.getSize();
        return local;
    }

    private void setFrameLocal(final int local, final Object type) {
        int l = newLocals.length;
        if (local >= l) {
            Object[] a = new Object[Math.max(2 * l, local + 1)];
            System.arraycopy(newLocals, 0, a, 0, l);
            newLocals = a;
        }
        newLocals[local] = type;
    }

    /**
     * Notifies subclasses that a local variable has been added or remapped. The
     * default implementation of this method does nothing.
     *
     * @param local a local variable identifier, as returned by {@link #newLocal
     *              newLocal()}.
     * @param type  the type of the value being stored in the local variable.
     */
    protected void setLocalType(final int local, final Type type) {
    }

    /**
     * Generates the instruction to store the top stack value in a local
     * variable.
     *
     * @param type  the type of the local variable to be stored.
     * @param index an index in the frame's local variables array.
     */
    private void storeInsn(final Type type, final int index) {
        // TODO: Insert the following
        // new VarInsnNode(type.getOpcode(Opcodes.ISTORE), index);
    }

    /**
     * Generates the instruction to push a local variable on the stack.
     *
     * @param type  the type of the local variable to be loaded.
     * @param index an index in the frame's local variables array.
     */
    private void loadInsn(final Type type, final int index) {
        // TODO: Insert the following
        // new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index);
    }

    protected Map<Integer, Integer> parameterToLocalMap = new HashMap<>();

    protected void popParametersToLocals(MethodNode mn) {
        Type[] args = Type.getArgumentTypes(mn.desc);
        for (int i = args.length - 1; i >= 0; i--) {
            int loc = newLocal(args[i]);
            storeLocal(loc);
            parameterToLocalMap.put(i, loc);
        }
    }

    protected void pushParametersToLocals(MethodNode mn) {
        Type[] args = Type.getArgumentTypes(mn.desc);
        for (int i = 0; i < args.length; i++) {
            loadLocal(parameterToLocalMap.get(i));
        }
        parameterToLocalMap.clear();
    }

}
