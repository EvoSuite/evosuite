/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.instrumentation;

import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BooleanArrayInterpreter class.</p>
 *
 * @author Gordon Fraser
 */
public class BooleanArrayInterpreter extends BasicInterpreter {

	/** Constant <code>logger</code> */
	protected static Logger logger = LoggerFactory.getLogger(BooleanArrayInterpreter.class);

	/** Constant <code>BYTE</code> */
	public final static BasicValue BYTE = new BasicValue(null);

	/** Constant <code>BOOLEAN</code> */
	public final static BasicValue BOOLEAN = new BasicValue(null);

	/** Constant <code>BOOLEAN_ARRAY</code> */
	public final static BasicValue BOOLEAN_ARRAY = new BasicValue(null);

	/** Constant <code>BYTE_ARRAY</code> */
	public final static BasicValue BYTE_ARRAY = new BasicValue(null);

	/** Constant <code>INT_ARRAY</code> */
	public final static BasicValue INT_ARRAY = new BasicValue(null);

	/** {@inheritDoc} */
	@Override
	public BasicValue newValue(Type type) {
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

	/** {@inheritDoc} */
	@SuppressWarnings("rawtypes")
	@Override
	public BasicValue naryOperation(AbstractInsnNode insn, List values)
	        throws AnalyzerException {
		if (insn.getOpcode() == INVOKESTATIC || insn.getOpcode() == INVOKEVIRTUAL
		        || insn.getOpcode() == INVOKEINTERFACE) {
			MethodInsnNode mn = (MethodInsnNode) insn;

			if (Type.getReturnType(mn.desc).equals(Type.BOOLEAN_TYPE)) {
				return BOOLEAN;
			} else if (mn.desc.equals("[Z")) {
				return BOOLEAN_ARRAY;
			} else if (mn.desc.equals("[B")) {
				return BYTE_ARRAY;
			} else if (mn.desc.equals("[I")) {
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
