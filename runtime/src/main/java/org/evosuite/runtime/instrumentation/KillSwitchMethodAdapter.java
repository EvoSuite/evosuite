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

import org.evosuite.runtime.thread.KillSwitchHandler;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Add a kill switch call at each line statement and before each jump
 *
 * @author arcuri
 */
public class KillSwitchMethodAdapter extends MethodVisitor {

    public KillSwitchMethodAdapter(MethodVisitor mv, String methodName, String desc) {
        super(Opcodes.ASM9, mv);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        int maxNum = 3;
        super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        addInstrumentation();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        addInstrumentation(); //add instrumentation before of the jump
        super.visitJumpInsn(opcode, label);
    }

    private void addInstrumentation() {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                KillSwitchHandler.class.getName().replace('.', '/'),
                "killIfTimeout", "()V", false);
    }
}
