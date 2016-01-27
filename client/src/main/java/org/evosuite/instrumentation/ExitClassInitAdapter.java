/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.instrumentation;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.evosuite.runtime.classhandling.ResetManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This adapter invokes adds a callback to trace
 * the exit from a <clinit>() method after the class
 * initialization has ended.
 * 
 * @author galeotti
 *
 */
public class ExitClassInitAdapter extends ClassVisitor {

	private static Logger logger = LoggerFactory
			.getLogger(ExitClassInitAdapter.class);

	private final String className;

	public ExitClassInitAdapter(ClassVisitor visitor, String className) {
		super(Opcodes.ASM5, visitor);
		this.className = className;
	}

	@Override
	public MethodVisitor visitMethod(int methodAccess, String methodName,
			String descriptor, String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, methodName,
				descriptor, signature, exceptions);

		if (methodName.equals("<clinit>")) {

			clinitFound =true; 
			ExitClassInitMethodAdapter staticResetMethodAdapter = new ExitClassInitMethodAdapter(
					className, methodName, mv);

			return staticResetMethodAdapter;
		} else {
			return mv;
		}
	}

	private boolean isInterface = false;
	private boolean clinitFound = false;
	private boolean hasStaticFields = false;

	private static final String EXIT_CLASS_INIT = "exitClassInit";

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
	}

	@Override
	public void visitEnd() {
		if (!clinitFound && !isInterface && hasStaticFields) {
			// create brand empty <clinit>()
			createEmptyClassInit();
		}
		super.visitEnd();
	}

	private static boolean isStatic(int access) {
		return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {

		if (isStatic(access)) {
			hasStaticFields = true;
		}

		return super.visitField(access, name, desc, signature, value);
	}

	private void createEmptyClassInit() {
		logger.info("Creating <clinit> in class "
				+ className);
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>",
				"()V", null, null);
		mv.visitCode();

		String executionTracerClassName = ResetManager.class.getName()
				.replace('.', '/');
		String executionTracerDescriptor = Type.getMethodDescriptor(
				Type.VOID_TYPE, Type.getType(String.class));

		String classNameWithDots = className.replace('/', '.');
		mv.visitLdcInsn(classNameWithDots);
		mv.visitMethodInsn(INVOKESTATIC, executionTracerClassName,
				EXIT_CLASS_INIT, executionTracerDescriptor, false);

		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

	}
}
