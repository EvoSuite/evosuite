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

import org.evosuite.PackageInfo;
import org.evosuite.runtime.instrumentation.AnnotatedLabel;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * ExplicitExceptionHandler class.
 * </p>
 *
 * @author gordon
 */
public class ExplicitExceptionHandler extends MethodVisitor {

    private final String fullMethodName;

    private final String className;

    private boolean inErrorBranch = false;

    /**
     * <p>
     * Constructor for ExplicitExceptionHandler.
     * </p>
     *
     * @param mv         a {@link org.objectweb.asm.MethodVisitor} object.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     */
    public ExplicitExceptionHandler(MethodVisitor mv, String className,
                                    String methodName, String desc) {
        super(Opcodes.ASM9, mv);
        fullMethodName = methodName + desc;
        this.className = className;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitLabel(org.objectweb.asm.Label)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitLabel(Label label) {
        if (label instanceof AnnotatedLabel) {
            AnnotatedLabel l = (AnnotatedLabel) label;
            inErrorBranch = Boolean.TRUE.equals(l.info);
        }
        super.visitLabel(label);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ATHROW && !inErrorBranch) {
            super.visitInsn(Opcodes.DUP);
            this.visitLdcInsn(className);
            this.visitLdcInsn(fullMethodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(ExecutionTracer.class),
                    "exceptionThrown",
                    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V", false);
        }
        super.visitInsn(opcode);
    }
}
