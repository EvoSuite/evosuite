/**
 * 
 */
package de.unisb.cs.st.evosuite.string;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Gordon Fraser
 *
 */
public class StringPoolMethodAdapter extends MethodAdapter {

	private PrimitivePool primitive_pool = PrimitivePool.getInstance();

	/**
	 * @param arg0
	 */
	public StringPoolMethodAdapter(MethodVisitor arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void visitLdcInsn(Object cst) {
		primitive_pool.add(cst);
		super.visitLdcInsn(cst);
	}

}
