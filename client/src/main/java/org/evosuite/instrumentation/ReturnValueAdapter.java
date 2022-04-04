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
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instrument classes to trace return values
 *
 * @author Gordon Fraser
 */
public class ReturnValueAdapter extends MethodVisitor {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(LineNumberMethodAdapter.class);

    private final String fullMethodName;

    protected String className;

    protected String methodName;

    /**
     * <p>Constructor for ReturnValueAdapter.</p>
     *
     * @param mv         a {@link org.objectweb.asm.MethodVisitor} object.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     */
    public ReturnValueAdapter(MethodVisitor mv, String className, String methodName,
                              String desc) {
        super(Opcodes.ASM9, mv);
        fullMethodName = methodName + desc;
        this.methodName = methodName;
        this.className = className;
    }

    // primitive data types
    private enum PDType {
        LONG, INTEGER, FLOAT, DOUBLE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitInsn(int opcode) {
        if (!methodName.equals("<clinit>")) {
            switch (opcode) {
                case Opcodes.IRETURN:
                    callLogIReturn();
                    break;
                case Opcodes.ARETURN:
                    callLogAReturn();
                    break;
                case Opcodes.ATHROW:
                    break;
                case Opcodes.DRETURN:
                    callLogDReturn();
                    break;
                case Opcodes.FRETURN:
                    callLogFReturn();
                    break;
                case Opcodes.LRETURN:
                    callLogLReturn();
                    break;
                case Opcodes.RETURN:
                    break;
                default:
                    break;
            }
        }
        super.visitInsn(opcode);

    }

    private void callLogPrototype(String traceMethod, PDType type) {
        if (type != PDType.LONG && type != PDType.DOUBLE) {
            this.visitInsn(Opcodes.DUP);
            if (type == PDType.FLOAT) {
                this.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float",
                        "floatToRawIntBits", "(F)I", false);
            }
        } else {
            this.visitInsn(Opcodes.DUP2);
            if (type == PDType.DOUBLE) {
                this.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double",
                        "doubleToRawLongBits", "(D)J", false);
            }
            this.visitInsn(Opcodes.DUP2);
            this.visitIntInsn(Opcodes.BIPUSH, 32);
            this.visitInsn(Opcodes.LSHR);
            this.visitInsn(Opcodes.LXOR);
            this.visitInsn(Opcodes.L2I);
        }

        this.visitLdcInsn(className);
        this.visitLdcInsn(fullMethodName);
        this.visitMethodInsn(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(ExecutionTracer.class),
                "returnValue", "(ILjava/lang/String;Ljava/lang/String;)V", false);
    }

    private void callLogIReturn() {
        callLogPrototype("logIReturn", PDType.INTEGER);
    }

    private void callLogAReturn() {
        this.visitInsn(Opcodes.DUP);
        this.visitLdcInsn(className);
        this.visitLdcInsn(fullMethodName);
        this.visitMethodInsn(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(ExecutionTracer.class),
                "returnValue",
                "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V", false);
    }

    private void callLogLReturn() {
        callLogPrototype("logLReturn", PDType.LONG);
    }

    private void callLogDReturn() {
        callLogPrototype("logDReturn", PDType.DOUBLE);
    }

    private void callLogFReturn() {
        callLogPrototype("logFReturn", PDType.FLOAT);
    }

}
