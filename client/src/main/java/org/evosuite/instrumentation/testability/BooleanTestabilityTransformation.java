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

package org.evosuite.instrumentation.testability;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeAnalyzer;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.instrumentation.BooleanArrayInterpreter;
import org.evosuite.instrumentation.BooleanValueInterpreter;
import org.evosuite.instrumentation.TransformationStatistics;
import org.evosuite.instrumentation.testability.transformer.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Transform everything Boolean to ints.
 * <p>
 * This transformation replaces: - TRUE/FALSE with +K/-K - IFEQ/IFNE with
 * IFLE/IFGT - Signatures in fields and calls - Inserts calls to remember
 * distance of last boolean calculation - Inserts calls to recall distance of
 * last boolean calculation when Boolean is used
 *
 * @author Gordon Fraser
 */
public class BooleanTestabilityTransformation {

    public static final Logger logger = LoggerFactory.getLogger(BooleanTestabilityTransformation.class);

    private final ClassNode cn;

    public final String className;

    public Frame[] currentFrames = null;

    private MethodNode currentMethodNode = null;

    public ClassLoader classLoader;

    /**
     * <p>
     * Constructor for BooleanTestabilityTransformation.
     * </p>
     *
     * @param cn a {@link org.objectweb.asm.tree.ClassNode} object.
     */
    public BooleanTestabilityTransformation(ClassNode cn, ClassLoader classLoader) {
        this.cn = cn;
        this.className = cn.name.replace('/', '.');
        this.classLoader = classLoader;
    }

    /**
     * Transform all methods and fields
     *
     * @return a {@link org.objectweb.asm.tree.ClassNode} object.
     */
    public ClassNode transform() {

        processFields();
        processMethods();
        clearIntermediateResults();
        if (className.equals(Properties.TARGET_CLASS)
                || className.startsWith(Properties.TARGET_CLASS + "$"))
            TransformationStatistics.writeStatistics(className);

        return cn;
    }

    @SuppressWarnings("unchecked")
    private void clearIntermediateResults() {
        List<MethodNode> methodNodes = cn.methods;
        for (MethodNode mn : methodNodes) {
            if ((mn.access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE)
                continue;
            GraphPool.clearAll(className, mn.name + mn.desc);
            BytecodeInstructionPool.clearAll(className, mn.name + mn.desc);
            BranchPool.getInstance(classLoader).clear(className, mn.name + mn.desc);
        }
    }

    /**
     * Handle transformation of fields defined in this class
     */
    @SuppressWarnings("unchecked")
    private void processFields() {
        List<FieldNode> fields = cn.fields;
        for (FieldNode field : fields) {
            if (DescriptorMapping.getInstance().isTransformedField(className, field.name,
                    field.desc)) {
                String newDesc = transformFieldDescriptor(className, field.name,
                        field.desc);
                logger.info("Transforming field " + field.name + " from " + field.desc
                        + " to " + newDesc);
                if (!newDesc.equals(field.desc))
                    TransformationStatistics.transformBooleanField();
                field.desc = newDesc;
            }
        }
    }

    /**
     * Handle transformation of methods defined in this class
     */
    @SuppressWarnings("unchecked")
    private void processMethods() {
        List<MethodNode> methodNodes = cn.methods;
        for (MethodNode mn : methodNodes) {
            if ((mn.access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE)
                continue;
            if (DescriptorMapping.getInstance().isTransformedMethod(className, mn.name,
                    mn.desc)) {
                logger.info("Transforming signature of method " + mn.name + mn.desc);
                transformMethodSignature(mn);
                logger.info("Transformed signature to " + mn.name + mn.desc);
            }
            transformMethod(mn);
        }
    }

    /**
     * <p>
     * getOriginalNameDesc
     * </p>
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getOriginalNameDesc(String className, String methodName,
                                             String desc) {
        String key = className.replace('.', '/') + "/" + methodName + desc;
        if (DescriptorMapping.getInstance().originalDesc.containsKey(key)) {
            logger.debug("Descriptor mapping contains original for " + key);
            return DescriptorMapping.getInstance().getOriginalName(className, methodName,
                    desc)
                    + DescriptorMapping.getInstance().originalDesc.get(key);
        } else {
            logger.debug("Descriptor mapping does not contain original for " + key);
            return methodName + desc;
        }
    }

    /**
     * <p>
     * getOriginalDesc
     * </p>
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getOriginalDesc(String className, String methodName, String desc) {
        String key = className.replace('.', '/') + "/" + methodName + desc;
        if (DescriptorMapping.getInstance().originalDesc.containsKey(key)) {
            logger.debug("Descriptor mapping contains original for " + key);
            return DescriptorMapping.getInstance().originalDesc.get(key);
        } else {
            logger.debug("Descriptor mapping does not contain original for " + key);
            return desc;
        }
    }

    /**
     * <p>
     * hasTransformedParameters
     * </p>
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean hasTransformedParameters(String className, String methodName,
                                                   String desc) {
        String key = className.replace('.', '/') + "/" + methodName + desc;
        if (DescriptorMapping.getInstance().originalDesc.containsKey(key)) {
            for (Type type : Type.getArgumentTypes(DescriptorMapping.getInstance().originalDesc.get(key))) {
                if (type.equals(Type.BOOLEAN_TYPE))
                    return true;
            }
        }

        return false;
    }

    /**
     * <p>
     * isTransformedField
     * </p>
     *
     * @param className a {@link java.lang.String} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param desc      a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isTransformedField(String className, String fieldName,
                                             String desc) {
        return DescriptorMapping.getInstance().isTransformedField(className, fieldName,
                desc);
    }

    /**
     * Insert a call to the isNull helper function
     *
     * @param opcode
     * @param position
     * @param list
     */
    public void insertPushNull(int opcode, JumpInsnNode position, InsnList list) {
        int branchId = getBranchID(currentMethodNode, position);
        logger.info("Inserting instrumentation for NULL check at branch " + branchId
                + " in method " + currentMethodNode.name);

        MethodInsnNode nullCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "isNull",
                Type.getMethodDescriptor(Type.INT_TYPE,
                        Type.getType(Object.class),
                        Type.INT_TYPE), false);
        list.insertBefore(position, new InsnNode(Opcodes.DUP));
        list.insertBefore(position, new LdcInsnNode(opcode));
        list.insertBefore(position, nullCheck);
        //list.insertBefore(position,
        //                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
        insertBranchIdPlaceholder(currentMethodNode, position, branchId);
        MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "pushPredicate",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE,
                        Type.INT_TYPE), false);
        list.insertBefore(position, push);

    }

    /**
     * Insert a call to the reference equality check helper function
     *
     * @param opcode
     * @param position
     * @param list
     */
    public void insertPushEquals(int opcode, JumpInsnNode position, InsnList list) {
        MethodInsnNode equalCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "isEqual",
                Type.getMethodDescriptor(Type.INT_TYPE,
                        Type.getType(Object.class),
                        Type.getType(Object.class),
                        Type.INT_TYPE), false);
        list.insertBefore(position, new InsnNode(Opcodes.DUP2));
        list.insertBefore(position, new LdcInsnNode(opcode));
        list.insertBefore(position, equalCheck);
        //list.insertBefore(position,
        //                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
        insertBranchIdPlaceholder(currentMethodNode, position);
        MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "pushPredicate",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE,
                        Type.INT_TYPE), false);
        list.insertBefore(position, push);

    }

    private BytecodeInstruction getBytecodeInstruction(MethodNode mn,
                                                       AbstractInsnNode node) {
        return BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
                mn.name
                        + mn.desc,
                node); // TODO: Adapt for classloaders
    }

    private int getBranchID(MethodNode mn, JumpInsnNode jumpNode) {
        assert (mn.instructions.contains(jumpNode));
        BytecodeInstruction insn = getBytecodeInstruction(mn, jumpNode);
        logger.info("Found instruction: " + insn);
        Branch branch = BranchPool.getInstance(classLoader).getBranchForInstruction(insn);
        return branch.getActualBranchId();
    }

    private int getControlDependentBranchID(MethodNode mn, AbstractInsnNode insnNode) {
        BytecodeInstruction insn = getBytecodeInstruction(mn, insnNode);
        // FIXXME: Handle multiple control dependencies
        return insn.getControlDependentBranchId();
    }

    private int getApproximationLevel(MethodNode mn, AbstractInsnNode insnNode) {
        BytecodeInstruction insn = getBytecodeInstruction(mn, insnNode);
        // FIXXME: Handle multiple control dependencies
        return insn.getCDGDepth();
    }

    private void insertBranchIdPlaceholder(MethodNode mn, JumpInsnNode jumpNode) {
        Label label = new Label();
        LabelNode labelNode = new LabelNode(label);
        //BooleanTestabilityPlaceholderTransformer.addBranchPlaceholder(label, jumpNode);
        mn.instructions.insertBefore(jumpNode, labelNode);
        //mn.instructions.insertBefore(jumpNode, new LdcInsnNode(0));
        mn.instructions.insertBefore(jumpNode, new LdcInsnNode(getBranchID(mn, jumpNode)));
    }

    private void insertBranchIdPlaceholder(MethodNode mn, JumpInsnNode jumpNode,
                                           int branchId) {
        Label label = new Label();
        LabelNode labelNode = new LabelNode(label);
        //BooleanTestabilityPlaceholderTransformer.addBranchPlaceholder(label, jumpNode);
        mn.instructions.insertBefore(jumpNode, labelNode);
        //mn.instructions.insertBefore(jumpNode, new LdcInsnNode(0));
        mn.instructions.insertBefore(jumpNode, new LdcInsnNode(branchId));
    }

    private void insertControlDependencyPlaceholder(MethodNode mn,
                                                    AbstractInsnNode insnNode) {
        Label label = new Label();
        LabelNode labelNode = new LabelNode(label);
        //BooleanTestabilityPlaceholderTransformer.addControlDependencyPlaceholder(label,
        //                                                                         insnNode);
        mn.instructions.insertBefore(insnNode, labelNode);
        //instructions.insertBefore(insnNode, new LdcInsnNode(0));
        //mn.instructions.insertBefore(insnNode, new LdcInsnNode(0));
        mn.instructions.insertBefore(insnNode, new LdcInsnNode(
                getControlDependentBranchID(mn, insnNode)));
        mn.instructions.insertBefore(insnNode,
                new LdcInsnNode(getApproximationLevel(mn, insnNode)));
        logger.info("Control dependent branch id: "
                + getControlDependentBranchID(mn, insnNode));
        logger.info("Approximation level: " + getApproximationLevel(mn, insnNode));
    }

    /**
     * Insert a call to the distance function for unary comparison
     *
     * @param opcode
     * @param position
     * @param list
     */
    public void insertPush(int opcode, JumpInsnNode position, InsnList list) {
        list.insertBefore(position, new InsnNode(Opcodes.DUP));
        // TODO: We have to put a placeholder here instead of the actual branch ID
        // TODO: And then later add another transformation where we replace this with
        //       actual branch IDs
        //list.insertBefore(position,
        //                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
        insertBranchIdPlaceholder(currentMethodNode, position);
        MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "pushPredicate",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE,
                        Type.INT_TYPE), false);
        list.insertBefore(position, push);

    }

    /**
     * Insert a call to the distance function for binary comparison
     *
     * @param opcode
     * @param position
     * @param list
     */
    public void insertPush2(int opcode, JumpInsnNode position, InsnList list) {
        list.insertBefore(position, new InsnNode(Opcodes.DUP2));
        //list.insertBefore(position, new InsnNode(Opcodes.ISUB));
        MethodInsnNode sub = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "intSub",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE,
                        Type.INT_TYPE), false);
        list.insertBefore(position, sub);
        insertBranchIdPlaceholder(currentMethodNode, position);

        //		list.insertBefore(position,
        //		                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
        MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "pushPredicate",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE,
                        Type.INT_TYPE), false);
        list.insertBefore(position, push);

    }

    /**
     * Insert a call that takes a boolean from the stack, and returns the
     * appropriate distance
     *
     * @param position
     * @param list
     */
    public void insertGet(AbstractInsnNode position, InsnList list) {
        logger.info("Inserting get call");
        // Here, branchId is the first control dependency
        //list.insertBefore(position,
        //                  new LdcInsnNode(getControlDependentBranchID(currentMethodNode,
        //                                                              position)));
        insertControlDependencyPlaceholder(currentMethodNode, position);

        MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "getDistance",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE,
                        Type.INT_TYPE, Type.INT_TYPE), false);
        list.insert(position, get);
    }

    /**
     * Insert a call that takes a boolean from the stack, and returns the
     * appropriate distance
     *
     * @param position
     * @param list
     */
    public void insertGetBefore(AbstractInsnNode position, InsnList list) {
        logger.info("Inserting get call before");
        // Here, branchId is the first control dependency
        //list.insertBefore(position,
        //                  new LdcInsnNode(getControlDependentBranchID(currentMethodNode,
        //                                                              position)));
        // insertControlDependencyPlaceholder(currentMethodNode, position);

        // branch
        // approx
        // value

        Label label = new Label();
        LabelNode labelNode = new LabelNode(label);
        //BooleanTestabilityPlaceholderTransformer.addControlDependencyPlaceholder(label,
        //                                                                         insnNode);
        currentMethodNode.instructions.insertBefore(position, labelNode);
        //instructions.insertBefore(insnNode, new LdcInsnNode(0));
        //mn.instructions.insertBefore(insnNode, new LdcInsnNode(0));
        currentMethodNode.instructions.insertBefore(position, new LdcInsnNode(
                getControlDependentBranchID(currentMethodNode, position)));
        currentMethodNode.instructions.insertBefore(position, new InsnNode(Opcodes.SWAP));
        currentMethodNode.instructions.insertBefore(position, new LdcInsnNode(
                getApproximationLevel(currentMethodNode, position)));
        currentMethodNode.instructions.insertBefore(position, new InsnNode(Opcodes.SWAP));

        MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "getDistance",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE,
                        Type.INT_TYPE, Type.INT_TYPE), false);
        list.insertBefore(position, get);
    }

    public boolean isBooleanOnStack(MethodNode mn, AbstractInsnNode node, int position) {
        int insnPosition = mn.instructions.indexOf(node);
        if (insnPosition >= currentFrames.length) {
            logger.info("Trying to access frame out of scope: " + insnPosition + "/"
                    + currentFrames.length);
            return false;
        }
        Frame frame = currentFrames[insnPosition];
        return frame.getStack(frame.getStackSize() - 1 - position) == BooleanValueInterpreter.BOOLEAN_VALUE;
    }

    public boolean isBooleanVariable(int var, MethodNode mn) {
        for (Object o : mn.localVariables) {
            LocalVariableNode vn = (LocalVariableNode) o;
            if (vn.index == var)
                return Type.getType(vn.desc).equals(Type.BOOLEAN_TYPE);
        }
        return false;
    }

    /**
     * This helper function determines whether the boolean on the stack at the
     * current position will be stored in a Boolean variable
     *
     * @param position
     * @param mn
     * @return
     */
    public boolean isBooleanAssignment(AbstractInsnNode position, MethodNode mn) {
        AbstractInsnNode node = position.getNext();
        logger.info("Checking for ISTORE after boolean");
        boolean done = false;
        while (!done) {

            if (node.getOpcode() == Opcodes.PUTFIELD
                    || node.getOpcode() == Opcodes.PUTSTATIC) {
                // TODO: Check whether field is static
                logger.info("Checking field assignment");
                FieldInsnNode fn = (FieldInsnNode) node;
                return Type.getType(DescriptorMapping.getInstance().getFieldDesc(fn.owner,
                        fn.name,
                        fn.desc)) == Type.BOOLEAN_TYPE;
            } else if (node.getOpcode() == Opcodes.ISTORE) {
                logger.info("Found ISTORE after boolean");

                VarInsnNode vn = (VarInsnNode) node;
                // TODO: Check whether variable at this position is a boolean
                if (isBooleanVariable(vn.var, mn)) {
                    logger.info("Assigning boolean to variable ");
                    return true;
                } else {
                    logger.info("Variable is not a bool");
                    return false;
                }
            } else if (node.getOpcode() == Opcodes.IRETURN) {
                logger.info("Checking return value of method " + cn.name + "." + mn.name);
                if (DescriptorMapping.getInstance().isTransformedOrBooleanMethod(cn.name,
                        mn.name,
                        mn.desc)) {
                    logger.info("Method returns a bool");
                    return true;
                } else {
                    logger.info("Method does not return a bool");
                    return false;
                }
            } else if (node.getOpcode() == Opcodes.BASTORE) {
                // We remove all bytes, so BASTORE is only used for booleans
                AbstractInsnNode start = position.getNext();
                boolean reassignment = false;
                while (start != node) {
                    if (node instanceof InsnNode) {
                        reassignment = true;
                    }
                    start = start.getNext();
                }
                logger.info("Possible assignment to array?");
                return !reassignment;

            } else if (node instanceof MethodInsnNode) {
                // if it is a boolean parameter of a converted method, then it needs to be converted
                // Problem: How do we know which parameter it represents?
                MethodInsnNode methodNode = (MethodInsnNode) node;
                String desc = DescriptorMapping.getInstance().getMethodDesc(methodNode.owner,
                        methodNode.name,
                        methodNode.desc);
                Type[] types = Type.getArgumentTypes(desc);
                return types.length > 0 && types[types.length - 1] == Type.BOOLEAN_TYPE;

            } else if (node.getOpcode() == Opcodes.GOTO
                    || node.getOpcode() == Opcodes.ICONST_0
                    || node.getOpcode() == Opcodes.ICONST_1 || node.getOpcode() == -1) {
                logger.info("Continuing search");

                // continue search
            } else if (!(node instanceof LineNumberNode || node instanceof FrameNode)) {
                logger.info("Search ended with opcode " + node.getOpcode());

                return false;
            }
            if (node != mn.instructions.getLast())
                node = node.getNext();
            else
                done = true;
        }

        return false;
    }

    private void generateCDG(MethodNode mn) {
        if (BytecodeInstructionPool.getInstance(classLoader).hasMethod(className, mn.name + mn.desc))
            return;

        BytecodeInstructionPool.getInstance(classLoader).registerMethodNode(mn,
                className,
                mn.name
                        + mn.desc); // TODO: Adapt for multiple classLoaders

        BytecodeAnalyzer bytecodeAnalyzer = new BytecodeAnalyzer();
        logger.info("Generating initial CFG for method " + mn.name);

        try {

            bytecodeAnalyzer.analyze(classLoader, className,
                    mn.name + mn.desc, mn); // TODO
        } catch (AnalyzerException e) {
            logger.error("Analyzer exception while analyzing " + className + "."
                    + mn.name + ": " + e);
            e.printStackTrace();
        }

        // compute Raw and ActualCFG and put both into GraphPool
        bytecodeAnalyzer.retrieveCFGGenerator().registerCFGs();
    }

    /**
     * Determine if the signature of the given method needs to be transformed,
     * and transform if necessary
     *
     * @param owner
     * @param name
     * @param desc
     * @return
     */
    public String transformMethodDescriptor(String owner, String name, String desc) {
        return DescriptorMapping.getInstance().getMethodDesc(owner, name, desc);
    }

    /**
     * Determine if the signature of the given field needs to be transformed,
     * and transform if necessary
     *
     * @param owner
     * @param name
     * @param desc
     * @return
     */
    public String transformFieldDescriptor(String owner, String name, String desc) {
        return DescriptorMapping.getInstance().getFieldDesc(owner, name, desc);
    }

    private void transformMethodSignature(MethodNode mn) {
        // If the method was declared in java.* then don't instrument
        // Otherwise change signature
        String newDesc = DescriptorMapping.getInstance().getMethodDesc(className,
                mn.name, mn.desc);
        if (Type.getReturnType(mn.desc) == Type.BOOLEAN_TYPE
                && Type.getReturnType(newDesc) == Type.INT_TYPE)
            TransformationStatistics.transformBooleanReturnValue();
        if (Arrays.asList(Type.getArgumentTypes(mn.desc)).contains(Type.BOOLEAN_TYPE)
                && !Arrays.asList(Type.getArgumentTypes(newDesc)).contains(Type.BOOLEAN_TYPE))
            TransformationStatistics.transformBooleanParameter();
        String newName = DescriptorMapping.getInstance().getMethodName(className,
                mn.name, mn.desc);
        logger.info("Changing method descriptor from "
                + mn.name
                + mn.desc
                + " to "
                + DescriptorMapping.getInstance().getMethodName(className, mn.name,
                mn.desc) + newDesc);
        mn.desc = DescriptorMapping.getInstance().getMethodDesc(className, mn.name,
                mn.desc);
        mn.name = newName;
    }

    private Frame[] getArrayFrames(MethodNode mn) {
        try {
            Analyzer a = new Analyzer(new BooleanArrayInterpreter());
            a.analyze(cn.name, mn);
            return a.getFrames();
        } catch (Exception e) {
            logger.info("[Array] Error during analysis: " + e);
            return null;
        }
    }

    /**
     * Apply testability transformation to an individual method
     *
     * @param mn
     */
    private void transformMethod(MethodNode mn) {
        logger.info("Transforming method " + mn.name + mn.desc);

        //currentCFG = GraphPool.getActualCFG(className, mn.name + mn.desc);

        // TODO: Skipping interfaces for now, but will need to handle Booleans in interfaces!
        if ((mn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
            return;

        String origDesc = getOriginalDesc(className, mn.name, mn.desc);
        logger.info("Analyzing " + mn.name + " for TT, signature " + origDesc + "/"
                + mn.desc);
        try {
            Analyzer a = new Analyzer(new BooleanValueInterpreter(origDesc,
                    (mn.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC));
            a.analyze(className, mn);
            currentFrames = a.getFrames();
        } catch (Exception e) {
            logger.info("1. Error during analysis: " + e);
            //e.printStackTrace();
            // TODO: Handle error
        }
        generateCDG(mn);
        currentMethodNode = mn;
        // First expand ifs without else/*
        new ImplicitElseTransformer(this).transform(mn);
        try {
            Analyzer a = new Analyzer(new BooleanValueInterpreter(origDesc,
                    (mn.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC));
            a.analyze(className, mn);
            currentFrames = a.getFrames();
        } catch (Exception e) {
            logger.info("2. Error during analysis: " + e);
            //e.printStackTrace();
            // TODO: Handle error
        }

        //		BytecodeInstructionPool.reRegisterMethodNode(mn, className, mn.name + mn.desc);
        // Replace all bitwise operators
        logger.info("Transforming Boolean bitwise operators");
        new BitwiseOperatorTransformer(this).transform(mn);

        // Transform IFEQ/IFNE to IFLE/IFGT
        logger.info("Transforming Boolean IFs");
        new BooleanIfTransformer(this).transform(mn);

        // Insert call to BooleanHelper.get after ICONST_0/1 or Boolean fields
        logger.info("Transforming Boolean definitions");
        new BooleanDefinitionTransformer(this).transform(mn);

        // Replace all instanceof comparisons
        logger.info("Transforming instanceof");
        new InstanceOfTransformer().transform(mn);

        // Replace all calls to methods/fields returning booleans
        new BooleanCallsTransformer(this).transform(mn);

        // Transform all flag based comparisons
        logger.info("Transforming Boolean distances");
        new BooleanDistanceTransformer(this).transform(mn);
        mn.maxStack += 3;

        // Replace all boolean arrays
        new BooleanArrayTransformer().transform(mn);

        new BooleanArrayIndexTransformer(getArrayFrames(mn)).transform(mn);

        // Replace all boolean return values
        logger.info("Transforming Boolean return values");
        new BooleanReturnTransformer(this).transform(mn);

        //		GraphPool.clear(className, mn.name + mn.desc);
        //		BytecodeInstructionPool.clear(className, mn.name + mn.desc);
        //		BranchPool.clear(className, mn.name + mn.desc);

        // Actually this should be done automatically by the ClassWriter...
        // +2 because we might do a DUP2
        mn.maxStack += 1;
    }

}
