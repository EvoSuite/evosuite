package org.evosuite.runtime.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Add instrumentation in each method to set up a kill switch to stop
 * the SUT threads
 * 
 * @author arcuri
 *
 */
public class KillSwitchClassAdapter  extends ClassVisitor{

	public KillSwitchClassAdapter(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		// Don't touch bridge and synthetic methods
		if ((access & Opcodes.ACC_SYNTHETIC) > 0
				|| (access & Opcodes.ACC_BRIDGE) > 0) {
			return mv;
		}
		
		if (name.equals("<clinit>")){
			//should not stop a static initializer
			return mv;
		}


		return new KillSwitchMethodAdapter(mv, name, desc);
	}
}
