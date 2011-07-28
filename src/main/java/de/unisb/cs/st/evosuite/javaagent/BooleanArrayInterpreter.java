/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

/**
 * @author Gordon Fraser
 * 
 */
public class BooleanArrayInterpreter extends BasicInterpreter {

	protected static Logger logger = LoggerFactory.getLogger(BooleanArrayInterpreter.class);

	public final static BasicValue BYTE = new BasicValue(null);

	public final static BasicValue BOOLEAN = new BasicValue(null);

	public final static BasicValue BOOLEAN_ARRAY = new BasicValue(null);

	public final static BasicValue BYTE_ARRAY = new BasicValue(null);

	public final static BasicValue INT_ARRAY = new BasicValue(null);

	@Override
	public Value newValue(Type type) {
		if (type == null) {
			return BasicValue.UNINITIALIZED_VALUE;
		}
		switch (type.getSort()) {
		case Type.ARRAY:
			String desc = type.getDescriptor();
			if (desc.equals("[Z"))
				return BOOLEAN_ARRAY;
			else if (desc.equals("[I"))
				return INT_ARRAY;
			else if (desc.equals("[B"))
				return BYTE_ARRAY;
			else
				return super.newValue(type);
		default:
			return super.newValue(type);
		}
	}

	/*
		@Override
		public Value newOperation(AbstractInsnNode insn) throws AnalyzerException {

			if (insn.getOpcode() == BIPUSH) {
				return BYTE;
			} else if (insn.getOpcode() == GETSTATIC) {
				FieldInsnNode fn = (FieldInsnNode) insn;
				if (Type.getType(fn.desc).equals(Type.BOOLEAN_TYPE)) {
					return BOOLEAN;
				} else if (fn.desc.equals("[Z")) {
					return BOOLEAN_ARRAY;
				} else if (fn.desc.equals("[B")) {
					return BYTE_ARRAY;
				} else if (fn.desc.equals("[I")) {
					return INT_ARRAY;
				} else if (Type.getType(fn.desc).equals(Type.BYTE_TYPE)) {
					return BYTE;
				} else
					return super.newOperation(insn);
			} else {
				return super.newOperation(insn);
			}
		}

		@Override
		public Value unaryOperation(AbstractInsnNode insn, Value value)
		        throws AnalyzerException {
			if (insn.getOpcode() == NEWARRAY) {
				IntInsnNode in = (IntInsnNode) insn;
				if (in.operand == Opcodes.T_BOOLEAN) {
					return BOOLEAN_ARRAY;
				} else if (in.operand == Opcodes.T_BYTE) {
					return BYTE_ARRAY;
				} else if (in.operand == Opcodes.T_INT) {
					return INT_ARRAY;
				} else {
					return super.unaryOperation(insn, value);
				}
			} else if (insn.getOpcode() == ANEWARRAY) {
				TypeInsnNode tn = (TypeInsnNode) insn;
				if (tn.desc.equals("[Z")) {
					return BOOLEAN_ARRAY;
				} else if (tn.desc.equals("[B")) {
					return BYTE_ARRAY;
				} else if (tn.desc.equals("[I")) {
					return INT_ARRAY;
				} else {
					return super.unaryOperation(insn, value);
				}
			} else if (insn.getOpcode() == GETFIELD || insn.getOpcode() == GETSTATIC) {
				FieldInsnNode fn = (FieldInsnNode) insn;
				if (Type.getType(fn.desc).equals(Type.BOOLEAN_TYPE)) {
					return BOOLEAN;
				} else if (fn.desc.equals("[Z")) {
					return BOOLEAN_ARRAY;
				} else if (fn.desc.equals("[B")) {
					return BYTE_ARRAY;
				} else if (fn.desc.equals("[I")) {
					return INT_ARRAY;
				} else if (Type.getType(fn.desc).equals(Type.BYTE_TYPE)) {
					return BYTE;
				} else
					return super.unaryOperation(insn, value);
			} else if (insn.getOpcode() == CHECKCAST) {
				TypeInsnNode tn = (TypeInsnNode) insn;
				if (Type.getType(tn.desc).equals(Type.BOOLEAN_TYPE)) {
					return BOOLEAN;
				} else if (tn.desc.equals("[Z")) {
					return BOOLEAN_ARRAY;
				} else if (tn.desc.equals("[I")) {
					return INT_ARRAY;
				} else if (tn.desc.equals("[B")) {
					return BYTE_ARRAY;
				} else {
					return super.unaryOperation(insn, value);
				}
			} else {
				return super.unaryOperation(insn, value);
			}
		}
		*/

	@SuppressWarnings("rawtypes")
	@Override
	public Value naryOperation(AbstractInsnNode insn, List values)
	        throws AnalyzerException {
		if (insn.getOpcode() == INVOKESTATIC || insn.getOpcode() == INVOKEVIRTUAL
		        || insn.getOpcode() == INVOKEINTERFACE) {
			MethodInsnNode mn = (MethodInsnNode) insn;

			if (Type.getReturnType(mn.desc).equals(Type.BOOLEAN_TYPE)) {
				return BOOLEAN;
			} else if (Type.getReturnType(mn.desc).equals("[Z")) {
				return BOOLEAN_ARRAY;
			} else if (Type.getReturnType(mn.desc).equals("[B")) {
				return BYTE_ARRAY;
			} else if (Type.getReturnType(mn.desc).equals("[I")) {
				return INT_ARRAY;
			} else if (Type.getReturnType(mn.desc).equals(Type.BYTE_TYPE)) {
				return BYTE;
			} else {
				if (mn.name.equals("clone") && mn.owner.equals("[I"))
					return INT_ARRAY;
				else if (mn.name.equals("clone") && mn.owner.equals("[Z"))
					return BOOLEAN_ARRAY;
				else if (mn.name.equals("clone") && mn.owner.equals("[B"))
					return BYTE_ARRAY;
				else {
					return super.naryOperation(insn, values);
				}
			}
		} else {
			return super.naryOperation(insn, values);
		}
	}
	/*
		@Override
		public Value copyOperation(AbstractInsnNode insn, Value value)
		        throws AnalyzerException {

			if (insn.getOpcode() == Opcodes.ALOAD) {
				VarInsnNode vn = (VarInsnNode) insn;
				// Check local variable array if the index of this value is a boolean array? Maybe it's in value?
			}
		}
		*/
}
