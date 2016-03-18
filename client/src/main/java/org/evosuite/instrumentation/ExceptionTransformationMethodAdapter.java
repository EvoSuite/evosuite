package org.evosuite.instrumentation;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.evosuite.instrumentation.error.ErrorBranchInstrumenter;
import org.evosuite.runtime.classhandling.ResetManager;
import org.evosuite.runtime.instrumentation.AnnotatedMethodNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

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
        super(Opcodes.ASM5, mv, access, methodName, desc);
        //super(Opcodes.ASM5,
        //        new AnnotatedMethodNode(access, methodName, desc, null, null), access,
        //        methodName, desc);
        this.className = className;
        this.methodName = methodName;
        next = mv;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

        if(!ExceptionTransformationClassAdapter.methodExceptionMap.containsKey(owner) ||
           !ExceptionTransformationClassAdapter.methodExceptionMap.get(owner).containsKey(name+desc)) {
            logger.warn("Method signature not seen yet: "+owner+"."+name+desc);
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        Set<Type> declaredExceptions = ExceptionTransformationClassAdapter.methodExceptionMap.get(owner).get(name+desc);

        // No instrumentation if the method doesn't throw anything
        if(declaredExceptions.isEmpty()) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        // Create a variable that stores the thrown exception, or null if no exception was thrown
        int exceptionInstanceVar = newLocal(Type.getType(Throwable.class));
        push((String)null);
        storeLocal(exceptionInstanceVar);

        // Insert start of try block label
        Label start = newLabel();
        Label end   = newLabel();
        Label catchLabel  = newLabel();
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

        mark(catchLabel);
        // Insert jump after catch block instruction
        // catchException(start, end, null);
        // super.visitTryCatchBlock(start, end, end, null);


        // assign exception to exceptionInstanceVar
        storeLocal(exceptionInstanceVar);
        Type returnType = Type.getReturnType(desc);
        switch (returnType.getSort()) {
            case Type.VOID:
                break;
            case Type.BOOLEAN:
                push(false);
                break;
            case Type.CHAR:
            case Type.BYTE:
            case Type.INT:
            case Type.SHORT:
                push(0);
                break;
            case Type.FLOAT:
                push(0f);
                break;
            case Type.LONG:
                push(0l);
                break;
            case Type.DOUBLE:
                push(0.0);
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                push((String)null);
                break;
        }

        // Insert end of catch block label
        mark(afterCatch);

        // If there was no exception, skip ahead to rest of the code
        loadLocal(exceptionInstanceVar);
        Label noExceptionLabel = newLabel();
        ifNull(noExceptionLabel);

        // If there was an exception, rethrow it, with one if per declared exception type
        for(Type exceptionType : declaredExceptions) {
            loadLocal(exceptionInstanceVar);
            instanceOf(exceptionType);
            Label noJump = newLabel();
            visitJumpInsn(Opcodes.IFEQ, noJump);
            loadLocal(exceptionInstanceVar);
            checkCast(exceptionType);
            throwException();
            visitLabel(noJump);
        }

        // It _must_ be a RuntimeException if we get to this point.
        Type runtimeExceptionType = Type.getType(RuntimeException.class);
        loadLocal(exceptionInstanceVar);
        checkCast(runtimeExceptionType);
        throwException();

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

    private final List<TryCatchBlock> tryCatchBlocks = new LinkedList<TryCatchBlock>();
    private final List<TryCatchBlock> instrumentedTryCatchBlocks = new LinkedList<TryCatchBlock>();

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
    /** {@inheritDoc} */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
    }

}
