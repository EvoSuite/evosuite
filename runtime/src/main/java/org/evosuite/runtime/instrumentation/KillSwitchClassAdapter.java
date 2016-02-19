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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Add instrumentation in each method to set up a kill switch to stop
 * the SUT threads
 * 
 * @author arcuri
 *
 */
public class KillSwitchClassAdapter  extends ClassVisitor{

	public KillSwitchClassAdapter(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		// Don't touch bridge and synthetic methods
		if ((access & Opcodes.ACC_SYNTHETIC) > 0
				|| (access & Opcodes.ACC_BRIDGE) > 0) {
			return mv;
		}
		
		if (name.equals("<clinit>")){
			//should not stop a static initializer
			return mv;
		}


		return new KillSwitchMethodAdapter(mv, name, desc);
	}
}
