package de.unisb.cs.st.evosuite.setup;

import java.lang.reflect.Modifier;

import mockit.external.asm.Type;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Visits given class in order to collect information about class hierarchies
 * 
 * @author Gordon Fraser
 * 
 */
public class InheritanceClassAdapter extends ClassAdapter {

	private String className = null;

	public InheritanceClassAdapter(ClassVisitor cv) {
		super(cv);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {

		this.className = name.replace("/", ".");

		if (superName != null)
			ClusterAnalysis.addSubclass(superName.replace("/", "."), className);
		for (String interfaceName : interfaces)
			ClusterAnalysis.addSubclass(interfaceName.replace("/", "."), className);

		if (Modifier.isAbstract(access) || Modifier.isInterface(access))
			ClusterAnalysis.addAbstract(className);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
	        String signature, Object value) {
		ClusterAnalysis.addGenerator(className, Type.getType(desc).getClassName());
		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public MethodVisitor visitMethod(int arg0, String name, String desc,
	        String signature, String[] arg4) {
		Type ret = Type.getReturnType(desc);
		while (ret.getSort() == Type.ARRAY)
			ret = ret.getElementType();

		if (ret.getSort() == Type.OBJECT)
			ClusterAnalysis.addParameter(className, ret.getClassName());

		for (Type type : Type.getArgumentTypes(desc)) {
			while (type.getSort() == Type.ARRAY)
				type = type.getElementType();

			if (type.getSort() == Type.OBJECT)
				ClusterAnalysis.addParameter(className, type.getClassName());
		}

		if (name.equals("<init>")) {
			ClusterAnalysis.addGenerator(className, className);
		} else if (ret.getSort() != Type.VOID)
			ClusterAnalysis.addGenerator(className, ret.getClassName());

		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitEnd()
	 */
	@Override
	public void visitEnd() {
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	@Override
	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {

	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitOuterClass(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitOuterClass(String arg0, String arg1, String arg2) {
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitAnnotation(java.lang.String, boolean)
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return null;
	}
}