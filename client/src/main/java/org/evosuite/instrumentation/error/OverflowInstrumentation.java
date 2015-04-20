package org.evosuite.instrumentation.error;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class OverflowInstrumentation extends ErrorBranchInstrumenter {

    private static final String CHECKER = ErrorConditionChecker.class.getCanonicalName().replace('.','/');

    public OverflowInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
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

			insertBranch(Opcodes.IFGT, "java/lang/ArithmeticException");

		case Opcodes.IDIV:
			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"overflowDistance", "(III)I", false);
			insertBranch(Opcodes.IFGT, "java/lang/ArithmeticException");

			break;

		case Opcodes.FADD:
		case Opcodes.FSUB:
		case Opcodes.FMUL:

			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"underflowDistance", "(FFI)I", false);
			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");

		case Opcodes.FDIV:
			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    CHECKER,
					"overflowDistance", "(FFI)I", false);
			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");
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

			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");

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

			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");
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

			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");

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

			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");
			break;
		}

	}
}
