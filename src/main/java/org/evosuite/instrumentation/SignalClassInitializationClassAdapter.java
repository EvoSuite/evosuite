package org.evosuite.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This adapter invokes the SignalClassInitializationClassAdapter
 * on the <clinit> method.
 * 
 * @author galeotti
 *
 */
public class SignalClassInitializationClassAdapter extends ClassVisitor {

	private final String className;

	public SignalClassInitializationClassAdapter(ClassVisitor visitor,
			String className) {
		super(Opcodes.ASM4, visitor);
		this.className = className;
	}

	@Override
	public MethodVisitor visitMethod(int methodAccess, String methodName,
			String descriptor, String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, methodName,
				descriptor, signature, exceptions);

		if (methodName.equals("<clinit>")) {

			SignalClassInitializationMethodAdapter staticResetMethodAdapter = new SignalClassInitializationMethodAdapter(
					className, methodName, mv);

			return staticResetMethodAdapter;
		} else {
			return mv;
		}
	}
}
