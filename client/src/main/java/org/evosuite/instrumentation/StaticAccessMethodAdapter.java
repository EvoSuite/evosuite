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

import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.runtime.instrumentation.RuntimeInstrumentation;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * For each PUTSTATIC or GETSTATIC we include a call to
 * <code>ExecutionTracer.passedPutStatic(String,String)</code> passing the class
 * name and the field name of the PUTSTATIC statement.
 *
 * @author Juan Galeotti
 */
public class StaticAccessMethodAdapter extends MethodVisitor {

    private static final String PASSED_PUT_STATIC = "passedPutStatic";
    private static final String PASSED_GET_STATIC = "passedGetStatic";

    private final String className;
    private final String methodName;

    /**
     * <p>
     * Constructor for StaticAccessMethodAdapter.
     * </p>
     *
     * @param mv        a {@link org.objectweb.asm.MethodVisitor} object.
     * @param className a {@link java.lang.String} object.
     */
    public StaticAccessMethodAdapter(String className, String methodName, MethodVisitor mv) {
        super(Opcodes.ASM9, mv);
        this.className = className;
        this.methodName = methodName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int,
     * java.lang.String, java.lang.String, java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        if ((opcode == Opcodes.PUTSTATIC || opcode == Opcodes.GETSTATIC)
                && !(className.equals(owner) && methodName.equals("<clinit>"))
                && !(className.equals(owner) && methodName.equals(ClassResetter.STATIC_RESET))) {

            String classNameWithDots = owner.replace('/', '.');
            if (RuntimeInstrumentation.checkIfCanInstrument(classNameWithDots)) {

                String executionTracerClassName = ExecutionTracer.class.getName().replace('.', '/');
                String executionTracerDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class),
                        Type.getType(String.class));

                super.visitLdcInsn(classNameWithDots);
                super.visitLdcInsn(name);
                if (opcode == Opcodes.PUTSTATIC)
                    super.visitMethodInsn(INVOKESTATIC, executionTracerClassName, PASSED_PUT_STATIC,
                            executionTracerDescriptor, false);
                else
                    super.visitMethodInsn(INVOKESTATIC, executionTracerClassName, PASSED_GET_STATIC,
                            executionTracerDescriptor, false);
            }
        }
        super.visitFieldInsn(opcode, owner, name, desc);
    }
}
