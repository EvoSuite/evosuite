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
package org.evosuite.runtime.instrumentation;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * MethodCallReplacementMethodAdapter class.
 * </p>
 * 
 * @author fraser
 */
public class MethodCallReplacementMethodAdapter extends GeneratorAdapter {

	private final String className;

	private final String superClassName;

	private boolean needToWaitForSuperConstructor = false;
	
	private boolean hasBeenInstrumented = false;

	/**
	 * <p>
	 * Constructor for MethodCallReplacementMethodAdapter.
	 * </p>
	 * 
	 * @param mv
	 *            a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param access
	 *            a int.
	 * @param desc
	 *            a {@link java.lang.String} object.
	 */
	public MethodCallReplacementMethodAdapter(MethodVisitor mv, String className,
			String superClassName, String methodName, int access, String desc) {
		super(Opcodes.ASM5, mv, access, methodName, desc);
		this.className = className;
		this.superClassName = superClassName;
		if (methodName.equals("<init>")) {
			needToWaitForSuperConstructor = true;
		}
	}



	public MethodVisitor getNextVisitor() {
		return mv;
	}
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

		boolean isReplaced = false;
		// Static replacement methods
		// For invokespecial this can only be used if a constructor is called,
		// not for super calls because not all mock classes may be superclasses
		// of the actual object. E.g. Throwable -> Exception -> RuntimeException
		// A MockRuntimeException is not a subclass of MockException and MockThrowable
		if(MethodCallReplacementCache.getInstance().hasReplacementCall(owner, name+desc) && 
				(opcode != Opcodes.INVOKESPECIAL || name.equals("<init>"))) {
			MethodCallReplacement replacement = MethodCallReplacementCache.getInstance().getReplacementCall(owner, name+desc);
			isReplaced = true;
			replacement.insertMethodCall(this, Opcodes.INVOKESTATIC);
			hasBeenInstrumented = true;
		}

		// for constructors
		if (!isReplaced) {
			if(MethodCallReplacementCache.getInstance().hasSpecialReplacementCall(owner, name+desc)) {
				MethodCallReplacement replacement = MethodCallReplacementCache.getInstance().getSpecialReplacementCall(owner, name+desc);
				if (replacement.isTarget(owner, name, desc)
						&& opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
					isReplaced = true;
					hasBeenInstrumented = true;
					boolean isSelf = false;
					if (needToWaitForSuperConstructor) {
						String originalClassNameWithDots = owner.replace('/', '.');
						if (originalClassNameWithDots.equals(superClassName)) {
							isSelf = true;
						}
					}
					if (replacement.getMethodName().equals("<init>"))
						replacement.insertConstructorCall(this, replacement, isSelf);
					else {
						replacement.insertMethodCall(this, Opcodes.INVOKESPECIAL);
					}
				}
			}
		}

		// non-static replacement methods
//		if (!isReplaced) {
//			iterator = MethodCallReplacementCache.getInstance().getVirtualReplacementCalls();
//			while(iterator.hasNext()) {
//				MethodCallReplacement replacement = iterator.next();
//				if (replacement.isTarget(owner, name, desc)) {
//					isReplaced = true;
//					replacement.insertMethodCall(this, Opcodes.INVOKEVIRTUAL);
//					break;
//				}
//			}
//		}

		if (!isReplaced) {
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}
		if (needToWaitForSuperConstructor) {
			if (opcode == Opcodes.INVOKESPECIAL) {
				String originalClassNameWithDots = owner.replace('/', '.');
				if (originalClassNameWithDots.equals(superClassName)) {
					needToWaitForSuperConstructor = false;
				}
			}
		}
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		// The instrumentation adds a boolean to the stack at one point
		// which _may_ increase the max stack size. A ASM
		// doesn't manage to calculate the maximum stack size
		// correctly we just add one here
		if(hasBeenInstrumented)
			super.visitMaxs(maxStack + 1, maxLocals);
		else
			super.visitMaxs(maxStack, maxLocals);
	}
}
