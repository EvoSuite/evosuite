/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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
/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * @author fraser
 * 
 */
public class ContainerBooleanInterpreter extends BasicInterpreter {

	public final static BasicValue CONTAINER_BOOLEAN = new BasicValue(null);

	/* (non-Javadoc)
	 * @see org.objectweb.asm.tree.analysis.BasicInterpreter#naryOperation(org.objectweb.asm.tree.AbstractInsnNode, java.util.List)
	 */
	@Override
	public BasicValue naryOperation(AbstractInsnNode insn,
	        @SuppressWarnings("rawtypes") List values) throws AnalyzerException {
		if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
			MethodInsnNode mn = (MethodInsnNode) insn;
			if (mn.owner.equals("de/unisb/cs/st/evosuite/javaagent/BooleanHelper")
			        && (mn.name.startsWith("collection") || mn.name.startsWith("map"))) {
				return CONTAINER_BOOLEAN;
			}
		}
		return super.naryOperation(insn, values);
	}
}
