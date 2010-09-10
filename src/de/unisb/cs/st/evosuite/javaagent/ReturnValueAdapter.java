package de.unisb.cs.st.evosuite.javaagent;

import org.apache.log4j.Logger;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ReturnValueAdapter extends MethodAdapter {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LineNumberMethodAdapter.class);
	
	private String fullMethodName;

	protected String className;

	protected String methodName;

	public ReturnValueAdapter(MethodVisitor mv, String className,
			String methodName, String desc) {
		super(mv);
		//super(mv, className, methodName, null, desc);
		fullMethodName = methodName + desc;
		this.methodName = methodName;
		this.className = className;
	}

	// primitive data types
	private enum PDType { LONG, INTEGER, FLOAT, DOUBLE };

	public void visitInsn(int opcode) {
		if (!methodName.equals("<clinit>")) {
			switch (opcode) {
			case Opcodes.IRETURN:
				callLogIReturn();
				break;
			case Opcodes.ARETURN:
				callLogAReturn();
				break;
			case Opcodes.ATHROW:
				break;
			case Opcodes.DRETURN:
				callLogDReturn();
				break;
			case Opcodes.FRETURN:
				callLogFReturn();
				break;
			case Opcodes.LRETURN:
				callLogLReturn();
				break;
			case Opcodes.RETURN:
				break;
			default:
				break;
			}
		}
		super.visitInsn(opcode);

	}
	
	private void callLogPrototype(String traceMethod, PDType type) {
		if (type != PDType.LONG && type != PDType.DOUBLE) {
			this.visitInsn(Opcodes.DUP);
			if (type == PDType.FLOAT) {
				this.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "floatToRawIntBits", "(F)I");
			}
		} else {
			this.visitInsn(Opcodes.DUP2);
			if (type == PDType.DOUBLE) {
				this.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "doubleToRawLongBits", "(D)J");
			}
			this.visitInsn(Opcodes.DUP2);
			this.visitIntInsn(Opcodes.BIPUSH, 32);
			this.visitInsn(Opcodes.LSHR);
			this.visitInsn(Opcodes.LXOR);
			this.visitInsn(Opcodes.L2I);
		}

		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		this.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				"returnValue", "(ILjava/lang/String;Ljava/lang/String;)V");		
	}

	private void callLogIReturn() {
		callLogPrototype("logIReturn", PDType.INTEGER);
	}

	private void callLogAReturn() {
		this.visitInsn(Opcodes.DUP);
		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		this.visitMethodInsn(Opcodes.INVOKESTATIC, "de/unisb/cs/st/evosuite/testcase/ExecutionTracer",
				"returnValue", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
	}


	private void callLogLReturn() {
		callLogPrototype("logLReturn", PDType.LONG);
	}

	private void callLogDReturn() {
		callLogPrototype("logDReturn", PDType.DOUBLE);
	}

	private void callLogFReturn() {
		callLogPrototype("logFReturn", PDType.FLOAT);
	}

}
