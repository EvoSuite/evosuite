package org.evosuite.instrumentation.error;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class CastErrorInstrumentation extends ErrorBranchInstrumenter {

	public CastErrorInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {

		if (opcode == Opcodes.CHECKCAST) {
			Label origTarget = new Label();
			// Label origTarget = new AnnotatedLabel();
			// origTarget.info = Boolean.FALSE;
			mv.visitInsn(Opcodes.DUP);
			mv.tagBranch();
			mv.visitJumpInsn(Opcodes.IFNULL, origTarget);
			mv.visitInsn(Opcodes.DUP);
			mv.visitTypeInsn(Opcodes.INSTANCEOF, type);
			mv.tagBranch();
			mv.visitJumpInsn(Opcodes.IFNE, origTarget);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/ClassCastException");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ClassCastException",
			                      "<init>", "()V");
			mv.visitInsn(Opcodes.ATHROW);
			mv.visitLabel(origTarget);
			mv.tagBranchExit();
		}
	}
}
