/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instruments classes to call the tracer each time a new line of the source
 * code is passed.
 * 
 * @author Gordon Fraser
 * 
 */
public class LineNumberMethodAdapter extends MethodVisitor {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LineNumberMethodAdapter.class);

	private final String fullMethodName;

	private final String methodName;

	private final String className;

	private boolean hadInvokeSpecial = false;

	int currentLine = 0;

	public LineNumberMethodAdapter(MethodVisitor mv, String className, String methodName,
	        String desc) {
		super(Opcodes.ASM4, mv);
		fullMethodName = methodName + desc;
		this.className = className;
		this.methodName = methodName;
		if (!methodName.equals("<init>"))
			hadInvokeSpecial = true;
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		currentLine = line;

		if (methodName.equals("<clinit>"))
			return;

		if (!hadInvokeSpecial)
			return;

		LinePool.addLine(className, methodName, line);
		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		this.visitLdcInsn(line);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
		                   "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
		                   "passedLine", "(Ljava/lang/String;Ljava/lang/String;I)V");
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (opcode == Opcodes.INVOKESPECIAL) {
			if (methodName.equals("<init>"))
				hadInvokeSpecial = true;
		}
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		int maxNum = 3;
		super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
	}
}
