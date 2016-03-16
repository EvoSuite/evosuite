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
package org.evosuite.instrumentation;

import org.evosuite.PackageInfo;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instrument classes to keep track of method entry and exit
 *
 * @author Gordon Fraser
 */
public class MethodEntryAdapter extends AdviceAdapter {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(MethodEntryAdapter.class);

	String className;
	String methodName;
	String fullMethodName;
	int access;

	/**
	 * <p>Constructor for MethodEntryAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param access a int.
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param desc a {@link java.lang.String} object.
	 */
	public MethodEntryAdapter(MethodVisitor mv, int access, String className,
	        String methodName, String desc) {
		super(Opcodes.ASM5, mv, access, methodName, desc);
		this.className = className;
		this.methodName = methodName;
		this.fullMethodName = methodName + desc;
		this.access = access;
	}

	/** {@inheritDoc} */
	@Override
	public void onMethodEnter() {

		if (methodName.equals("<clinit>"))
			return; // FIXXME: Should we call super.onMethodEnter() here?

		mv.visitLdcInsn(className);
		mv.visitLdcInsn(fullMethodName);
		if ((access & Opcodes.ACC_STATIC) > 0) {
			mv.visitInsn(Opcodes.ACONST_NULL);
		} else {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
		}
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				PackageInfo.getNameWithSlash(ExecutionTracer.class),
		                   "enteredMethod",
		                   "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V", false);

		super.onMethodEnter();
	}

	/** {@inheritDoc} */
	@Override
	public void onMethodExit(int opcode) {
		// TODO: Check for <clinit>

		if (opcode != Opcodes.ATHROW) {

			mv.visitLdcInsn(className);
			mv.visitLdcInsn(fullMethodName);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(org.evosuite.testcase.execution.ExecutionTracer.class),
			                   "leftMethod", "(Ljava/lang/String;Ljava/lang/String;)V", false);
		}
		super.onMethodExit(opcode);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		int maxNum = 3;
		super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
	}
}
