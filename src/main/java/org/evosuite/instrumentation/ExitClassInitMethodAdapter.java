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

import org.evosuite.reset.ResetManager;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Adds a call to ExecutionTracer.passedClassInitializationMethod() when
 * the <clinit> method begins its execution.
 *
 * @author Juan Galeotti
 */
public class ExitClassInitMethodAdapter extends MethodVisitor {

	private static final String EXIT_CLASS_INIT = "exitClassInit";
	private final String className;
	private final String methodName;
	private Label startingTryLabel;
	private Label endingTryLabel;

	/**
	 * <p>Constructor for PutStaticMethodAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 * @param finalFields a {@link java.util.List} object.
	 */
	public ExitClassInitMethodAdapter(String className, String methodName,
			MethodVisitor mv) {
		super(Opcodes.ASM4, mv);
		this.className = className;
		this.methodName = methodName;
	}

	@Override
	public void visitInsn(int opcode) {
		if (opcode == Opcodes.RETURN && (methodName.equals("<clinit>"))) {

			String executionTracerClassName = ResetManager.class.getName()
					.replace(".", "/");
			String executionTracerDescriptor = Type.getMethodDescriptor(
					Type.VOID_TYPE, Type.getType(String.class));

			String classNameWithDots = className.replace("/", ".");
			super.visitLdcInsn(classNameWithDots);
			super.visitMethodInsn(INVOKESTATIC, executionTracerClassName,
					EXIT_CLASS_INIT, executionTracerDescriptor);

		}
		super.visitInsn(opcode);
	}

	@Override
	public void visitCode() {
		super.visitCode();
		if (methodName.equals("<clinit>")) {
			startingTryLabel = new Label();
			endingTryLabel = new Label();
			super.visitTryCatchBlock(startingTryLabel, endingTryLabel,
					endingTryLabel, null);
			
			super.visitLabel(startingTryLabel);
		}
	}

	@Override
	public void visitEnd() {
		if (methodName.equals("<clinit>")) {
			super.visitLabel(endingTryLabel);
			String executionTracerClassName = ResetManager.class.getName()
					.replace(".", "/");
			String executionTracerDescriptor = Type.getMethodDescriptor(
					Type.VOID_TYPE, Type.getType(String.class));

			String classNameWithDots = className.replace("/", ".");
			super.visitLdcInsn(classNameWithDots);
			super.visitMethodInsn(INVOKESTATIC, executionTracerClassName,
					EXIT_CLASS_INIT, executionTracerDescriptor);
			super.visitInsn(Opcodes.ATHROW);

		}
		super.visitEnd();
	}

}
