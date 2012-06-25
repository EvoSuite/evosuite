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
package org.evosuite.callgraph;

import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DistanceMethodAdapter extends MethodVisitor {

	private final String className;
	private final String methodName;
	private final String description;
	private final ConnectionData connectionData;
	private final Set<String> packageClasses;

	public DistanceMethodAdapter(MethodVisitor mv, String className, String methodName,
	        String description, ConnectionData connectionData, Set<String> packageClasses) {
		super(Opcodes.ASM4, mv);
		this.className = className;
		this.methodName = methodName;
		this.description = description;
		this.connectionData = connectionData;
		this.packageClasses = packageClasses;
		connectionData.addConnection(className, methodName, description, className,
		                             methodName, description);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		String ownerDots = owner.replace('/', '.');
		if (packageClasses.contains(ownerDots)) {
			connectionData.addConnection(className, methodName, description, owner, name,
			                             desc);
		}
	}

}
