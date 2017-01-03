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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Instruments classes to call the tracer each time a new line of the source
 * code is passed.
 *
 * @author Gordon Fraser
 */
public class LineNumberMethodAdapter extends MethodVisitor {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LineNumberMethodAdapter.class);

	private final String fullMethodName;

	private final String methodName;

	private final String className;

	private boolean hadInvokeSpecial = false;

	private List<Integer> skippedLines = new ArrayList<>();

	int currentLine = 0;

	/**
	 * <p>Constructor for LineNumberMethodAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param desc a {@link java.lang.String} object.
	 */
	public LineNumberMethodAdapter(MethodVisitor mv, String className, String methodName,
	        String desc) {
		super(Opcodes.ASM5, mv);
		fullMethodName = methodName + desc;
		this.className = className;
		this.methodName = methodName;
		if (!methodName.equals("<init>"))
			hadInvokeSpecial = true;
	}

	private void addLineNumberInstrumentation(int line) {
		LinePool.addLine(className, fullMethodName, line);
		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		this.visitLdcInsn(line);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				PackageInfo.getNameWithSlash(ExecutionTracer.class),
				"passedLine", "(Ljava/lang/String;Ljava/lang/String;I)V", false);
	}

	/** {@inheritDoc} */
	@Override
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		currentLine = line;

		if (methodName.equals("<clinit>"))
			return;

		if (!hadInvokeSpecial) {
			skippedLines.add(line);
			return;
		}

		addLineNumberInstrumentation(line);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		super.visitMethodInsn(opcode, owner, name, desc, itf);
		if (opcode == Opcodes.INVOKESPECIAL) {
			if (methodName.equals("<init>")) {
				hadInvokeSpecial = true;
				for(int line : skippedLines) {
					addLineNumberInstrumentation(line);
				}
				skippedLines.clear();
			}
		}
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
