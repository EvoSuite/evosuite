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
package org.evosuite.runtime.instrumentation;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * MethodVisitor that acts as a proxy to two other visitors
 *
 * @author Gordon Fraser
 */
public class MultiMethodVisitor extends MethodVisitor {

	MethodVisitor mv1;
	MethodVisitor mv2;

	Map<Label, Label> label_mapping = new HashMap<Label, Label>();

	/**
	 * <p>Constructor for MultiMethodVisitor.</p>
	 *
	 * @param mv1 a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param mv2 a {@link org.objectweb.asm.MethodVisitor} object.
	 */
	public MultiMethodVisitor(MethodVisitor mv1, MethodVisitor mv2) {
		super(Opcodes.ASM5);
		this.mv1 = mv1;
		this.mv2 = mv2;
	}

	private Label getLabel(Label l) {
		if (label_mapping.containsKey(l))
			return label_mapping.get(l);
		else {
			Label l2 = new Label();
			label_mapping.put(l, l2);
			return l2;
		}

	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String, boolean)
	 */
	/** {@inheritDoc} */
	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		mv1.visitAnnotation(arg0, arg1);
		return mv2.visitAnnotation(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitAnnotationDefault()
	 */
	/** {@inheritDoc} */
	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		mv1.visitAnnotationDefault();
		return mv2.visitAnnotationDefault();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitAttribute(org.objectweb.asm.Attribute)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitAttribute(Attribute arg0) {
		mv1.visitAttribute(arg0);
		mv2.visitAttribute(arg0);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitCode()
	 */
	/** {@inheritDoc} */
	@Override
	public void visitCode() {
		mv1.visitCode();
		mv2.visitCode();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitEnd()
	 */
	/** {@inheritDoc} */
	@Override
	public void visitEnd() {
		mv1.visitEnd();
		mv2.visitEnd();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
		mv1.visitFieldInsn(arg0, arg1, arg2, arg3);
		mv2.visitFieldInsn(arg0, arg1, arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitFrame(int, int, java.lang.Object[], int, java.lang.Object[])
	 */
	/** {@inheritDoc} */
	@Override
	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
		mv1.visitFrame(arg0, arg1, arg2, arg3, arg4);
		mv2.visitFrame(arg0, arg1, arg2, arg3, arg4);

	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitIincInsn(int, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitIincInsn(int arg0, int arg1) {
		mv1.visitIincInsn(arg0, arg1);
		mv2.visitIincInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitInsn(int arg0) {
		mv1.visitInsn(arg0);
		mv2.visitInsn(arg0);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitIntInsn(int, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitIntInsn(int arg0, int arg1) {
		mv1.visitIntInsn(arg0, arg1);
		mv2.visitIntInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitJumpInsn(int, org.objectweb.asm.Label)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitJumpInsn(int arg0, Label arg1) {
		mv1.visitJumpInsn(arg0, arg1);
		mv2.visitJumpInsn(arg0, getLabel(arg1));
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLabel(org.objectweb.asm.Label)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitLabel(Label arg0) {
		mv1.visitLabel(arg0);
		mv2.visitLabel(getLabel(arg0));
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLdcInsn(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitLdcInsn(Object arg0) {
		mv1.visitLdcInsn(arg0);
		mv2.visitLdcInsn(arg0);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLineNumber(int, org.objectweb.asm.Label)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitLineNumber(int arg0, Label arg1) {
		mv1.visitLineNumber(arg0, arg1);
		//mv2.visitLineNumber(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLocalVariable(java.lang.String, java.lang.String, java.lang.String, org.objectweb.asm.Label, org.objectweb.asm.Label, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3,
	        Label arg4, int arg5) {
		mv1.visitLocalVariable(arg0, arg1, arg2, arg3, arg4, arg5);
		mv2.visitLocalVariable(arg0, arg1, arg2, getLabel(arg3), getLabel(arg4), arg5);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLookupSwitchInsn(org.objectweb.asm.Label, int[], org.objectweb.asm.Label[])
	 */
	/** {@inheritDoc} */
	@Override
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		mv1.visitLookupSwitchInsn(arg0, arg1, arg2);
		Label[] arg2Copy = new Label[arg2.length];
		for (int i = 0; i < arg2.length; i++)
			arg2Copy[i] = getLabel(arg2[i]);

		mv2.visitLookupSwitchInsn(getLabel(arg0), arg1, arg2Copy);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMaxs(int, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMaxs(int arg0, int arg1) {
		mv1.visitMaxs(arg0, arg1);
		mv2.visitMaxs(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3, boolean itf) {
		mv1.visitMethodInsn(arg0, arg1, arg2, arg3, itf);
		mv2.visitMethodInsn(arg0, arg1, arg2, arg3, itf);
	}
	
	@Override
	public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
		mv1.visitMethodInsn(arg0, arg1, arg2, arg3);
		mv2.visitMethodInsn(arg0, arg1, arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMultiANewArrayInsn(java.lang.String, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		mv1.visitMultiANewArrayInsn(arg0, arg1);
		mv2.visitMultiANewArrayInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitParameterAnnotation(int, java.lang.String, boolean)
	 */
	/** {@inheritDoc} */
	@Override
	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		mv1.visitParameterAnnotation(arg0, arg1, arg2);
		return mv2.visitParameterAnnotation(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTableSwitchInsn(int, int, org.objectweb.asm.Label, org.objectweb.asm.Label[])
	 */
	/** {@inheritDoc} */
	@Override
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label... arg3) {
		mv1.visitTableSwitchInsn(arg0, arg1, arg2, arg3);
		Label[] arg3Copy = new Label[arg3.length];
		for (int i = 0; i < arg3.length; i++)
			arg3Copy[i] = getLabel(arg3[i]);
		mv2.visitTableSwitchInsn(arg0, arg1, getLabel(arg2), arg3Copy);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTryCatchBlock(org.objectweb.asm.Label, org.objectweb.asm.Label, org.objectweb.asm.Label, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
		mv1.visitTryCatchBlock(arg0, arg1, arg2, arg3);
		mv2.visitTryCatchBlock(getLabel(arg0), getLabel(arg1), getLabel(arg2), arg3);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitTypeInsn(int arg0, String arg1) {
		mv1.visitTypeInsn(arg0, arg1);
		mv2.visitTypeInsn(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitVarInsn(int, int)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitVarInsn(int arg0, int arg1) {
		mv1.visitVarInsn(arg0, arg1);
		mv2.visitVarInsn(arg0, arg1);
	}

}
