package org.evosuite.instrumentation;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.evosuite.instrumentation.error.ErrorBranchInstrumenter;
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
        Label start = mark();
        super.visitMethodInsn(opcode, owner, name, desc, itf);

        // Insert end of try block label
        Label end = mark();

        // Skip catch block if no exception was thrown
        Label afterCatch = newLabel();
        goTo(afterCatch);

        // Insert jump after catch block instruction
        catchException(start, end, null);

        // assign exception to exceptionInstanceVar
        storeLocal(exceptionInstanceVar);

        // Insert end of catch block label
        mark(afterCatch);

        for(Type exceptionType : declaredExceptions) {
            loadLocal(exceptionInstanceVar);
            instanceOf(exceptionType);
            Label noJump = newLabel();
            visitJumpInsn(Opcodes.IFEQ, noJump);
            loadLocal(exceptionInstanceVar);
            throwException();
            visitLabel(noJump);
        }

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
