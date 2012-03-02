/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author fraser
 * 
 */
public class ErrorConditionMethodAdapter extends MethodVisitor {

	private final String className;

	private final String methodName;

	/**
     * 
     */
	public ErrorConditionMethodAdapter(MethodVisitor mv, String className,
	        String methodName) {
		super(Opcodes.ASM4, mv);
		this.className = className;
		this.methodName = methodName;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// If non-static, add a null check
		if (opcode != Opcodes.INVOKESTATIC) {

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
		} else if (opcode == Opcodes.PUTFIELD) {
			Label origTarget = new Label();
			if (Type.getType(desc).getSize() == 2) {
				// 2 words
				// v1 v2 v3

				super.visitInsn(Opcodes.DUP_X2);
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

		// Check I*, D*, F*, L* for overflows

		super.visitInsn(opcode);
	}

}
