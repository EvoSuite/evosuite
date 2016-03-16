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

import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Turn protected / default access rights to public access rights. This was
 * necessary because EvoSuite is not in the same package as the UUT, but the
 * tests will reside in the same package.
 *
 * However, this does not hold now, as all methods can actually be called through reflection
 *
 * @author Gordon Fraser
 */
@Deprecated
public class AccessibleClassAdapter extends ClassVisitor {

	private boolean exclude = false;

	/**
	 * <p>Constructor for AccessibleClassAdapter.</p>
	 *
	 * @param cv a {@link org.objectweb.asm.ClassVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 */
	public AccessibleClassAdapter(ClassVisitor cv, String className) {
		super(Opcodes.ASM5, cv);
		className = ResourceList.getClassNameFromResourcePath(className); 
		String packageName = "";
		if (className.contains("."))
			packageName = className.substring(0, className.lastIndexOf('.'));
		if (!packageName.equals(Properties.CLASS_PREFIX)) {
			exclude = true;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Change subclasses to public
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		if (!exclude && (access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE
		        && (access & Opcodes.ACC_PROTECTED) != Opcodes.ACC_PROTECTED) {
			access = access | Opcodes.ACC_PUBLIC;
			// access = access & ~Opcodes.ACC_PROTECTED;
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Change fields to public
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
	        String signature, Object value) {
		if (!exclude && (access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE
		        && (access & Opcodes.ACC_PROTECTED) != Opcodes.ACC_PROTECTED) {
			access = access | Opcodes.ACC_PUBLIC;
			// access = access & ~Opcodes.ACC_PROTECTED;
			//System.out.println("Setting field to public: "+name);
		}

		return super.visitField(access, name, desc, signature, value);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Change methods to public
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, final String[] exceptions) {
		int skipMask = Opcodes.ACC_NATIVE | Opcodes.ACC_BRIDGE;
		
		if (!exclude && ((access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) && ((access & skipMask) == 0)) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
		}

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return mv;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitInnerClass(String name, String outerName, String innerName,
	        int access) {
		if (!exclude && (access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
		}

		super.visitInnerClass(name, outerName, innerName, access);
	}

}
