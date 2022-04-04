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

import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cdg.ControlDependenceGraph;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.io.Serializable;
import java.util.Set;

/**
 * Internal representation of a BytecodeInstruction
 * <p>
 * Extends ASMWrapper which serves as an interface to the ASM library.
 * <p>
 * Known super classes are DefUse and Branch which yield specific functionality
 * needed to achieve theirs respective coverage criteria
 * <p>
 * Old: Node of the control flow graph
 *
 * @author Gordon Fraser, Andre Mis
 */
public class BytecodeInstruction extends ASMWrapper implements Serializable,
        Comparable<BytecodeInstruction> {

    private static final long serialVersionUID = 3630449183355518857L;

    // identification of a byteCode instruction inside EvoSuite
    protected ClassLoader classLoader;
    protected String className;
    protected String methodName;
    protected int instructionId;
    protected int bytecodeOffset;

    // auxiliary information
    private int lineNumber = -1;

    // experiment: also searching through all CFG nodes in order to determine an
    // instruction BasicBlock might be a little to expensive too just to safe
    // space for one reference
    private BasicBlock basicBlock;

    /**
     * Generates a ByteCodeInstruction instance that represents a byteCode
     * instruction as indicated by the given ASMNode in the given method and
     * class
     *
     * @param className      a {@link java.lang.String} object.
     * @param methodName     a {@link java.lang.String} object.
     * @param instructionId  a int.
     * @param bytecodeOffset a int.
     * @param asmNode        a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    public BytecodeInstruction(ClassLoader classLoader, String className,
                               String methodName, int instructionId, int bytecodeOffset, AbstractInsnNode asmNode) {

        if (className == null || methodName == null || asmNode == null)
            throw new IllegalArgumentException("null given");
        if (instructionId < 0)
            throw new IllegalArgumentException(
                    "expect instructionId to be positive, not " + instructionId);

        this.instructionId = instructionId;
        this.bytecodeOffset = bytecodeOffset;
        this.asmNode = asmNode;

        this.classLoader = classLoader;

        setClassName(className);
        setMethodName(methodName);
    }

    /**
     * Can represent any byteCode instruction
     *
     * @param wrap a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public BytecodeInstruction(BytecodeInstruction wrap) {

        this(wrap.classLoader, wrap.className, wrap.methodName, wrap.instructionId,
                wrap.bytecodeOffset, wrap.asmNode, wrap.lineNumber, wrap.basicBlock);
        this.frame = wrap.frame;
    }

    /**
     * <p>
     * Constructor for BytecodeInstruction.
     * </p>
     *
     * @param className      a {@link java.lang.String} object.
     * @param methodName     a {@link java.lang.String} object.
     * @param instructionId  a int.
     * @param bytecodeOffset a int.
     * @param asmNode        a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     * @param lineNumber     a int.
     * @param basicBlock     a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     */
    public BytecodeInstruction(ClassLoader classLoader, String className,
                               String methodName, int instructionId, int bytecodeOffset, AbstractInsnNode asmNode,
                               int lineNumber, BasicBlock basicBlock) {

        this(classLoader, className, methodName, instructionId, bytecodeOffset, asmNode,
                lineNumber);

        this.basicBlock = basicBlock;
    }

    /**
     * <p>
     * Constructor for BytecodeInstruction.
     * </p>
     *
     * @param className      a {@link java.lang.String} object.
     * @param methodName     a {@link java.lang.String} object.
     * @param instructionId  a int.
     * @param bytecodeOffset a int.
     * @param asmNode        a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     * @param lineNumber     a int.
     */
    public BytecodeInstruction(ClassLoader classLoader, String className,
                               String methodName, int instructionId, int bytecodeOffset, AbstractInsnNode asmNode,
                               int lineNumber) {

        this(classLoader, className, methodName, instructionId, bytecodeOffset, asmNode);

        if (lineNumber != -1)
            setLineNumber(lineNumber);
    }

    // getter + setter

    private void setMethodName(String methodName) {
        if (methodName == null)
            throw new IllegalArgumentException("null given");

        this.methodName = methodName;
    }

    private void setClassName(String className) {
        if (className == null)
            throw new IllegalArgumentException("null given");

        this.className = className;
    }

    /**
     * <p>
     * setCFGFrame
     * </p>
     *
     * @param frame a {@link org.evosuite.graphs.cfg.CFGFrame} object.
     */
    public void setCFGFrame(CFGFrame frame) {
        this.frame = frame;
    }

    // --- Field Management ---

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInstructionId() {
        return instructionId;
    }

    /**
     * <p>
     * getBytecodeOffset
     * </p>
     *
     * @return a int.
     */
    public int getBytecodeOffset() {
        return bytecodeOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethodName() {
        return methodName;
    }

    /**
     * <p>
     * Getter for the field <code>className</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getClassName() {
        return className;
    }

    /**
     * <p>
     * getName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return "BytecodeInstruction " + instructionId + " in " + methodName;
    }

    /**
     * Return's the BasicBlock that contain's this instruction in it's CFG.
     * <p>
     * If no BasicBlock containing this instruction was created yet, null is
     * returned.
     *
     * @return a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     */
    public BasicBlock getBasicBlock() {
        if (!hasBasicBlockSet())
            retrieveBasicBlock();
        return basicBlock;
    }

    private void retrieveBasicBlock() {

        if (basicBlock == null)
            basicBlock = getActualCFG().getBlockOf(this);
    }

    /**
     * Once the CFG has been asked for this instruction's BasicBlock it sets
     * this instance's internal basicBlock field.
     *
     * @param block a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     */
    public void setBasicBlock(BasicBlock block) {
        if (block == null)
            throw new IllegalArgumentException("null given");

        if (!block.getClassName().equals(getClassName())
                || !block.getMethodName().equals(getMethodName()))
            throw new IllegalArgumentException(
                    "expect block to be for the same method and class as this instruction");
        if (this.basicBlock != null)
            throw new IllegalArgumentException(
                    "basicBlock already set! not allowed to overwrite");

        this.basicBlock = block;
    }

    /**
     * Checks whether this instance's basicBlock has already been set by the CFG
     * or
     *
     * @return a boolean.
     */
    public boolean hasBasicBlockSet() {
        return basicBlock != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineNumber() {

        if (lineNumber == -1 && isLineNumber())
            retrieveLineNumber();

        return lineNumber;
    }

    /**
     * <p>
     * Setter for the field <code>lineNumber</code>.
     * </p>
     *
     * @param lineNumber a int.
     */
    public void setLineNumber(int lineNumber) {
        if (lineNumber <= 0)
            throw new IllegalArgumentException(
                    "expect lineNumber value to be positive");

        if (isLabel())
            return;

        if (isLineNumber()) {
            int asmLine = super.getLineNumber();
            // sanity check
            if (lineNumber != -1 && asmLine != lineNumber)
                throw new IllegalStateException(
                        "linenumber instruction has lineNumber field set to a value different from instruction linenumber");
            this.lineNumber = asmLine;
        } else {
            this.lineNumber = lineNumber;
        }
    }

    /**
     * At first, if this instruction constitutes a line number instruction this
     * method tries to retrieve the lineNumber from the underlying asmNode and
     * set the lineNumber field to the value given by the asmNode.
     * <p>
     * This can lead to an IllegalStateException, should the lineNumber field
     * have been set to another value previously
     * <p>
     * After that, if the lineNumber field is still not initialized, this method
     * returns false Otherwise it returns true
     *
     * @return a boolean.
     */
    public boolean hasLineNumberSet() {
        retrieveLineNumber();
        return lineNumber != -1;
    }

    /**
     * If the underlying ASMNode is a LineNumberNode the lineNumber field of
     * this instance will be set to the lineNumber contained in that
     * LineNumberNode
     * <p>
     * Should the lineNumber field have been set to a value different from that
     * contained in the asmNode, this method throws an IllegalStateExeption
     */
    private void retrieveLineNumber() {
        if (isLineNumber()) {
            int asmLine = super.getLineNumber();
            // sanity check
            if (this.lineNumber != -1 && asmLine != this.lineNumber)
                throw new IllegalStateException(
                        "lineNumber field was manually set to a value different from the actual lineNumber contained in LineNumberNode");
            this.lineNumber = asmLine;
        }
    }

    // --- graph section ---

    /**
     * Returns the ActualControlFlowGraph of this instructions method
     * <p>
     * Convenience method. Redirects the call to GraphPool.getActualCFG()
     *
     * @return a {@link org.evosuite.graphs.cfg.ActualControlFlowGraph} object.
     */
    public ActualControlFlowGraph getActualCFG() {

        ActualControlFlowGraph myCFG = GraphPool.getInstance(classLoader).getActualCFG(className,
                methodName);
        if (myCFG == null)
            throw new IllegalStateException(
                    "expect GraphPool to know CFG for every method for which an instruction is known");

        return myCFG;
    }

    /**
     * Returns the RawControlFlowGraph of this instructions method
     * <p>
     * Convenience method. Redirects the call to GraphPool.getRawCFG()
     *
     * @return a {@link org.evosuite.graphs.cfg.RawControlFlowGraph} object.
     */
    public RawControlFlowGraph getRawCFG() {

        RawControlFlowGraph myCFG = GraphPool.getInstance(classLoader).getRawCFG(className,
                methodName);
        if (myCFG == null)
            throw new IllegalStateException(
                    "expect GraphPool to know CFG for every method for which an instruction is known");

        return myCFG;
    }

    /**
     * Returns the ControlDependenceGraph of this instructions method
     * <p>
     * Convenience method. Redirects the call to GraphPool.getCDG()
     *
     * @return a {@link org.evosuite.graphs.cdg.ControlDependenceGraph} object.
     */
    public ControlDependenceGraph getCDG() {

        ControlDependenceGraph myCDG = GraphPool.getInstance(classLoader).getCDG(className,
                methodName);
        if (myCDG == null)
            throw new IllegalStateException(
                    "expect GraphPool to know CDG for every method for which an instruction is known");

        return myCDG;
    }

    // --- CDG-Section ---

    /**
     * Returns a cfg.Branch object for each branch this instruction is control
     * dependent on as determined by the ControlDependenceGraph. If this
     * instruction is only dependent on the root branch this method returns an
     * empty set
     * <p>
     * If this instruction is a Branch and it is dependent on itself - which can
     * happen in loops for example - the returned set WILL contain this. If you
     * do not need the full set in order to avoid loops, call
     * getAllControlDependentBranches instead
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<ControlDependency> getControlDependencies() {

        BasicBlock myBlock = getBasicBlock();

        // return new
        // HashSet<ControlDependency>(myBlock.getControlDependencies());
        return myBlock.getControlDependencies();
    }

    /**
     * This method returns a random Branch among all Branches this instruction
     * is control dependent on
     * <p>
     * If this instruction is only dependent on the root branch, this method
     * returns null
     * <p>
     * Since EvoSuite was previously unable to detect multiple control
     * dependencies for one instruction this method serves as a backwards
     * compatibility bridge
     *
     * @return a {@link org.evosuite.coverage.branch.Branch} object.
     */
    public Branch getControlDependentBranch() {

        Set<ControlDependency> controlDependentBranches = getControlDependencies();

        for (ControlDependency cd : controlDependentBranches)
            return cd.getBranch();

        return null; // root branch
    }

    /**
     * Returns all branchIds of Branches this instruction is directly control
     * dependent on as determined by the ControlDependenceGraph for this
     * instruction's method.
     * <p>
     * If this instruction is control dependent on the root branch the id -1
     * will be contained in this set
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Integer> getControlDependentBranchIds() {

        BasicBlock myBlock = getBasicBlock();

        return myBlock.getControlDependentBranchIds();
    }

    /**
     * Determines whether or not this instruction is control dependent on the
     * root branch of it's method by calling getControlDependentBranchIds() to
     * see if the return contains -1.
     *
     * @return a boolean.
     */
    public boolean isRootBranchDependent() {
        return getControlDependencies().isEmpty();
    }

    /**
     * This method returns a random branchId among all branchIds this
     * instruction is control dependent on.
     * <p>
     * This method returns -1 if getControlDependentBranch() returns null,
     * otherwise that Branch's branchId is returned
     * <p>
     * Note: The returned branchExpressionValue comes from the same Branch
     * getControlDependentBranch() and getControlDependentBranchId() return
     * <p>
     * Since EvoSuite was previously unable to detect multiple control
     * dependencies for one instruction this method serves as a backwards
     * compatibility bridge
     *
     * @return a int.
     */
    public int getControlDependentBranchId() {

        Branch b = getControlDependentBranch();
        if (b == null)
            return -1;

        return b.getActualBranchId();
    }

    /**
     * This method returns the branchExpressionValue from a random Branch among
     * all Branches this instruction is control dependent on.
     * <p>
     * This method returns true if getControlDependentBranch() returns null,
     * otherwise that Branch's branchExpressionValue is returned
     * <p>
     * Note: The returned branchExpressionValue comes from the same Branch
     * getControlDependentBranch() and getControlDependentBranchId() return
     * <p>
     * Since EvoSuite was previously unable to detect multiple control
     * dependencies for one instruction this method serves as a backwards
     * compatibility bridge
     *
     * @return a boolean.
     */
    public boolean getControlDependentBranchExpressionValue() {

        Branch b = getControlDependentBranch();
        return getBranchExpressionValue(b);
    }

    /**
     * <p>
     * getBranchExpressionValue
     * </p>
     *
     * @param b a {@link org.evosuite.coverage.branch.Branch} object.
     * @return a boolean.
     */
    public boolean getBranchExpressionValue(Branch b) {
        if (!isDirectlyControlDependentOn(b))
            throw new IllegalArgumentException(
                    "this method can only be called for branches that this instruction is directly control dependent on.");

        if (b == null)
            return true; // root branch special case

        return getControlDependency(b).getBranchExpressionValue();
    }

    /**
     * Determines whether this BytecodeInstruction is directly control dependent
     * on the given Branch. Meaning within this instruction CDG there is an
     * incoming ControlFlowEdge to this instructions BasicBlock holding the
     * given Branch as it's branchInstruction.
     * <p>
     * If the given Branch is null, this method checks whether the this
     * instruction is control dependent on the root branch of it's method.
     *
     * @param branch a {@link org.evosuite.coverage.branch.Branch} object.
     * @return a boolean.
     */
    public boolean isDirectlyControlDependentOn(Branch branch) {
        if (branch == null)
            return getControlDependentBranchIds().contains(-1);

        for (ControlDependency cd : getControlDependencies())
            if (cd.getBranch().equals(branch))
                return true;

        return false;
    }

    /**
     * <p>
     * getControlDependency
     * </p>
     *
     * @param branch a {@link org.evosuite.coverage.branch.Branch} object.
     * @return a {@link org.evosuite.graphs.cfg.ControlDependency} object.
     */
    public ControlDependency getControlDependency(Branch branch) {
        if (!isDirectlyControlDependentOn(branch))
            throw new IllegalArgumentException(
                    "instruction not directly control dependent on given branch");

        for (ControlDependency cd : getControlDependencies())
            if (cd.getBranch().equals(branch))
                return cd;

        throw new IllegalStateException(
                "expect getControlDependencies() to contain a CD for each branch that isDirectlyControlDependentOn() returns true on");
    }

    // /**
    // * WARNING: better don't user this method right now TODO
    // *
    // * Determines whether the CFGVertex is transitively control dependent on
    // the
    // * given Branch
    // *
    // * A CFGVertex is transitively control dependent on a given Branch if the
    // * Branch and the vertex are in the same method and the vertex is either
    // * directly control dependent on the Branch - look at
    // * isDirectlyControlDependentOn(Branch) - or the CFGVertex of the control
    // * dependent branch of this CFGVertex is transitively control dependent on
    // * the given branch.
    // *
    // */
    // public boolean isTransitivelyControlDependentOn(Branch branch) {
    // if (!getClassName().equals(branch.getClassName()))
    // return false;
    // if (!getMethodName().equals(branch.getMethodName()))
    // return false;
    //
    // // TODO: this method does not take into account, that there might be
    // // multiple branches this instruction is control dependent on
    //
    // BytecodeInstruction vertexHolder = this;
    // do {
    // if (vertexHolder.isDirectlyControlDependentOn(branch))
    // return true;
    // vertexHolder = vertexHolder.getControlDependentBranch()
    // .getInstruction();
    // } while (vertexHolder != null);
    //
    // return false;
    // }

    // /**
    // * WARNING: better don't user this method right now TODO
    // *
    // * Determines the number of branches that have to be passed in order to
    // pass
    // * this CFGVertex
    // *
    // * Used to determine TestFitness difficulty
    // */

    /**
     * <p>
     * getCDGDepth
     * </p>
     *
     * @return a int.
     */
    public int getCDGDepth() {
        int min = Integer.MAX_VALUE;
        Set<ControlDependency> dependencies = getControlDependencies();
        if (dependencies.isEmpty())
            min = 1;
        for (ControlDependency dependency : dependencies) {
            int depth = getCDG().getControlDependenceDepth(dependency);
            if (depth < min)
                min = depth;
        }
        return min;
        /*
         * // TODO: this method does not take into account, that there might be
         * // multiple branches this instruction is control dependent on Branch
         * current = getControlDependentBranch(); int r = 1; while (current !=
         * null) { r++; current =
         * current.getInstruction().getControlDependentBranch(); } return r;
         */
    }

    // String methods

    /**
     * <p>
     * explain
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String explain() {
        if (isBranch()) {
            if (BranchPool.getInstance(classLoader).isKnownAsBranch(this)) {
                Branch b = BranchPool.getInstance(classLoader).getBranchForInstruction(this);
                if (b == null)
                    throw new IllegalStateException(
                            "expect BranchPool to be able to return Branches for instructions fullfilling BranchPool.isKnownAsBranch()");

                return "Branch " + b.getActualBranchId() + " - "
                        + getInstructionType();
            }
            return "UNKNOWN Branch I" + instructionId + " "
                    + getInstructionType() + ", jump to " + ((JumpInsnNode) asmNode).label.getLabel();

            // + " - " + ((JumpInsnNode) asmNode).label.getLabel();
        }

        return getASMNodeString();
    }

    /**
     * <p>
     * getASMNodeString
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getASMNodeString() {
        String type = getType();
        String opcode = getInstructionType();

        String stack = "";
        if (frame == null)
            stack = "null";
        else
            for (int i = 0; i < frame.getStackSize(); i++) {
                stack += frame.getStack(i) + ",";
            }

        if (asmNode instanceof LabelNode) {
            return "LABEL " + ((LabelNode) asmNode).getLabel().toString();
        } else if (asmNode instanceof FieldInsnNode)
            return "Field" + " " + ((FieldInsnNode) asmNode).owner + "."
                    + ((FieldInsnNode) asmNode).name + " Type=" + type
                    + ", Opcode=" + opcode;
        else if (asmNode instanceof FrameNode)
            return "Frame" + " " + asmNode.getOpcode() + " Type=" + type
                    + ", Opcode=" + opcode;
        else if (asmNode instanceof IincInsnNode)
            return "IINC " + ((IincInsnNode) asmNode).var + " Type=" + type
                    + ", Opcode=" + opcode;
        else if (asmNode instanceof InsnNode)
            return "" + opcode;
        else if (asmNode instanceof IntInsnNode)
            return "INT " + ((IntInsnNode) asmNode).operand + " Type=" + type
                    + ", Opcode=" + opcode;
        else if (asmNode instanceof MethodInsnNode)
            return opcode + " " + ((MethodInsnNode) asmNode).owner + "." + ((MethodInsnNode) asmNode).name + ((MethodInsnNode) asmNode).desc;
        else if (asmNode instanceof JumpInsnNode)
            return "JUMP " + ((JumpInsnNode) asmNode).label.getLabel()
                    + " Type=" + type + ", Opcode=" + opcode + ", Stack: "
                    + stack + " - Line: " + lineNumber;
        else if (asmNode instanceof LdcInsnNode)
            return "LDC " + ((LdcInsnNode) asmNode).cst + " Type=" + type; // +
            // ", Opcode=";
            // + opcode; // cst starts with mutationid if
            // this is location of mutation
        else if (asmNode instanceof LineNumberNode)
            return "LINE " + " " + ((LineNumberNode) asmNode).line;
        else if (asmNode instanceof LookupSwitchInsnNode)
            return "LookupSwitchInsnNode" + " " + asmNode.getOpcode()
                    + " Type=" + type + ", Opcode=" + opcode;
        else if (asmNode instanceof MultiANewArrayInsnNode)
            return "MULTIANEWARRAY " + " " + asmNode.getOpcode() + " Type="
                    + type + ", Opcode=" + opcode;
        else if (asmNode instanceof TableSwitchInsnNode)
            return "TableSwitchInsnNode" + " " + asmNode.getOpcode() + " Type="
                    + type + ", Opcode=" + opcode;
        else if (asmNode instanceof TypeInsnNode) {
            switch (asmNode.getOpcode()) {
                case Opcodes.NEW:
                    return "NEW " + ((TypeInsnNode) asmNode).desc;
                case Opcodes.ANEWARRAY:
                    return "ANEWARRAY " + ((TypeInsnNode) asmNode).desc;
                case Opcodes.CHECKCAST:
                    return "CHECKCAST " + ((TypeInsnNode) asmNode).desc;
                case Opcodes.INSTANCEOF:
                    return "INSTANCEOF " + ((TypeInsnNode) asmNode).desc;
                default:
                    return "Unknown node" + " Type=" + type + ", Opcode=" + opcode;
            }
        }
        // return "TYPE " + " " + node.getOpcode() + " Type=" + type
        // + ", Opcode=" + opcode;
        else if (asmNode instanceof VarInsnNode)
            return opcode + " " + ((VarInsnNode) asmNode).var;
        else
            return "Unknown node" + " Type=" + type + ", Opcode=" + opcode;
    }

    /**
     * <p>
     * printFrameInformation
     * </p>
     */
    public void printFrameInformation() {
        System.out.println("Frame STACK:");
        for (int i = 0; i < frame.getStackSize(); i++) {
            SourceValue v = (SourceValue) frame.getStack(i);
            System.out.print(" " + i + "(" + v.insns.size() + "): ");
            for (Object n : v.insns) {
                AbstractInsnNode node = (AbstractInsnNode) n;
                BytecodeInstruction ins = BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
                        methodName,
                        node);
                System.out.print(ins.toString() + ", ");
            }
            System.out.println();
        }

        System.out.println("Frame LOCALS:");
        for (int i = 1; i < frame.getLocals(); i++) {
            SourceValue v = (SourceValue) frame.getLocal(i);
            System.out.print(" " + i + "(" + v.insns.size() + "): ");
            for (Object n : v.insns) {
                AbstractInsnNode node = (AbstractInsnNode) n;
                BytecodeInstruction ins = BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
                        methodName,
                        node);
                System.out.print(ins.toString() + ", ");
            }
            System.out.println();
        }
    }

    // --- Inherited from Object ---

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String r = "I" + instructionId;

        r += " (" + +bytecodeOffset + ")";
        r += " " + explain();

        if (hasLineNumberSet() && !isLineNumber())
            r += " l" + getLineNumber();

        return r;
    }

    /**
     * Convenience method:
     * <p>
     * If this instruction is known by the BranchPool to be a Branch, you can
     * call this method in order to retrieve the corresponding Branch object
     * registered within the BranchPool.
     * <p>
     * Otherwise this method will return null;
     *
     * @return a {@link org.evosuite.coverage.branch.Branch} object.
     */
    public Branch toBranch() {

        try {
            return BranchPool.getInstance(classLoader).getBranchForInstruction(this);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <p>
     * proceedsOwnConstructorInvocation
     * </p>
     *
     * @return a boolean.
     */
    public boolean proceedsOwnConstructorInvocation() {

        RawControlFlowGraph cfg = getRawCFG();
        for (BytecodeInstruction other : cfg.vertexSet())
            if (other.isConstructorInvocation()
                    && other.isMethodCallOnSameObject())
                if (getInstructionId() < other.getInstructionId())
                    return true;

        return false;
    }

    /**
     * <p>
     * isWithinConstructor
     * </p>
     *
     * @return a boolean.
     */
    public boolean isWithinConstructor() {
        return getMethodName().startsWith("<init>");
    }

    /**
     * <p>
     * isLastInstructionInMethod
     * </p>
     *
     * @return a boolean.
     */
    public boolean isLastInstructionInMethod() {
        return equals(getRawCFG().getInstructionWithBiggestId());
    }

    /**
     * <p>
     * canBeExitPoint
     * </p>
     *
     * @return a boolean.
     */
    public boolean canBeExitPoint() {
        return canReturnFromMethod() || isLastInstructionInMethod();
    }

    /**
     * Returns the RawCFG of the method called by this instruction
     *
     * @return a {@link org.evosuite.graphs.cfg.RawControlFlowGraph} object.
     */
    public RawControlFlowGraph getCalledCFG() {
        if (!isMethodCall())
            return null;

        return GraphPool.getInstance(classLoader).getRawCFG(getCalledMethodsClass(),
                getCalledMethod());
    }

    /**
     * Determines whether this instruction calls a method on its own Object
     * ('this')
     * <p>
     * This is done using the getSourceOfMethodInvocationInstruction() method
     * and checking if the return of that method loads this using loadsReferenceToThis()
     *
     * @return a boolean.
     */
    public boolean isMethodCallOnSameObject() {
        BytecodeInstruction srcInstruction = getSourceOfMethodInvocationInstruction();
        if (srcInstruction == null)
            return false;
        return srcInstruction.loadsReferenceToThis();
    }

    /**
     * Determines whether this instruction calls a method on a field variable
     * <p>
     * This is done using the getSourceOfMethodInvocationInstruction() method
     * and checking if the return of that method is a field use instruction
     *
     * @return a boolean.
     */


    public boolean isMethodCallOfField() {
        if (!this.isMethodCall())
            return false;
        if (this.isInvokeStatic())
            return false;
        // If the instruction belongs to static initialization block of the
        // class, then the method call cannot be done on a fields.
        if (this.methodName.contains("<clinit>"))
            return false;
        BytecodeInstruction srcInstruction = getSourceOfMethodInvocationInstruction();
        if (srcInstruction == null)
            return false;

        //is a field use? But field uses are also "GETSTATIC"
        if (srcInstruction.isFieldNodeUse()) {

            //is static? if not, return yes. This control is not necessary in theory, but you never know...
            if (srcInstruction.isStaticDefUse()) {
                //is static, check if the name of the class that contain the static field is equals to the current class name
                //if is equals, return true, otherwise we are in a case where we are calling a field over an external static class
                //e.g. System.out
                if (srcInstruction.asmNode instanceof FieldInsnNode) {
                    String classNameField = ((FieldInsnNode) srcInstruction.asmNode).owner;
                    classNameField = classNameField.replace('/', '.');
                    return classNameField.equals(className);
                }
            } else {
                return true;
            }
        }
        return false;

    }

    /**
     * Determines the name of the field variable this method call is invoked on
     * <p>
     * This is done using the getSourceOfMethodInvocationInstruction() method
     * and returning its variable name
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getFieldMethodCallName() {
        BytecodeInstruction srcInstruction = getSourceOfMethodInvocationInstruction();
        if (srcInstruction == null)
            return null;
        return srcInstruction.getVariableName();
    }

    /**
     * If this is a method call instruction this method will return the
     * instruction that loaded the reference of the Object the method is invoked
     * onto the stack.
     * <p>
     * This is done using getSourceOfStackInstruction()
     * <p>
     * The reference is found on top of the stack minus the number of the called
     * methods argument
     */
    public BytecodeInstruction getSourceOfMethodInvocationInstruction() {
        if (!isMethodCall())
            return null;

        // the object on which this method is called is on top of the stack
        // minus the number of arguments the called method has
        return getSourceOfStackInstruction(getCalledMethodsArgumentCount());
    }

    /**
     * If this instruction is an array instruction this method will return the
     * BytecodeInstruction that loaded the reference of the array onto the
     * stack.
     * <p>
     * This is done using getSourceOfStackMethod()
     * <p>
     * The reference is found on top of the stack minus two
     */
    public BytecodeInstruction getSourceOfArrayReference() {
        if (isArrayStoreInstruction()) {
            // when reaching an array store instruction the stack should end in
            // <arrayref>,<index>,<value>. so the array reference is on top of the
            // stack minus two
            return getSourceOfStackInstruction(2);

        } else if (isArrayLoadInstruction()) {
            // when reaching an array store instruction the stack should end in
            // <arrayref>,<index>. so the array reference is on top of the
            // stack minus one
            return getSourceOfStackInstruction(1);

        } else {
            return null;
        }

    }

    /**
     * This method returns the BytecodeInstruction that loaded the reference
     * which is located on top of the stack minus positionFromTop when this
     * instruction is executed.
     * <p>
     * This is done using the CFGFrame created by the SourceInterpreter() of the
     * BytecodeAnalyzer via the CFGGenerator
     * <p>
     * Note that this method may return null. This can happen when aliasing is
     * involved. For example for method invocations on objects this can happen
     * when you first store the object in a local variable and then call a
     * method on that variable
     * <p>
     * see PairTestClass.sourceCallerTest() for an even worse example.
     * <p>
     * TODO: this could be done better by following the SourceValues even
     * further.
     */
    public BytecodeInstruction getSourceOfStackInstruction(int positionFromTop) {
        if (frame == null)
            throw new IllegalStateException(
                    "expect each BytecodeInstruction to have its CFGFrame set");

        int stackPos = frame.getStackSize() - (1 + positionFromTop);
        if (stackPos < 0) {
            StackTraceElement[] se = new Throwable().getStackTrace();
            int t = 0;
            System.out.println("Stack trace: ");
            while (t < se.length) {
                System.out.println(se[t]);
                t++;
            }
            return null;
        }
        SourceValue source = (SourceValue) frame.getStack(stackPos);
        if (source.insns.size() != 1) {
            // we don't know for sure, let's be conservative
            return null;
        }
        Object sourceIns = source.insns.iterator().next();
        AbstractInsnNode sourceInstruction = (AbstractInsnNode) sourceIns;
        BytecodeInstruction src = BytecodeInstructionPool.getInstance(classLoader).getInstruction(className,
                methodName,
                sourceInstruction);
        return src;
    }

    public boolean isFieldMethodCallDefinition() {
        if (!isMethodCallOfField())
            return false;
        // before this instruction is categorized in the DefUsePool we do not
        // know if
        // this instruction calls a pure or impure method, so we just label it
        // as both a Use and Definition for now
        if (!(DefUsePool.isKnownAsUse(this) && DefUsePool
                .isKnownAsFieldMethodCall(this))) {
            return true;
        }
        // once the DefUsePool knows about this instruction we only return true
        // if it was
        // categorized as a Use
        return DefUsePool.isKnownAsDefinition(this);
    }

    public boolean isFieldMethodCallUse() {
        if (!isMethodCallOfField())
            return false;
        // before this instruction is categorized in the DefUsePool we do not
        // know if
        // this instruction calls a pure or impure method, so we just label it
        // as both a Use and Definition for now
        if ((DefUsePool.isKnownAsFieldMethodCall(this) && !DefUsePool.isKnownAsDefinition(this))) {
            return true;
        }
        // once the DefUsePool knows about this instruction we only return true
        // if it was
        // categorized as a Use
        return DefUsePool.isKnownAsUse(this);
    }

    /**
     * <p>
     * isCallToPublicMethod
     * </p>
     *
     * @return a boolean.
     */
    public boolean isCallToPublicMethod() {
        if (!isMethodCall())
            return false;

        if (getCalledCFG() == null) {
            // TODO not sure if I am supposed to throw an Exception at this
            // point
            return false;
        }

        return getCalledCFG().isPublicMethod();
    }

    /**
     * <p>
     * isCallToStaticMethod
     * </p>
     *
     * @return a boolean.
     */
    public boolean isCallToStaticMethod() {
        if (!isMethodCall())
            return false;

        if (getCalledCFG() == null) {
            // TODO not sure if I am supposed to throw an Exception at this
            // point
            return false;
        }

        return getCalledCFG().isStaticMethod();
    }

    /**
     * <p>
     * canBeInstrumented
     * </p>
     *
     * @return a boolean.
     */
    public boolean canBeInstrumented() {
        // System.out.println("i cant be instrumented "+toString());
        return !isWithinConstructor() || !proceedsOwnConstructorInvocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((className == null) ? 0 : className.hashCode());
        result = prime * result + instructionId;
        result = prime * result
                + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BytecodeInstruction other = (BytecodeInstruction) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (instructionId != other.instructionId)
            return false;
        if (methodName == null) {
            return other.methodName == null;
        } else return methodName.equals(other.methodName);
    }

    // inherited from Object

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(BytecodeInstruction o) {
        return getLineNumber() - o.getLineNumber();
    }

}
