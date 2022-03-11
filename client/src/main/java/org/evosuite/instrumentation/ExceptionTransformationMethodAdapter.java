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

import org.evosuite.runtime.instrumentation.AnnotatedLabel;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by gordon on 17/03/2016.
 */
public class ExceptionTransformationMethodAdapter extends GeneratorAdapter {

    protected final static Logger logger = LoggerFactory.getLogger(ExceptionTransformationMethodAdapter.class);

    private final String className;

    private final String methodName;

    private final MethodVisitor next;

    public ExceptionTransformationMethodAdapter(MethodVisitor mv, String className,
                                                String methodName, int access, String desc) {
        super(Opcodes.ASM9, mv, access, methodName, desc);
        //super(Opcodes.ASM9,
        //        new AnnotatedMethodNode(access, methodName, desc, null, null), access,
        //        methodName, desc);
        this.className = className;
        this.methodName = methodName;
        next = mv;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

        if (!ExceptionTransformationClassAdapter.methodExceptionMap.containsKey(owner) ||
                !ExceptionTransformationClassAdapter.methodExceptionMap.get(owner).containsKey(name + desc)) {
            logger.debug("Method {} does not throw exceptions", name);
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        Set<Type> declaredExceptions = ExceptionTransformationClassAdapter.methodExceptionMap.get(owner).get(name + desc);

        // No instrumentation if the method doesn't throw anything
        if (declaredExceptions.isEmpty()) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        // Create a variable that stores the thrown exception, or null if no exception was thrown
        int exceptionInstanceVar = newLocal(Type.getType(Throwable.class));
        push((String) null);
        storeLocal(exceptionInstanceVar);

        // Insert start of try block label
        Label start = newLabel();
        Label end = newLabel();
        Label catchLabel = newLabel();
        super.visitTryCatchBlock(start, end, catchLabel, null);
        TryCatchBlock block = new TryCatchBlock(start, end, catchLabel, null);
        instrumentedTryCatchBlocks.add(block);

        mark(start);

        super.visitMethodInsn(opcode, owner, name, desc, itf);

        // Insert end of try block label
        mark(end);

        // Skip catch block if no exception was thrown
        Label afterCatch = newLabel();
        goTo(afterCatch);
        //push((String)null);

        mark(catchLabel);

        // assign exception to exceptionInstanceVar
        storeLocal(exceptionInstanceVar);

        // TODO: Here the JVM is expecting the proper stack but it is cleared except for the throwable

        // Insert end of catch block label
        // mark(afterCatch);


        // If there was no exception, skip ahead to rest of the code
        loadLocal(exceptionInstanceVar);
        Label noExceptionLabel = newLabel();

        // If there was an exception, rethrow it, with one if per declared exception type
        for (Type exceptionType : declaredExceptions) {
            loadLocal(exceptionInstanceVar);
            instanceOf(exceptionType);
            Label noJump = newLabel();
            Label jump = newLabel();
            tagBranch();


            visitJumpInsn(Opcodes.IFNE, jump);
            visitJumpInsn(Opcodes.GOTO, noJump);
            tagBranchExit();
            mark(jump);
            loadLocal(exceptionInstanceVar);
            checkCast(exceptionType);
            throwException();
            visitLabel(noJump);
        }

        // It _must_ be a RuntimeException if we get to this point.
        tagBranch();
        Type runtimeExceptionType = Type.getType(RuntimeException.class);
        loadLocal(exceptionInstanceVar);
        checkCast(runtimeExceptionType);
        throwException();
        tagBranchExit();

        mark(afterCatch);
        loadLocal(exceptionInstanceVar);

        Label startTag = new AnnotatedLabel(false, true);
        ((AnnotatedLabel) startTag).setIgnoreFalse(true);
        super.visitLabel(startTag);

        // Dummy branch signaling that no exception was thrown
        ifNull(noExceptionLabel);

        Label endTag = new AnnotatedLabel(false, false);
        ((AnnotatedLabel) startTag).setIgnoreFalse(true);
        super.visitLabel(endTag);

        mark(noExceptionLabel);

    }


    @Override
    public void visitEnd() {
        // regenerate try-catch table
        for (TryCatchBlock tryCatchBlock : instrumentedTryCatchBlocks) {
            super.visitTryCatchBlock(tryCatchBlock.start,
                    tryCatchBlock.end, tryCatchBlock.handler,
                    tryCatchBlock.type);
        }
        for (TryCatchBlock tryCatchBlock : tryCatchBlocks) {
            super.visitTryCatchBlock(tryCatchBlock.start,
                    tryCatchBlock.end, tryCatchBlock.handler,
                    tryCatchBlock.type);
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
    private final List<TryCatchBlock> instrumentedTryCatchBlocks = new LinkedList<>();

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
                                   String type) {
        TryCatchBlock block = new TryCatchBlock(start, end, handler, type);
        tryCatchBlocks.add(block);
        // super.visitTryCatchBlock(start, end, handler, type);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitEnd()
     */
    /** {@inheritDoc} */
//    @Override
//    public void visitEnd() {
//        MethodNode mn = (MethodNode) mv;
//        mn.accept(next);
//    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
    }

    public void tagBranch() {
        Label dummyTag = new AnnotatedLabel(false, true);
        // dummyTag.info = Boolean.TRUE;
        super.visitLabel(dummyTag);
    }

    public void tagBranchExit() {
        Label dummyTag = new AnnotatedLabel(false, false);
        // dummyTag.info = Boolean.FALSE;
        super.visitLabel(dummyTag);
    }

}
