/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author fraser
 *
 */
public class AccessibleClassAdapter extends ClassAdapter {

	
	/**
	 * @param arg0
	 */
	public AccessibleClassAdapter(ClassVisitor arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		if((access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
		}	
		super.visit(version, access, name, signature, superName, interfaces);
	}

	
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if((access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
			//System.out.println("Setting field to public: "+className);
		}
		
		return super.visitField(access, name, desc, signature, value);
	}
	
	
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, final String[] exceptions) {
		
		if((access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
		}
		
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		return mv;
	}
}
