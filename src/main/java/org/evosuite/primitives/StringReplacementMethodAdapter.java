/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.primitives;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

/**
 * @author Gordon Fraser
 * 
 */
public class StringReplacementMethodAdapter extends LocalVariablesSorter {

	protected static Logger logger = LoggerFactory.getLogger(StringReplacementMethodAdapter.class);

	private final Map<Integer, Integer> flags = new HashMap<Integer, Integer>();

	private int current_var = -1;

	private int current_write = -1;

	/**
	 * @param arg0
	 */
	public StringReplacementMethodAdapter(int access, String desc, MethodVisitor mv) {
		super(access, desc, mv);
		// TODO Auto-generated constructor stub
	}

	private void insertFlagCode() {
		// assign the return value to a new local variable
		int index = newLocal(Type.INT_TYPE);
		logger.debug("Inserting new variable with index " + index
		        + " as replacement for " + nextLocal);
		//flags.put(index, nextLocal);
		current_write = index;
		current_var = nextLocal;
		super.visitVarInsn(Opcodes.ISTORE, index);
		// put new variable on stack
		super.visitVarInsn(Opcodes.ILOAD, index);
		// then put a converted boolean on the stack
		Label l = new Label();
		Label l2 = new Label();

		super.visitJumpInsn(Opcodes.IFNE, l);
		super.visitInsn(Opcodes.ICONST_1);
		super.visitJumpInsn(Opcodes.GOTO, l2);
		super.visitLabel(l);
		super.visitInsn(Opcodes.ICONST_0);
		super.visitLabel(l2);

	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (owner.equals("java/lang/String")) {
			if (name.equals("equals")) {
				logger.debug("Replacing string call equals!");
				String replacement_owner = "org/evosuite/primitives/StringReplacementFunctions";
				String replacement_name = "equalsDistance";
				String replacement_desc = "(Ljava/lang/String;Ljava/lang/String;)I";
				super.visitMethodInsn(Opcodes.INVOKESTATIC, replacement_owner,
				                      replacement_name, replacement_desc);
				insertFlagCode();

			} else if (name.equals("equalsIgnoreCase")) {
				logger.debug("Replacing string call equalsignorecase!");
				String replacement_owner = "org/evosuite/primitives/StringReplacementFunctions";
				String replacement_name = "equalsIgnoreCaseDistance";
				String replacement_desc = "(Ljava/lang/String;Ljava/lang/String;)I";
				super.visitMethodInsn(Opcodes.INVOKESTATIC, replacement_owner,
				                      replacement_name, replacement_desc);
				insertFlagCode();

			} else if (name.equals("startsWith")) {
				logger.debug("Replacing string call startsWith!");
				String replacement_owner = "org/evosuite/primitives/StringReplacementFunctions";
				String replacement_name = "startsWith";
				String replacement_desc = "(Ljava/lang/String;Ljava/lang/String;)I";
				super.visitMethodInsn(Opcodes.INVOKESTATIC, replacement_owner,
				                      replacement_name, replacement_desc);
				insertFlagCode();

			} else {
				logger.debug("Not replacing string call: " + owner + " - " + name);
				super.visitMethodInsn(opcode, owner, name, desc);
			}
		} else {
			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		// Need to know which variable is on the top of the stack
		switch (opcode) {
		case Opcodes.ALOAD:
		case Opcodes.ILOAD:
		case Opcodes.DLOAD:
		case Opcodes.FLOAD:
		case Opcodes.AALOAD:
		case Opcodes.LLOAD:
			current_var = var;
			break;
			
		case Opcodes.ASTORE:
		case Opcodes.ISTORE:
		case Opcodes.DSTORE:
		case Opcodes.FSTORE:
		case Opcodes.AASTORE:
		case Opcodes.LSTORE:
			current_var = var;
			if (current_write >= 0) {
				//				flags.put(current_write, var);
				flags.put(var, current_write);
			} else if (flags.containsKey(var)) {
				flags.remove(var);
			}
			current_write = -1;
		}
		super.visitVarInsn(opcode, var);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		if ((opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE)
		        && (flags.containsKey(current_var) || current_write >= 0)) {

			int var = current_var;
			//if(flags.get(var) != nextLocal) {
			// Pop flag from stack
			super.visitInsn(Opcodes.POP);
			// Push distance value on stack
			if (current_write >= 0)
				super.visitVarInsn(Opcodes.ILOAD, current_write);
			else
				super.visitVarInsn(Opcodes.ILOAD, flags.get(var));
			//}
			// Now add replacement jump
			if (opcode == Opcodes.IFEQ) {
				// False distance
				super.visitJumpInsn(Opcodes.IFNE, label);
			} else {
				// True distance
				super.visitJumpInsn(Opcodes.IFEQ, label);
			}
		} else {
			super.visitJumpInsn(opcode, label);
		}

	}

}
