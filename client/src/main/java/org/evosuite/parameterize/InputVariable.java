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
/**
 *
 */
package org.evosuite.parameterize;

import java.lang.reflect.Type;
import java.util.Map;

import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.utils.generic.GenericClass;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * InputVariable class.
 * </p>
 *
 * @author fraser
 */
public class InputVariable implements VariableReference {

	private static final long serialVersionUID = -5552253395284020019L;

	private int numInput = 0;
	private String originalCode;

	/** Type (class) of the variable */
	protected GenericClass type;

	/**
	 * <p>
	 * Constructor for InputVariable.
	 * </p>
	 *
	 * @param clazz
	 *            a {@link java.lang.reflect.Type} object.
	 * @param num
	 *            a int.
	 */
	public InputVariable(Type clazz, int num) {
		numInput = num;
		type = new GenericClass(clazz);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getStPosition()
	 */
	/** {@inheritDoc} */
	@Override
	public int getStPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getDistance()
	 */
	/** {@inheritDoc} */
	@Override
	public int getDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#setDistance(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setDistance(int distance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#clone(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference clone(TestCase newTest) {
		return copy(newTest, 0);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone() {
		throw new UnsupportedOperationException(
		        "This method SHOULD not be used, as only the original reference is keeped up to date");
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#copy(org.evosuite.testcase.TestCase, int)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference copy(TestCase newTest, int offset) {
		return newTest.getStatement(getStPosition() + offset).getReturnValue();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return class name
	 */
	@Override
	public String getClassName() {
		return type.getClassName();
	}

	/** {@inheritDoc} */
	@Override
	public String getComponentName() {
		return type.getComponentName();
	}

	/** {@inheritDoc} */
	@Override
	public Type getComponentType() {
		return type.getComponentType();
	}

	@Override
	public boolean isAccessible() {
		return TestUsageChecker.canUse(type.getRawClass());
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#isArray()
	 */
	@Override
	public boolean isArray() {
		return type.isArray();
	}

	@Override
	public boolean isArrayIndex() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if variable is an enumeration
	 */
	@Override
	public boolean isEnum() {
		return type.isEnum();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if variable is a primitive type
	 */
	@Override
	public boolean isPrimitive() {
		return type.isPrimitive();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if variable is void
	 */
	@Override
	public boolean isVoid() {
		return type.isVoid();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if variable is a string
	 */
	@Override
	public boolean isString() {
		return type.isString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if type of variable is a primitive wrapper
	 */
	@Override
	public boolean isWrapperType() {
		return type.isWrapperType();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if other type can be assigned to this variable
	 */
	@Override
	public boolean isAssignableFrom(Type other) {
		return type.isAssignableFrom(other);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if this variable can by assigned to a variable of other type
	 */
	@Override
	public boolean isAssignableTo(Type other) {
		return type.isAssignableTo(other);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if other type can be assigned to this variable
	 */
	@Override
	public boolean isAssignableFrom(VariableReference other) {
		return type.isAssignableFrom(other.getType());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return true if this variable can by assigned to a variable of other type
	 */
	@Override
	public boolean isAssignableTo(VariableReference other) {
		return type.isAssignableTo(other.getType());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return type of this variable
	 */
	@Override
	public Type getType() {
		return type.getType();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Set type of this variable
	 */
	@Override
	public void setType(Type type) {
		this.type = new GenericClass(type);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return raw class of this variable
	 */
	@Override
	public Class<?> getVariableClass() {
		return type.getRawClass();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return raw class of this variable's component
	 */
	@Override
	public Class<?> getComponentClass() {
		return type.getRawClass().getComponentType();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getSimpleClassName()
	 */
	/** {@inheritDoc} */
	@Override
	public String getSimpleClassName() {
		// TODO: Workaround for bug in commons lang
		if (type.isPrimitive()
		        || (type.isArray() && new GenericClass(type.getComponentType()).isPrimitive()))
			return type.getRawClass().getSimpleName();

		return type.getSimpleName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getGenericClass()
	 */
	/** {@inheritDoc} */
	@Override
	public GenericClass getGenericClass() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getObject(org.evosuite.testcase.Scope)
	 */
	/** {@inheritDoc} */
	@Override
	public Object getObject(Scope scope) throws CodeUnderTestException {
		return scope.getObject(this);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#setObject(org.evosuite.testcase.Scope, java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void setObject(Scope scope, Object value) throws CodeUnderTestException {
		scope.setObject(this, value);

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getName()
	 */
	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "input" + numInput;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getAdditionalVariableReference()
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getAdditionalVariableReference() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#setAdditionalVariableReference(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void setAdditionalVariableReference(VariableReference var) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replaceAdditionalVariableReference(VariableReference var1,
	        VariableReference var2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#loadBytecode(org.objectweb.asm.commons.GeneratorAdapter, java.util.Map)
	 */
	/** {@inheritDoc} */
	@Override
	public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#storeBytecode(org.objectweb.asm.commons.GeneratorAdapter, java.util.Map)
	 */
	/** {@inheritDoc} */
	@Override
	public void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#changeClassLoader(java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getDefaultValue()
	 */
	/** {@inheritDoc} */
	@Override
	public Object getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getDefaultValueString()
	 */
	/** {@inheritDoc} */
	@Override
	public String getDefaultValueString() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#compareTo(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareTo(VariableReference other) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#same(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean same(VariableReference r) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * <p>
	 * Getter for the field <code>originalCode</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	public String getOriginalCode() {
		return originalCode;
	}

	/** {@inheritDoc} */
	@Override
	public void setOriginalCode(String originalCode) {
		this.originalCode = originalCode;
	}

	@Override
	public TestCase getTestCase() {
		return this.getTestCase();
	}

	@Override
	public boolean isFieldReference() {
		return false;
	}
}
