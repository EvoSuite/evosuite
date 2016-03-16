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
/**
 * 
 */
package org.evosuite.instrumentation;

import org.evosuite.PackageInfo;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>YieldAtLineNumberMethodAdapter class.</p>
 *
 * @author fraser
 */
public class YieldAtLineNumberMethodAdapter extends MethodVisitor {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LineNumberMethodAdapter.class);

	private final String className;

	private final String methodName;

	private boolean hadInvokeSpecial = false;

	int currentLine = 0;

	/**
	 * <p>Constructor for YieldAtLineNumberMethodAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 */
	public YieldAtLineNumberMethodAdapter(MethodVisitor mv, String className,
	        String methodName) {
		super(Opcodes.ASM5, mv);
		this.className = className;
		this.methodName = methodName;
		if (!methodName.equals("<init>"))
			hadInvokeSpecial = true;
	}

	/** {@inheritDoc} */
	@Override
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		currentLine = line;

		if (methodName.equals("<clinit>"))
			return;

		if (!hadInvokeSpecial)
			return;

		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				PackageInfo.getNameWithSlash(ExecutionTracer.class),
		                   "checkTimeout", "()V", false);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		if (opcode == Opcodes.INVOKESPECIAL) {
			if (methodName.equals("<init>"))
				hadInvokeSpecial = true;
		}
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitInsn(int opcode) {
		if (opcode == Opcodes.ATHROW) {
			super.visitInsn(Opcodes.DUP);
			this.visitLdcInsn(className);
			this.visitLdcInsn(methodName);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ExecutionTracer.class),
			                   "exceptionThrown",
			                   "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V", false);
		}
		super.visitInsn(opcode);
	}
}
