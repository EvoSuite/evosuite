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
package org.evosuite.runtime.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class RegisterObjectForDeterministicHashCodeVisitor extends AdviceAdapter {


	
	
	protected RegisterObjectForDeterministicHashCodeVisitor(MethodVisitor mv, int access, String name, String desc) {
		super(Opcodes.ASM5, mv, access, name, desc);
	}

	@Override
	public void visitInsn(int opcode) {
		// We don't use the AdviceAdapter here because this is not properly initialised if the constructor is
		// exited with an exception
		if(opcode == Opcodes.RETURN) {
			loadThis();
			invokeStatic(Type.getType(org.evosuite.runtime.System.class), Method.getMethod("void registerObjectForIdentityHashCode(Object)"));						
		}
		super.visitInsn(opcode);
	}	
}
