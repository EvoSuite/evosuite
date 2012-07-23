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
package org.evosuite.javaagent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * <p>MethodCallReplacementClassAdapter class.</p>
 *
 * @author fraser
 */
public class MethodCallReplacementClassAdapter extends ClassVisitor {

	private final String className;

	/**
	 * <p>Constructor for MethodCallReplacementClassAdapter.</p>
	 *
	 * @param cv a {@link org.objectweb.asm.ClassVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 */
	public MethodCallReplacementClassAdapter(ClassVisitor cv, String className) {
		super(Opcodes.ASM4, cv);
		this.className = className;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new MethodCallReplacementMethodAdapter(mv, className, name, access, desc);
	}
}
