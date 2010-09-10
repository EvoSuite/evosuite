/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Gordon Fraser
 *
 */
public class StaticInitializationClassAdapter extends ClassAdapter {

	
	private String className;
	
//	private boolean exclude;

//	private Excludes e = Excludes.getInstance();

	public static List<String> static_classes = new ArrayList<String>();
	
	private static Logger logger = Logger.getLogger(StaticInitializationClassAdapter.class);
	
	//private List<String> instrument;
	
//	public CFGClassAdapter(ClassVisitor visitor, String className, List<String> instrument_methods) {
	public StaticInitializationClassAdapter (ClassVisitor visitor, String className) {
		super(visitor);
//		instrument = instrument_methods;
		this.className = className;
//		String classNameWithDots = className.replace('/', '.');
		/*
		if (e.shouldExclude(classNameWithDots) || EXCLUDES.contains(className)) {
			exclude = true;
		} else {
			exclude = false;
		}
		*/
	}

	public MethodVisitor visitMethod(int methodAccess, String name,
			String descriptor, String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor,
				signature, exceptions);
		if(name.equals("<clinit>")) {
			logger.info("Found static initializer in class "+className);
			MethodVisitor mv2 = super.visitMethod(methodAccess | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "__STATIC_RESET", descriptor, signature, exceptions);
			static_classes.add(className.replace('/', '.'));
			return new MultiMethodVisitor(mv2, mv);
		}
		return mv;
	}
}
