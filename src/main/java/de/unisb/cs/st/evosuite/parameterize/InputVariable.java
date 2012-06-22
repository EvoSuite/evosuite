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
package de.unisb.cs.st.evosuite.parameterize;

import java.lang.reflect.Type;
import java.util.Map;

import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.testcase.CodeUnderTestException;
import de.unisb.cs.st.evosuite.testcase.GenericClass;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author fraser
 * 
 */
public class InputVariable implements VariableReference {

	private static final long serialVersionUID = -5552253395284020019L;

	private int numInput = 0;
	private String originalCode;

	/** Type (class) of the variable */
	protected GenericClass type;

	public InputVariable(Type clazz, int num) {
		numInput = num;
		type = new GenericClass(clazz);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getStPosition()
	 */
	@Override
	public int getStPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getDistance()
	 */
	@Override
	public int getDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#setDistance(int)
	 */
	@Override
	public void setDistance(int distance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#clone(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public VariableReference clone(TestCase newTest) {
		return copy(newTest, 0);
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone() {
		throw new UnsupportedOperationException(
		        "This method SHOULD not be used, as only the original reference is keeped up to date");
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#copy(de.unisb.cs.st.evosuite.testcase.TestCase, int)
	 */
	@Override
	public VariableReference copy(TestCase newTest, int offset) {
		return newTest.getStatement(getStPosition() + offset).getReturnValue();
	}

	/**
	 * Return class name
	 */
	@Override
	public String getClassName() {
		return type.getClassName();
	}

	@Override
	public String getComponentName() {
		return type.getComponentName();
	}

	@Override
	public Type getComponentType() {
		return type.getComponentType();
	}

	/**
	 * Return true if variable is an enumeration
	 */
	@Override
	public boolean isEnum() {
		return type.isEnum();
	}

	/**
	 * Return true if variable is a primitive type
	 */
	@Override
	public boolean isPrimitive() {
		return type.isPrimitive();
	}

	/**
	 * Return true if variable is void
	 */
	@Override
	public boolean isVoid() {
		return type.isVoid();
	}

	/**
	 * Return true if variable is a string
	 */
	@Override
	public boolean isString() {
		return type.isString();
	}

	/**
	 * Return true if type of variable is a primitive wrapper
	 */
	@Override
	public boolean isWrapperType() {
		return type.isWrapperType();
	}

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	@Override
	public boolean isAssignableFrom(Type other) {
		return type.isAssignableFrom(other);
	}

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	@Override
	public boolean isAssignableTo(Type other) {
		return type.isAssignableTo(other);
	}

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 */
	@Override
	public boolean isAssignableFrom(VariableReference other) {
		return type.isAssignableFrom(other.getType());
	}

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 */
	@Override
	public boolean isAssignableTo(VariableReference other) {
		return type.isAssignableTo(other.getType());
	}

	/**
	 * Return type of this variable
	 */
	@Override
	public Type getType() {
		return type.getType();
	}

	/**
	 * Set type of this variable
	 */
	@Override
	public void setType(Type type) {
		this.type = new GenericClass(type);
	}

	/**
	 * Return raw class of this variable
	 */
	@Override
	public Class<?> getVariableClass() {
		return type.getRawClass();
	}

	/**
	 * Return raw class of this variable's component
	 */
	@Override
	public Class<?> getComponentClass() {
		return type.getRawClass().getComponentType();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getSimpleClassName()
	 */
	@Override
	public String getSimpleClassName() {
		// TODO: Workaround for bug in commons lang
		if (type.isPrimitive()
		        || (type.isArray() && new GenericClass(type.getComponentType()).isPrimitive()))
			return type.getRawClass().getSimpleName();

		return type.getSimpleName();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getGenericClass()
	 */
	@Override
	public GenericClass getGenericClass() {
		return type;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getObject(de.unisb.cs.st.evosuite.testcase.Scope)
	 */
	@Override
	public Object getObject(Scope scope) throws CodeUnderTestException {
		return scope.getObject(this);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#setObject(de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Object)
	 */
	@Override
	public void setObject(Scope scope, Object value) throws CodeUnderTestException {
		scope.setObject(this, value);

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getName()
	 */
	@Override
	public String getName() {
		return "input" + numInput;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getAdditionalVariableReference()
	 */
	@Override
	public VariableReference getAdditionalVariableReference() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#setAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void setAdditionalVariableReference(VariableReference var) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceAdditionalVariableReference(VariableReference var1,
	        VariableReference var2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#loadBytecode(org.objectweb.asm.commons.GeneratorAdapter, java.util.Map)
	 */
	@Override
	public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#storeBytecode(org.objectweb.asm.commons.GeneratorAdapter, java.util.Map)
	 */
	@Override
	public void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#changeClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getDefaultValue()
	 */
	@Override
	public Object getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getDefaultValueString()
	 */
	@Override
	public String getDefaultValueString() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#compareTo(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public int compareTo(VariableReference other) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#same(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public boolean same(VariableReference r) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getOriginalCode() {
		return originalCode;
	}

	public void setOriginalCode(String originalCode) {
		this.originalCode = originalCode;
	}
}
