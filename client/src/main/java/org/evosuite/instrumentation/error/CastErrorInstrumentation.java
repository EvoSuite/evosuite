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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class CastErrorInstrumentation extends ErrorBranchInstrumenter {

    public CastErrorInstrumentation(ErrorConditionMethodAdapter mv) {
        super(mv);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {

        if (opcode == Opcodes.CHECKCAST) {
            Label origTarget = new Label();
            // Label origTarget = new AnnotatedLabel();
            // origTarget.info = Boolean.FALSE;
            mv.visitInsn(Opcodes.DUP);
            mv.tagBranch();
            mv.visitJumpInsn(Opcodes.IFNULL, origTarget);
            mv.visitInsn(Opcodes.DUP);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, type);
            mv.tagBranch();
            mv.visitJumpInsn(Opcodes.IFNE, origTarget);
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/ClassCastException");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ClassCastException",
                    "<init>", "()V", false);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(origTarget);
            mv.tagBranchExit();
        }
    }
}
