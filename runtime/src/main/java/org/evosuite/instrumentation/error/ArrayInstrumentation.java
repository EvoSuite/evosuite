package org.evosuite.instrumentation.error;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ArrayInstrumentation extends ErrorBranchInstrumenter {

	public ArrayInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}
	
	@Override
	public void visitInsn(int opcode) {
		// Check array accesses
		if (opcode == Opcodes.IALOAD || opcode == Opcodes.BALOAD
				|| opcode == Opcodes.CALOAD || opcode == Opcodes.SALOAD
				|| opcode == Opcodes.LALOAD || opcode == Opcodes.FALOAD
				|| opcode == Opcodes.DALOAD || opcode == Opcodes.AALOAD) {

			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFGE, "java/lang/ArrayIndexOutOfBoundsException");

			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.SWAP);
			//mv.visitInsn(Opcodes.POP);
			mv.visitInsn(Opcodes.ARRAYLENGTH);
			insertBranch(Opcodes.IF_ICMPLT, "java/lang/ArrayIndexOutOfBoundsException");
	
			
		} else if (opcode == Opcodes.IASTORE || opcode == Opcodes.BASTORE
				|| opcode == Opcodes.CASTORE || opcode == Opcodes.SASTORE
				|| opcode == Opcodes.AASTORE || opcode == Opcodes.LASTORE
				|| opcode == Opcodes.FASTORE || opcode == Opcodes.DASTORE) {

			int loc = 0;
			if (opcode == Opcodes.IASTORE)
				loc = mv.newLocal(Type.INT_TYPE);
			else if (opcode == Opcodes.BASTORE)
				loc = mv.newLocal(Type.BYTE_TYPE);
			else if (opcode == Opcodes.CASTORE)
				loc = mv.newLocal(Type.CHAR_TYPE);
			else if (opcode == Opcodes.SASTORE)
				loc = mv.newLocal(Type.SHORT_TYPE);
			else if (opcode == Opcodes.AASTORE)
				loc = mv.newLocal(Type.getType(Object.class));
			else if (opcode == Opcodes.LASTORE)
				loc = mv.newLocal(Type.LONG_TYPE);
			else if (opcode == Opcodes.FASTORE)
				loc = mv.newLocal(Type.FLOAT_TYPE);
			else if (opcode == Opcodes.DASTORE)
				loc = mv.newLocal(Type.DOUBLE_TYPE);
			else
				throw new RuntimeException("Unknown type");
			mv.storeLocal(loc);

			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFGE, "java/lang/ArrayIndexOutOfBoundsException");

			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.SWAP);
			//mv.visitInsn(Opcodes.POP);
			mv.visitInsn(Opcodes.ARRAYLENGTH);
			insertBranch(Opcodes.IF_ICMPLT, "java/lang/ArrayIndexOutOfBoundsException");

			mv.loadLocal(loc);
		}
	}
	
	@Override
	public void visitIntInsn(int opcode, int operand) {
		if(opcode == Opcodes.NEWARRAY) {
			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFGE, "java/lang/NegativeArraySizeException");	
		}
	}
}
