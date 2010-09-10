/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Gordon Fraser
 *
 */
public class ObjectCallAdapter extends MethodAdapter {

	protected static Logger logger = Logger.getLogger(ObjectCallAdapter.class);
	
	Map<String, String> descriptors = null;

	public ObjectCallAdapter(MethodVisitor mv, Map<String, String> descriptors) {
		super(mv);
		this.descriptors = descriptors;
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if(descriptors.containsKey(name+desc)) {
			logger.info("Replacing call to "+name+desc+" with "+descriptors.get(name+desc));
			super.visitMethodInsn(opcode, owner, name, descriptors.get(name+desc));
		} else {
			super.visitMethodInsn(opcode, owner, name, desc);			
		}
	}

}
