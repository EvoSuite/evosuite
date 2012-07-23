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
package org.evosuite.setup;

import java.lang.reflect.Modifier;

import mockit.external.asm.Type;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Visits given class in order to collect information about class hierarchies
 *
 * @author Gordon Fraser
 */
public class InheritanceClassAdapter extends ClassVisitor {

	private String className = null;

	/**
	 * <p>Constructor for InheritanceClassAdapter.</p>
	 *
	 * @param cv a {@link org.objectweb.asm.ClassVisitor} object.
	 */
	public InheritanceClassAdapter(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {

		this.className = name.replace("/", ".");

		if ((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) {
			return;
		}

		if (superName != null)
			ClusterAnalysis.addSubclass(superName.replace("/", "."), className);
		for (String interfaceName : interfaces)
			ClusterAnalysis.addSubclass(interfaceName.replace("/", "."), className);

		if (Modifier.isAbstract(access) || Modifier.isInterface(access))
			ClusterAnalysis.addAbstract(className);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
	        String signature, Object value) {
		if (Type.getType(desc).getSort() == Type.OBJECT)
			ClusterAnalysis.addGenerator(className, Type.getType(desc).getClassName());
		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int arg0, String name, String desc,
	        String signature, String[] arg4) {
		Type ret = Type.getReturnType(desc);
		while (ret.getSort() == Type.ARRAY)
			ret = ret.getElementType();

		if (ret.getSort() == Type.OBJECT)
			ClusterAnalysis.addParameter(className, ret.getClassName());

		for (Type type : Type.getArgumentTypes(desc)) {
			while (type.getSort() == Type.ARRAY)
				type = type.getElementType();

			if (type.getSort() == Type.OBJECT)
				ClusterAnalysis.addParameter(className, type.getClassName());
		}

		if ((Opcodes.ACC_PRIVATE & arg0) == Opcodes.ACC_PRIVATE)
			return null;

		if (name.equals("<init>")) {
			ClusterAnalysis.addGenerator(className, className);
		} else if (ret.getSort() == Type.OBJECT)
			ClusterAnalysis.addGenerator(className, ret.getClassName());

		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitEnd()
	 */
	/** {@inheritDoc} */
	@Override
	public void visitEnd() {
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitInnerClass(String arg0, String arg1, String arg2, int access) {
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitOuterClass(java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitOuterClass(String arg0, String arg1, String arg2) {
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitAnnotation(java.lang.String, boolean)
	 */
	/** {@inheritDoc} */
	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return null;
	}
}
