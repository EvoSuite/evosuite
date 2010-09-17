/**
 * 
 */
package de.unisb.cs.st.evosuite.string;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;

/**
 * @author Gordon Fraser
 *
 */
public class StringClassAdapter extends ClassAdapter {
	
	private String className;
	
	private static String target_class = Properties.TARGET_CLASS;
	
	private Excludes e = Excludes.getInstance();
	
	private boolean exclude;
	
	private PrimitivePool primitive_pool = PrimitivePool.getInstance();
	/**
	 * @param arg0
	 */
	public StringClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className;
		String classNameWithDots = className.replace('/', '.');
		if (e.shouldExclude(classNameWithDots)) {
			exclude = true;
		} else {
			exclude = false;
		}	
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if(value instanceof String) {
			primitive_pool.add(value);
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

		String classNameWithDots = className.replace('/', '.');
		if (!exclude && (classNameWithDots.equals(target_class) || (classNameWithDots.startsWith(target_class+"$")))) {
			mv = new StringReplacementMethodAdapter(methodAccess, descriptor, mv);
		}
		mv = new StringPoolMethodAdapter(mv);
		
		return mv;
	}
}
