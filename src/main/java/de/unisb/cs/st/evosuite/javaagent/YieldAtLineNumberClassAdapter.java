/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author fraser
 * 
 */
public class YieldAtLineNumberClassAdapter extends ClassVisitor {

	/**
	 * @param cv
	 */
	public YieldAtLineNumberClassAdapter(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, final String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new YieldAtLineNumberMethodAdapter(mv, name);
	}
}
