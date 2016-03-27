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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class OverflowInstrumentation extends ErrorBranchInstrumenter {

    private static final String CHECKER = ErrorConditionChecker.class.getCanonicalName().replace('.','/');

    public OverflowInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	protected void insertBranchWithoutException(int opcode) {
		Label origTarget = new Label();
		mv.tagBranch();
		mv.visitJumpInsn(opcode, origTarget);
		mv.visitLabel(origTarget);
		mv.tagBranchExit();
	}

	@Override
	public void visitInsn(int opcode) {
		// Overflow checks
		switch (opcode) {
		case Opcodes.IADD:
		case Opcodes.ISUB:
		case Opcodes.IMUL:
			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"underflowDistance", "(III)I", false);

			insertBranchWithoutException(Opcodes.IFGT);

		case Opcodes.IDIV:
			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"overflowDistance", "(III)I", false);
			insertBranchWithoutException(Opcodes.IFGT);

			break;

		case Opcodes.FADD:
		case Opcodes.FSUB:
		case Opcodes.FMUL:

			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"underflowDistance", "(FFI)I", false);
			insertBranchWithoutException(Opcodes.IFGE);

		case Opcodes.FDIV:
			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"overflowDistance", "(FFI)I", false);
			insertBranchWithoutException(Opcodes.IFGE);
			break;
			
		case Opcodes.DADD:
		case Opcodes.DSUB:
		case Opcodes.DMUL:

			int loc = mv.newLocal(Type.DOUBLE_TYPE);
			mv.storeLocal(loc);
			mv.visitInsn(Opcodes.DUP2);
			mv.loadLocal(loc);
			mv.visitInsn(Opcodes.DUP2_X2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"underflowDistance", "(DDI)I", false);

			insertBranchWithoutException(Opcodes.IFGE);

		case Opcodes.DDIV:
			loc = mv.newLocal(Type.DOUBLE_TYPE);

			mv.storeLocal(loc);
			mv.visitInsn(Opcodes.DUP2);
			mv.loadLocal(loc);
			mv.visitInsn(Opcodes.DUP2_X2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"overflowDistance", "(DDI)I", false);

			insertBranchWithoutException(Opcodes.IFGE);
			break;

		case Opcodes.LADD:
		case Opcodes.LSUB:
		case Opcodes.LMUL:
			int loc2 = mv.newLocal(Type.LONG_TYPE);
			mv.storeLocal(loc2);
			mv.visitInsn(Opcodes.DUP2);
			mv.loadLocal(loc2);
			mv.visitInsn(Opcodes.DUP2_X2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"underflowDistance", "(JJI)I", false);

			insertBranchWithoutException(Opcodes.IFGE);

		case Opcodes.LDIV:

			loc2 = mv.newLocal(Type.LONG_TYPE);
			mv.storeLocal(loc2);
			mv.visitInsn(Opcodes.DUP2);
			mv.loadLocal(loc2);
			mv.visitInsn(Opcodes.DUP2_X2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"overflowDistance", "(JJI)I", false);

			insertBranchWithoutException(Opcodes.IFGE);
			break;
		}

	}
}
