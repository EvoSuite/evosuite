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
	protected void onMethodExit(int opcode) {
		loadThis();
		invokeStatic(Type.getType(org.evosuite.runtime.System.class), Method.getMethod("void registerObjectForIdentityHashCode(Object)"));			
		super.onMethodExit(opcode);
	}
}
