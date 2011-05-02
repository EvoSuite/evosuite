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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.javalanche.mutation.bytecodeMutations.MutationMarker;

/**
 * Instrument classes to keep track of method entry and exit
 * 
 * @author Gordon Fraser
 * 
 */
public class MethodEntryAdapter extends AdviceAdapter {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MethodEntryAdapter.class);

	private static final boolean MUTATION = Properties.CRITERION.equals(Criterion.MUTATION);

	String className;
	String methodName;
	String fullMethodName;
	int access;

	public MethodEntryAdapter(MethodVisitor mv, int access, String className,
	        String methodName, String desc) {
		super(mv, access, methodName, desc);
		this.className = className;
		this.methodName = methodName;
		this.fullMethodName = methodName + desc;
		this.access = access;
	}

	@Override
	public void onMethodEnter() {

		if (methodName.equals("<clinit>"))
			return;

		if (MUTATION) {
			Label mutationStartLabel = new Label();
			mutationStartLabel.info = new MutationMarker(true);
			mv.visitLabel(mutationStartLabel);
		}

		mv.visitLdcInsn(className);
		mv.visitLdcInsn(fullMethodName);
		if ((access & Opcodes.ACC_STATIC) > 0) {
			mv.visitInsn(Opcodes.ACONST_NULL);
		} else {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
		}
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
		                   "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
		                   "enteredMethod",
		                   "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V");

		if (MUTATION) {
			Label mutationEndLabel = new Label();
			mutationEndLabel.info = new MutationMarker(false);
			mv.visitLabel(mutationEndLabel);
		}
		super.onMethodEnter();
	}

	@Override
	public void onMethodExit(int opcode) {
		if (MUTATION) {
			Label mutationStartLabel = new Label();
			mutationStartLabel.info = new MutationMarker(true);
			mv.visitLabel(mutationStartLabel);
		}
		mv.visitLdcInsn(className);
		mv.visitLdcInsn(fullMethodName);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
		                   "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
		                   "leftMethod", "(Ljava/lang/String;Ljava/lang/String;)V");
		super.onMethodExit(opcode);
		if (MUTATION) {
			Label mutationEndLabel = new Label();
			mutationEndLabel.info = new MutationMarker(false);
			mv.visitLabel(mutationEndLabel);
		}
	}
}
