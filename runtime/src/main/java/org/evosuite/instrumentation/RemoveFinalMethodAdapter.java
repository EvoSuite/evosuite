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
/**
 * 
 */
package org.evosuite.instrumentation;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * <p>RemoveFinalMethodAdapter class.</p>
 *
 * @author Gordon Fraser
 */
public class RemoveFinalMethodAdapter extends MethodVisitor {

	private final List<String> finalFields;

	private final String className;

	/**
	 * <p>Constructor for RemoveFinalMethodAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 * @param finalFields a {@link java.util.List} object.
	 */
	public RemoveFinalMethodAdapter(String className, MethodVisitor mv,
	        List<String> finalFields) {
		super(Opcodes.ASM4, mv);
		this.finalFields = finalFields;
		this.className = className;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if ((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)
		        && owner.equals(className)) {

			if (!finalFields.contains(name)) {
				//System.out.println("Keeping non-final field " + name + " in class "
				//        + owner);
				super.visitFieldInsn(opcode, owner, name, desc);
			} else {
				//System.out.println("Omitting final field " + name + " in class " + owner);
				Type type = Type.getType(desc);
				if (type.getSize() == 1)
					super.visitInsn(Opcodes.POP);
				else if (type.getSize() == 2)
					super.visitInsn(Opcodes.POP2);
				if (opcode == Opcodes.PUTFIELD)
					super.visitInsn(Opcodes.POP);
			}
		} else {
			//if (!owner.equals(className))
			//	System.out.println("Mismatch: " + className + " / " + owner);
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}
}
