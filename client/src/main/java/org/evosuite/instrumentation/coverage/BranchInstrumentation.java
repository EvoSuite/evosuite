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

package org.evosuite.instrumentation.coverage;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.runtime.instrumentation.AnnotatedLabel;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * BranchInstrumentation class.
 * </p>
 *
 * @author Copied from CFGMethodAdapter
 */
public class BranchInstrumentation implements MethodInstrumentation {

    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(BranchInstrumentation.class);

    private static final String EXECUTION_TRACER = Type.getInternalName(ExecutionTracer.class);

    private ClassLoader classLoader;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb
     * .asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String,
     * java.lang.String, int)
     */

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void analyze(ClassLoader classLoader, MethodNode mn, String className,
                        String methodName, int access) {
        this.classLoader = classLoader;

        RawControlFlowGraph graph = GraphPool.getInstance(classLoader).getRawCFG(className,
                methodName);
        Iterator<AbstractInsnNode> j = mn.instructions.iterator();
        while (j.hasNext()) {
            AbstractInsnNode in = j.next();
            for (BytecodeInstruction v : graph.vertexSet()) {

                // If this is in the CFG and it's a branch...
                if (in.equals(v.getASMNode())) {
                    if (v.isBranch()) {
                        if (in.getPrevious() instanceof LabelNode) {
                            LabelNode label = (LabelNode) in.getPrevious();
                            if (label.getLabel() instanceof AnnotatedLabel) {
                                AnnotatedLabel aLabel = (AnnotatedLabel) label.getLabel();
                                if (aLabel.isStartTag()) {
                                    if (!aLabel.shouldIgnore()) {
                                        logger.debug("Found artificial branch: " + v);
                                        Branch b = BranchPool.getInstance(classLoader).getBranchForInstruction(v);
                                        b.setInstrumented(true);
                                        if (aLabel.shouldIgnoreFalse())
                                            b.setIgnoreFalse(true);
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        }
                        mn.instructions.insertBefore(v.getASMNode(), getInstrumentation(v));

                    } else if (v.isSwitch()) {

                        mn.instructions.insertBefore(v.getASMNode(),
                                getSwitchInstrumentation(v, mn,
                                        className,
                                        methodName));
                    }
                }
            }
        }
        mn.maxStack += 4;
    }

    /**
     * <p>
     * getInstrumentation
     * </p>
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a {@link org.objectweb.asm.tree.InsnList} object.
     */
    protected InsnList getInstrumentation(BytecodeInstruction instruction) {
        if (instruction == null)
            throw new IllegalArgumentException("null given");
        if (!instruction.isActualBranch())
            throw new IllegalArgumentException("branch instruction expected");
        if (!BranchPool.getInstance(classLoader).isKnownAsNormalBranchInstruction(instruction))
            throw new IllegalArgumentException(
                    "expect given instruction to be known by the BranchPool as a normal branch instruction");

        int opcode = instruction.getASMNode().getOpcode();
        int instructionId = instruction.getInstructionId();
        int branchId = BranchPool.getInstance(classLoader).getActualBranchIdForNormalBranchInstruction(instruction);
        if (branchId < 0)
            throw new IllegalStateException(
                    "expect BranchPool to know branchId for all branch instructions");

        InsnList instrumentation = new InsnList();

        switch (opcode) {
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
                instrumentation.add(new InsnNode(Opcodes.DUP));
                instrumentation.add(new LdcInsnNode(opcode));
                // instrumentation.add(new LdcInsnNode(id));
                instrumentation.add(new LdcInsnNode(branchId));
                instrumentation.add(new LdcInsnNode(instructionId));
                instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        EXECUTION_TRACER, "passedBranch", "(IIII)V", false));
                logger.debug("Adding passedBranch val=?, opcode=" + opcode + ", branch="
                        + branchId + ", bytecode_id=" + instructionId);

                break;
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
                instrumentation.add(new InsnNode(Opcodes.DUP2));
                instrumentation.add(new LdcInsnNode(opcode));
                // instrumentation.add(new LdcInsnNode(id));
                instrumentation.add(new LdcInsnNode(branchId));
                instrumentation.add(new LdcInsnNode(instructionId));
                instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        EXECUTION_TRACER, "passedBranch", "(IIIII)V", false));
                break;
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
                instrumentation.add(new InsnNode(Opcodes.DUP2));
                instrumentation.add(new LdcInsnNode(opcode));
                // instrumentation.add(new LdcInsnNode(id));
                instrumentation.add(new LdcInsnNode(branchId));
                instrumentation.add(new LdcInsnNode(instructionId));
                instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        EXECUTION_TRACER, "passedBranch",
                        "(Ljava/lang/Object;Ljava/lang/Object;III)V", false));
                break;
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                instrumentation.add(new InsnNode(Opcodes.DUP));
                instrumentation.add(new LdcInsnNode(opcode));
                // instrumentation.add(new LdcInsnNode(id));
                instrumentation.add(new LdcInsnNode(branchId));
                instrumentation.add(new LdcInsnNode(instructionId));
                instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        EXECUTION_TRACER, "passedBranch",
                        "(Ljava/lang/Object;III)V", false));
                break;
        }
        return instrumentation;
    }

    /**
     * Creates the instrumentation for switch statements as follows:
     * <p>
     * For each case <key>: in the switch, two calls to the ExecutionTracer are
     * added to the instrumentation, indicating whether the case is hit directly
     * or not. This is done by addInstrumentationForSwitchCases().
     * <p>
     * Additionally in order to trace the execution of the default: case of the
     * switch, the following instrumentation is added using
     * addDefaultCaseInstrumentation():
     * <p>
     * A new switch, holding the same <key>s as the original switch we want to
     * cover. All cases point to a label after which a call to the
     * ExecutionTracer is added, indicating that the default case was not hit
     * directly. Symmetrically the new switch has a default case: holding a call
     * to the ExecutionTracer to indicate that the default will be hit directly.
     *
     * @param v          a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param mn         a {@link org.objectweb.asm.tree.MethodNode} object.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @return a {@link org.objectweb.asm.tree.InsnList} object.
     */
    protected InsnList getSwitchInstrumentation(BytecodeInstruction v, MethodNode mn,
                                                String className, String methodName) {
        InsnList instrumentation = new InsnList();

        if (!v.isSwitch())
            throw new IllegalArgumentException("switch instruction expected");

        addInstrumentationForDefaultSwitchCase(v, instrumentation);

        addInstrumentationForSwitchCases(v, instrumentation, className, methodName);

        return instrumentation;
    }

    /**
     * For each actual case <key>: of a switch this method adds instrumentation
     * for the Branch corresponding to that case to the given instruction list.
     *
     * @param v               a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param instrumentation a {@link org.objectweb.asm.tree.InsnList} object.
     * @param className       a {@link java.lang.String} object.
     * @param methodName      a {@link java.lang.String} object.
     */
    protected void addInstrumentationForSwitchCases(BytecodeInstruction v,
                                                    InsnList instrumentation, String className, String methodName) {

        if (!v.isSwitch())
            throw new IllegalArgumentException("switch instruction expected");

        List<Branch> caseBranches = BranchPool.getInstance(classLoader).getCaseBranchesForSwitch(v);

        if (caseBranches == null || caseBranches.isEmpty())
            throw new IllegalStateException(
                    "expect BranchPool to know at least one Branch for each switch instruction");

        for (Branch targetCaseBranch : caseBranches) {
            if (targetCaseBranch.isDefaultCase())
                continue; // handled elsewhere

            Integer targetCaseValue = targetCaseBranch.getTargetCaseValue();
            Integer targetCaseBranchId = targetCaseBranch.getActualBranchId();

            instrumentation.add(new InsnNode(Opcodes.DUP));
            instrumentation.add(new LdcInsnNode(targetCaseValue));
            instrumentation.add(new LdcInsnNode(Opcodes.IF_ICMPEQ));
            instrumentation.add(new LdcInsnNode(targetCaseBranchId));
            instrumentation.add(new LdcInsnNode(v.getInstructionId()));
            instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    EXECUTION_TRACER, "passedBranch", "(IIIII)V", false));
        }
    }

    /**
     * <p>
     * addInstrumentationForDefaultSwitchCase
     * </p>
     *
     * @param v               a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param instrumentation a {@link org.objectweb.asm.tree.InsnList} object.
     */
    protected void addInstrumentationForDefaultSwitchCase(BytecodeInstruction v,
                                                          InsnList instrumentation) {

        if (v.isTableSwitch())
            addInstrumentationForDefaultTableswitchCase(v, instrumentation);

        if (v.isLookupSwitch())
            addInstrumentationForDefaultLookupswitchCase(v, instrumentation);

    }

    /**
     * <p>
     * addInstrumentationForDefaultTableswitchCase
     * </p>
     *
     * @param v               a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param instrumentation a {@link org.objectweb.asm.tree.InsnList} object.
     */
    protected void addInstrumentationForDefaultTableswitchCase(BytecodeInstruction v,
                                                               InsnList instrumentation) {

        if (!v.isTableSwitch())
            throw new IllegalArgumentException("tableswitch instruction expected");

        // setup instructions

        TableSwitchInsnNode toInstrument = (TableSwitchInsnNode) v.getASMNode();

        LabelNode caseLabel = new LabelNode();
        LabelNode defaultLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();

        int keySize = (toInstrument.max - toInstrument.min) + 1;
        LabelNode[] caseLabels = new LabelNode[keySize];
        for (int i = 0; i < keySize; i++)
            caseLabels[i] = caseLabel;

        TableSwitchInsnNode mySwitch = new TableSwitchInsnNode(toInstrument.min,
                toInstrument.max, defaultLabel, caseLabels);

        // add instrumentation
        addDefaultCaseInstrumentation(v, instrumentation, mySwitch, defaultLabel,
                caseLabel, endLabel);

    }

    /**
     * <p>
     * addInstrumentationForDefaultLookupswitchCase
     * </p>
     *
     * @param v               a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param instrumentation a {@link org.objectweb.asm.tree.InsnList} object.
     */
    protected void addInstrumentationForDefaultLookupswitchCase(BytecodeInstruction v,
                                                                InsnList instrumentation) {

        if (!v.isLookupSwitch())
            throw new IllegalArgumentException("lookup switch expected");

        // setup instructions
        LookupSwitchInsnNode toInstrument = (LookupSwitchInsnNode) v.getASMNode();

        LabelNode caseLabel = new LabelNode();
        LabelNode defaultLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();

        int keySize = toInstrument.keys.size();

        int[] keys = new int[keySize];
        LabelNode[] labels = new LabelNode[keySize];
        for (int i = 0; i < keySize; i++) {
            keys[i] = toInstrument.keys.get(i);
            labels[i] = caseLabel;
        }

        LookupSwitchInsnNode myLookup = new LookupSwitchInsnNode(defaultLabel, keys,
                labels);

        addDefaultCaseInstrumentation(v, instrumentation, myLookup, defaultLabel,
                caseLabel, endLabel);

    }

    /**
     * <p>
     * addDefaultCaseInstrumentation
     * </p>
     *
     * @param v               a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param instrumentation a {@link org.objectweb.asm.tree.InsnList} object.
     * @param mySwitch        a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     * @param defaultLabel    a {@link org.objectweb.asm.tree.LabelNode} object.
     * @param caseLabel       a {@link org.objectweb.asm.tree.LabelNode} object.
     * @param endLabel        a {@link org.objectweb.asm.tree.LabelNode} object.
     */
    protected void addDefaultCaseInstrumentation(BytecodeInstruction v,
                                                 InsnList instrumentation, AbstractInsnNode mySwitch, LabelNode defaultLabel,
                                                 LabelNode caseLabel, LabelNode endLabel) {

        int defaultCaseBranchId = BranchPool.getInstance(classLoader).getDefaultBranchForSwitch(v).getActualBranchId();

        // add helper switch
        instrumentation.add(new InsnNode(Opcodes.DUP));
        instrumentation.add(mySwitch);

        // add call for default case not covered
        instrumentation.add(caseLabel);
        addDefaultCaseNotCoveredCall(v, instrumentation, defaultCaseBranchId);

        // jump over default (break)
        instrumentation.add(new JumpInsnNode(Opcodes.GOTO, endLabel));

        // add call for default case covered
        instrumentation.add(defaultLabel);
        addDefaultCaseCoveredCall(v, instrumentation, defaultCaseBranchId);

        instrumentation.add(endLabel);

    }

    /**
     * <p>
     * addDefaultCaseCoveredCall
     * </p>
     *
     * @param v                   a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param instrumentation     a {@link org.objectweb.asm.tree.InsnList} object.
     * @param defaultCaseBranchId a int.
     */
    protected void addDefaultCaseCoveredCall(BytecodeInstruction v,
                                             InsnList instrumentation, int defaultCaseBranchId) {

        instrumentation.add(new LdcInsnNode(0));
        instrumentation.add(new LdcInsnNode(Opcodes.IFEQ));
        instrumentation.add(new LdcInsnNode(defaultCaseBranchId));
        instrumentation.add(new LdcInsnNode(v.getInstructionId()));
        instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                EXECUTION_TRACER, "passedBranch", "(IIII)V", false));

    }

    /**
     * <p>
     * addDefaultCaseNotCoveredCall
     * </p>
     *
     * @param v                   a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param instrumentation     a {@link org.objectweb.asm.tree.InsnList} object.
     * @param defaultCaseBranchId a int.
     */
    protected void addDefaultCaseNotCoveredCall(BytecodeInstruction v,
                                                InsnList instrumentation, int defaultCaseBranchId) {

        instrumentation.add(new LdcInsnNode(0));
        instrumentation.add(new LdcInsnNode(Opcodes.IFNE));
        instrumentation.add(new LdcInsnNode(defaultCaseBranchId));
        instrumentation.add(new LdcInsnNode(v.getInstructionId()));
        instrumentation.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                EXECUTION_TRACER, "passedBranch", "(IIII)V", false));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods
     * ()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeOnExcludedMethods() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeOnMainMethod() {
        return false;
    }

}
