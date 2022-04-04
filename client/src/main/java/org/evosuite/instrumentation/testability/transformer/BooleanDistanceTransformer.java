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

import org.evosuite.instrumentation.TransformationStatistics;
import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This transformer inserts calls to the put function before a Boolean
 * predicate
 */
public class BooleanDistanceTransformer extends MethodNodeTransformer {

    private final BooleanTestabilityTransformation booleanTestabilityTransformation;

    /**
     * @param booleanTestabilityTransformation
     */
    public BooleanDistanceTransformer(
            BooleanTestabilityTransformation booleanTestabilityTransformation) {
        this.booleanTestabilityTransformation = booleanTestabilityTransformation;
    }

    /* (non-Javadoc)
     * @see org.evosuite.instrumentation.MethodNodeTransformer#transformJumpInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.JumpInsnNode)
     */
    @Override
    protected AbstractInsnNode transformJumpInsnNode(MethodNode mn,
                                                     JumpInsnNode jumpNode) {

        switch (jumpNode.getOpcode()) {
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
                TransformationStatistics.insertPush(jumpNode.getOpcode());
                this.booleanTestabilityTransformation.insertPush(jumpNode.getOpcode(), jumpNode, mn.instructions);
                break;
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
                TransformationStatistics.insertPush(jumpNode.getOpcode());
                this.booleanTestabilityTransformation.insertPush2(jumpNode.getOpcode(), jumpNode, mn.instructions);
                break;
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                TransformationStatistics.insertPush(jumpNode.getOpcode());
                this.booleanTestabilityTransformation.insertPushNull(jumpNode.getOpcode(), jumpNode, mn.instructions);
                break;
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
                TransformationStatistics.insertPush(jumpNode.getOpcode());
                this.booleanTestabilityTransformation.insertPushEquals(jumpNode.getOpcode(), jumpNode, mn.instructions);
                break;
            default:
                // GOTO, JSR: Do nothing
        }
        return jumpNode;
    }
}