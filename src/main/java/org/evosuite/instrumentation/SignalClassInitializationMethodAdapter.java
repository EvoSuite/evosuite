/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.instrumentation;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.evosuite.testcase.ExecutionTracer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Adds a call to ExecutionTracer.passedClassInitializationMethod() when
 * the <clinit> method begins its execution.
 *
 * @author Juan Galeotti
 */
public class SignalClassInitializationMethodAdapter extends MethodVisitor {

	private static final String PASSED_CLASS_INITIALIZATION_METHOD = "passedClassInitializationMethod";
	private final String className;
	private final String methodName;

	/**
	 * <p>Constructor for PutStaticMethodAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 * @param finalFields a {@link java.util.List} object.
	 */
	public SignalClassInitializationMethodAdapter(String className,
			String methodName, MethodVisitor mv) {
		super(Opcodes.ASM4, mv);
		this.className = className;
		this.methodName = methodName;
	}

	@Override
	public void visitCode() {
		super.visitCode();

		if (methodName.equals("<clinit>")) {
			String executionTracerClassName = ExecutionTracer.class.getName()
					.replace(".", "/");
			String executionTracerDescriptor = Type.getMethodDescriptor(
					Type.VOID_TYPE, Type.getType(String.class));

			String classNameWithDots = className.replace("/", ".");
			mv.visitLdcInsn(classNameWithDots);
			mv.visitMethodInsn(INVOKESTATIC, executionTracerClassName,
					PASSED_CLASS_INITIALIZATION_METHOD, executionTracerDescriptor);

		}

	}

}
