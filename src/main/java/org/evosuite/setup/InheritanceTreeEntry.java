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
/**
 * 
 */
package org.evosuite.setup;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;

/**
 * @author gordon
 * 
 */
public class InheritanceTreeEntry {

	private final String className;

	private final String superClass;

	private final List<String> interfaces;

	private final int access;

	public InheritanceTreeEntry(String className, String superClass, int access) {
		this.className = className;
		this.superClass = superClass;
		this.access = access;
		interfaces = new ArrayList<String>();
	}

	public InheritanceTreeEntry(String className, String superClass,
	        List<String> interfaces, int access) {
		this.className = className;
		this.superClass = superClass;
		this.interfaces = interfaces;
		this.access = access;
	}

	public void addInterface(String interfaceName) {
		this.interfaces.add(interfaceName);
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the superClass
	 */
	public String getSuperClass() {
		return superClass;
	}

	/**
	 * @return the interfaces
	 */
	public List<String> getInterfaces() {
		return interfaces;
	}

	public boolean isAbstract() {
		return (access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
	}

	public boolean isFinal() {
		return (access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL;
	}

	public boolean isStatic() {
		return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
	}

	public boolean isPublic() {
		return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return className + " <- " + superClass;
	}

}
