/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.primitives;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Gordon Fraser
 *
 */
public class PrimitivePoolMethodAdapter extends MethodAdapter {

	private PrimitivePool primitive_pool = PrimitivePool.getInstance();

	/**
	 * @param mv
	 */
	public PrimitivePoolMethodAdapter(MethodVisitor mv) {
		super(mv);
	}
	
	public void visitIntInsn(int opcode, int operand) {
		if(opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
			primitive_pool.add(operand);
		}
		super.visitIntInsn(opcode, operand);
	}
	
	public void visitLdcInsn(Object cst) {
		primitive_pool.add(cst);
		super.visitLdcInsn(cst);
	}

}
