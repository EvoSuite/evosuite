package de.unisb.cs.st.evosuite.javaagent;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LineNumberMethodAdapter extends MethodAdapter {

	public static int branch_id = 0;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LineNumberMethodAdapter.class);
	
	private String fullMethodName;
	
	private String className;
	
	int current_line = 0;
	
	public LineNumberMethodAdapter(MethodVisitor mv, String className,
			String methodName, String desc) {
		super(mv);
//		super(mv, className, methodName, null, desc);
		fullMethodName = methodName + desc;
		this.className = className;
		//logger.info("Created ExecutionPathAdapter");
	}

	
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		this.visitLdcInsn(line);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				"passedLine", "(Ljava/lang/String;Ljava/lang/String;I)V");
		current_line = line;
		//logger.info("EPA: Visited Line");

	}
	/*
	public void visitLabel(Label l) {
		super.visitLabel(l);
		this.visitLdcInsn(l.toString());
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/javalanche/tester/ExecutionTracer",
				"passedLabel", "(Ljava/lang/String;)V");
	}
	*/
	
/*
	public void visitCode() {
		super.visitCode();
		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/javalanche/tester/ExecutionTracer",
				"enteredMethod", "(Ljava/lang/String;Ljava/lang/String;)V");
	}

	public void visitEnd() {
		super.visitEnd();
		logger.trace("Instrumented "+className+"."+fullMethodName);
	}
	*/
	
	/*
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		//this.visitLdcInsn(owner);
		//this.visitLdcInsn(name+desc);
		//mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/javalanche/tester/ExecutionTracer",
		//		"enteredMethod", "(Ljava/lang/String;Ljava/lang/String;)V");
		mv.visitMethodInsn(opcode, owner, name, desc);
		this.visitLdcInsn(owner);
		this.visitLdcInsn(name+desc);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/javalanche/tester/ExecutionTracer",
				"leftMethod", "(Ljava/lang/String;Ljava/lang/String;)V");
	}
	*/
	
	/*
	public void visitInsn(int opcode) {
		switch(opcode) {
		case Opcodes.ARETURN:
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.FRETURN:
		case Opcodes.DRETURN:
		case Opcodes.RETURN:
		case Opcodes.ATHROW:
			this.visitLdcInsn(className);
			this.visitLdcInsn(fullMethodName);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/javalanche/tester/ExecutionTracer",
					"leftMethod", "(Ljava/lang/String;Ljava/lang/String;)V");		
		}
		mv.visitInsn(opcode);
	}
	*/
}
