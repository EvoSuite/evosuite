package org.evosuite.instrumentation.error;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class ErrorBranchInstrumenter {

	protected ErrorConditionMethodAdapter mv;
	
	protected String methodName;
	
	public ErrorBranchInstrumenter(ErrorConditionMethodAdapter mv) {
		this.mv = mv;
		this.methodName = mv.getMethodName();
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		
	}
	
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		
	}
	
	public void visitTypeInsn(int opcode, String type) {
				
	}
	
	public void visitInsn(int opcode) {

	}
	
	public void visitIntInsn(int opcode,
            int operand) {
		
	}
	
	protected void insertBranch(int opcode, String exception) {
		Label origTarget = new Label();
		mv.tagBranch();
		mv.visitJumpInsn(opcode, origTarget);
		mv.visitTypeInsn(Opcodes.NEW, exception);
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, exception,
		                      "<init>", "()V");
		mv.visitInsn(Opcodes.ATHROW);
		mv.visitLabel(origTarget);
		mv.tagBranchExit();

	}
	
}
