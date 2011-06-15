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

package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;
import java.util.Map;

import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This class represents a variable in a test case
 * 
 * TODO: Store generic types in this variable - we know at creation what it is
 * (from method calls)
 * 
 * @author Gordon Fraser
 * 
 */
public interface VariableReference extends Comparable<VariableReference> {

	/**
	 * The position of the statement, defining this VariableReference, in the
	 * testcase.
	 * 
	 * @return
	 */
	public int getStPosition();

	/**
	 * Create a copy of the current variable
	 */
	public abstract VariableReference clone();

	/**
	 * Return simple class name
	 */
	public String getSimpleClassName();

	/**
	 * Return class name
	 */
	public String getClassName();

	public String getComponentName();

	public Type getComponentType();

	public GenericClass getGenericClass();

	/**
	 * Return true if variable is an enumeration
	 */
	public boolean isEnum();

	/**
	 * Return true if variable is a primitive type
	 */
	public boolean isPrimitive();

	/**
	 * Return true if variable is void
	 */
	public boolean isVoid();

	/**
	 * Return true if variable is a string
	 */
	public boolean isString();

	/**
	 * Return true if type of variable is a primitive wrapper
	 */
	public boolean isWrapperType();

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	public boolean isAssignableFrom(Type other);

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	public boolean isAssignableTo(Type other);

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	public boolean isAssignableFrom(VariableReference other);

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	public boolean isAssignableTo(VariableReference other);

	/**
	 * Return type of this variable
	 */
	public Type getType();

	/**
	 * Set type of this variable
	 */
	public void setType(Type type);

	/**
	 * Return raw class of this variable
	 */
	public Class<?> getVariableClass();

	/**
	 * Return raw class of this variable's component
	 */
	public Class<?> getComponentClass();

	/**
	 * Return the actual object represented by this variable for a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 */
	public Object getObject(Scope scope);

	/**
	 * Comparison
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * Hash function
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Return string representation of the variable
	 */
	@Override
	public String toString();

	/**
	 * Return name for source code representation
	 * 
	 * @return
	 */
	public String getName();

	public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals);

	public void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals);

	public Object getDefaultValue();

	public String getDefaultValueString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VariableReference other);

	public boolean same(VariableReference r);
}
