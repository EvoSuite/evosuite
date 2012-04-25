/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author fraser
 * 
 */
public class AnnotatedClassNode extends ClassNode {
	@Override
	public MethodVisitor visitMethod(final int access, final String name,
	        final String desc, final String signature, final String[] exceptions) {
		MethodNode mn = new AnnotatedMethodNode(access, name, desc, signature, exceptions);
		methods.add(mn);
		return mn;
	}
}
