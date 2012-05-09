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

	private final String className;

	/**
	 * @param cv
	 */
	public YieldAtLineNumberClassAdapter(ClassVisitor cv, String className) {
		super(Opcodes.ASM4, cv);
		this.className = className;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, final String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new YieldAtLineNumberMethodAdapter(mv, className, name);
	}
}
