/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.javaagent;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class ObjectCallAdapter extends MethodVisitor {

	protected static Logger logger = LoggerFactory.getLogger(ObjectCallAdapter.class);

	Map<String, String> descriptors = null;

	public ObjectCallAdapter(MethodVisitor mv, Map<String, String> descriptors) {
		super(Opcodes.ASM4, mv);
		this.descriptors = descriptors;
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (descriptors.containsKey(name + desc)) {
			logger.info("Replacing call to " + name + desc + " with "
			        + descriptors.get(name + desc));
			super.visitMethodInsn(opcode, owner, name, descriptors.get(name + desc));
		} else {
			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}

}
