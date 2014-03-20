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
package org.evosuite.testcase;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.assertion.Assertion;
import org.evosuite.contracts.ContractViolation;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.utils.Listenable;


/**
 *
 * A test case is a list of statements
 *
 * @author Gordon Fraser
 * @author Sebastian Steenbuck
 */
public interface TestCase extends Iterable<StatementInterface>, Cloneable,
        Listenable<Void> {

	/**
	 * Handle test visitor
	 *
	 * @param visitor a {@link org.evosuite.testcase.TestVisitor} object.
	 */
	public void accept(TestVisitor visitor);

	/**
	 * Copy all the assertions from other test case
	 *
	 * @param other
	 *            The other test case
	 */
	public void addAssertions(TestCase other);

	/**
	 * Keep track of an additional covered goal
	 *
	 * @param goal a {@link org.evosuite.testcase.TestFitnessFunction} object.
	 */
	public void addCoveredGoal(TestFitnessFunction goal);

	/**
	 * Keep track of an additional test failure
	 *
	 * @param goal a {@link org.evosuite.testcase.TestFitnessFunction} object.
	 */
	public void addContractViolation(ContractViolation violation);

	/**
	 * Append new statement at end of test case
	 *
	 * @param statement
	 *            New statement
	 * @return VariableReference of return value
	 */
	public VariableReference addStatement(StatementInterface statement);

	/**
	 * Add new statement at position and fix following variable references
	 *
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement. Notice that the test might choose to
	 *         modify the statement you inserted. You should use the returned
	 *         variable reference and not use references
	 */
	public VariableReference addStatement(StatementInterface statement, int position);

	/**
	 * <p>addStatements</p>
	 *
	 * @param statements a {@link java.util.List} object.
	 */
	public void addStatements(List<? extends StatementInterface> statements);

	/**
	 * Remove all statements after a given position
	 *
	 * @param length
	 *            Length of the test case after chopping
	 */
	public void chop(int length);

	/**
	 * Remove all covered goals
	 */
	public void clearCoveredGoals();

	/**
	 * <p>clone</p>
	 *
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase clone();

	/**
	 * Determine the set of classes that are accessed by the test case
	 *
	 * @return Set of accessed classes
	 */
	public Set<Class<?>> getAccessedClasses();

	/**
	 * Retrieve a list of filenames accessed during the last execution
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getAccessedFiles();

	/**
	 * Get all assertions that exist for this test case
	 *
	 * @return List of assertions
	 *
	 *         TODO: Also return ExceptionAssertion?
	 */
	public List<Assertion> getAssertions();

	/**
	 * Retrieve all violations observed during test execution
	 * 
	 * @return
	 */
	public Set<ContractViolation> getContractViolations();
		
	
	/**
	 * Retrieve all coverage goals covered by this test
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<TestFitnessFunction> getCoveredGoals();
	
	/**
	 * <p>getDeclaredExceptions</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Class<?>> getDeclaredExceptions();
	
	/**
	 * Determine the set of variables that var depends on
	 *
	 * @param var
	 *            Variable to check for
	 * @return Set of dependency variables
	 */
	public Set<VariableReference> getDependencies(VariableReference var);

	/**
	 * Get the last object of the defined type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference getLastObject(Type type)
			throws ConstructionFailedException;
	
	/**
	 * Get the last object of the defined type
	 * 
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference getLastObject(Type type, int position)
			throws ConstructionFailedException;


	/**
	 * Get actual object represented by a variable for a given execution scope
	 *
	 * @param reference
	 *            Variable
	 * @param scope
	 *            Excution scope
	 * @return Object in scope
	 */
	public Object getObject(VariableReference reference, Scope scope);

	/**
	 * Get all objects up to position satisfying constraint
	 *
	 * @param position a int.
	 * @return a {@link java.util.List} object.
	 */
	public List<VariableReference> getObjects(int position);

	/**
	 * Get all objects up to position satisfying constraint
	 *
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position a int.
	 * @return a {@link java.util.List} object.
	 */
	public List<VariableReference> getObjects(Type type, int position);

	/**
	 * Get a random object matching type
	 *
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position
	 *            Upper bound in test case up to which objects are considered
	 * @throws org.evosuite.ga.ConstructionFailedException
	 *             if no such object exists
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * Get a random object matching type
	 *
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position
	 *            Upper bound in test case up to which objects are considered
	 * @throws org.evosuite.ga.ConstructionFailedException
	 *             if no such object exists
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getRandomNonNullObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * Get a random object matching type
	 *
	 * @return Random object
	 * @throws ConstructionFailedException if any.
	 */
	public VariableReference getRandomObject();

	/**
	 * Get a random object matching type
	 *
	 * @param position
	 *            Upper bound in test case up to which objects are considered
	 * @throws ConstructionFailedException
	 *             if no such object exists
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getRandomObject(int position);

	/**
	 * Get a random object matching type
	 *
	 * @param type
	 *            Class we are looking for
	 * @return Random object
	 * @throws org.evosuite.ga.ConstructionFailedException if any.
	 */
	public VariableReference getRandomObject(Type type)
	        throws ConstructionFailedException;

	/**
	 * Get a random object matching type
	 *
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position
	 *            Upper bound in test case up to which objects are considered
	 * @throws org.evosuite.ga.ConstructionFailedException
	 *             if no such object exists
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getRandomObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * Determine the set of variables that depend on var
	 *
	 * @param var
	 *            Variable to check for
	 * @return Set of dependent variables
	 */
	public Set<VariableReference> getReferences(VariableReference var);

	/**
	 * Get return value (variable) of statement at position
	 *
	 * @param position a int.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getReturnValue(int position);

	/**
	 * Access statement by index
	 *
	 * @param position
	 *            Index of statement
	 * @return Statement at position
	 */
	public StatementInterface getStatement(int position);

	/**
	 * Check if there are any assertions
	 *
	 * @return True if there are assertions
	 */
	public boolean hasAssertions();

	/**
	 * <p>hasCastableObject</p>
	 *
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @return a boolean.
	 */
	public boolean hasCastableObject(Type type);

	/**
	 * Check if the test case has an object of a given class
	 *
	 * @param type
	 *            Type to look for
	 * @param position
	 *            Upper bound up to which the test is checked
	 * @return True if there is something usable
	 */
	public boolean hasObject(Type type, int position);

	/**
	 * Check if var is referenced after its definition
	 *
	 * @param var
	 *            Variable to check for
	 * @return True if there is a use of var
	 */
	public boolean hasReferences(VariableReference var);

	/**
	 * Check if all methods/fields accessed are accessible also for the current SUT
	 * 
	 * @return
	 */
	public boolean isAccessible();
	
	/**
	 * <p>isEmpty</p>
	 *
	 * @return true if size()==0
	 */
	public boolean isEmpty();

	public boolean isFailing();
	
	/**
	 * Check if this test case is a prefix of t
	 *
	 * <p>
	 * A test case {@code A} is a prefix of a test case {@code B} if
	 * and only if the first {@code length(A)} statements of {@code B} are
	 * equal to ones of {@code A}, in the same order.
	 * In other words, {@code B} can be seen as an extension of {@code A}.
	 *
	 * @param t
	 *            Test case to check against
	 * @return True if this test is a prefix of t
	 */
	public boolean isPrefix(TestCase t);

	/**
	 * A test can be unstable if its assertions fail, eg due to non-determinism,
	 * non-properly handled static variables and side effects on environment, etc
	 * 
	 * @return
	 */
	public boolean isUnstable();

	/**
	 * Check if test case is valid (executable)
	 *
	 * @return a boolean.
	 */
	public boolean isValid();

	/**
	 * Remove statement at position and fix variable references
	 *
	 * @param position a int.
	 */
	public void remove(int position);

	/**
	 * Remove assertion from test case
	 */
	public void removeAssertion(Assertion assertion);

	/**
	 * Remove all assertions from test case
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
	 * Keep track of accessed files
	 *
	 * @param files a {@link java.util.List} object.
	 */
	public void setAccessedFiles(List<String> files);

	/**
	 * Set new statement at position
	 *
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement. Notice that the test might choose to
	 *         modify the statement you inserted. You should use the returned
	 *         variable reference and not use references
	 */
	public VariableReference setStatement(StatementInterface statement, int position);

	/**
	 * Define whether this test case is unstable or not
	 * 
	 * @param unstable
	 */
	public void setUnstable(boolean unstable);

	/**
	 * <p>size</p>
	 *
	 * @return Number of statements
	 */
	public int size();

	/**
	 * Get Java code representation of the test case
	 *
	 * @return Code as string
	 */
	public String toCode();
	
	/**
	 * Get Java code representation of the test case
	 *
	 * @return Code as string
	 * @param exceptions a {@link java.util.Map} object.
	 */
	public String toCode(Map<Integer, Throwable> exceptions);
	
}
