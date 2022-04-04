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

package org.evosuite.runtime.instrumentation;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * <p>AnnotatedMethodNode class.</p>
 *
 * @author fraser
 */
public class AnnotatedMethodNode extends MethodNode {

    /**
     * <p>Constructor for AnnotatedMethodNode.</p>
     *
     * @param access     a int.
     * @param name       a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     * @param signature  a {@link java.lang.String} object.
     * @param exceptions an array of {@link java.lang.String} objects.
     */
    public AnnotatedMethodNode(int access, String name, String desc, String signature,
                               String[] exceptions) {
        super(Opcodes.ASM9, access, name, desc, signature, exceptions);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the LabelNode corresponding to the given Label. Creates a new
     * LabelNode if necessary. The default implementation of this method uses
     * the {@link Label#info} field to store associations between labels and
     * label nodes.
     */
    @Override
    protected LabelNode getLabelNode(final Label l) {
        if (l instanceof AnnotatedLabel) {
            AnnotatedLabel al = (AnnotatedLabel) l;
            al.setParent(new LabelNode(al));
            return al.getParent();
        } else {
            if (!(l.info instanceof LabelNode)) {
                l.info = new LabelNode(l);
            }
            return (LabelNode) l.info;
        }
    }
}
