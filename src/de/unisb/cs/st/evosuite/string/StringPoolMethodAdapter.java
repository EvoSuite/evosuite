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

	private StringPool string_pool = StringPool.getInstance();

	/**
	 * @param arg0
	 */
	public StringPoolMethodAdapter(MethodVisitor arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	
	public void visitLdcInsn(Object cst) {
		if(cst instanceof String) {
			string_pool.addString((String)cst);
		}
		super.visitLdcInsn(cst);
	}

}
