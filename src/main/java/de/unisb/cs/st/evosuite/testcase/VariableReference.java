/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;


import java.lang.reflect.Type;

import org.apache.log4j.Logger;

/**
 * This class represents a variable in a test case 
 * 
 * TODO: Store generic types in this variable - we know at creation what it is (from method calls)
 * 
 * @author Gordon Fraser
 * 
 */
public class VariableReference {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(VariableReference.class);
	
	/**
	 * Type (class) of the variable
	 */
	protected GenericClass type;

	/**
	 * Position within the test case
	 * I.e., the statement at which it is created
	 */
	public int statement = 0;

	/**
	 * If this variable is contained in an array, this is the reference to the array
	 */
	protected VariableReference array = null;
	
	/**
	 * Index in the array
	 */
	protected int array_index = 0;

	/**
	 * Index in the array
	 */
	public int array_length = 0;


	/**
	 * Constructor
	 * @param type
	 *        The type (class) of the variable
	 * @param position
	 *        The statement in the test case that declares this variable
	 */
	public VariableReference(GenericClass type, int position) {
		this.type = type;
		statement = position;
	}

	/**
	 * Constructor
	 * @param type
	 *        The type (class) of the variable
	 * @param position
	 *        The statement in the test case that declares this variable
	 */
	public VariableReference(VariableReference array, int index, int length, int position) {
		this.type = new GenericClass(array.getComponentType());
		statement = position;
		this.array = array;
		this.array_index = index;
		this.array_length = length;
	}
	
	public VariableReference(Type type, int position) {
		this.type = new GenericClass(type);
		statement = position;
	}

	/**
	 * Create a copy of the current variable
	 */
	public VariableReference clone() {
		VariableReference copy = new VariableReference(type, statement);
		if(array != null) {
			copy.array = array.clone();
			copy.array_index = array_index;
		}
		return copy;
	}
	
	/**
	 * Return simple class name
	 */
	public String getSimpleClassName() {
		return type.getSimpleName();
	}

	/**
	 * Return class name
	 */
	public String getClassName() {
		return type.getClassName();
	}

	public String getComponentName() {
		return type.getComponentName();
	}
	
	public Type getComponentType() {
		return type.getComponentType();
	}
	
	public VariableReference getArray() {
		return array;
	}
	
	/**
	 * Return true if variable is an enumeration
	 */
	public boolean isEnum() {
		return type.isEnum();
	}
	
	/**
	 * Return true if variable is a primitive type
	 */
	public boolean isPrimitive() {
		return type.isPrimitive();
	}
	
	/**
	 * Return true if variable is void
	 */
	public boolean isVoid() {
		return type.isVoid();
	}
	
	/**
	 * Return true if variable is an array
	 */
	public boolean isArray() {
		return type.isArray();
	}

	/**
	 * Return true if variable is an array
	 */
	public boolean isArrayIndex() {
		return array != null;
	}

	/**
	 * Return true if type of variable is a primitive wrapper
	 */
	public boolean isWrapperType() {
		return type.isWrapperType();
	}
	
	/**
	 * Return true if other type can be assigned to this variable
	 * @param other
	 *     Right hand side of the assignment
	 */
	public boolean isAssignableFrom(Type other) {
		return type.isAssignableFrom(other);
	}
	
	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * @param other
	 *     Left hand side of the assignment
	 */
	public boolean isAssignableTo(Type other) {
		return type.isAssignableTo(other);
	}

	/**
	 * Return true if other type can be assigned to this variable
	 * @param other
	 *     Right hand side of the assignment
	 */
	public boolean isAssignableFrom(VariableReference other) {
		return type.isAssignableFrom(other.type);
	}
	
	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * @param other
	 *     Left hand side of the assignment
	 */
	public boolean isAssignableTo(VariableReference other) {
		return type.isAssignableTo(other.type);
	}

	/**
	 * Return type of this variable
	 */
	public Type getType() {
		return type.getType();
	}

	/**
	 * Set type of this variable
	 */
	public void setType(Type type) {
		this.type = new GenericClass(type);
	}
	

	/**
	 * Return raw class of this variable
	 */
	public Class<?> getVariableClass() {
		return type.getRawClass();
	}
	
	/**
	 * Add delta to the position of all variables up to a position
	 * @param delta
	 *    The delta that will be added to the position of each variable
	 * @param position
	 *    The maximum position up to which variables are changed
	 */
	public void adjust(int delta, int position) {
		if(statement >= position) {
			statement += delta;
		}
		if(array != null) {
			array.adjust(delta, position);
		}
	}
	
	/**
	 * Return the actual object represented by this variable for a given scope
	 * @param scope
	 *    The scope of the test case execution
	 */
	public Object getObject(Scope scope) {
		return scope.get(this);
	}
	
	/**
	 * Comparison
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableReference other = (VariableReference) obj;
		if (statement != other.statement)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if(array == null) {
			if(other.array != null)
				return false;
		} else if(!array.equals(other.array))
				return false;
		if(array_index != other.array_index)
			return false;
		
		return true;
	}
	
	/**
	 * Hash function
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + statement;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((array == null) ? 0 : array.hashCode() + array_index);
		return result;
	}
	
	/**
	 * Return string representation of the variable
	 */
	public String toString() {
		return "VariableReference: Statement "+statement+", type "+type.getTypeName();
	}
	
	/**
	 * Return name for source code representation
	 * @return
	 */
	public String getName() {
		if(array != null)
			return array.getName()+"["+array_index+"]";
		else
			return "var"+statement;
	}
}
