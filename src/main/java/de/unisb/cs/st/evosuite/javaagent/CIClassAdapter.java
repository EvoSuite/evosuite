package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * Visits given class in order to collect information about classes used. 
 * 
 * @author Andrey Tarasevich
 *
 */
public class CIClassAdapter extends ClassAdapter {

	public CIClassAdapter(ClassVisitor cv) {
		super(cv);
	}
	CIMethodAdapter mv = new CIMethodAdapter();
	private Set<String> classesReferenced = new HashSet<String>(); 

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		if(value != null)
			classesReferenced.addAll(Utils.classesDescFromString(value.toString()));
		classesReferenced.addAll(Utils.classesDescFromString(desc));
		return null;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if(exceptions != null)
			for(String e : exceptions)
				classesReferenced.add(e);
		
		classesReferenced.addAll(Utils.classesDescFromString(desc));
		return mv;
	}
	
	@Override
	public void visitEnd() {
		classesReferenced.addAll(mv.getClassesReferenced());
		super.visitEnd();
	}
	
	public Set<String> getClassesReferenced() {
		return classesReferenced;
	}
}