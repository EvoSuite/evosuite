package org.evosuite.runtime.instrumentation;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.runtime.mock.MockFramework;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 
 * @author gordon
 *
 */
public class MethodCallReplacement {
	private final String className;
	
	private final String methodName;
	private final String desc;
	private final int origOpcode;

	private final String replacementClassName;
	private final String replacementMethodName;
	private final String replacementDesc;

	private final boolean popCallee;
	private final boolean popUninitialisedReference;

	/**
	 * 
	 * @param className
	 * @param methodName
	 * @param desc
	 * @param replacementClassName
	 * @param replacementMethodName
	 * @param replacementDesc
	 * @param pop
	 *            if {@code true}, then get rid of the receiver object from
	 *            the stack. This is needed when a non-static method is
	 *            replaced by a static one, unless you make the callee of
	 *            the original method a parameter of the static replacement
	 *            method
	 * @param pop2
	 *            is needed if you replace the super-constructor call
	 */
	public MethodCallReplacement(String className, String methodName, String desc, int opcode,
			String replacementClassName, String replacementMethodName,
			String replacementDesc, boolean pop, boolean pop2) {
		this.className = className;
		this.methodName = methodName;
		this.desc = desc;
		this.replacementClassName = replacementClassName;
		this.replacementMethodName = replacementMethodName;
		this.replacementDesc = replacementDesc;
		this.popCallee = pop;
		this.popUninitialisedReference = pop2;
		this.origOpcode = opcode;
	}

	public boolean isTarget(String owner, String name, String desc) {
		return className.equals(owner) && methodName.equals(name)
				&& this.desc.equals(desc);
	}

	public void insertMethodCall(MethodCallReplacementMethodAdapter mv, int opcode) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, MockFramework.class.getCanonicalName().replace('.', '/'), "isEnabled", "()Z", false);
		Label origCallLabel = new Label();
		Label afterOrigCallLabel = new Label();

		Label annotationStartTag = new AnnotatedLabel(true, true);
		annotationStartTag.info = Boolean.TRUE;			
		mv.visitLabel(annotationStartTag);
		mv.visitJumpInsn(Opcodes.IFEQ, origCallLabel);
		Label annotationEndTag = new AnnotatedLabel(true, false);
		annotationEndTag.info = Boolean.FALSE;			
		mv.visitLabel(annotationEndTag);

		if (popCallee) {
			Type[] args = Type.getArgumentTypes(desc);
			Map<Integer, Integer> to = new HashMap<Integer, Integer>();
			for (int i = args.length - 1; i >= 0; i--) {
				int loc = mv.newLocal(args[i]);
				mv.storeLocal(loc);
				to.put(i, loc);
			}

			mv.pop();//callee
			if (popUninitialisedReference)
				mv.pop();

			for (int i = 0; i < args.length; i++) {
				mv.loadLocal(to.get(i));
			}
		}
		mv.visitMethodInsn(opcode, replacementClassName, replacementMethodName,
				replacementDesc, false);
		mv.visitJumpInsn(Opcodes.GOTO, afterOrigCallLabel);
		mv.visitLabel(origCallLabel);
		mv.getNextVisitor().visitMethodInsn(origOpcode, className, methodName, desc, false); // TODO: What is itf here?
		mv.visitLabel(afterOrigCallLabel);
	}

	public void insertConstructorCall(MethodCallReplacementMethodAdapter mv,
			MethodCallReplacement replacement, boolean isSelf) {
		Label origCallLabel = new Label();
		Label afterOrigCallLabel = new Label();
		
		if (!isSelf) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, MockFramework.class.getCanonicalName().replace('.', '/'), "isEnabled", "()Z", false);
			Label annotationStartTag = new AnnotatedLabel(true, true);
			annotationStartTag.info = Boolean.TRUE;			
			mv.visitLabel(annotationStartTag);
			mv.visitJumpInsn(Opcodes.IFEQ, origCallLabel);
			Label annotationEndTag = new AnnotatedLabel(true, false);
			annotationEndTag.info = Boolean.FALSE;			
			mv.visitLabel(annotationEndTag);

			Type[] args = Type.getArgumentTypes(desc);
			Map<Integer, Integer> to = new HashMap<Integer, Integer>();
			for (int i = args.length - 1; i >= 0; i--) {
				int loc = mv.newLocal(args[i]);
				mv.storeLocal(loc);
				to.put(i, loc);
			}

			mv.pop2();//uninitialized reference (which is duplicated)
			mv.newInstance(Type.getType(replacement.replacementClassName));
			mv.dup();

			for (int i = 0; i < args.length; i++) {
				mv.loadLocal(to.get(i));
			}
		}
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, replacementClassName,
				replacementMethodName, replacementDesc, false);
		if (!isSelf) {
			mv.visitJumpInsn(Opcodes.GOTO, afterOrigCallLabel);
			mv.visitLabel(origCallLabel);
			mv.getNextVisitor().visitMethodInsn(Opcodes.INVOKESPECIAL, className, methodName, desc, false);
			mv.visitLabel(afterOrigCallLabel);
		}
	}
	
	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodNameWithDesc() {
		return methodName+desc;
	}

}
