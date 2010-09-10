package de.unisb.cs.st.evosuite.javaagent;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;

public class ExecutionPathClassAdapter extends ClassAdapter {

	private Excludes e = Excludes.getTestExcludesInstance();
	
	private String className;

	private boolean exclude = false;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ExecutionPathClassAdapter.class);
	
	public ExecutionPathClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className.replace('/', '.');
		
		if (e.shouldExclude(this.className) || !(this.className.startsWith(Properties.PROJECT_PREFIX))) {
			exclude = true;
		} else {
			exclude = false;
		}
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * Set default (package) access rights to public access rights
	 */
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
//		if((access & Opcodes.ACC_PRIVATE) == 0 && (access & Opcodes.ACC_PROTECTED) == 0 && (access & Opcodes.ACC_PUBLIC) == 0) {
		if((access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~(Opcodes.ACC_PROTECTED);
		}
		return super.visitField(access, name, desc, signature, value);
	}
	
	/*
	 * Set default access rights to public access rights
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public MethodVisitor visitMethod(int methodAccess, String name,
			String descriptor, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor,
				signature, exceptions);
		if (!exclude) {
//			if((methodAccess & Opcodes.ACC_PRIVATE) == 0 && (methodAccess & Opcodes.ACC_PROTECTED) == 0 && (methodAccess & Opcodes.ACC_PUBLIC) == 0) {
			if((methodAccess & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
				methodAccess = methodAccess | Opcodes.ACC_PUBLIC;
				methodAccess = methodAccess & ~Opcodes.ACC_PROTECTED;
			}
		}
		
		// Don't touch bridge and synthetic methods
		if( (methodAccess & Opcodes.ACC_SYNTHETIC) > 0 || (methodAccess & Opcodes.ACC_BRIDGE) > 0) {
			return mv;
		}

		if (!exclude) {
			mv = new MethodEntryAdapter(mv, methodAccess, className, name, descriptor);
			mv = new LineNumberMethodAdapter(mv, className, name, descriptor);
			//mv = new StringReplacementMethodAdapter(methodAccess, descriptor, mv);
			//mv = new ReturnValueAdapter(mv, className, name, descriptor);
		}
		return mv;
	}
	
}
