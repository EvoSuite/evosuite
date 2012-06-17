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
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author fraser
 * 
 */
public class MethodCallReplacementMethodAdapter extends GeneratorAdapter {

	private class MethodCallReplacement {
		private final String className;
		private final String methodName;
		private final String desc;

		private final String replacementClassName;
		private final String replacementMethodName;
		private final String replacementDesc;

		private final boolean popCallee;

		public MethodCallReplacement(String className, String methodName, String desc,
		        String replacementClassName, String replacementMethodName,
		        String replacementDesc, boolean pop) {
			this.className = className;
			this.methodName = methodName;
			this.desc = desc;
			this.replacementClassName = replacementClassName;
			this.replacementMethodName = replacementMethodName;
			this.replacementDesc = replacementDesc;
			this.popCallee = pop;
			// Currently assume that the methods take identical params 
			assert (desc.equals(replacementDesc));
		}

		public boolean isTarget(String owner, String name, String desc) {
			return className.equals(owner) && methodName.equals(name)
			        && this.desc.equals(desc);
		}

		public void insertMethodCall(MethodVisitor mv, int opcode) {
			if (popCallee) {
				Type[] args = Type.getArgumentTypes(desc);
				Map<Integer, Integer> to = new HashMap<Integer, Integer>();
				for (int i = args.length - 1; i >= 0; i--) {
					int loc = newLocal(args[i]);
					storeLocal(loc);
					to.put(i, loc);
				}

				pop();//callee

				for (int i = 0; i < args.length; i++) {
					loadLocal(to.get(i));
				}
			}
			mv.visitMethodInsn(opcode, replacementClassName,
			                   replacementMethodName, replacementDesc);
		}
	}
	
	/**
	 * method replacements, which are called with Opcodes.INVOKESTATIC
	 */
	private final Set<MethodCallReplacement> replacementCalls = new HashSet<MethodCallReplacement>();
	
	/**
	 * method replacements, which are called with Opcodes.INVOKEVIRTUAL
	 */
	private final Set<MethodCallReplacement> virtualReplacementCalls = new HashSet<MethodCallReplacement>();

	/**
	 * @param api
	 */
	public MethodCallReplacementMethodAdapter(MethodVisitor mv, String className,
	        String methodName, int access, String desc) {
		super(Opcodes.ASM4, mv, access, methodName, desc);
		if (Properties.REPLACE_CALLS) {
			replacementCalls.add(new MethodCallReplacement("java/lang/System", "exit",
			        "(I)V", "de/unisb/cs/st/evosuite/runtime/System", "exit", "(I)V", false));
			replacementCalls.add(new MethodCallReplacement("java/lang/System",
			        "currentTimeMillis", "()J", "de/unisb/cs/st/evosuite/runtime/System",
			        "currentTimeMillis", "()J", false));
			replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextInt",
			        "()I", "de/unisb/cs/st/evosuite/runtime/Random", "nextInt", "()I", true));
			replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextInt",
			        "(I)I", "de/unisb/cs/st/evosuite/runtime/Random", "nextInt", "(I)I", true));
			replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextDouble",
			        "()D", "de/unisb/cs/st/evosuite/runtime/Random", "nextDouble", "()D",
			        true));
			replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextFloat",
			        "()F", "de/unisb/cs/st/evosuite/runtime/Random", "nextFloat", "()F", true));
			replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextLong",
			        "()J", "de/unisb/cs/st/evosuite/runtime/Random", "nextLong", "()J", true));
		} 
		if (Properties.VIRTUAL_FS) {
			virtualReplacementCalls.add(new MethodCallReplacement("java/io/FileInputStream", "available",
					"()I", "java/io/FileInputStream", "availableNew", "()I", false));
			virtualReplacementCalls.add(new MethodCallReplacement("java/io/FileInputStream", "skip",
					"(J)J", "java/io/FileInputStream", "skipNew", "(J)J", false));
		}		
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		boolean isReplaced = false;
		// static replacement methods
		for (MethodCallReplacement replacement : replacementCalls) {
			if (replacement.isTarget(owner, name, desc)) {
				isReplaced = true;
				replacement.insertMethodCall(this, Opcodes.INVOKESTATIC);
				break;
			}
		}
		// non-static replacement methods
		for (MethodCallReplacement replacement : virtualReplacementCalls) {
			if (replacement.isTarget(owner, name, desc)) {
				isReplaced = true;
				replacement.insertMethodCall(this, Opcodes.INVOKEVIRTUAL);
				break;
			}
		}
		if (!isReplaced)
			super.visitMethodInsn(opcode, owner, name, desc);
	}
}
