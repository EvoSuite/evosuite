package org.evosuite.instrumentation.error;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ErrorBranchInstrumenter {

	protected ErrorConditionMethodAdapter mv;
	
	protected String methodName;
	
	public ErrorBranchInstrumenter(ErrorConditionMethodAdapter mv) {
		this.mv = mv;
		this.methodName = mv.getMethodName();
	}
	
	public Map<Integer, Integer> getMethodCallee(String desc) {
		Type[] args = Type.getArgumentTypes(desc);
		Map<Integer, Integer> to = new HashMap<Integer, Integer>();
		for (int i = args.length - 1; i >= 0; i--) {
			int loc = mv.newLocal(args[i]);
			mv.storeLocal(loc);
			to.put(i, loc);
		}

		mv.dup();//callee
		return to;
	}
	
	public void restoreMethodParameters(Map<Integer, Integer> to, String desc) {
		Type[] args = Type.getArgumentTypes(desc);

		for (int i = 0; i < args.length; i++) {
			mv.loadLocal(to.get(i));
		}	
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

	protected void insertBranchWithoutTag(int opcode, String exception) {
		Label origTarget = new Label();
		mv.visitJumpInsn(opcode, origTarget);
		mv.visitTypeInsn(Opcodes.NEW, exception);
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, exception,
		                      "<init>", "()V");
		mv.visitInsn(Opcodes.ATHROW);
		mv.visitLabel(origTarget);
	}
	
	protected void tagBranchStart() {
		mv.tagBranch();
	}

	protected void tagBranchEnd() {
		mv.tagBranchExit();
	}

}
