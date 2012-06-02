/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * Visits all instructions in method, that could possibly contain references to
 * the classes used in the given class
 * 
 * @author Andrey Tarasevich
 * 
 */
public class CIMethodAdapter extends MethodVisitor {

	private final Set<String> classesReferenced = new HashSet<String>();

	public Set<String> getClassesReferenced() {
		return classesReferenced;
	}

	public CIMethodAdapter() {
		super(Opcodes.ASM4); //, new EmptyVisitor());
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		classesReferenced.addAll(Utils.classesDescFromString(owner));
		classesReferenced.addAll(Utils.classesDescFromString(desc));
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		classesReferenced.addAll(Utils.classesDescFromString(owner));
		classesReferenced.addAll(Utils.classesDescFromString(desc));
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
	        Label start, Label end, int index) {
		classesReferenced.addAll(Utils.classesDescFromString(desc));
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		classesReferenced.addAll(Utils.classesDescFromString(desc));
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		classesReferenced.addAll(Utils.classesDescFromString(type));
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		classesReferenced.addAll(Utils.classesDescFromString(type));
	}
}