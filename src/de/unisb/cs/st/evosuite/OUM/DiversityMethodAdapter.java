/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author fraser
 *
 */
public class DiversityMethodAdapter extends MethodAdapter {

	public static Set<String> classes = new HashSet<String>();
	public static Set<String> calls = new HashSet<String>();
	
	/**
	 * @param arg0
	 */
	public DiversityMethodAdapter(MethodVisitor arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void	visitMethodInsn(int opcode, String owner, String name, String desc) {
		calls.add(owner+"."+name);
		classes.add(owner);
		super.visitMethodInsn(opcode, owner, name, desc);
	}
	
	public void	visitFieldInsn(int opcode, String owner, String name, String desc) {
		calls.add(owner+"."+name);
		classes.add(owner);
		super.visitFieldInsn(opcode, owner, name, desc);
	}



}
