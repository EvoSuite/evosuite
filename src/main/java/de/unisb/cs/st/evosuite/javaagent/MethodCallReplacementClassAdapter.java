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
public class MethodCallReplacementClassAdapter extends ClassVisitor {

	private final String className;

	/**
     * 
     */
	public MethodCallReplacementClassAdapter(ClassVisitor cv, String className) {
		super(Opcodes.ASM4, cv);
		this.className = className;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new MethodCallReplacementMethodAdapter(mv, className, name, access, desc);
	}
}
