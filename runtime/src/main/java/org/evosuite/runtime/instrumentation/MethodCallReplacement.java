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

import org.evosuite.runtime.mock.InvokeSpecialMock;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.MockList;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gordon
 */
public class MethodCallReplacement {
    private final String className;

    private final String methodName;
    private final String desc;
    private final int origOpcode;

    private final String replacementClassName;
    private final String replacementMethodName;
    private final String replacementDesc;

    private final boolean popCallee;
    private final boolean popUninitialisedReference;

    private static final Logger logger = LoggerFactory.getLogger(MethodCallReplacement.class);

    /**
     * @param className
     * @param methodName
     * @param desc
     * @param replacementClassName
     * @param replacementMethodName
     * @param replacementDesc
     * @param pop                   if {@code true}, then get rid of the receiver object from
     *                              the stack. This is needed when a non-static method is
     *                              replaced by a static one, unless you make the callee of
     *                              the original method a parameter of the static replacement
     *                              method
     * @param pop2                  is needed if you replace the super-constructor call
     */
    public MethodCallReplacement(String className, String methodName, String desc, int opcode,
                                 String replacementClassName, String replacementMethodName,
                                 String replacementDesc, boolean pop, boolean pop2) {
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
        this.replacementClassName = replacementClassName;
        this.replacementMethodName = replacementMethodName;
        this.replacementDesc = replacementDesc;
        this.popCallee = pop;
        this.popUninitialisedReference = pop2;
        this.origOpcode = opcode;
    }

    public boolean isTarget(String owner, String name, String desc) {
        return className.equals(owner) && methodName.equals(name)
                && this.desc.equals(desc);
    }

    public void insertMethodCall(MethodCallReplacementMethodAdapter mv, int opcode) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, MockFramework.class.getCanonicalName().replace('.', '/'), "isEnabled", "()Z", false);
        Label origCallLabel = new Label();
        Label afterOrigCallLabel = new Label();

        Label annotationStartTag = new AnnotatedLabel(true, true);
        annotationStartTag.info = Boolean.TRUE;
        mv.visitLabel(annotationStartTag);
        mv.visitJumpInsn(Opcodes.IFEQ, origCallLabel);
        Label annotationEndTag = new AnnotatedLabel(true, false);
        annotationEndTag.info = Boolean.FALSE;
        mv.visitLabel(annotationEndTag);

        if (popCallee) {
            Type[] args = Type.getArgumentTypes(desc);
            Map<Integer, Integer> to = new HashMap<>();
            for (int i = args.length - 1; i >= 0; i--) {
                int loc = mv.newLocal(args[i]);
                mv.storeLocal(loc);
                to.put(i, loc);
            }

            mv.pop();//callee
            if (popUninitialisedReference)
                mv.pop();

            for (int i = 0; i < args.length; i++) {
                mv.loadLocal(to.get(i));
            }
        }
        if (opcode == Opcodes.INVOKESPECIAL && MockList.shouldBeMocked(className.replace('/', '.'))) {
            insertInvokeSpecialForMockedSuperclass(mv);
        } else {
            if (opcode == Opcodes.INVOKESPECIAL) {
                logger.info("Not mocking invokespecial: " + replacementMethodName + " for class " + className);
            }
            mv.visitMethodInsn(opcode, replacementClassName, replacementMethodName,
                    replacementDesc, false);
        }
        mv.visitJumpInsn(Opcodes.GOTO, afterOrigCallLabel);
        mv.visitLabel(origCallLabel);
        mv.getNextVisitor().visitMethodInsn(origOpcode, className, methodName, desc, false); // TODO: What is itf here?
        mv.visitLabel(afterOrigCallLabel);
    }

    public void insertInvokeSpecialForMockedSuperclass(MethodCallReplacementMethodAdapter mv) {
        int numArguments = Type.getArgumentTypes(replacementDesc).length;
        mv.push(numArguments);
        mv.newArray(Type.getType(Object.class));
        for (int i = 0; i < numArguments; i++) {
            // param, array
            mv.dupX1();                    // array, param, array
            mv.swap();                     // array, array, param
            mv.push(numArguments - i - 1); // array, array, param, index
            mv.swap();                     // array, array, index, param
            mv.arrayStore(Type.getType(Object.class));
            // array
        }
        mv.push(methodName);
        mv.push(desc);
        Method invokeSpecialMethod = InvokeSpecialMock.class.getDeclaredMethods()[0];

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, InvokeSpecialMock.class.getCanonicalName().replace('.', '/'),
                "invokeSpecial", Type.getMethodDescriptor(invokeSpecialMethod), false);

        if (Type.getReturnType(desc).equals(Type.VOID_TYPE)) {
            mv.pop();
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getReturnType(desc).getInternalName());
        }

    }

    public void insertConstructorCall(MethodCallReplacementMethodAdapter mv,
                                      MethodCallReplacement replacement, boolean isSelf) {
        Label origCallLabel = new Label();
        Label afterOrigCallLabel = new Label();

        if (!isSelf) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, MockFramework.class.getCanonicalName().replace('.', '/'), "isEnabled", "()Z", false);
            Label annotationStartTag = new AnnotatedLabel(true, true);
            annotationStartTag.info = Boolean.TRUE;
            mv.visitLabel(annotationStartTag);
            mv.visitJumpInsn(Opcodes.IFEQ, origCallLabel);
            Label annotationEndTag = new AnnotatedLabel(true, false);
            annotationEndTag.info = Boolean.FALSE;
            mv.visitLabel(annotationEndTag);

            Type[] args = Type.getArgumentTypes(desc);
            Map<Integer, Integer> to = new HashMap<>();
            for (int i = args.length - 1; i >= 0; i--) {
                int loc = mv.newLocal(args[i]);
                mv.storeLocal(loc);
                to.put(i, loc);
            }

            mv.pop2();//uninitialized reference (which is duplicated)
            mv.newInstance(Type.getObjectType(replacement.replacementClassName));
            mv.dup();

            for (int i = 0; i < args.length; i++) {
                mv.loadLocal(to.get(i));
            }
        }
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, replacementClassName,
                replacementMethodName, replacementDesc, false);
        if (!isSelf) {
            mv.visitJumpInsn(Opcodes.GOTO, afterOrigCallLabel);
            mv.visitLabel(origCallLabel);
            mv.getNextVisitor().visitMethodInsn(Opcodes.INVOKESPECIAL, className, methodName, desc, false);
            mv.visitLabel(afterOrigCallLabel);
        }
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodNameWithDesc() {
        return methodName + desc;
    }

}
