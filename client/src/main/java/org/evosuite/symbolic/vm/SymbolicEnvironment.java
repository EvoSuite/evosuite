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
package org.evosuite.symbolic.vm;

import org.evosuite.dse.MainConfig;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.instrument.ConcolicInstrumentingClassLoader;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;
import org.evosuite.utils.TypeUtil;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author galeotti
 */
public final class SymbolicEnvironment {

    /**
     * Storage for symbolic information in the memory heap
     * This might be extended at some point
     */
    public final SymbolicHeap heap = new SymbolicHeap();

    /**
     * Stack of function/method/constructor invocation frames
     */
    private final Deque<Frame> stackFrame = new LinkedList<>();

    /**
     * Classes whose static fields have been set to the default zero value or a
     * dummy value.
     */
    private final Set<Class<?>> preparedClasses = new HashSet<>();

    private final ConcolicInstrumentingClassLoader instrumentingClassLoader;

    public SymbolicEnvironment(ConcolicInstrumentingClassLoader instrumentingClassLoader) {
        this.instrumentingClassLoader = instrumentingClassLoader;
    }

    public Frame topFrame() {
        return stackFrame.peek();
    }

    public Frame popFrame() {
        return stackFrame.pop();
    }

    public boolean isEmpty() {
        return stackFrame.isEmpty();
    }

    public void pushFrame(Frame frame) {
        stackFrame.push(frame);
    }

    public Frame callerFrame() {
        Frame top = stackFrame.pop();
        Frame res = stackFrame.peek();
        stackFrame.push(top);
        return res;
    }

    public Class<?> ensurePrepared(String className) {
        Type ownerType = Type.getObjectType(className);
        if (ownerType.getSort() == Type.ARRAY) {
            Type elemType = ownerType.getElementType();
            if (TypeUtil.isValue(elemType))
                return TypeUtil.getPrimitiveArrayClassFromElementType(elemType);
            else {
                // ensurePrepared component class
                className = elemType.getClassName();
                Class<?> claz = instrumentingClassLoader.getClassForName(className);
                ensurePrepared(claz);

                // returns claz[] instead of claz
                Class<?> arrayClaz = Array.newInstance(claz, 0).getClass();
                return arrayClaz;
            }
        } else {
            Class<?> claz = instrumentingClassLoader.getClassForName(className);
            ensurePrepared(claz);
            return claz;
        }
    }

    public void ensurePrepared(Class<?> claz) {
        if (preparedClasses.contains(claz))
            return; // done, we have prepared this class earlier

        Class<?> superClass = claz.getSuperclass();
        if (superClass != null)
            ensurePrepared(superClass); // prepare super class first

        String className = claz.getCanonicalName();
        if (className == null) {
            // no canonical name
        }
        /*
         * Field[] fields = claz.getDeclaredFields();
         *
         * final boolean isIgnored = MainConfig.get().isIgnored(className);
         *
         * for (Field field : fields) {
         *
         * final int fieldModifiers = field.getModifiers(); if (isIgnored &&
         * Modifier.isPrivate(fieldModifiers)) continue; // skip private field
         * of ignored class.
         *
         * }
         */
        preparedClasses.add(claz);

    }

    /**
     * Prepare stack of function invocation frames.
     * <p>
     * Clear function invocation stack, push a frame that pretends to call the
     * method under test. We push variables for our method onto the
     * pseudo-callers stack, so our method can pop them from there.
     */
    public void prepareStack(Method mainMethod) {
        stackFrame.clear();
        // bottom of the stack trace
        this.pushFrame(new FakeBottomFrame());

        // frame for argument purposes
        final FakeMainCallerFrame fakeMainCallerFrame = new FakeMainCallerFrame(
                mainMethod, MainConfig.get().MAX_LOCALS_DEFAULT); // fake caller
        // of method
        // under
        // test

        if (mainMethod != null) {
            boolean isInstrumented = isInstrumented(mainMethod);
            fakeMainCallerFrame.invokeInstrumentedCode(isInstrumented);
            String[] emptyStringArray = new String[]{};
            ReferenceExpression emptyStringRef = heap.getReference(emptyStringArray);
            fakeMainCallerFrame.operandStack.pushRef(emptyStringRef);
        }
        this.pushFrame(fakeMainCallerFrame);
    }

    /**
     * @return method is instrumented. It is neither native nor declared by an
     * ignored JDK class, etc.
     */
    private static boolean isInstrumented(Method method) {
        if (Modifier.isNative(method.getModifiers()))
            return false;

        String declClass = method.getDeclaringClass().getCanonicalName();
        return !MainConfig.get().isIgnored(declClass);
    }
}
