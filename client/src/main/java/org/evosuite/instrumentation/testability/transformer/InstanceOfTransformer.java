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
package org.evosuite.instrumentation.testability.transformer;

import org.evosuite.instrumentation.testability.BooleanHelper;
import org.evosuite.instrumentation.TransformationStatistics;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Replace instanceof operation with helper that puts int on the stack
 */
public class InstanceOfTransformer extends MethodNodeTransformer {
	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformTypeInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.TypeInsnNode)
	 */
	@Override
	protected AbstractInsnNode transformTypeInsnNode(MethodNode mn,
	        TypeInsnNode typeNode) {
		if (typeNode.getOpcode() == Opcodes.INSTANCEOF) {
			TransformationStatistics.transformInstanceOf();

			// Depending on the class version we need a String or a Class
			// TODO: This needs to be class version of the class that's loaded, not cn!
			//ClassReader reader;
			int version = 48;
			/*
			String name = typeNode.desc.replace('/', '.');
			try {
				reader = new ClassReader(name);
				ClassNode parent = new ClassNode();
				reader.accept(parent, ClassReader.SKIP_CODE);
				version = parent.version;
			} catch (IOException e) {
				TestabilityTransformation.logger.info("Error reading class " + name);
			}
			*/
			if (version >= 49) {
				if (!typeNode.desc.startsWith("[")) {
					LdcInsnNode lin = new LdcInsnNode(Type.getType("L"
					        + typeNode.desc + ";"));
					mn.instructions.insertBefore(typeNode, lin);
				} else {
					LdcInsnNode lin = new LdcInsnNode(Type.getType(typeNode.desc
					        + ";"));
					mn.instructions.insertBefore(typeNode, lin);
				}
			} else {
				LdcInsnNode lin = new LdcInsnNode(typeNode.desc.replace('/', '.'));
				mn.instructions.insertBefore(typeNode, lin);
				MethodInsnNode n = new MethodInsnNode(
				        Opcodes.INVOKESTATIC,
				        Type.getInternalName(Class.class),
				        "forName",
				        Type.getMethodDescriptor(Type.getType(Class.class),
				                                 new Type[] { Type.getType(String.class) }));
				mn.instructions.insertBefore(typeNode, n);
			}
			MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
			        Type.getInternalName(BooleanHelper.class), "instanceOf",
			        Type.getMethodDescriptor(Type.INT_TYPE,
			                                 new Type[] { Type.getType(Object.class),
			                                         Type.getType(Class.class) }));
			mn.instructions.insertBefore(typeNode, n);
			mn.instructions.remove(typeNode);
			return n;
		}
		return typeNode;
	}
}