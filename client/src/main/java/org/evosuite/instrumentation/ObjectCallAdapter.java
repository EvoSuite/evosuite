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
package org.evosuite.instrumentation;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ObjectCallAdapter class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ObjectCallAdapter extends MethodVisitor {

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(ObjectCallAdapter.class);

	Map<String, String> descriptors = null;

	/**
	 * <p>
	 * Constructor for ObjectCallAdapter.
	 * </p>
	 * 
	 * @param mv
	 *            a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param descriptors
	 *            a {@link java.util.Map} object.
	 */
	public ObjectCallAdapter(MethodVisitor mv, Map<String, String> descriptors) {
		super(Opcodes.ASM5, mv);
		this.descriptors = descriptors;
	}

	/** {@inheritDoc} */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		if (descriptors.containsKey(name + desc)) {
			logger.info("Replacing call to " + name + desc + " with "
			        + descriptors.get(name + desc));
			super.visitMethodInsn(opcode, owner, name, descriptors.get(name + desc), itf);
		} else {
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}
	}

}
