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

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * For each PUTSTATIC/PUTFIELD marks the method as impure.
 *
 * @author Juan Galeotti
 */
public class UpdatesFieldMethodAdapter extends MethodVisitor {

	private boolean updatesField;

	/**
	 * <p>Constructor for PutStaticMethodAdapter.</p>
	 *
	 * @param mv a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className a {@link java.lang.String} object.
	 * @param finalFields a {@link java.util.List} object.
	 */
	public UpdatesFieldMethodAdapter(MethodVisitor mv) {
		super(Opcodes.ASM4, mv);
		updatesField = false;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		if (opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD) {
			updatesField = true;
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	public boolean updatesField() {
		return updatesField;
	}
}
