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

import org.evosuite.instrumentation.testability.BooleanHelper;
import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * If a method needs to return a Boolean and not an int, then we need to
 * transform the int back to a Boolean
 */
public class BooleanReturnTransformer extends MethodNodeTransformer {

    private final BooleanTestabilityTransformation booleanTestabilityTransformation;

    /**
     * @param booleanTestabilityTransformation
     */
    public BooleanReturnTransformer(
            BooleanTestabilityTransformation booleanTestabilityTransformation) {
        this.booleanTestabilityTransformation = booleanTestabilityTransformation;
    }

    /* (non-Javadoc)
     * @see org.evosuite.instrumentation.MethodNodeTransformer#transformInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnNode)
     */
    @Override
    protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
        //String desc = DescriptorMapping.getInstance().getMethodDesc(className, mn.name, mn.desc);
        Type returnType = Type.getReturnType(mn.desc);
        if (!returnType.equals(Type.BOOLEAN_TYPE)) {
            return insnNode;
        }

        if (insnNode.getOpcode() == Opcodes.IRETURN) {
            BooleanTestabilityTransformation.logger.debug("Inserting conversion before IRETURN of " + this.booleanTestabilityTransformation.className + "."
                    + mn.name);
            // If this function cannot be transformed, add a call to convert the value to a proper Boolean
            MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
                    Type.getInternalName(BooleanHelper.class), "intToBoolean",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                            Type.INT_TYPE));
            mn.instructions.insertBefore(insnNode, n);
        }

        return insnNode;
    }
}