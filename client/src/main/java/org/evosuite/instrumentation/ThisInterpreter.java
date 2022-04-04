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

package org.evosuite.instrumentation;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * <p>ThisInterpreter class.</p>
 *
 * @author Gordon Fraser
 */
public class ThisInterpreter extends BasicInterpreter {

    /**
     * Constant <code>THIS_VALUE</code>
     */
    public final static BasicValue THIS_VALUE = new BasicValue(Type.INT_TYPE);

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.analysis.BasicInterpreter#copyOperation(org.objectweb.asm.tree.AbstractInsnNode, org.objectweb.asm.tree.analysis.BasicValue)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public BasicValue copyOperation(AbstractInsnNode insn, BasicValue value)
            throws AnalyzerException {
        if (insn.getOpcode() == Opcodes.ALOAD) {
            VarInsnNode varNode = (VarInsnNode) insn;
            if (varNode.var == 0) {
                return THIS_VALUE;
            }
        }
        return super.copyOperation(insn, value);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.analysis.BasicInterpreter#merge(org.objectweb.asm.tree.analysis.BasicValue, org.objectweb.asm.tree.analysis.BasicValue)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public BasicValue merge(BasicValue v, BasicValue w) {
        if (v == THIS_VALUE && w == BasicValue.REFERENCE_VALUE)
            return BasicValue.REFERENCE_VALUE;
        else if (w == THIS_VALUE && v == BasicValue.REFERENCE_VALUE)
            return BasicValue.REFERENCE_VALUE;
        else
            return super.merge(v, w);
    }

}
