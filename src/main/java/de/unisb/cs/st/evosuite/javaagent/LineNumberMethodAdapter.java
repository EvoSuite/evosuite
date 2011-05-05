/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.javaagent;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.MutationMarker;

/**
 * Instruments classes to call the tracer each time a new line of the source
 * code is passed.
 * 
 * @author Gordon Fraser
 * 
 */
public class LineNumberMethodAdapter extends MethodAdapter {

	public static int branch_id = 0;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LineNumberMethodAdapter.class);

	private static final boolean MUTATION = Properties.CRITERION.equals(Criterion.MUTATION);

	private final String fullMethodName;

	private final String methodName;

	private final String className;

	int current_line = 0;

	public LineNumberMethodAdapter(MethodVisitor mv, String className, String methodName,
	        String desc) {
		super(mv);
		fullMethodName = methodName + desc;
		this.className = className;
		this.methodName = methodName;
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		if (methodName.equals("<clinit>"))
			return;

		if (MUTATION) {
			Label mutationStartLabel = new Label();
			mutationStartLabel.info = new MutationMarker(true);
			mv.visitLabel(mutationStartLabel);
		}
		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		this.visitLdcInsn(line);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
		                   "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
		                   "passedLine", "(Ljava/lang/String;Ljava/lang/String;I)V");
		current_line = line;
		if (MUTATION) {
			Label mutationEndLabel = new Label();
			mutationEndLabel.info = new MutationMarker(false);
			mv.visitLabel(mutationEndLabel);
		}
	}
}
