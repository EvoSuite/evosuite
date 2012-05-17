/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * @author Gordon Fraser
 * 
 */
public class ArrayAllocationLimitMethodAdapter extends GeneratorAdapter {

	/**
	 * 
	 */
	public ArrayAllocationLimitMethodAdapter(MethodVisitor mv, String className,
	        String methodName, int access, String desc) {
		super(Opcodes.ASM4, mv, access, methodName, desc);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitIntInsn(int, int)
	 */
	@Override
	public void visitIntInsn(int opcode, int operand) {
		if (opcode == Opcodes.NEWARRAY) {
			Label origTarget = new Label();
			visitInsn(Opcodes.DUP);
			visitFieldInsn(Opcodes.GETSTATIC, "de/unisb/cs/st/evosuite/Properties",
			               "ARRAY_LIMIT", "I");
			super.visitJumpInsn(Opcodes.IF_ICMPLT, origTarget);
			super.visitTypeInsn(Opcodes.NEW,
			                    "de/unisb/cs/st/evosuite/testcase/TestCaseExecutor$TimeoutExceeded");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL,
			                      "de/unisb/cs/st/evosuite/testcase/TestCaseExecutor$TimeoutExceeded",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);

		}
		super.visitIntInsn(opcode, operand);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int, java.lang.String)
	 */
	@Override
	public void visitTypeInsn(int opcode, String type) {

		if (opcode == Opcodes.ANEWARRAY) {
			Label origTarget = new Label();
			visitInsn(Opcodes.DUP);
			visitFieldInsn(Opcodes.GETSTATIC, "de/unisb/cs/st/evosuite/Properties",
			               "ARRAY_LIMIT", "I");
			super.visitJumpInsn(Opcodes.IF_ICMPLT, origTarget);
			super.visitTypeInsn(Opcodes.NEW,
			                    "de/unisb/cs/st/evosuite/testcase/TestCaseExecutor$TimeoutExceeded");
			super.visitInsn(Opcodes.DUP);
			super.visitMethodInsn(Opcodes.INVOKESPECIAL,
			                      "de/unisb/cs/st/evosuite/testcase/TestCaseExecutor$TimeoutExceeded",
			                      "<init>", "()V");
			super.visitInsn(Opcodes.ATHROW);
			super.visitLabel(origTarget);

		}
		super.visitTypeInsn(opcode, type);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMultiANewArrayInsn(java.lang.String, int)
	 */
	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {

		Label origTarget = new Label();
		Label errorTarget = new Label();
		visitInsn(Opcodes.DUP);
		visitFieldInsn(Opcodes.GETSTATIC, "de/unisb/cs/st/evosuite/Properties",
		               "ARRAY_LIMIT", "I");
		super.visitJumpInsn(Opcodes.IF_ICMPGE, errorTarget);

		Map<Integer, Integer> to = new HashMap<Integer, Integer>();
		Type type = Type.getType(desc);
		for (int i = dims - 1; i >= 0; i--) {
			int loc = newLocal(type);
			storeLocal(loc);
			to.put(i, loc);
		}

		for (int i = 0; i < dims; i++) {
			loadLocal(to.get(i));
			dup();
			visitFieldInsn(Opcodes.GETSTATIC, "de/unisb/cs/st/evosuite/Properties",
			               "ARRAY_LIMIT", "I");
			super.visitJumpInsn(Opcodes.IF_ICMPGE, errorTarget);
		}
		goTo(origTarget);
		super.visitLabel(errorTarget);
		super.visitTypeInsn(Opcodes.NEW,
		                    "de/unisb/cs/st/evosuite/testcase/TestCaseExecutor$TimeoutExceeded");
		super.visitInsn(Opcodes.DUP);
		super.visitMethodInsn(Opcodes.INVOKESPECIAL,
		                      "de/unisb/cs/st/evosuite/testcase/TestCaseExecutor$TimeoutExceeded",
		                      "<init>", "()V");
		super.visitInsn(Opcodes.ATHROW);
		super.visitLabel(origTarget);

		super.visitMultiANewArrayInsn(desc, dims);
	}

}
