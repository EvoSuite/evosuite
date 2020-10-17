/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase.variable;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.utils.generic.GenericClass;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This class represents a variable in a test case
 * 
 * TODO: Store generic types in this variable - we know at creation what it is
 * (from method calls)
 * 
 * @author Gordon Fraser
 */
public interface VariableReference extends Comparable<VariableReference>, Serializable {

	/**
	 * The position of the statement, defining this VariableReference, in the
	 * testcase.
	 * 
	 * @return a int.
	 */
    int getStPosition();

	/**
	 * Distance metric used to select variables for mutation based on how close
	 * they are to the SUT
	 * 
	 * @return a int.
	 */
    int getDistance();

	/**
	 * Set the distance metric
	 * 
	 * @param distance
	 *            a int.
	 */
    void setDistance(int distance);

	/**
	 * Create a copy of the current variable
	 * 
	 * @return a {@link VariableReference} object.
	 */
    VariableReference clone();

	/**
	 * Create a copy of the current variable for new test
	 * 
	 * @param newTest
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link VariableReference} object.
	 */
    VariableReference clone(TestCase newTest);

	/**
	 * Create a copy of the current variable for new test
	 * 
	 * @param newTest
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param offset
	 *            a int.
	 * @return a {@link VariableReference} object.
	 */
    VariableReference copy(TestCase newTest, int offset);

	/**
	 * Return simple class name
	 * 
	 * @return a {@link java.lang.String} object.
	 */
    String getSimpleClassName();

	/**
	 * Return class name
	 * 
	 * @return a {@link java.lang.String} object.
	 */
    String getClassName();

	/**
	 * <p>
	 * getComponentName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
    String getComponentName();

	/**
	 * <p>
	 * getComponentType
	 * </p>
	 * 
	 * @return a {@link java.lang.reflect.Type} object.
	 */
    Type getComponentType();

	/**
	 * <p>
	 * getGenericClass
	 * </p>
	 * 
	 * @return a {@link GenericClass} object.
	 */
    GenericClass getGenericClass();
	
	/**
	 * <p>
	 * getTestCase
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
    TestCase getTestCase();

	/**
	 * Return true if variable is an enumeration
	 * 
	 * @return a boolean.
	 */
    boolean isEnum();

	/**
	 * Return true if variable is a primitive type
	 * 
	 * @return a boolean.
	 */
    boolean isPrimitive();

	/**
	 * Return true if variable is void
	 * 
	 * @return a boolean.
	 */
    boolean isVoid();

	/**
	 * Return true if variable is an array
	 * 
	 * @return a boolean.
	 */
    boolean isArray();
	
	/**
	 * Return true if this is an index into an array variable
	 * 
	 * @return a boolean
	 */
    boolean isArrayIndex();

	/**
	 * Return true if this is a reference to a public field
	 *
	 * @return a boolean
	 */
    boolean isFieldReference();

	/**
	 * Return true if variable is a string
	 * 
	 * @return a boolean.
	 */
    boolean isString();

	/**
	 * Return true if type of variable is a primitive wrapper
	 * 
	 * @return a boolean.
	 */
    boolean isWrapperType();

	/**
	 * Return true if we can validly access this variable. This might not be the case for a field reference if the owner class is not accessible
	 * 
	 * @return
	 */
    boolean isAccessible();
	
	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 * @return a boolean.
	 */
    boolean isAssignableFrom(Type other);

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 * @return a boolean.
	 */
    boolean isAssignableTo(Type other);

	/**
	 * Return true if other type can be assigned to this variable
	 * 
	 * @param other
	 *            Right hand side of the assignment
	 * @return a boolean.
	 */
    boolean isAssignableFrom(VariableReference other);

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 * 
	 * @param other
	 *            Left hand side of the assignment
	 * @return a boolean.
	 */
    boolean isAssignableTo(VariableReference other);

	/**
	 * Return type of this variable
	 * 
	 * @return a {@link java.lang.reflect.Type} object.
	 */
    Type getType();

	/**
	 * Set type of this variable
	 * 
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 */
    void setType(Type type);

	/**
	 * Return raw class of this variable
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
    Class<?> getVariableClass();

	/**
	 * Return raw class of this variable's component
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
    Class<?> getComponentClass();

	/**
	 * Return the actual object represented by this variable for a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 * @throws org.evosuite.testcase.execution.CodeUnderTestException
	 *             if code from the class under test throws an exception. (E.g.
	 *             the static init of a field)
	 * @return a {@link java.lang.Object} object.
	 */
    Object getObject(Scope scope) throws CodeUnderTestException;

	/**
	 * <p>
	 * getOriginalCode
	 * </p>
	 * 
	 * @return the code this variable reference stems from or null if it was
	 *         generated.
	 */
    String getOriginalCode();

	/**
	 * Set the actual object represented by this variable in a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 * @param value
	 *            The value to be assigned
	 * @throws org.evosuite.testcase.execution.CodeUnderTestException
	 *             if code from the class under test throws an exception. (E.g.
	 *             the static init of a field)
	 */
    void setObject(Scope scope, Object value) throws CodeUnderTestException;

	/**
	 * Set the code fragment that defined this variable reference if imported
	 * from an existing test case.
	 * 
	 * @param code
	 *            The code fragment that defined this variable reference.
	 */
    void setOriginalCode(String code);

	/**
	 * {@inheritDoc}
	 * 
	 * Return string representation of the variable
	 */
	@Override
    String toString();

	/**
	 * Return name for source code representation
	 * 
	 * @return a {@link java.lang.String} object.
	 */
    String getName();

	/**
	 * <p>
	 * getAdditionalVariableReference
	 * </p>
	 * 
	 * @return a {@link VariableReference} object.
	 */
    VariableReference getAdditionalVariableReference();

	/**
	 * <p>
	 * setAdditionalVariableReference
	 * </p>
	 * 
	 * @param var
	 *            a {@link VariableReference} object.
	 */
    void setAdditionalVariableReference(VariableReference var);

	/**
	 * <p>
	 * replaceAdditionalVariableReference
	 * </p>
	 * 
	 * @param var1
	 *            a {@link VariableReference} object.
	 * @param var2
	 *            a {@link VariableReference} object.
	 */
    void replaceAdditionalVariableReference(VariableReference var1,
                                            VariableReference var2);

	/**
	 * <p>
	 * loadBytecode
	 * </p>
	 * 
	 * @param mg
	 *            a {@link org.objectweb.asm.commons.GeneratorAdapter} object.
	 * @param locals
	 *            a {@link java.util.Map} object.
	 */
    void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals);

	/**
	 * <p>
	 * storeBytecode
	 * </p>
	 * 
	 * @param mg
	 *            a {@link org.objectweb.asm.commons.GeneratorAdapter} object.
	 * @param locals
	 *            a {@link java.util.Map} object.
	 */
    void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals);

	/**
	 * <p>
	 * changeClassLoader
	 * </p>
	 * 
	 * @param loader
	 *            a {@link java.lang.ClassLoader} object.
	 */
    void changeClassLoader(ClassLoader loader);

	/**
	 * <p>
	 * getDefaultValue
	 * </p>
	 * 
	 * @return a {@link java.lang.Object} object.
	 */
    Object getDefaultValue();

	/**
	 * <p>
	 * getDefaultValueString
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
    String getDefaultValueString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
    int compareTo(VariableReference other);

	/**
	 * <p>
	 * same
	 * </p>
	 * 
	 * @param r
	 *            a {@link VariableReference} object.
	 * @return a boolean.
	 */
    boolean same(VariableReference r);
}
