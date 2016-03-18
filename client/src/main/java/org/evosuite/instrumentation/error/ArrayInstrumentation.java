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
package org.evosuite.instrumentation.error;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ArrayInstrumentation extends ErrorBranchInstrumenter {

	public ArrayInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}
	
	@Override
	public void visitInsn(int opcode) {
		// Check array accesses
		if (opcode == Opcodes.IALOAD || opcode == Opcodes.BALOAD
				|| opcode == Opcodes.CALOAD || opcode == Opcodes.SALOAD
				|| opcode == Opcodes.LALOAD || opcode == Opcodes.FALOAD
				|| opcode == Opcodes.DALOAD || opcode == Opcodes.AALOAD) {

			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFGE, "java/lang/ArrayIndexOutOfBoundsException");

			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.SWAP);
			//mv.visitInsn(Opcodes.POP);
			mv.visitInsn(Opcodes.ARRAYLENGTH);
			insertBranch(Opcodes.IF_ICMPLT, "java/lang/ArrayIndexOutOfBoundsException");
	
			
		} else if (opcode == Opcodes.IASTORE || opcode == Opcodes.BASTORE
				|| opcode == Opcodes.CASTORE || opcode == Opcodes.SASTORE
				|| opcode == Opcodes.AASTORE || opcode == Opcodes.LASTORE
				|| opcode == Opcodes.FASTORE || opcode == Opcodes.DASTORE) {

			int loc = 0;
			if (opcode == Opcodes.IASTORE)
				loc = mv.newLocal(Type.INT_TYPE);
			else if (opcode == Opcodes.BASTORE)
				loc = mv.newLocal(Type.BYTE_TYPE);
			else if (opcode == Opcodes.CASTORE)
				loc = mv.newLocal(Type.CHAR_TYPE);
			else if (opcode == Opcodes.SASTORE)
				loc = mv.newLocal(Type.SHORT_TYPE);
			else if (opcode == Opcodes.AASTORE)
				loc = mv.newLocal(Type.getType(Object.class));
			else if (opcode == Opcodes.LASTORE)
				loc = mv.newLocal(Type.LONG_TYPE);
			else if (opcode == Opcodes.FASTORE)
				loc = mv.newLocal(Type.FLOAT_TYPE);
			else if (opcode == Opcodes.DASTORE)
				loc = mv.newLocal(Type.DOUBLE_TYPE);
			else
				throw new RuntimeException("Unknown type");
			mv.storeLocal(loc);

			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFGE, "java/lang/ArrayIndexOutOfBoundsException");

			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.SWAP);
			//mv.visitInsn(Opcodes.POP);
			mv.visitInsn(Opcodes.ARRAYLENGTH);
			insertBranch(Opcodes.IF_ICMPLT, "java/lang/ArrayIndexOutOfBoundsException");

			mv.loadLocal(loc);
		}
	}
	
	@Override
	public void visitIntInsn(int opcode, int operand) {
		if(opcode == Opcodes.NEWARRAY) {
			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFGE, "java/lang/NegativeArraySizeException");	
		}
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		mv.visitLdcInsn(dims);
		// Number of dimensions
		insertBranch(Opcodes.IFGE, "java/lang/NegativeArraySizeException");
		// TODO: Check for each dimension that it is geq 0
	}

	@Override
	public void visitTypeInsn(int opcode,
							  String type) {
		if(opcode == Opcodes.ANEWARRAY) {
			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFGE, "java/lang/NegativeArraySizeException");
		}
	}
}
