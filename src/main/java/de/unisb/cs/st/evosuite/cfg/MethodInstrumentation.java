/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

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
	 * @param methodName the name of the current method
	 * @param access the access of the current method (see org.objectweb.asm.ClassAdapter#visitMethod(int access, String name,
	        String descriptor, String signature, String[] exceptions))
	 */
	public void analyze(MethodNode mn, Graph<CFGVertex, DefaultEdge> graph, String className, String methodName, int access);
	
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
