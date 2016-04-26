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
package org.evosuite.testcase.statements;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.assertion.Assertion;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * StatementInterface interface.
 * </p>
 * 
 * @author Sebastian Steenbuck
 * @author Gordon Fraser
 */
public interface Statement {

	/**
	 * Add a new assertion to statement
	 * 
	 * @param assertion
	 *            Assertion to be added
	 */
	public void addAssertion(Assertion assertion);
	
	/**
	 * A statement can have a textual comment that will be included
	 * in the JUnit output
	 * 
	 * @param comment
	 */
	public void addComment(String comment);

	/**
	 * Class instances are bound to a class loader - if we want to reexecute a
	 * test on a different classloader we need to be able to change the class of
	 * the reflection object
	 * 
	 * @param loader
	 *            a {@link java.lang.ClassLoader} object.
	 */
	public void changeClassLoader(ClassLoader loader);

	/**
	 * Create deep copy of statement
	 * 
	 * @return a {@link Statement} object.
	 */
	public Statement clone();

	/**
	 * <p>
	 * clone
	 * </p>
	 * 
	 * @param newTestCase
	 *            the testcase in which this statement will be inserted
	 * @return a {@link Statement} object.
	 */
	public Statement clone(TestCase newTestCase);

	/**
	 * <p>
	 * copy
	 * </p>
	 * 
	 * @param newTestCase
	 *            the testcase in which this statement will be inserted
	 * @param offset
	 *            a int.
	 * @return a {@link Statement} object.
	 */
	public Statement copy(TestCase newTestCase, int offset);

	/**
	 * <p>
	 * copyAssertions
	 * </p>
	 * 
	 * @param newTestCase
	 *            the testcase in which this statement will be inserted
	 * @param offset
	 *            a int.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Assertion> copyAssertions(TestCase newTestCase, int offset);

	/**
	 * {@inheritDoc}
	 * 
	 * Equality check
	 */
	@Override
	public boolean equals(Object s);

	/**
	 * This method executes the statement under the given scope. If execution of
	 * the statement is aborted abnormally (i.e. an exception is thrown.) The
	 * exception is returned. Otherwise the return value is null.
	 * 
	 * @param scope
	 *            the scope under which the statement is executed
	 * @param out
	 *            a {@link java.io.PrintStream} object.
	 * @return if an exception was thrown during execution this is the exception
	 * @throws java.lang.reflect.InvocationTargetException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.InstantiationException
	 *             if any.
	 */
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException;

	/**
	 * Returns the accessibleObject which is used to generate this kind of
	 * statement E.g. the Field of a FieldStatement, the Method of a
	 * MethodStatement and so on MAY return NULL (for example for
	 * NullStatements)
	 * 
	 * @return a {@link java.lang.reflect.AccessibleObject} object.
	 */
	public GenericAccessibleObject<?> getAccessibleObject();

	/**
	 * Get Java code representation of assertions
	 * 
	 * @return String representing all assertions attached to this statement
	 */
	public String getAssertionCode();

	/**
	 * Return list of assertions
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Assertion> getAssertions();

	/**
	 * Create a string representing the statement as Java code
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getCode();

	/**
	 * Create a string representing the statement as Java code
	 * 
	 * @param exception
	 *            a {@link java.lang.Throwable} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getCode(Throwable exception);

	/**
	 * Retrieve comment for this statement
	 * @return
	 */
	public String getComment();
	
	
	/**
	 * <p>
	 * getDeclaredExceptions
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Class<?>> getDeclaredExceptions();

	/**
	 * Retrieve the number of parameters of this statement
	 * 
	 * @return
	 */
	public int getNumParameters();

	/**
	 * <p>
	 * getPosition
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getPosition();

	/**
	 * <p>
	 * getReturnClass
	 * </p>
	 * 
	 * @return Raw class of return value
	 */
	public Class<?> getReturnClass();

	/**
	 * <p>
	 * getReturnType
	 * </p>
	 * 
	 * @return Generic type of return value
	 */
	public Type getReturnType();

	/**
	 * <p>
	 * getReturnValue
	 * </p>
	 * 
	 * @return Variable representing return value
	 */
	public VariableReference getReturnValue();

	/**
	 * Retrieve the test case this statement is part of
	 * 
	 * @return
	 */
	public TestCase getTestCase();
	
	/**
	 * <p>
	 * getUniqueVariableReferences
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<VariableReference> getUniqueVariableReferences();

	/**
	 * <p>
	 * getVariableReferences
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<VariableReference> getVariableReferences();

	/**
	 * Check if there are assertions
	 * 
	 * @return True if there are assertions
	 */
	public boolean hasAssertions();

	/**
	 * {@inheritDoc}
	 * 
	 * Generate hash code
	 */
	@Override
	public int hashCode();

	/**
	 * Determine if the underlying reflection object is currently accessible 
	 * @return
	 */
	public boolean isAccessible();
	
	/**
	 * Returns true if this statement should be handled as an
	 * AssignmentStatement. This method was added to allow the wrapping of
	 * AssignmentStatements (in which case "a instanceof AssignmentStatement" is
	 * no longer working)
	 * 
	 * @return a boolean.
	 */
	public boolean isAssignmentStatement();

	public boolean isReflectionStatement();

	/**
	 * Tests if the throwable defined by t is declared to be thrown by the
	 * underlying type. Obviously this can only return true for methods and
	 * constructors.
	 * 
	 * @param t
	 *            a {@link java.lang.Throwable} object.
	 * @return a boolean.
	 */
	public boolean isDeclaredException(Throwable t);

	/**
	 * Various consistency checks. This method might also return with an
	 * assertionError Functionality might depend on the status of
	 * enableAssertions in this JVM
	 * 
	 * @return a boolean.
	 */
	public boolean isValid();

	/**
	 * <p>
	 * mutate
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param factory
	 *            a {@link org.evosuite.testcase.TestFactory} object.
	 * @return a boolean.
	 */
	public boolean mutate(TestCase test, TestFactory factory);

	/**
	 * Check if the statement makes use of var
	 * 
	 * @param var
	 *            Variable we are checking for
	 * @return True if var is referenced
	 */
	public boolean references(VariableReference var);

	/**
	 * Delete assertion attached to this statement
	 * 
	 * @param assertion
	 *            a {@link org.evosuite.assertion.Assertion} object.
	 */
	public void removeAssertion(Assertion assertion);

	/**
	 * Delete all assertions attached to this statement
	 */
	public void removeAssertions();

	/**
	 * Replace a VariableReference with another one
	 * 
	 * @param var1
	 *            The old variable
	 * @param var2
	 *            The new variable
	 */
	public void replace(VariableReference var1, VariableReference var2);

	/**
	 * Allows the comparing of Statements between TestCases. I.e. this is a more
	 * semantic comparison than the one done by equals. E.g. two Variable are
	 * equal if they are at the same position and they reference to objects of
	 * the same type.
	 * 
	 * @param s
	 *            a {@link Statement} object.
	 * @return a boolean.
	 */
	public boolean same(Statement s);

	/**
	 * Sets the set of assertions to statement
	 * 
	 * @param assertions
	 *            a {@link java.util.Set} object.
	 */
	public void setAssertions(Set<Assertion> assertions);

	/**
	 * <p>
	 * setRetval
	 * </p>
	 * 
	 * @param newRetVal
	 *            a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public void setRetval(VariableReference newRetVal);

}
