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
package org.evosuite.seeding;

import org.evosuite.setup.DependencyAnalysis;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * <p>
 * PrimitiveClassAdapter class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class PrimitiveClassAdapter extends ClassVisitor {

	private final String className;

	//private static String target_class = Properties.TARGET_CLASS;

	//private static final boolean REPLACE_STRING = Properties.STRING_REPLACEMENT
	//        && !Properties.TT;

	// private final PrimitivePool primitive_pool = PrimitivePool.getInstance();

	private final ConstantPoolManager poolManager = ConstantPoolManager.getInstance();

	/** Skip methods on enums - at least some */
	private boolean isEnum = false;

	/**
	 * <p>
	 * Constructor for PrimitiveClassAdapter.
	 * </p>
	 * 
	 * @param visitor
	 *            a {@link org.objectweb.asm.ClassVisitor} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public PrimitiveClassAdapter(ClassVisitor visitor, String className) {
		super(Opcodes.ASM5, visitor);
		this.className = className.replaceAll("/", ".");
	}
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		if (superName.equals("java/lang/Enum"))
			isEnum = true;
	}

	/** {@inheritDoc} */
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
	        String signature, Object value) {

		// We don't use serial numbers because they can be very long and are not used in any branches
		if (!"serialVersionUID".equals(name)) {
			if (DependencyAnalysis.isTargetClassName(className)) {
				poolManager.addSUTConstant(value);
				poolManager.addSUTConstant(Type.getType(desc));
			} else {
				poolManager.addNonSUTConstant(value);
			}
			if(isEnum) {
				// static final values in enums are likely enum values
				if((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL &&
				   (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
						if (DependencyAnalysis.isTargetClassName(className)) {
							poolManager.addSUTConstant(name);
						} else {
							poolManager.addNonSUTConstant(name);
						}
				   }
			}
			// primitive_pool.add(value);
		}
		return super.visitField(access, name, desc, signature, value);
	}

	/*
	 * Set default access rights to public access rights
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int methodAccess, String name, String descriptor,
	        String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor, signature,
		                                     exceptions);

		/*
		String classNameWithDots = Utils.getClassNameFromResourcePath(className);
		if (REPLACE_STRING
		        && (classNameWithDots.equals(target_class) || (classNameWithDots.startsWith(target_class
		                + "$")))) {
			mv = new StringReplacementMethodAdapter(methodAccess, descriptor, mv);
		}
		*/
		if (DependencyAnalysis.isTargetClassName(className)) {
			for (Type argumentType : Type.getArgumentTypes(descriptor)) {
				poolManager.addSUTConstant(argumentType);
			}
		}
		mv = new PrimitivePoolMethodAdapter(mv, className);

		return mv;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	@Override
	public void visitInnerClass(String name, String outerName, String innerName,
	        int access) {
		if ((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
			if (DependencyAnalysis.isTargetClassName(className)) {
				Type type = Type.getObjectType(name);
				poolManager.addSUTConstant(type);
			}
		}

		super.visitInnerClass(name, outerName, innerName, access);
	}
}
