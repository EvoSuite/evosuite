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
package org.evosuite.primitives;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * <p>PrimitivePoolMethodAdapter class.</p>
 *
 * @author Gordon Fraser
 */
public class PrimitivePoolMethodAdapter extends MethodVisitor {

	private final PrimitivePool primitive_pool = PrimitivePool.getInstance();

	/**
	 * <p>Constructor for PrimitivePoolMethodAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 */
	public PrimitivePoolMethodAdapter(MethodVisitor mv) {
		super(Opcodes.ASM4, mv);
	}

	/**
	 * {@inheritDoc}
	 *
	 * This is a hack to avoid deadlocks because we are only testing single
	 * threaded stuff. This will be replaced with something nicer once Sebastian
	 * has found a solution
	 */
	/*
	@Override
	public void visitInsn(int opcode) {
		if (opcode != Opcodes.MONITORENTER && opcode != Opcodes.MONITOREXIT)
			super.visitInsn(opcode);
		else
			super.visitInsn(Opcodes.POP);

	}
	*/

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitLookupSwitchInsn(org.objectweb.asm.Label, int[], org.objectweb.asm.Label[])
	 */
	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		for (int key : keys) {
			primitive_pool.add(key);
		}
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}

	/** {@inheritDoc} */
	@Override
	public void visitIntInsn(int opcode, int operand) {
		if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
			primitive_pool.add(operand);
		}
		super.visitIntInsn(opcode, operand);
	}

	/** {@inheritDoc} */
	@Override
	public void visitLdcInsn(Object cst) {
		primitive_pool.add(cst);
		super.visitLdcInsn(cst);
	}
}
