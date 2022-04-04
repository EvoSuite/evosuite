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

import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * This visitor inserts a callback to at the exit of each <clinit>() method
 * (i.e. after class initialization has ended). If the class has no static
 * fields, no callback is inserted.
 *
 * @author galeotti
 */
public class EndOfClassInitializerVisitor extends ClassVisitor {

    /**
     * The method visitor for the containing class visitor. This method visitor
     * implements the functionality of the containing class at the method level.
     *
     * @author galeotti
     */
    private static class EndOfClassInitializerMethodVisitor extends MethodVisitor {

        private final String className;
        private final String methodName;
        private Label startingTryLabel;
        private Label endingTryLabel;

        /**
         * <p>
         * Constructor for PutStaticMethodAdapter.
         * </p>
         *
         * @param mv          a {@link org.objectweb.asm.MethodVisitor} object.
         * @param className   a {@link java.lang.String} object.
         * @param finalFields a {@link java.util.List} object.
         */
        public EndOfClassInitializerMethodVisitor(String className, String methodName, MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN && (methodName.equals("<clinit>"))) {

                String executionTracerClassName = ExecutionTracer.class.getName().replace('.', '/');
                String executionTracerDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class));

                String classNameWithDots = className.replace('/', '.');
                super.visitLdcInsn(classNameWithDots);
                super.visitMethodInsn(INVOKESTATIC, executionTracerClassName, EXIT_CLASS_INIT,
                        executionTracerDescriptor, false);

            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            if (methodName.equals("<clinit>")) {

                startingTryLabel = new Label();
                endingTryLabel = new Label();
                super.visitLabel(startingTryLabel);
            }
        }

        @Override
        public void visitEnd() {
            if (methodName.equals("<clinit>")) {
                super.visitLabel(endingTryLabel);
                String executionTracerClassName = ExecutionTracer.class.getName().replace('.', '/');
                String executionTracerDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class));

                String classNameWithDots = className.replace('/', '.');
                super.visitLdcInsn(classNameWithDots);
                super.visitMethodInsn(INVOKESTATIC, executionTracerClassName, EXIT_CLASS_INIT,
                        executionTracerDescriptor, false);
                super.visitInsn(Opcodes.ATHROW);

                // regenerate try-catch table
                for (TryCatchBlock tryCatchBlock : tryCatchBlocks) {
                    super.visitTryCatchBlock(tryCatchBlock.start, tryCatchBlock.end, tryCatchBlock.handler,
                            tryCatchBlock.type);
                }
                // add new try-catch for exiting method
                super.visitTryCatchBlock(startingTryLabel, endingTryLabel, endingTryLabel, null);
            }
            super.visitEnd();
        }

        private static class TryCatchBlock {
            public TryCatchBlock(Label start, Label end, Label handler, String type) {
                this.start = start;
                this.end = end;
                this.handler = handler;
                this.type = type;
            }

            Label start;
            Label end;
            Label handler;
            String type;
        }

        private final List<TryCatchBlock> tryCatchBlocks = new LinkedList<>();

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            if (methodName.equals("<clinit>")) {
                TryCatchBlock block = new TryCatchBlock(start, end, handler, type);
                tryCatchBlocks.add(block);
            }
            super.visitTryCatchBlock(start, end, handler, type);
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(EndOfClassInitializerVisitor.class);

    private final String className;

    public EndOfClassInitializerVisitor(ClassVisitor visitor, String className) {
        super(Opcodes.ASM9, visitor);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int methodAccess, String methodName, String descriptor, String signature,
                                     String[] exceptions) {

        MethodVisitor mv = super.visitMethod(methodAccess, methodName, descriptor, signature, exceptions);

        if (methodName.equals("<clinit>")) {

            clinitFound = true;
            EndOfClassInitializerMethodVisitor staticResetMethodAdapter = new EndOfClassInitializerMethodVisitor(
                    className, methodName, mv);

            return staticResetMethodAdapter;
        } else {
            return mv;
        }
    }

    private boolean isInterface = false;
    private boolean clinitFound = false;
    private boolean hasStaticFields = false;

    private static final String EXIT_CLASS_INIT = "exitClassInit";

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
    }

    @Override
    public void visitEnd() {
        if (!clinitFound && !isInterface && hasStaticFields) {
            // create brand empty <clinit>()
            createEmptyClassInit();
        }
        super.visitEnd();
    }

    private static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

        if (isStatic(access)) {
            hasStaticFields = true;
        }

        return super.visitField(access, name, desc, signature, value);
    }

    private void createEmptyClassInit() {
        logger.info("Creating <clinit> in class " + className);
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();

        String executionTracerClassName = ExecutionTracer.class.getName().replace('.', '/');
        String executionTracerDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class));

        String classNameWithDots = className.replace('/', '.');
        mv.visitLdcInsn(classNameWithDots);
        mv.visitMethodInsn(INVOKESTATIC, executionTracerClassName, EXIT_CLASS_INIT, executionTracerDescriptor, false);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }
}
