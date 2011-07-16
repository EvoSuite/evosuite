/*
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Javalanche is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * Javalanche. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.callgraph;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import de.unisb.cs.st.evosuite.Properties;

public class DistanceMethodAdapter extends MethodAdapter {

	private final String className;
	private final String methodName;
	private final String description;
	private final ConnectionData connectionData;

	public DistanceMethodAdapter(MethodVisitor mv, String className, String methodName,
	        String description, ConnectionData connectionData) {
		super(mv);
		this.className = className;
		this.methodName = methodName;
		this.description = description;
		this.connectionData = connectionData;
		connectionData.addConnection(className, methodName, description, className,
		                             methodName, description);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		String ownerDots = owner.replace('/', '.');
		if (ownerDots.startsWith(Properties.PROJECT_PREFIX)) {
			connectionData.addConnection(className, methodName, description, owner, name,
			                             desc);
		}
	}

}
