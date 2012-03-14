/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * @author fraser
 * 
 */
public class ErrorConditionMethodAdapter extends GeneratorAdapter {

	private final String className;

	private final String methodName;

	private final MethodVisitor next;

	private Frame[] frames;

	/**
     * 
     */
	public ErrorConditionMethodAdapter(MethodVisitor mv, String className,
	        String methodName, int access, String desc) {
		//super(Opcodes.ASM4, mv, access, methodName, desc);
		super(Opcodes.ASM4, new MethodNode(access, methodName, desc, null, null), access,
		        methodName, desc);
		this.className = className;
		this.methodName = methodName;
		next = mv;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// If non-static, add a null check
		if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
			Type[] args = Type.getArgumentTypes(desc);
			Map<Integer, Integer> to = new HashMap<Integer, Integer>();
			for (int i = args.length - 1; i >= 0; i--) {
				int loc = newLocal(args[i]);
				storeLocal(loc);
				to.put(i, loc);
			}

			dup();//callee
			Label origTarget = new Label();
			super.visitJumpInsn(Opcodes.IFNONNULL, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL,
			                      "java/lang/NullPointerException", "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);

			for (int i = 0; i < args.length; i++) {
				loadLocal(to.get(i));
			}
		}
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// If non-static, add a null check
		if (opcode == Opcodes.GETFIELD) {
			Label origTarget = new Label();
			super.visitInsn(Opcodes.DUP);
			super.visitJumpInsn(Opcodes.IFNONNULL, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL,
			                      "java/lang/NullPointerException", "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);
		} else if (opcode == Opcodes.PUTFIELD && !methodName.equals("<init>")) {
			Label origTarget = new Label();
			if (Type.getType(desc).getSize() == 2) {
				// 2 words
				// v1 v2 v3
				super.visitInsn(Opcodes.DUP2_X1);
				// v2 v3 v1 v2 v3

				super.visitInsn(Opcodes.POP2);
				// v2 v3 v1

				super.visitInsn(Opcodes.DUP_X2);
				// v1 v2 v3 v1

			} else {
				// 1 word
				super.visitInsn(Opcodes.DUP2);
				//super.visitInsn(Opcodes.SWAP);
				super.visitInsn(Opcodes.POP);
			}
			super.visitJumpInsn(Opcodes.IFNONNULL, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL,
			                      "java/lang/NullPointerException", "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int, java.lang.String)
	 */
	@Override
	public void visitTypeInsn(int opcode, String type) {

		if (opcode == Opcodes.CHECKCAST) {
			Label origTarget = new Label();
			super.visitInsn(Opcodes.DUP);
			super.visitTypeInsn(Opcodes.INSTANCEOF, type);
			super.visitJumpInsn(Opcodes.IFNE, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ClassCastException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ClassCastException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);

		}
		super.visitTypeInsn(opcode, type);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
	 */
	@Override
	public void visitInsn(int opcode) {
		// Check *DIV for divisonbyzero
		if (opcode == Opcodes.IDIV) {
			Label origTarget = new Label();
			super.visitInsn(Opcodes.DUP);
			super.visitJumpInsn(Opcodes.IFNE, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArithmeticException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ArithmeticException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);
		} else if (opcode == Opcodes.FDIV) {
			Label origTarget = new Label();
			super.visitInsn(Opcodes.DUP);
			super.visitLdcInsn(0F);
			super.visitInsn(Opcodes.FCMPL);
			super.visitJumpInsn(Opcodes.IFNE, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArithmeticException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ArithmeticException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);
		} else if (opcode == Opcodes.LDIV || opcode == Opcodes.DDIV) {
			Label origTarget = new Label();
			super.visitInsn(Opcodes.DUP2);
			if (opcode == Opcodes.LDIV) {
				super.visitLdcInsn(0L);
				super.visitInsn(Opcodes.LCMP);
			} else {
				super.visitLdcInsn(0.0);
				super.visitInsn(Opcodes.DCMPL);
			}
			super.visitJumpInsn(Opcodes.IFNE, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArithmeticException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ArithmeticException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);

		}

		// Check array accesses
		if (opcode == Opcodes.IALOAD || opcode == Opcodes.BALOAD
		        || opcode == Opcodes.CALOAD || opcode == Opcodes.SALOAD
		        || opcode == Opcodes.LALOAD || opcode == Opcodes.FALOAD
		        || opcode == Opcodes.DALOAD) {
			Label origTarget = new Label();
			super.visitInsn(Opcodes.DUP2);
			super.visitInsn(Opcodes.SWAP);
			//super.visitInsn(Opcodes.POP);
			super.visitInsn(Opcodes.ARRAYLENGTH);
			super.visitJumpInsn(Opcodes.IF_ICMPLT, origTarget);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArrayIndexOutOfBoundsException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL,
			                      "java/lang/ArrayIndexOutOfBoundsException", "<init>",
			                      "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);
		}

		// Overflow checks
		switch (opcode) {
		case Opcodes.IADD:
		case Opcodes.ISUB:
		case Opcodes.IMUL:
			Label origTarget1 = new Label();
			super.visitInsn(Opcodes.DUP2);
			super.visitLdcInsn(opcode);
			super.visitMethodInsn(Opcodes.INVOKESTATIC,
			                      "de/unisb/cs/st/evosuite/javaagent/ErrorConditionChecker",
			                      "overflowDistance", "(III)I");

			super.visitJumpInsn(Opcodes.IFGT, origTarget1);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArithmeticException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ArithmeticException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget1);
			break;
		case Opcodes.FADD:
		case Opcodes.FSUB:
		case Opcodes.FMUL:
			Label origTarget2 = new Label();
			super.visitInsn(Opcodes.DUP2);
			super.visitLdcInsn(opcode);
			super.visitMethodInsn(Opcodes.INVOKESTATIC,
			                      "de/unisb/cs/st/evosuite/javaagent/ErrorConditionChecker",
			                      "overflowDistance", "(FFI)I");

			super.visitJumpInsn(Opcodes.IFGE, origTarget2);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArithmeticException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ArithmeticException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget2);

			break;
		case Opcodes.DADD:
		case Opcodes.DSUB:
		case Opcodes.DMUL:
			Label origTarget3 = new Label();
			int loc = newLocal(Type.DOUBLE_TYPE);
			storeLocal(loc);
			super.visitInsn(Opcodes.DUP2);
			loadLocal(loc);
			super.visitInsn(Opcodes.DUP2_X2);
			super.visitLdcInsn(opcode);
			super.visitMethodInsn(Opcodes.INVOKESTATIC,
			                      "de/unisb/cs/st/evosuite/javaagent/ErrorConditionChecker",
			                      "overflowDistance", "(DDI)I");

			super.visitJumpInsn(Opcodes.IFGE, origTarget3);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArithmeticException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ArithmeticException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget3);
			break;

		case Opcodes.LADD:
		case Opcodes.LSUB:
		case Opcodes.LMUL:
			Label origTarget4 = new Label();
			int loc2 = newLocal(Type.LONG_TYPE);
			storeLocal(loc2);
			super.visitInsn(Opcodes.DUP2);
			loadLocal(loc2);
			super.visitInsn(Opcodes.DUP2_X2);
			super.visitLdcInsn(opcode);
			super.visitMethodInsn(Opcodes.INVOKESTATIC,
			                      "de/unisb/cs/st/evosuite/javaagent/ErrorConditionChecker",
			                      "overflowDistance", "(JJI)I");

			super.visitJumpInsn(Opcodes.IFGE, origTarget4);
			super.visitTypeInsn(Opcodes.NEW, "java/lang/ArithmeticException");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ArithmeticException",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget4);
			break;
		}
		// Check I*, D*, F*, L* for overflows

		super.visitInsn(opcode);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitCode()
	 */
	@Override
	public void visitCode() {
		MethodNode mn = (MethodNode) mv;
		try {
			Analyzer a = new Analyzer(new ThisInterpreter());
			a.analyze(className, mn);
			frames = a.getFrames();
		} catch (Exception e) {
			frames = new Frame[0];
		}
		super.visitCode();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitEnd()
	 */
	@Override
	public void visitEnd() {
		MethodNode mn = (MethodNode) mv;
		mn.accept(next);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack + 4, maxLocals);
	}

}
