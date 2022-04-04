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

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.annotation.EvoSuiteExclude;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.mock.StaticReplacementMock;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * <p>MethodCallReplacementClassAdapter class.</p>
 *
 * @author fraser
 */
public class MethodCallReplacementClassAdapter extends ClassVisitor {

    private final String className;

    private String superClassName;

    private boolean definesHashCode = false;

    private boolean isInterface = false;

    private boolean definesUid = false;

    private boolean canChangeSignature = true;

    /**
     * <p>Constructor for MethodCallReplacementClassAdapter.</p>
     *
     * @param cv        a {@link org.objectweb.asm.ClassVisitor} object.
     * @param className a {@link java.lang.String} object.
     */
    public MethodCallReplacementClassAdapter(ClassVisitor cv, String className) {
        this(cv, className, true);
    }

    public MethodCallReplacementClassAdapter(ClassVisitor cv, String className, boolean canAddMethods) {
        super(Opcodes.ASM9, cv);
        this.className = className;
        this.superClassName = null;
        this.canChangeSignature = canAddMethods;
    }


    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        if (name.equals("hashCode"))
            definesHashCode = true;

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>")) {
            mv = new RegisterObjectForDeterministicHashCodeVisitor(mv, access, name, desc);
        }

        return new MethodCallReplacementMethodAdapter(mv, className, superClassName, name, access, desc);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        if (name.equals("serialVersionUID")) {
            definesUid = true;
            // FIXXME: This shouldn't be necessary, but the ASM SerialUIDVisitor seems to set a
            //         wrong access modifier for the serialVersionUID field on interfaces
            //         so we're overriding the access modifier here.
            if (isInterface) {
                return super.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, name, desc, signature, value);
            } else {
                return super.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, name, desc, signature, value);
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        String superNameWithDots = superName.replace('/', '.');
        superClassName = superNameWithDots;
        if ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE)
            isInterface = true;
        else {
            /*
                FIXME: this should be moved in its own adapter, because it is not executed if we do
                only reset of static state and no mocking
             */
            boolean found = false;
            String instrumentedInterface = InstrumentedClass.class.getCanonicalName().replace('.', '/');
            for (String interf : interfaces) {
                if (interf.equals(instrumentedInterface))
                    found = true;
            }
            if (!found) {
                logger.info("Adding mock interface to class " + name);
                String[] mockedInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
                mockedInterfaces[interfaces.length] = InstrumentedClass.class.getCanonicalName().replace('.', '/');
                interfaces = mockedInterfaces;
            }
        }

        if (MockList.shouldBeMocked(superNameWithDots)) {

            /*
             * TODO: likely need to suppress the change of superclass if !canChangeSignature
             */

            Class<?> mockSuperClass = MockList.getMockClass(superNameWithDots);
            if (StaticReplacementMock.class.isAssignableFrom(mockSuperClass)) {
                super.visit(version, access, name, signature, superName, interfaces);

            } else {
                String mockSuperClassName = mockSuperClass.getCanonicalName().replace('.', '/');
                super.visit(version, access, name, signature, mockSuperClassName, interfaces);
            }
        } else {
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MethodCallReplacementClassAdapter.class);

    @Override
    public void visitEnd() {
        if (canChangeSignature && !definesHashCode && !isInterface && RuntimeSettings.mockJVMNonDeterminism) {

//			logger.info("No hashCode defined for: "+className+", superclass = "+superClassName);

            if (superClassName.equals("java.lang.Object")) { //TODO: why only if superclass is Object??? unclear
                Method hashCodeMethod = Method.getMethod("int hashCode()");
                GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, hashCodeMethod, null, null, this);
                mg.loadThis();
                mg.visitAnnotation(Type.getDescriptor(EvoSuiteExclude.class), true);
                mg.invokeStatic(Type.getType(org.evosuite.runtime.System.class), Method.getMethod("int identityHashCode(Object)"));
                mg.returnValue();
                mg.endMethod();
            }

        }

        super.visitEnd();
    }
}
