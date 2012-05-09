/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gordon
 * 
 */
public class ExplicitExceptionHandler extends MethodVisitor {

	private final String fullMethodName;

	private final String className;

	private boolean inErrorBranch = false;

	int currentLine = 0;

	public ExplicitExceptionHandler(MethodVisitor mv, String className,
	        String methodName, String desc) {
		super(Opcodes.ASM4, mv);
		fullMethodName = methodName + desc;
		this.className = className;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitLabel(org.objectweb.asm.Label)
	 */
	@Override
	public void visitLabel(Label label) {
		if (label instanceof AnnotatedLabel) {
			AnnotatedLabel l = (AnnotatedLabel) label;
			if (l.info == Boolean.TRUE) {
				inErrorBranch = true;
			} else {
				inErrorBranch = false;
			}
		}
		inErrorBranch = false;
		super.visitLabel(label);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
	 */
	@Override
	public void visitInsn(int opcode) {
		if (opcode == Opcodes.ATHROW && !inErrorBranch) {
			super.visitInsn(Opcodes.DUP);
			this.visitLdcInsn(className);
			this.visitLdcInsn(fullMethodName);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
			                   "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
			                   "exceptionThrown",
			                   "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
		}
		super.visitInsn(opcode);
	}
}
