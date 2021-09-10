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
package org.evosuite.graphs.cfg;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

// TODO: the following methods about control dependence are flawed right now:
// - the BytecodeInstruction of a Branch does not have it's control dependent
// branchId
// but it's own branchId set
// - this seems to be OK for ChromosomeRecycling as it stands, but
// especially getControlDependentBranch() will fail hard when called on a Branch
// the same may hold for the other ones as well.
// - look at BranchCoverageGoal and Branch for more information

/**
 * Wrapper class for the underlying byteCode instruction library ASM
 * <p>
 * Gives access to a lot of methods that interpret the raw information in
 * AbstractInsnNode to usable chunks of information, inside EvoSuite
 * <p>
 * This class is supposed to hide the ASM library from the rest of EvoSuite as
 * much as possible
 * <p>
 * After initialization, all information about byteCode instructions should be
 * accessible via the BytecodeInstruction-, DefUse- and BranchPool. Each of
 * those has data structures holding all BytecodeInstruction, DefUse and Branch
 * objects respectively created during initialization.
 * <p>
 * BytecodeInstruction directly extends ASMWrapper and is the first way to
 * instantiate an ASMWrapper. Branch and DefUse extend BytecodeInstruction,
 * where DefUse is abstract and Branch is not ("concrete"?) DefUse is further
 * extended by Definition and Use
 *
 * @author Andre Mis
 */
public abstract class ASMWrapper {

    // from ASM library
    protected AbstractInsnNode asmNode;
    protected CFGFrame frame;

    /**
     * <p>
     * getASMNode
     * </p>
     *
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    public AbstractInsnNode getASMNode() {
        return asmNode;
    }

    /**
     * <p>
     * getInstructionType
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstructionType() {

        if (asmNode.getOpcode() >= 0 && asmNode.getOpcode() < Printer.OPCODES.length)
            return Printer.OPCODES[asmNode.getOpcode()];

        if (isLineNumber())
            return "LINE " + this.getLineNumber();

        return getType();
    }

    public String getMethodCallDescriptor() {
        if (!isMethodCall())
            return null;
        MethodInsnNode meth = (MethodInsnNode) asmNode;
        return meth.desc;
    }

    /**
     * <p>
     * getType
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        // TODO explain
        String type = "";
        if (asmNode.getType() >= 0 && asmNode.getType() < Printer.TYPES.length)
            type = Printer.TYPES[asmNode.getType()];

        return type;
    }

    /**
     * <p>
     * getInstructionId
     * </p>
     *
     * @return a int.
     */
    public abstract int getInstructionId();

    /**
     * <p>
     * getMethodName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getMethodName();

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getClassName();

    public abstract boolean isMethodCallOfField();

    public abstract String getFieldMethodCallName();

    // methods for branch analysis

    /**
     * <p>
     * isActualBranch
     * </p>
     *
     * @return a boolean.
     */
    public boolean isActualBranch() {
        return isBranch() || isSwitch();
    }

    /**
     * <p>
     * isSwitch
     * </p>
     *
     * @return a boolean.
     */
    public boolean isSwitch() {
        return isLookupSwitch() || isTableSwitch();
    }

    /**
     * <p>
     * canReturnFromMethod
     * </p>
     *
     * @return a boolean.
     */
    public boolean canReturnFromMethod() {
        return isReturn() || isThrow();
    }

    /**
     * <p>
     * isReturn
     * </p>
     *
     * @return a boolean.
     */
    public boolean isReturn() {
        switch (asmNode.getOpcode()) {
            case Opcodes.RETURN:
            case Opcodes.ARETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
                return true;
            default:
                return false;
        }
    }

    /**
     * <p>
     * isThrow
     * </p>
     *
     * @return a boolean.
     */
    public boolean isThrow() {
        // TODO: Need to check if this is a caught exception?
        return asmNode.getOpcode() == Opcodes.ATHROW;
    }

    /**
     * <p>
     * isTableSwitch
     * </p>
     *
     * @return a boolean.
     */
    public boolean isTableSwitch() {
        return (asmNode instanceof TableSwitchInsnNode);
    }

    /**
     * <p>
     * isLookupSwitch
     * </p>
     *
     * @return a boolean.
     */
    public boolean isLookupSwitch() {
        return (asmNode instanceof LookupSwitchInsnNode);
    }

    /**
     * <p>
     * isBranchLabel
     * </p>
     *
     * @return a boolean.
     */
    public boolean isBranchLabel() {
        return asmNode instanceof LabelNode
                && ((LabelNode) asmNode).getLabel().info instanceof Integer;
    }

    /**
     * <p>
     * isJump
     * </p>
     *
     * @return a boolean.
     */
    public boolean isJump() {
        return (asmNode instanceof JumpInsnNode);
    }

    /**
     * <p>
     * isInvokeStatic
     * </p>
     *
     * @return a boolean representing whether the instruction is a static method
     * call.
     */
    public boolean isInvokeStatic() {
        if (asmNode instanceof MethodInsnNode) {
            return (asmNode.getOpcode() == Opcodes.INVOKESTATIC);
        }
        return false;
    }

    /**
     * <p>
     * isGoto
     * </p>
     *
     * @return a boolean.
     */
    public boolean isGoto() {
        if (asmNode instanceof JumpInsnNode) {
            return (asmNode.getOpcode() == Opcodes.GOTO);
        }
        return false;
    }

    /**
     * <p>
     * isBranch
     * </p>
     *
     * @return a boolean.
     */
    public boolean isBranch() {
        return (isJump() && !isGoto());
    }

    // public int getBranchId() {
    // // return ((Integer)((LabelNode)node).getLabel().info).intValue();
    // return line_no;
    // }

    /**
     * <p>
     * isIfNull
     * </p>
     *
     * @return a boolean.
     */
    public boolean isIfNull() {
        if (asmNode instanceof JumpInsnNode) {
            return (asmNode.getOpcode() == Opcodes.IFNULL);
        }
        return false;
    }

    /**
     * <p>
     * isFrame
     * </p>
     *
     * @return a boolean.
     */
    public boolean isFrame() {
        return asmNode instanceof FrameNode;
    }

    /**
     * <p>
     * isMethodCall
     * </p>
     *
     * @return a boolean.
     */
    public boolean isMethodCall() {
        return asmNode instanceof MethodInsnNode;
    }

    /**
     * Returns the conjunction of the name and method descriptor of the method
     * called by this instruction
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCalledMethod() {
        if (!isMethodCall())
            return null;
        MethodInsnNode meth = (MethodInsnNode) asmNode;
        return meth.name + meth.desc;
    }

    /**
     * Returns the conjunction of the name of the method called by this
     * instruction
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCalledMethodName() {
        if (!isMethodCall())
            return null;
        MethodInsnNode meth = (MethodInsnNode) asmNode;
        return meth.name;
    }

    /**
     * Returns true if and only if the class of the method called by this
     * instruction is the same as the given className
     *
     * @param className a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isMethodCallForClass(String className) {
        if (isMethodCall()) {
            // System.out.println("in isMethodCallForClass of "+toString()+" for arg "+className+" calledMethodsClass: "+getCalledMethodsClass()+" calledMethod "+getCalledMethod());
            return getCalledMethodsClass().equals(className);
        }
        return false;
    }

    /**
     * Returns the class of the method called by this instruction
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCalledMethodsClass() {
        if (isMethodCall()) {
            MethodInsnNode mn = (MethodInsnNode) asmNode;
            return mn.owner.replaceAll("/", "\\.");
        }
        return null;
    }

    /**
     * Returns the number of arguments of the method called by this instruction
     *
     * @return a int.
     */
    public int getCalledMethodsArgumentCount() {
        if (isMethodCall()) {
            // int r = 0;
            MethodInsnNode mn = (MethodInsnNode) asmNode;
            Type[] argTypes = Type.getArgumentTypes(mn.desc);

            return argTypes.length;
            // for(Type argType : argTypes) {
            // r+=argType.getSize();
            // }
            // return r;
        }
        return -1;
    }

    /**
     * <p>
     * isLoadConstant
     * </p>
     *
     * @return a boolean.
     */
    public boolean isLoadConstant() {
        return asmNode.getOpcode() == Opcodes.LDC;
    }

    /**
     * <p>
     * isConstant
     * </p>
     *
     * @return a boolean.
     */
    public boolean isConstant() {
        switch (asmNode.getOpcode()) {
            case Opcodes.LDC:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.ICONST_M1:
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                return true;
            default:
                return false;
        }
    }

    // methods for defUse analysis

    /**
     * <p>
     * isDefUse
     * </p>
     *
     * @return a boolean.
     */
    public boolean isDefUse() {
        return isDefinition() || isUse();
    }

    /**
     * <p>
     * isFieldDU
     * </p>
     *
     * @return a boolean.
     */
    public boolean isFieldDU() {
        return isFieldDefinition() || isFieldUse();
    }

    public boolean isFieldNodeDU() {
        return isFieldNodeDefinition() || isFieldNodeUse();
    }

    /**
     * <p>
     * isLocalDU
     * </p>
     *
     * @return a boolean.
     */
    public boolean isLocalDU() {
        return isLocalVariableDefinition() || isLocalVariableUse();
    }

    /**
     * <p>
     * isDefinition
     * </p>
     *
     * @return a boolean.
     */
    public boolean isDefinition() {
        return isFieldDefinition() || isLocalVariableDefinition();
    }

    /**
     * <p>
     * isUse
     * </p>
     *
     * @return a boolean.
     */
    public boolean isUse() {
        return isFieldUse() || isLocalVariableUse() || isArrayLoadInstruction();
    }

    public abstract boolean isFieldMethodCallDefinition();

    public abstract boolean isFieldMethodCallUse();

    /**
     * <p>
     * isFieldDefinition
     * </p>
     *
     * @return a boolean.
     */
    public boolean isFieldDefinition() {
        return asmNode.getOpcode() == Opcodes.PUTFIELD
                || asmNode.getOpcode() == Opcodes.PUTSTATIC || isFieldArrayDefinition()
                || isFieldMethodCallDefinition();
    }

    /**
     * <p>
     * isFieldDefinition
     * </p>
     *
     * @return a boolean.
     */
    public boolean isFieldNodeDefinition() {
        return asmNode.getOpcode() == Opcodes.PUTFIELD
                || asmNode.getOpcode() == Opcodes.PUTSTATIC || isFieldArrayDefinition();
    }

    /**
     * <p>
     * isFieldUse
     * </p>
     *
     * @return a boolean.
     */
    public boolean isFieldUse() {
        return asmNode.getOpcode() == Opcodes.GETFIELD
                || asmNode.getOpcode() == Opcodes.GETSTATIC || isFieldMethodCallUse();
    }

    /**
     * <p>
     * isFieldUse
     * </p>
     *
     * @return a boolean.
     */
    public boolean isFieldNodeUse() {
        return asmNode.getOpcode() == Opcodes.GETFIELD
                || asmNode.getOpcode() == Opcodes.GETSTATIC;
    }

    /**
     * <p>
     * isStaticDefUse
     * </p>
     *
     * @return a boolean.
     */
    public boolean isStaticDefUse() {
        return asmNode.getOpcode() == Opcodes.PUTSTATIC
                || asmNode.getOpcode() == Opcodes.GETSTATIC || isStaticArrayUsage();
    }

    // retrieving information about variable names from ASM

    /**
     * <p>
     * getDUVariableName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVariableName() {
        if (isArrayStoreInstruction()) {
            return getArrayVariableName();
        } else if (isArrayLoadInstruction()) {
            return getArrayVariableName();
        } else if (isLocalDU()) {
            return getLocalVariableName();
        } else if (isMethodCallOfField()) {
            return getFieldMethodCallName();
        } else if (isFieldDU()) {
            return getFieldName();
        } else {
            return null;
        }
    }

    /**
     * <p>
     * getFieldName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getFieldSimpleName() {
        FieldInsnNode fieldNode = (FieldInsnNode) asmNode;
        return fieldNode.name;
        // return fieldNode.name;
    }

    /**
     * <p>
     * getFieldName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getFieldName() {
        FieldInsnNode fieldNode = (FieldInsnNode) asmNode;
        return fieldNode.owner + "." + fieldNode.name;
        // return fieldNode.name;
    }

    /**
     * <p>
     * getFieldName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFieldType() {
        FieldInsnNode fieldNode = (FieldInsnNode) asmNode;
        return fieldNode.desc;
        // return fieldNode.name;
    }

    /**
     * <p>
     * getLocalVarName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getLocalVariableName() {
        String ret = getMethodName() + "_LV_" + getLocalVariableSlot();
        return ret;
    }

    // TODO unsafe

    /**
     * <p>
     * getLocalVar
     * </p>
     *
     * @return a int.
     */
    public int getLocalVariableSlot() {
        if (asmNode instanceof VarInsnNode)
            return ((VarInsnNode) asmNode).var;
        else if (asmNode instanceof IincInsnNode)
            return ((IincInsnNode) asmNode).var;
        else
            return -1;
    }

    /**
     * <p>
     * isLocalVarDefinition
     * </p>
     *
     * @return a boolean.
     */
    public boolean isLocalVariableDefinition() {
        return asmNode.getOpcode() == Opcodes.ISTORE
                || asmNode.getOpcode() == Opcodes.LSTORE
                || asmNode.getOpcode() == Opcodes.FSTORE
                || asmNode.getOpcode() == Opcodes.DSTORE
                || asmNode.getOpcode() == Opcodes.ASTORE
                || asmNode.getOpcode() == Opcodes.IINC || isLocalArrayDefinition();
    }

    /**
     * <p>
     * isLocalVarUse
     * </p>
     *
     * @return a boolean.
     */
    public boolean isLocalVariableUse() {
        return asmNode.getOpcode() == Opcodes.ILOAD
                || asmNode.getOpcode() == Opcodes.LLOAD
                || asmNode.getOpcode() == Opcodes.FLOAD
                || asmNode.getOpcode() == Opcodes.DLOAD
                || asmNode.getOpcode() == Opcodes.IINC
                || (asmNode.getOpcode() == Opcodes.ALOAD && !loadsReferenceToThis());
    }

    public boolean isIINC() {
        return asmNode.getOpcode() == Opcodes.IINC;
    }

    /**
     * Determines whether this is the special ALOAD that pushes 'this' onto the
     * stack
     * <p>
     * In non static methods the variable slot 0 holds the reference to "this".
     * Loading this reference is not seen as a Use for defuse purposes. This
     * method checks if this is the case
     *
     * @return a boolean.
     */
    public boolean loadsReferenceToThis() {
        if (getRawCFG().isStaticMethod()) {
            return false;
        }

        return asmNode.getOpcode() == Opcodes.ALOAD && getLocalVariableSlot() == 0;
    }

    public abstract RawControlFlowGraph getRawCFG();

    public boolean isLocalArrayDefinition() {
        if (!isArrayStoreInstruction())
            return false;

        // only local var if arrayref on stack is from local var use
        BytecodeInstruction arrayLoader = getSourceOfArrayReference();
        if (arrayLoader == null)
            return false;

        return arrayLoader.isLocalVariableUse();
    }

    public boolean isFieldArrayDefinition() {
        if (!isArrayStoreInstruction())
            return false;

        // only field var if arrayref on stack is from field var use
        BytecodeInstruction arrayLoader = getSourceOfArrayReference();
        if (arrayLoader == null)
            return false;

        return arrayLoader.isFieldUse();
    }

    public boolean isStaticArrayUsage() {
        if (!isArrayStoreInstruction())
            return false;

        BytecodeInstruction arrayLoader = getSourceOfArrayReference();
        if (arrayLoader == null)
            return false;

        return arrayLoader.isStaticDefUse();
    }

    public boolean isArrayStoreInstruction() {
        return asmNode.getOpcode() == Opcodes.IASTORE
                || asmNode.getOpcode() == Opcodes.LASTORE
                || asmNode.getOpcode() == Opcodes.FASTORE
                || asmNode.getOpcode() == Opcodes.DASTORE
                || asmNode.getOpcode() == Opcodes.AASTORE;
    }

    public boolean isArrayLoadInstruction() {
        return asmNode.getOpcode() == Opcodes.IALOAD
                || asmNode.getOpcode() == Opcodes.LALOAD
                || asmNode.getOpcode() == Opcodes.FALOAD
                || asmNode.getOpcode() == Opcodes.DALOAD
                || asmNode.getOpcode() == Opcodes.AALOAD;
    }

    protected String getArrayVariableName() {
        BytecodeInstruction arrayLoader = getSourceOfArrayReference();
        if (arrayLoader == null)
            return null;

        return arrayLoader.getVariableName();
    }

    public abstract BytecodeInstruction getSourceOfArrayReference();

    /**
     * <p>
     * isDefinitionForVariable
     * </p>
     *
     * @param var a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isDefinitionForVariable(String var) {
        return (isDefinition() && getVariableName().equals(var));
    }

    /**
     * <p>
     * isInvokeSpecial
     * </p>
     *
     * @return a boolean.
     */
    public boolean isInvokeSpecial() {
        return asmNode.getOpcode() == Opcodes.INVOKESPECIAL;
    }

    /**
     * Checks whether this instruction is an INVOKESPECIAL instruction that
     * calls a constructor.
     *
     * @return a boolean.
     */
    public boolean isConstructorInvocation() {
        if (!isInvokeSpecial())
            return false;

        MethodInsnNode invoke = (MethodInsnNode) asmNode;
        // if (!invoke.owner.equals(className.replaceAll("\\.", "/")))
        // return false;

        return invoke.name.equals("<init>");
    }

    /**
     * Checks whether this instruction is an INVOKESPECIAL instruction that
     * calls a constructor of the given class.
     *
     * @param className a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isConstructorInvocation(String className) {
        if (!isInvokeSpecial())
            return false;

        MethodInsnNode invoke = (MethodInsnNode) asmNode;
        if (!invoke.owner.equals(className.replaceAll("\\.", "/")))
            return false;

        return invoke.name.equals("<init>");
    }

    // other classification methods

    /**
     * Determines if this instruction is a line number instruction
     * <p>
     * More precisely this method checks if the underlying asmNode is a
     * LineNumberNode
     *
     * @return a boolean.
     */
    public boolean isLineNumber() {
        return (asmNode instanceof LineNumberNode);
    }

    /**
     * <p>
     * getLineNumber
     * </p>
     *
     * @return a int.
     */
    public int getLineNumber() {
        if (!isLineNumber())
            return -1;

        return ((LineNumberNode) asmNode).line;
    }

    /**
     * <p>
     * isLabel
     * </p>
     *
     * @return a boolean.
     */
    public boolean isLabel() {
        return asmNode instanceof LabelNode;
    }

    // sanity checks

    /**
     * <p>
     * sanityCheckAbstractInsnNode
     * </p>
     *
     * @param node a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    public boolean sanityCheckAbstractInsnNode(AbstractInsnNode node) {
        if (node == null)
            return false; //throw new IllegalArgumentException("null given");
        return node.equals(this.asmNode);
        //
        //	throw new IllegalStateException("sanity check failed for "
        //			+ node.toString() + " on " + getMethodName() + toString());
    }

}
