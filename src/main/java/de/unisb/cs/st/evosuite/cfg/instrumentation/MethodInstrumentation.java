/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import org.objectweb.asm.tree.MethodNode;


/**
 * An interface which criterions (like defUse, concurrency, LCSAJs) can use to instrument the code of methods
 * @author Sebastian Steenbuck
 *
 */
public interface MethodInstrumentation {
	
	/**
	 * 
	 * @param mn the ASM Node of the method
	 * @param graph the current CFG
	 * @param className the name of current class
	 * @param methodName the name of the current method. This name includes the description of the method and is therefore unique per class.
	 * @param access the access of the current method (see org.objectweb.asm.ClassAdapter#visitMethod(int access, String name,
	        String descriptor, String signature, String[] exceptions))
	 */
	public void analyze(MethodNode mn, String className, String methodName, int access);
	
	/**
	 * If this method returns true, the analyze method is also called on 
	 * public static void main() methods
	 * @return
	 */
	public boolean executeOnMainMethod();
	
	/**
	 * if this method returns true the analyze method is also called on methods which are excluded in
	 * CFGMethodAdapter.EXCLUDE
	 * @return
	 */
	public boolean executeOnExcludedMethods();
	
}
