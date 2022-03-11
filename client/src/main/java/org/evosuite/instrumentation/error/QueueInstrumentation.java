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
package org.evosuite.instrumentation.error;

import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class QueueInstrumentation extends ErrorBranchInstrumenter {

    private static final List<String> LISTNAMES = Arrays.asList(Queue.class.getCanonicalName().replace('.', '/'),
            PriorityQueue.class.getCanonicalName().replace('.', '/'),
            Deque.class.getCanonicalName().replace('.', '/'),
            LinkedBlockingDeque.class.getCanonicalName().replace('.', '/'),
            BlockingDeque.class.getCanonicalName().replace('.', '/'),
            ArrayDeque.class.getCanonicalName().replace('.', '/'));

    private final List<String> emptyListMethods = Arrays.asList("remove", "element");

    public QueueInstrumentation(ErrorConditionMethodAdapter mv) {
        super(mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc, boolean itf) {
        if (LISTNAMES.contains(owner)) {
            if (emptyListMethods.contains(name)) {
                // empty
                Map<Integer, Integer> tempVariables = getMethodCallee(desc);

                tagBranchStart();
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner,
                        "isEmpty", "()Z", false);
                insertBranch(Opcodes.IFLE, "java/util/NoSuchElementException");
                tagBranchEnd();
                restoreMethodParameters(tempVariables, desc);
            }
        }
    }
}
