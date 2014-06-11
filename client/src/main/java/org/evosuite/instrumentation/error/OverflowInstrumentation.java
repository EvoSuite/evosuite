package org.evosuite.instrumentation.error;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class OverflowInstrumentation extends ErrorBranchInstrumenter {

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
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"underflowDistance", "(III)I");

			insertBranch(Opcodes.IFGT, "java/lang/ArithmeticException");

		case Opcodes.IDIV:
			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"overflowDistance", "(III)I");
			insertBranch(Opcodes.IFGT, "java/lang/ArithmeticException");

			break;

		case Opcodes.FADD:
		case Opcodes.FSUB:
		case Opcodes.FMUL:

			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"underflowDistance", "(FFI)I");
			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");

		case Opcodes.FDIV:
			mv.visitInsn(Opcodes.DUP2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"overflowDistance", "(FFI)I");
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
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"underflowDistance", "(DDI)I");

			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");

		case Opcodes.DDIV:
			loc = mv.newLocal(Type.DOUBLE_TYPE);

			mv.storeLocal(loc);
			mv.visitInsn(Opcodes.DUP2);
			mv.loadLocal(loc);
			mv.visitInsn(Opcodes.DUP2_X2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"overflowDistance", "(DDI)I");

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
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"underflowDistance", "(JJI)I");

			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");

		case Opcodes.LDIV:

			loc2 = mv.newLocal(Type.LONG_TYPE);
			mv.storeLocal(loc2);
			mv.visitInsn(Opcodes.DUP2);
			mv.loadLocal(loc2);
			mv.visitInsn(Opcodes.DUP2_X2);
			mv.visitLdcInsn(opcode);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"org/evosuite/instrumentation/ErrorConditionChecker",
					"overflowDistance", "(JJI)I");

			insertBranch(Opcodes.IFGE, "java/lang/ArithmeticException");
			break;
		}

	}
}
