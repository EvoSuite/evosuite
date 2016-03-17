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
package org.evosuite.instrumentation.error;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class NullPointerExceptionInstrumentation extends ErrorBranchInstrumenter {

	public NullPointerExceptionInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		
		// If non-static, add a null check
		// TODO: Do we need to also check INVOKESPECIAL?
		if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
			Type[] args = Type.getArgumentTypes(desc);
			Map<Integer, Integer> to = new HashMap<Integer, Integer>();
			for (int i = args.length - 1; i >= 0; i--) {
				int loc = mv.newLocal(args[i]);
				mv.storeLocal(loc);
				to.put(i, loc);
			}

			mv.dup();//callee
			insertBranch(Opcodes.IFNONNULL, "java/lang/NullPointerException");

			for (int i = 0; i < args.length; i++) {
				mv.loadLocal(to.get(i));
			}
		}	
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// If non-static, add a null check
		
		if (opcode == Opcodes.GETFIELD) {
			mv.visitInsn(Opcodes.DUP);
			insertBranch(Opcodes.IFNONNULL, "java/lang/NullPointerException");

		} else if (opcode == Opcodes.PUTFIELD && !methodName.equals("<init>")) {
			if (Type.getType(desc).getSize() == 2) {
				// 2 words
				// v1 v2 v3
				mv.visitInsn(Opcodes.DUP2_X1);
				// v2 v3 v1 v2 v3

				mv.visitInsn(Opcodes.POP2);
				// v2 v3 v1

				mv.visitInsn(Opcodes.DUP_X2);
				// v1 v2 v3 v1

			} else {
				// 1 word
				mv.visitInsn(Opcodes.DUP2);
				//mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.POP);
			}
			insertBranch(Opcodes.IFNONNULL, "java/lang/NullPointerException");
		}
	}

	@Override
	public void visitInsn(int opcode) {
		switch(opcode) {
			case Opcodes.ARRAYLENGTH:
				// TODO
				break;
			case Opcodes.BALOAD:
			case Opcodes.CALOAD:
			case Opcodes.SALOAD:
			case Opcodes.IALOAD:
			case Opcodes.LALOAD:
			case Opcodes.FALOAD:
			case Opcodes.DALOAD:
			case Opcodes.AALOAD:
				// TODO
				break;

			case Opcodes.BASTORE:
			case Opcodes.CASTORE:
			case Opcodes.SASTORE:
			case Opcodes.IASTORE:
			case Opcodes.LASTORE:
			case Opcodes.FASTORE:
			case Opcodes.DASTORE:
			case Opcodes.AASTORE:
				// TODO
				break;
			default:
				// Ignore everything else
		}
		super.visitInsn(opcode);
	}
}
