/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.ClassVisitor;

/**
 * @author fraser
 * 
 */
public interface ClassAdapterFactory {

	public ClassVisitor getVisitor(ClassVisitor cv, String className);

}
