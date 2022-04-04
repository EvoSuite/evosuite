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

import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cdg.ControlDependenceGraph;
import org.evosuite.graphs.cfg.*;
import org.evosuite.instrumentation.TransformationStatistics;
import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.evosuite.instrumentation.testability.DescriptorMapping;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Expand ifs without else
 */
public class ImplicitElseTransformer extends MethodNodeTransformer {


    private final BooleanTestabilityTransformation booleanTestabilityTransformation;

    /**
     * @param booleanTestabilityTransformation
     */
    public ImplicitElseTransformer(
            BooleanTestabilityTransformation booleanTestabilityTransformation) {
        this.booleanTestabilityTransformation = booleanTestabilityTransformation;
    }

    private final Set<ControlDependency> addedNodes = new HashSet<>();

    /*** Keep track of inserted PUTFIELDs */
    private final Set<AbstractInsnNode> addedInsns = new HashSet<>();

    @SuppressWarnings("unchecked")
    private boolean isDefinedBefore(MethodNode mn, VarInsnNode var,
                                    AbstractInsnNode position) {
        // TODO: Iterate over local variables and check if local is defined here
        List<LocalVariableNode> localVar = mn.localVariables;
        if (localVar.isEmpty()) {
            // If we have no debug information, try to guess
            AbstractInsnNode pos = position.getPrevious();
            while (pos != mn.instructions.getFirst()) {
                if (pos instanceof VarInsnNode) {
                    VarInsnNode vn = (VarInsnNode) pos;
                    if (var.var == vn.var) {
                        return true;
                    }
                }
                pos = pos.getPrevious();
            }
        } else {

            int current = mn.instructions.indexOf(position);
            for (LocalVariableNode local : localVar) {
                if (local.index == var.var) {
                    int start = mn.instructions.indexOf(local.start);
                    int end = mn.instructions.indexOf(local.end);
                    if (current >= start && current <= end)
                        return true;
                }
            }
        }

        return false;
    }

    private void handleDependency(ControlDependency dependency,
                                  ControlDependenceGraph cdg, MethodNode mn, FieldInsnNode varNode,
                                  BytecodeInstruction parentLevel) {

        if (addedNodes.contains(dependency))
            return;

        // Get the basic blocks reachable if the dependency would evaluate different
        Set<BasicBlock> blocks = cdg.getAlternativeBlocks(dependency);
        addedNodes.add(dependency);

        Set<ControlDependency> dependencies = dependency.getBranch().getInstruction().getControlDependencies();
        //if (dependencies.size() == 1) {
        //	ControlDependency dep = dependencies.iterator().next();
        for (ControlDependency dep : dependencies) {
            if (!addedNodes.contains(dep) && dep != dependency)
                handleDependency(dep, cdg, mn, varNode,
                        dependency.getBranch().getInstruction());
        }

        // TODO: Need to check that there is an assignment in every alternative path through CDG

        boolean hasAssignment = false;
        for (BasicBlock block : blocks) {
            // If this block also assigns a value to the same variable
            for (BytecodeInstruction instruction : block) {
                if (instruction.getASMNode().getOpcode() == Opcodes.PUTFIELD
                        || instruction.getASMNode().getOpcode() == Opcodes.PUTSTATIC) {
                    FieldInsnNode otherFieldNode = (FieldInsnNode) instruction.getASMNode();
                    FieldInsnNode thisFieldNode = varNode;
                    if (otherFieldNode.owner.equals(thisFieldNode.owner)
                            && otherFieldNode.name.equals(thisFieldNode.name)) {
                        hasAssignment = true;
                        break;
                    }
                }
            }
            if (hasAssignment) {
                break;
            }
        }

        // The Flag assignment is is the dependency evaluates to the given value
        // We thus need to insert the tautoligical assignment either directly after the IF (if the value is true)
        // or before the jump target (if the value is false)

        if (!hasAssignment) {
            if (dependency.getBranch().getInstruction().isSwitch()) {
                BooleanTestabilityTransformation.logger.warn("Don't know how to handle Switches yet");
                return;
            }

            TransformationStatistics.transformedImplicitElse();

            JumpInsnNode jumpNode = (JumpInsnNode) dependency.getBranch().getInstruction().getASMNode();
            FieldInsnNode newLoad = new FieldInsnNode(
                    varNode.getOpcode() == Opcodes.PUTSTATIC ? Opcodes.GETSTATIC
                            : Opcodes.GETFIELD, varNode.owner, varNode.name,
                    varNode.desc);
            FieldInsnNode newStore = new FieldInsnNode(varNode.getOpcode(),
                    varNode.owner, varNode.name, varNode.desc);
            AbstractInsnNode newOwnerLoad1 = null;
            AbstractInsnNode newOwnerLoad2 = null;
            if (varNode.getOpcode() == Opcodes.PUTFIELD) {
                // Need to copy the bloody owner
                // Check for VarInsn
                //if (varNode.getPrevious().getOpcode() == Opcodes.ALOAD) {
                newOwnerLoad1 = new VarInsnNode(Opcodes.ALOAD, 0);
                newOwnerLoad2 = new VarInsnNode(Opcodes.ALOAD, 0);
				/*
				} else {
				// Else use helper function
				// Insert DUP and
				logger.info("Wargh");
				System.exit(0);
				fieldOwnerId++;
				InsnNode dupNode = new InsnNode(Opcodes.DUP);
				mn.instructions.insertBefore(varNode, new LdcInsnNode(
				        fieldOwnerId));
				mn.instructions.insertBefore(varNode, dupNode);
				registerInstruction(mn, varNode, dupNode);
				MethodInsnNode storeOwner = new MethodInsnNode(
				        Opcodes.INVOKESTATIC,
				        "org/evosuite/instrumentation/BooleanHelper",
				        "setFieldOwner", "(ILjava/lang/Object;)V");
				mn.instructions.insertBefore(varNode, storeOwner);
				registerInstruction(mn, varNode, storeOwner);
				newOwnerLoad1 = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        "org/evosuite/instrumentation/BooleanHelper",
				        "getFieldOwner", "(I)Ljava/lang/Object;");
				newOwnerLoad2 = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        "org/evosuite/instrumentation/BooleanHelper",
				        "getFieldOwner", "(I)Ljava/lang/Object;");
				}
				*/
            }

            if (dependency.getBranchExpressionValue()) {
                BooleanTestabilityTransformation.logger.info("Inserting after if");
                // Insert directly after if
                mn.instructions.insert(jumpNode, newStore);
                mn.instructions.insert(jumpNode, newLoad);
                if (newOwnerLoad1 != null) {
                    mn.instructions.insert(jumpNode, newOwnerLoad1);
                    registerInstruction(mn, varNode, newOwnerLoad1);
                }
                if (newOwnerLoad2 != null) {
                    mn.instructions.insert(jumpNode, newOwnerLoad2);
                    registerInstruction(mn, varNode, newOwnerLoad2);
                }
                registerInstruction(mn, varNode, newStore);
                registerInstruction(mn, varNode, newLoad);

            } else {
                BooleanTestabilityTransformation.logger.info("Inserting as jump target");

                // Insert as jump target
                LabelNode target = jumpNode.label;
                LabelNode newTarget = new LabelNode(new Label());

                registerInstruction(mn, target, newStore);
                registerInstruction(mn, target, newLoad);

                InsnList assignment = new InsnList();
                assignment.add(new JumpInsnNode(Opcodes.GOTO, target));
                assignment.add(newTarget);
                if (newOwnerLoad1 != null) {
                    assignment.add(newOwnerLoad1);
                    registerInstruction(mn, target, newOwnerLoad1);
                }
                if (newOwnerLoad2 != null) {
                    assignment.add(newOwnerLoad2);
                    registerInstruction(mn, target, newOwnerLoad2);
                }
                assignment.add(newLoad);
                assignment.add(newStore);
                jumpNode.label = newTarget;

                mn.instructions.insertBefore(target, assignment);
            }
            addedInsns.add(newStore);
            addedInsns.add(newLoad);
        }

    }

    private void registerInstruction(MethodNode mn, AbstractInsnNode oldValue,
                                     AbstractInsnNode newValue) {
        BytecodeInstruction oldInstruction = BytecodeInstructionPool.getInstance(this.booleanTestabilityTransformation.classLoader).getInstruction(this.booleanTestabilityTransformation.className,
                mn.name
                        + mn.desc,
                oldValue);
        BytecodeInstruction instruction = BytecodeInstructionFactory.createBytecodeInstruction(this.booleanTestabilityTransformation.classLoader,
                this.booleanTestabilityTransformation.className,
                mn.name
                        + mn.desc,
                oldInstruction.getInstructionId(),
                0,
                newValue);
        instruction.setBasicBlock(oldInstruction.getBasicBlock());
        BytecodeInstructionPool.getInstance(this.booleanTestabilityTransformation.classLoader).registerInstruction(instruction);
    }

    private void handleDependency(ControlDependency dependency,
                                  ControlDependenceGraph cdg, MethodNode mn, VarInsnNode varNode,
                                  BytecodeInstruction parentLevel) {

        if (addedNodes.contains(dependency))
            return;

        // Get the basic blocks reachable if the dependency would evaluate different
        Set<BasicBlock> blocks = cdg.getAlternativeBlocks(dependency);
        addedNodes.add(dependency);

        Set<ControlDependency> dependencies = dependency.getBranch().getInstruction().getControlDependencies();
        //if (dependencies.size() == 1) {
        //	ControlDependency dep = dependencies.iterator().next();
        for (ControlDependency dep : dependencies) {
            if (!addedNodes.contains(dep) && dep != dependency)
                handleDependency(dep, cdg, mn, varNode,
                        dependency.getBranch().getInstruction());
        }

        // TODO: Need to check that there is an assignment in every alternative path through CDG

        boolean hasAssignment = false;
        for (BasicBlock block : blocks) {
            // If this block also assigns a value to the same variable
            for (BytecodeInstruction instruction : block) {
                if (instruction.getASMNode().getOpcode() == Opcodes.ISTORE) {
                    VarInsnNode otherVarNode = (VarInsnNode) instruction.getASMNode();
                    VarInsnNode thisVarNode = varNode;
                    if (otherVarNode.var == thisVarNode.var) {
                        hasAssignment = true;
                        break;
                    }
                }
            }
            if (hasAssignment) {
                break;
            }
        }

        // The Flag assignment is is the dependency evaluates to the given value
        // We thus need to insert the tautoligical assignment either directly after the IF (if the value is true)
        // or before the jump target (if the value is false)

        if (!hasAssignment) {
            TransformationStatistics.transformedImplicitElse();
            if (dependency.getBranch().getInstruction().isSwitch()) {
                BooleanTestabilityTransformation.logger.warn("Don't know how to handle Switches yet");
                return;
            }
            JumpInsnNode jumpNode = (JumpInsnNode) dependency.getBranch().getInstruction().getASMNode();
            VarInsnNode newStore = new VarInsnNode(Opcodes.ISTORE, varNode.var);
            VarInsnNode newLoad = new VarInsnNode(Opcodes.ILOAD, varNode.var);
            if (dependency.getBranchExpressionValue()) {
                BooleanTestabilityTransformation.logger.info("Inserting else branch directly after if");
                // Insert directly after if
                if (isDefinedBefore(mn, varNode, jumpNode)) {
                    mn.instructions.insert(jumpNode, newStore);
                    mn.instructions.insert(jumpNode, newLoad);
                    registerInstruction(mn, varNode, newStore);
                    registerInstruction(mn, varNode, newLoad);
                }

            } else {
                BooleanTestabilityTransformation.logger.info("Inserting else branch as jump target");
                // Insert as jump target
                if (isDefinedBefore(mn, varNode, jumpNode)) {

                    LabelNode target = jumpNode.label;
                    LabelNode newTarget = new LabelNode(new Label());

                    // jumpNode or target?
                    registerInstruction(mn, jumpNode.getNext(), newStore);
                    registerInstruction(mn, jumpNode.getNext(), newLoad);

                    InsnList assignment = new InsnList();
                    assignment.add(new JumpInsnNode(Opcodes.GOTO, target));
                    assignment.add(newTarget);
                    assignment.add(newLoad);
                    assignment.add(newStore);
                    jumpNode.label = newTarget;

                    mn.instructions.insertBefore(target, assignment);
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see org.evosuite.instrumentation.MethodNodeTransformer#transformFieldInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.FieldInsnNode)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
                                                      FieldInsnNode fieldNode) {

        if ((fieldNode.getOpcode() == Opcodes.PUTFIELD || fieldNode.getOpcode() == Opcodes.PUTSTATIC)
                && DescriptorMapping.getInstance().isTransformedOrBooleanField(fieldNode.owner,
                fieldNode.name,
                fieldNode.desc)) {

            if (addedInsns.contains(fieldNode))
                return fieldNode;

            // Can only handle cases where the field owner is loaded directly before the field
            // TODO: We could pop the top of the stack and DUP the owner, but would need to take care
            // whether we need to pop one or two words
            if (fieldNode.getOpcode() == Opcodes.PUTFIELD) {
                AbstractInsnNode previous = fieldNode.getPrevious();
                while (previous instanceof LineNumberNode
                        || previous instanceof FrameNode
                        || previous.getOpcode() == Opcodes.ICONST_0
                        || previous.getOpcode() == Opcodes.ICONST_1)
                    previous = previous.getPrevious();
                if (previous.getOpcode() != Opcodes.ALOAD) {
                    BooleanTestabilityTransformation.logger.info("Can't handle case of " + previous);
                    return fieldNode;
                }
                VarInsnNode varNode = (VarInsnNode) previous;
                if (varNode.var != 0) {
                    BooleanTestabilityTransformation.logger.info("Can't handle case of " + previous);
                    return fieldNode;
                }
            }
            BooleanTestabilityTransformation.logger.info("Handling PUTFIELD case!");

            // Check if ICONST_0 or ICONST_1 are on the stack
            ControlDependenceGraph cdg = GraphPool.getInstance(this.booleanTestabilityTransformation.classLoader).getCDG(this.booleanTestabilityTransformation.className.replace("/",
                            "."),
                    mn.name
                            + mn.desc);
            int index = mn.instructions.indexOf(fieldNode);
            BooleanTestabilityTransformation.logger.info("Getting bytecode instruction for " + fieldNode.name + "/"
                    + ((FieldInsnNode) mn.instructions.get(index)).name);
            InsnList nodes = mn.instructions;
            ListIterator<AbstractInsnNode> it = nodes.iterator();
            while (it.hasNext()) {
                BytecodeInstruction in = new BytecodeInstruction(
                        this.booleanTestabilityTransformation.classLoader, this.booleanTestabilityTransformation.className, mn.name,
                        0, 0, it.next());
                BooleanTestabilityTransformation.logger.info(in.toString());
            }
            BytecodeInstruction insn = BytecodeInstructionPool.getInstance(this.booleanTestabilityTransformation.classLoader).getInstruction(this.booleanTestabilityTransformation.className.replace("/",
                            "."),
                    mn.name
                            + mn.desc,
                    index);
            if (insn == null)
                insn = BytecodeInstructionPool.getInstance(this.booleanTestabilityTransformation.classLoader).getInstruction(this.booleanTestabilityTransformation.className.replace("/",
                                "."),
                        mn.name
                                + mn.desc,
                        fieldNode);
            //varNode);
            if (insn == null) {
                // TODO: Find out why
                BooleanTestabilityTransformation.logger.info("ERROR: Could not find node");
                return fieldNode;
            }
            if (insn.getASMNode().getOpcode() != fieldNode.getOpcode()) {
                BooleanTestabilityTransformation.logger.info("Found wrong bytecode instruction at this index!");
                BytecodeInstructionPool.getInstance(this.booleanTestabilityTransformation.classLoader).getInstruction(this.booleanTestabilityTransformation.className,
                        mn.name
                                + mn.desc,
                        fieldNode);
            }
            if (insn.getBasicBlock() == null) {
                BooleanTestabilityTransformation.logger.info("ERROR: Problematic node found");
                return fieldNode;
            }
            Set<ControlDependency> dependencies = insn.getControlDependencies();
            BooleanTestabilityTransformation.logger.info("Found flag assignment: " + insn + ", checking "
                    + dependencies.size() + " control dependencies");

            for (ControlDependency dep : dependencies) {
                if (!addedNodes.contains(dep))
                    handleDependency(dep, cdg, mn, fieldNode, insn);
            }
        }
        return fieldNode;
    }

    /* (non-Javadoc)
     * @see org.evosuite.instrumentation.MethodNodeTransformer#transformVarInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.VarInsnNode)
     */
    @Override
    protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
        if (varNode.getOpcode() == Opcodes.ISTORE
                && this.booleanTestabilityTransformation.isBooleanVariable(varNode.var, mn)) {

            // Check if ICONST_0 or ICONST_1 are on the stack
            ControlDependenceGraph cdg = GraphPool.getInstance(this.booleanTestabilityTransformation.classLoader).getCDG(this.booleanTestabilityTransformation.className.replace("/",
                            "."),
                    mn.name
                            + mn.desc);
            int index = mn.instructions.indexOf(varNode);
            BytecodeInstruction insn = BytecodeInstructionPool.getInstance(this.booleanTestabilityTransformation.classLoader).getInstruction(this.booleanTestabilityTransformation.className.replace("/",
                            "."),
                    mn.name
                            + mn.desc,
                    index);
            //varNode);
            if (insn == null) {
                // TODO: Debug this on org.exolab.jms.net.uri.URI
                BooleanTestabilityTransformation.logger.info("WARNING: Instruction not found!");
                return varNode;
            }
            if (insn.getASMNode().getOpcode() != varNode.getOpcode()) {
                BooleanTestabilityTransformation.logger.info("Found wrong bytecode instruction at this index!");
                insn = BytecodeInstructionPool.getInstance(this.booleanTestabilityTransformation.classLoader).getInstruction(this.booleanTestabilityTransformation.className,
                        mn.name
                                + mn.desc,
                        varNode);
                if (insn == null) {
                    // TODO: Debug this on org.exolab.jms.net.uri.URI
                    BooleanTestabilityTransformation.logger.info("WARNING: Instruction not found!");
                    return varNode;
                }
            }
            Set<ControlDependency> dependencies = insn.getControlDependencies();
            BooleanTestabilityTransformation.logger.info("Found flag assignment: " + insn + ", checking "
                    + dependencies.size() + " control dependencies");

            for (ControlDependency dep : dependencies) {
                if (!addedNodes.contains(dep))
                    handleDependency(dep, cdg, mn, varNode, insn);
            }

            // Only do completion if there's only one dependency
            // Not sure how other cases would look like
			/*
							//if (dependencies.size() > 1)
							//	return varNode;
							//else
							if (dependencies.isEmpty())
								return varNode;

							ControlDependency dep = dependencies.iterator().next();
							if (!addedNodes.contains(dep))
								handleDependency(dep, cdg, mn, varNode, insn);
								*/

        }
        return varNode;
    }

}