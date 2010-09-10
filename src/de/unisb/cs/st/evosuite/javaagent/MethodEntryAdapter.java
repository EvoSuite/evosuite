package de.unisb.cs.st.evosuite.javaagent;

import org.apache.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class MethodEntryAdapter extends AdviceAdapter{

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MethodEntryAdapter.class);
	
	String className;
	String methodName;
	String fullMethodName;
	
	public MethodEntryAdapter(MethodVisitor mv, int access, String className, String methodName,
			String desc) {
		super(mv, access, methodName, desc);
		this.className = className;
		this.methodName = methodName;
		this.fullMethodName = methodName+desc;
		//logger.info("AdviceAdapter for "+className+":"+methodName);
	}
	
	
	public void onMethodEnter() {
		mv.visitLdcInsn(className);
		mv.visitLdcInsn(fullMethodName);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				"enteredMethod", "(Ljava/lang/String;Ljava/lang/String;)V");
		//super.onMethodEnter();
	}

	public void onMethodExit(int opcode) {
		mv.visitLdcInsn(className);
		mv.visitLdcInsn(fullMethodName);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				"leftMethod", "(Ljava/lang/String;Ljava/lang/String;)V");
		//super.onMethodExit(opcode);
	}
}
