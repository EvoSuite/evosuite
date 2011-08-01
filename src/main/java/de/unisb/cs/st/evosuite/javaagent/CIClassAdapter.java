package de.unisb.cs.st.evosuite.javaagent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

	private String className;
	
	public CIClassAdapter(ClassVisitor cv) {
		super(cv);
	}

	CIMethodAdapter mv = new CIMethodAdapter();
	private final Set<String> classesReferenced = new HashSet<String>();

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.className = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
	        String signature, Object value) {
		if (value != null)
			classesReferenced.addAll(Utils.classesDescFromString(value.toString()));
		classesReferenced.addAll(Utils.classesDescFromString(desc));
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, String[] exceptions) {
		if (exceptions != null)
			for (String e : exceptions)
				classesReferenced.add(e);

		classesReferenced.addAll(Utils.classesDescFromString(desc));
		return mv;
	}

	@Override
	public void visitEnd() {
		saveCItoFile(mv.getClassesReferenced());
		super.visitEnd();
	}

	private void saveCItoFile(Set<String> cr){
		try {
			cr.remove(className);
			FileWriter fw = new FileWriter("evosuite-files/" + className.replace("/", ".") + ".CIs");
			BufferedWriter bw = new BufferedWriter(fw);
			String lineSeparator = System.getProperty("line.separator");
			for(String s : cr)
				bw.write(s + lineSeparator);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}