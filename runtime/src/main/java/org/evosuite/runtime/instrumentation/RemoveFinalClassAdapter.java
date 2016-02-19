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

import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RemoveFinalClassAdapter extends ClassVisitor {
	
	public static final Set<String> finalClasses = new LinkedHashSet<String>();
	
	public RemoveFinalClassAdapter(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	/**
	 * Remove "final" accessor from class definition
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		if((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
			finalClasses.add(name.replace('/', '.'));
		}
		super.visit(version, access & ~Opcodes.ACC_FINAL, name, signature, superName, interfaces);
	}
	
	/**
	 * Remove "final" accessor from inner class definition
	 */
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		if((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
			finalClasses.add(name.replace('/', '.'));
		}
		super.visitInnerClass(name, outerName, innerName, access & ~Opcodes.ACC_FINAL);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return super.visitMethod(access & ~Opcodes.ACC_FINAL, name, desc, signature, exceptions);
	}
	
	public static void reset() {
		finalClasses.clear();
	}
}
