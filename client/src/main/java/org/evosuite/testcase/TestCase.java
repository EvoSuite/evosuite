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
package org.evosuite.testcase;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.istack.internal.NotNull;
import org.evosuite.assertion.Assertion;
import org.evosuite.contracts.ContractViolation;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.environment.AccessedEnvironment;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Listenable;


/**
 * A test case is essentially a program that executes the system under test. Formally, a test case
 * is defined as a sequence of {@link Statement statements}. This class prvides a representation of
 * test cases, as well as useful methods to analyze and manipulate test cases.
 *
 * @author Gordon Fraser
 * @author Sebastian Steenbuck
 */
public interface TestCase extends Iterable<Statement>, Cloneable, Listenable<Void> {

	/**
	 * Get a unique ID representing this test. This is mainly useful for debugging.
	 *
	 * @return the unique ID of this test
	 */
	public int getID();

	/**
	 * Handle the given test visitor.
	 *
	 * @param visitor a {@link org.evosuite.testcase.TestVisitor} object.
	 */
	public void accept(TestVisitor visitor);

	/**
	 * Copy all the assertions from the other test case {@code other} to this test case.
	 *
	 * @param other the other test case from which to copy the assertions
	 */
	public void addAssertions(TestCase other);

	/**
	 * Keep track of an additional covered goal
	 *
	 * @param goal a {@link org.evosuite.testcase.TestFitnessFunction} object.
	 */
	public void addCoveredGoal(TestFitnessFunction goal);

	/**
	 * Remove goal that may have been covered
	 *
	 * @param goal a {@link org.evosuite.testcase.TestFitnessFunction} object.
	 */
	public void removeCoveredGoal(TestFitnessFunction goal);

	/**
	 * Keep track of an additional test failure
	 *
	 * @param violation a {@link org.evosuite.contracts.ContractViolation} object.
	 */
	public void addContractViolation(ContractViolation violation);

	/**
	 * Appends the given {@code statement} at the end of this test case.
	 *
	 * @param statement the statement to append
	 * @return VariableReference of the appended statement's return value, never {@code null}
	 */
	@NotNull
	public VariableReference addStatement(Statement statement);

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
	public VariableReference addStatement(Statement statement, int position);

	/**
	 * <p>addStatements</p>
	 *
	 * @param statements a {@link java.util.List} object.
	 */
	public void addStatements(List<? extends Statement> statements);

	/**
	 * Chops this test case to the given {@code length}. The first {@code length} statements will
	 * be kept, while all other statements will be deleted. The position of the last statement in
	 * the test case after chopping will be {@code length - 1}.
	 *
	 * @param length length of the test case after chopping
	 */
	public void chop(int length);

	public int sliceFor(VariableReference var);

	/**
	 * Remove all covered goals
	 */
	public void clearCoveredGoals();


	/**
	 * Tests if this test case contains the given {@code statement}
	 * @param statement the statement to check for
	 * @return {@code true} if this test case contains the given statement, {@code false} otherwise
	 */
	public boolean contains(Statement statement);

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
	 * Retrieve an object containing information about what environment components this test interacted with
	 *
	 * @return a {@link java.util.List} object.
	 */
	public AccessedEnvironment getAccessedEnvironment();

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
	public VariableReference getLastObject(Type type) throws ConstructionFailedException;

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
	 * Returns all objects in this test case up to the given position (exclusively).
	 *
	 * @param position exclusive upper bound for the position
	 * @return a list of objects up to {@code position}
	 */
	public List<VariableReference> getObjects(int position);

	/**
	 * Returns all objects in this test case up to the given position (exclusively) matching the
	 * specified {@code type}.
	 *
	 * @param type the type of objects to be returned
	 * @param position exclusive upper bound for the position
	 * @return a list of objects up to {@code position} where each object matches the given type
	 */
	public List<VariableReference> getObjects(Type type, int position);

	/**
	 * In this test case up to the given {@code position}, returns a random object of the given
	 * {@code type} that is neither primitive nor null. In EvoSuite, the following data types are
	 * considered primitive:
	 * <ul>
	 *     <li>all primitive data types according to JLS8 §4.2 ({@code byte}, {@code short},
	 *     {@code int}, {@code long}, {@code float}, {@code double}, {@code boolean},
	 *     {@code char}),</li>
	 *     <li>{@code String}s,</li>
	 *     <li>enumeration types ("enums"),</li>
	 *     <li>EvoSuite environment data types as defined in
	 *     {@link org.evosuite.runtime.testdata.EnvironmentDataList EnvironmentDataList}, and</li>
	 *     <li>class primitives ({@code Class.class}).</li>
	 * </ul>
	 *
	 * @param type the type of the object to return
	 * @param position upper bound in test case up to which objects are considered
	 * @throws org.evosuite.ga.ConstructionFailedException if no such object exists
	 * @return a random non-null non-primitive object matching the given type
	 */
	public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * Gets a random non-null object matching the specified {@code type} up to the given {@code
	 * position} in this test case.
	 *
	 * @param type the type of the object to return
	 * @param position upper bound in this test case up to which objects are considered
	 * @throws org.evosuite.ga.ConstructionFailedException if no such object exists
	 * @return a reference to a random non-null object
	 */
	public VariableReference getRandomNonNullObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * Returns a random object present in this test case.
	 *
	 * @return a random object
	 */
	public VariableReference getRandomObject();

	/**
	 * Returns a random object present in this test case up to the given {@code position}.
	 *
	 * @param position upper bound in this test case up to which objects are considered
	 * @return a random object
	 */
	public VariableReference getRandomObject(int position);

	/**
	 * Gets a random object matching the given {@code type}.
	 *
	 * @param type the type we are looking for
	 * @return a random object matching the given {@code type}
	 * @throws org.evosuite.ga.ConstructionFailedException if no object matching the specified
	 * {@code type} is present in this test case
	 */
	public VariableReference getRandomObject(Type type) throws ConstructionFailedException;

	/**
	 * Get a random object matching type
	 *
	 * @param type a {@link java.lang.reflect.Type} object.
	 * @param position
	 *            Upper bound in test case up to which objects are considered
	 * @throws org.evosuite.ga.ConstructionFailedException
	 *             if no such object exists
	 * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public VariableReference getRandomObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * In this test case, determines and returns the set of variables that depend on the given
	 * variable {@code var}. Returns . Note
	 * that the result set does not include {@code var} itself.
	 *
	 * @param var the variable for which to return all dependent variables
	 * @return the set of variables that depend on {@code var}
	 */
	default public Set<VariableReference> getVariablesDependingOn(VariableReference var) {
		return getVariablesDependingOn(var, false);
	}

	/**
	 * In this test case, determines and returns the set of variables that depend on the given
	 * variable {@code var}. If {@code reflexive} is {@code true}, the dependency relation used
	 * to compute the dependent variables will be reflexive, i.e., the result set will
	 * always contain at least {@code var} itself. If {@code reflexive} is {@code false},
	 * the dependency relation is not reflexive and the result set does not include {@code var}.
	 * In this case, the empty set will be returned if no variables other than {@code var} depend on
	 * {@code var}.
	 *
	 * @param var the variable for which to return all dependent variables
	 * @param reflexive whether the dependency relation should be reflexive (see above)
	 * @return the set of variables that depend on {@code var}
	 */
	public Set<VariableReference> getVariablesDependingOn(VariableReference var, boolean reflexive);

	/**
	 * Get return value (variable) of statement at position
	 *
	 * @param position a int.
	 * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public VariableReference getReturnValue(int position);

	/**
	 * Access a statement of this test case by the given {@code index}.
	 *
	 * @param position the index of the statement to return
	 * @return the statement at the given {@code position}
	 */
	public Statement getStatement(int position);

	/**
	 * Checks if there is a statement at the given position.
	 *
	 * @param position the index to check
	 * @return whether or not there is a statement at the given position
	 */
	public boolean hasStatement(int position);

	/**
	 * Checks if there are any assertions in this test case.
	 *
	 * @return {@code true} if there are assertions, {@code false} otherwise
	 * @see Statement#hasAssertions()
	 * @see Assertion
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

	public void setFailing();

	/**
	 * Check if the current test case does cover the given goal.
	 * @param goal
	 * @return
	 */
	public boolean isGoalCovered(TestFitnessFunction goal);

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
	public VariableReference setStatement(Statement statement, int position);

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
	 * Get number of statements plus the number of assertions
	 *
	 * @return Number of statements plus number of assertions
	 */
	public int sizeWithAssertions();

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
