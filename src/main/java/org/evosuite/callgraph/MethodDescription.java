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
/*
* Copyright (C) 2011 Saarland University
* 
* This file is part of Javalanche.
* 
* Javalanche is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Javalanche is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser Public License for more details.
* 
* You should have received a copy of the GNU Lesser Public License
* along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * <p>MethodDescription class.</p>
 *
 * @author Gordon Fraser
 */
package org.evosuite.callgraph;

import java.io.Serializable;
public class MethodDescription implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String className;
	private String methodName;
	private String desc;

	/**
	 * <p>Constructor for MethodDescription.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param desc a {@link java.lang.String} object.
	 */
	public MethodDescription(String className, String methodName, String desc) {
		super();
		this.className = className.replace('/', '.');
		this.methodName = methodName;
		this.desc = desc;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodDescription other = (MethodDescription) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		return true;
	}

	/**
	 * <p>Getter for the field <code>className</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * <p>Getter for the field <code>methodName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * <p>Getter for the field <code>desc</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * <p>getSuper</p>
	 *
	 * @param superClass a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.callgraph.MethodDescription} object.
	 */
	public MethodDescription getSuper(String superClass) {
		return new MethodDescription(superClass, methodName, desc);
	}
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return className + "." + methodName + desc;
	}
}
