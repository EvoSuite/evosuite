package org.evosuite.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class RegisterObjectForDeterministicHashCodeVisitor extends AdviceAdapter {


	
	
	protected RegisterObjectForDeterministicHashCodeVisitor(MethodVisitor mv, int access, String name, String desc) {
		super(Opcodes.ASM4, mv, access, name, desc);
	}

	@Override
	public void visitInsn(int opcode) {
		// We don't use the AdviceAdapter here because this is not properly initialised if the constructor is
		// exited with an exception
		if(opcode == Opcodes.RETURN) {
			loadThis();
			invokeStatic(Type.getType(org.evosuite.runtime.System.class), Method.getMethod("void registerObjectForIdentityHashCode(Object)"));						
		}
		super.visitInsn(opcode);
	}	
}
