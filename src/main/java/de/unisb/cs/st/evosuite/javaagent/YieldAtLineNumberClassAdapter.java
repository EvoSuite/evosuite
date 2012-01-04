/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author fraser
 * 
 */
public class YieldAtLineNumberClassAdapter extends ClassAdapter {

	/**
	 * @param cv
	 */
	public YieldAtLineNumberClassAdapter(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, final String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new YieldAtLineNumberMethodAdapter(mv, name);
	}
}
