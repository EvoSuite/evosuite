/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.runtime.LoopCounter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Add loop check before each jump instruction.
 *
 * <p>
 * Note: not all jumps represent a loop (eg, for/while).
 * Non-loops are for examples if/switch.
 * However, such extra instrumentation should not really be a problem
 *
 * Created by Andrea Arcuri on 29/03/15.
 */
public class LoopCounterMethodAdapter extends MethodVisitor {

    private static final String LOOP_COUNTER = Type.getInternalName(LoopCounter.class);

    public LoopCounterMethodAdapter(MethodVisitor mv, String methodName, String desc) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack+2, maxLocals);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        addInstrumentation(); //add instrumentation before of the jump
        super.visitJumpInsn(opcode, label);
    }

    private void addInstrumentation(){

        int index = LoopCounter.getInstance().getNewIndex();

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,LOOP_COUNTER,
                "getInstance", "()L"+LOOP_COUNTER+";" , false);

        mv.visitLdcInsn(index);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LOOP_COUNTER,
                "checkLoop", "(I)V", false);
    }
}
