package org.evosuite.instrumentation.error;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class NullPointerExceptionInstrumentation extends ErrorBranchInstrumenter {

	public NullPointerExceptionInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		// If non-static, add a null check
		if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
			Type[] args = Type.getArgumentTypes(desc);
			Map<Integer, Integer> to = new HashMap<Integer, Integer>();
			for (int i = args.length - 1; i >= 0; i--) {
				int loc = mv.newLocal(args[i]);
				mv.storeLocal(loc);
				to.put(i, loc);
			}

			mv.dup();//callee
			insertBranch(Opcodes.IFNONNULL, "java/lang/NullPointerException");

			for (int i = 0; i < args.length; i++) {
				mv.loadLocal(to.get(i));
			}
		}	
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// If non-static, add a null check
		if (opcode == Opcodes.GETFIELD) {
			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFNONNULL, "java/lang/NullPointerException");

		} else if (opcode == Opcodes.PUTFIELD && !methodName.equals("<init>")) {
			if (Type.getType(desc).getSize() == 2) {
				// 2 words
				// v1 v2 v3
				mv.visitInsn(Opcodes.DUP2_X1);
				// v2 v3 v1 v2 v3

				mv.visitInsn(Opcodes.POP2);
				// v2 v3 v1

				mv.visitInsn(Opcodes.DUP_X2);
				// v1 v2 v3 v1

			} else {
				// 1 word
				mv.visitInsn(Opcodes.DUP2);
				//mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.POP);
			}
			insertBranch(Opcodes.IFNONNULL, "java/lang/NullPointerException");
		}
	}
}
