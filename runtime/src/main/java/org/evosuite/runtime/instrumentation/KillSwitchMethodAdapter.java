package org.evosuite.runtime.instrumentation;

import org.evosuite.runtime.thread.KillSwitchHandler;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Add a kill switch call at each line statement and before each jump
 *  
 * @author arcuri
 *
 */
public class KillSwitchMethodAdapter extends MethodVisitor {

	public KillSwitchMethodAdapter(MethodVisitor mv, String methodName, String desc) {
		super(Opcodes.ASM5, mv);
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		int maxNum = 3;
		super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
	}
		
	@Override
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		addInstrumentation();
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label label) {
		addInstrumentation(); //add instrumentation before of the jump
		super.visitJumpInsn(opcode, label);
	}
	
	private void addInstrumentation(){
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				KillSwitchHandler.class.getName().replace('.', '/'),
                "killIfTimeout", "()V" , false);
	}
}
