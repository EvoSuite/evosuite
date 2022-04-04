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

import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/**
 * Make sure arrays of booleans are also transformed
 */
public class BooleanArrayTransformer extends MethodNodeTransformer {
    /* (non-Javadoc)
     * @see org.evosuite.instrumentation.MethodNodeTransformer#transformIntInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.IntInsnNode)
     */
    @Override
    protected AbstractInsnNode transformIntInsnNode(MethodNode mn,
                                                    IntInsnNode intInsnNode) {
        if (intInsnNode.operand == Opcodes.T_BOOLEAN) {
            intInsnNode.operand = Opcodes.T_INT;
        }
        return intInsnNode;
    }

    /* (non-Javadoc)
     * @see org.evosuite.instrumentation.MethodNodeTransformer#transformMultiANewArrayInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.MultiANewArrayInsnNode)
     */
    @Override
    protected AbstractInsnNode transformMultiANewArrayInsnNode(MethodNode mn,
                                                               MultiANewArrayInsnNode arrayInsnNode) {
        String new_desc = "";
        Type t = Type.getType(arrayInsnNode.desc);
        while (t.getSort() == Type.ARRAY) {
            new_desc += "[";
            t = t.getElementType();
        }
        if (t.equals(Type.BOOLEAN_TYPE))
            new_desc += "I";
        else
            new_desc += t.getDescriptor();
        arrayInsnNode.desc = new_desc;
        return arrayInsnNode;
    }

    /* (non-Javadoc)
     * @see org.evosuite.instrumentation.MethodNodeTransformer#transformTypeInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.TypeInsnNode)
     */
    @Override
    protected AbstractInsnNode transformTypeInsnNode(MethodNode mn,
                                                     TypeInsnNode typeNode) {
        String new_desc = "";
        int pos = 0;
        while (pos < typeNode.desc.length() && typeNode.desc.charAt(pos) == '[') {
            new_desc += "[";
            pos++;
        }
        String d = typeNode.desc.substring(pos);
        BooleanTestabilityTransformation.logger.info("Unfolded arrays to: " + d);
        if (d.equals("Z"))
            //if (t.equals(Type.BOOLEAN_TYPE))
            new_desc += "I";
        else
            new_desc += d; //t.getInternalName();
        typeNode.desc = new_desc;
        return typeNode;
    }
}