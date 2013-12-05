package org.evosuite.testcarver.instrument;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class JSRInlinerClassVisitor extends ClassVisitor {

	public JSRInlinerClassVisitor(ClassVisitor parent) {
		super(Opcodes.ASM4, parent);
	}
	
	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, final String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		mv = new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
		return mv;
	}
}
