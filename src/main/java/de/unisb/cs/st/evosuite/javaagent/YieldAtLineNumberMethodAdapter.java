/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fraser
 * 
 */
public class YieldAtLineNumberMethodAdapter extends MethodVisitor {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LineNumberMethodAdapter.class);

	private final String methodName;

	private boolean hadInvokeSpecial = false;

	int currentLine = 0;

	public YieldAtLineNumberMethodAdapter(MethodVisitor mv, String methodName) {
		super(Opcodes.ASM4, mv);
		this.methodName = methodName;
		if (!methodName.equals("<init>"))
			hadInvokeSpecial = true;
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		currentLine = line;

		if (methodName.equals("<clinit>"))
			return;

		if (!hadInvokeSpecial)
			return;

		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
		                   "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
		                   "checkTimeout", "()V");
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (opcode == Opcodes.INVOKESPECIAL) {
			if (methodName.equals("<init>"))
				hadInvokeSpecial = true;
		}
		super.visitMethodInsn(opcode, owner, name, desc);
	}
}
