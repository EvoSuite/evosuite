/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.instrumentation.epa;

import org.evosuite.PackageInfo;
import org.evosuite.coverage.epa.EPAMonitor;
import org.evosuite.epa.EpaAction;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.bytecode.Opcode;

/**
 * Instrument classes to keep track of method entry and exit
 *
 * @author Gordon Fraser
 */
public class EPAMonitorMethodEntryExitAdapter extends AdviceAdapter {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(EPAMonitorMethodEntryExitAdapter.class);

	String className;
	String methodName;
	String fullMethodName;
	int access;

	/**
	 * <p>
	 * Constructor for MethodEntryAdapter.
	 * </p>
	 *
	 * @param mv
	 *            a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param access
	 *            a int.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param desc
	 *            a {@link java.lang.String} object.
	 */
	public EPAMonitorMethodEntryExitAdapter(MethodVisitor mv, int access, String className, String methodName,
			String desc) {
		super(Opcodes.ASM5, mv, access, methodName, desc);
		this.className = className;
		this.methodName = methodName;
		this.fullMethodName = methodName + desc;
		this.access = access;
	}

	/**
	 * Annotates entry point if the method is annotated as an @EpaAction
	 */
	@Override
	public void onMethodEnter() {

		if (isMethodAnnotedAsAnEpaActionMethod) {

			if (methodName.equals("<clinit>"))
				return; // FIXXME: Should we call super.onMethodEnter() here?

			mv.visitLdcInsn(className);
			mv.visitLdcInsn(fullMethodName);
			if ((access & Opcodes.ACC_STATIC) > 0) {
				mv.visitInsn(Opcodes.ACONST_NULL);
			} else {
				mv.visitVarInsn(Opcodes.ALOAD, 0);
			}
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PackageInfo.getNameWithSlash(EPAMonitor.class), "enteredMethod",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V", false);

		}
		super.onMethodEnter();
	}

	/**
	 * Annotates explicit exit points: RETURN and THROW instructions if the
	 * method is annotated as an @EpaAction
	 */
	@Override
	public void onMethodExit(int opcode) {
		// TODO: Check for <clinit>

		if (isMethodAnnotedAsAnEpaActionMethod) {

			if (opcode == Opcode.ATHROW) {
				// duplicates the reference to the exception to be thrown
				mv.visitInsn(Opcodes.DUP);
			} else {
				// because this is a return instruction, no exception is being
				// thrown
				mv.visitInsn(Opcodes.ACONST_NULL);
			}

			mv.visitLdcInsn(className);
			mv.visitLdcInsn(fullMethodName);
			if ((access & Opcodes.ACC_STATIC) > 0) {
				// load null constant because this is a static method
				mv.visitInsn(Opcodes.ACONST_NULL);
			} else {
				// load this reference
				mv.visitVarInsn(Opcodes.ALOAD, 0);
			}

			// "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"
			final String leftMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Exception.class),
					Type.getType(String.class), Type.getType(String.class), Type.getType(Object.class));
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PackageInfo.getNameWithSlash(EPAMonitor.class), "exitMethod",
					leftMethodDescriptor, false);
		}
		super.onMethodExit(opcode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		int maxNum = 3;
		super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
	}

	/**
	 * Indicates if the current method is an EpaAction method
	 */
	private boolean isMethodAnnotedAsAnEpaActionMethod = false;

	/**
	 * Check if the method has an @EpaAction annotation
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (Type.getDescriptor(EpaAction.class).equals(desc)) {
			isMethodAnnotedAsAnEpaActionMethod = true;
		}
		return super.visitAnnotation(desc, visible);
	}
}
