package org.evosuite.instrumentation.error;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;

public class DivisionByZeroInstrumentation extends ErrorBranchInstrumenter {

	public DivisionByZeroInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitInsn(int opcode) {
		// Check *DIV for divisonbyzero
		if (opcode == Opcodes.IDIV) {
			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFNE, "java/lang/ArithmeticException");

		} else if (opcode == Opcodes.FDIV) {
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(0F);
			mv.visitInsn(Opcodes.FCMPL);
			insertBranch(Opcodes.IFNE, "java/lang/ArithmeticException");

		} else if (opcode == Opcodes.LDIV || opcode == Opcodes.DDIV) {
			mv.visitInsn(Opcodes.DUP2);
			if (opcode == Opcodes.LDIV) {
				mv.visitLdcInsn(0L);
				mv.visitInsn(Opcodes.LCMP);
			} else {
				mv.visitLdcInsn(0.0);
				mv.visitInsn(Opcodes.DCMPL);
			}
			insertBranch(Opcodes.IFNE, "java/lang/ArithmeticException");
		}
	}
}
