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

import org.evosuite.assertion.Assertion;
import org.evosuite.contracts.ContractViolation;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.environment.AccessedEnvironment;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Listenable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A test case is a sequence of {@link Statement statements}.
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
    int getID();

    /**
     * Handle test visitor
     *
     * @param visitor a {@link org.evosuite.testcase.TestVisitor} object.
     */
    void accept(TestVisitor visitor);

    /**
     * Copy all the assertions from other test case
     *
     * @param other The other test case
     */
    void addAssertions(TestCase other);

    /**
     * Keep track of an additional covered goal
     *
     * @param goal a {@link org.evosuite.testcase.TestFitnessFunction} object.
     */
    void addCoveredGoal(TestFitnessFunction goal);

    /**
     * Remove goal that may have been covered
     *
     * @param goal a {@link org.evosuite.testcase.TestFitnessFunction} object.
     */
    void removeCoveredGoal(TestFitnessFunction goal);

    /**
     * Keep track of an additional test failure
     *
     * @param violation a {@link org.evosuite.contracts.ContractViolation} object.
     */
    void addContractViolation(ContractViolation violation);

    /**
     * Append new statement at end of test case
     *
     * @param statement New statement
     * @return VariableReference of return value
     */
    VariableReference addStatement(Statement statement);

    /**
     * Add new statement at position and fix following variable references
     *
     * @param statement New statement
     * @param position  Position at which to add
     * @return Return value of statement. Notice that the test might choose to
     * modify the statement you inserted. You should use the returned
     * variable reference and not use references
     */
    VariableReference addStatement(Statement statement, int position);

    /**
     * <p>addStatements</p>
     *
     * @param statements a {@link java.util.List} object.
     */
    void addStatements(List<? extends Statement> statements);

    /**
     * Remove all statements after a given position
     *
     * @param length Length of the test case after chopping
     */
    void chop(int length);

    int sliceFor(VariableReference var);

    /**
     * Remove all covered goals
     */
    void clearCoveredGoals();


    boolean contains(Statement statement);

    /**
     * <p>clone</p>
     *
     * @return a {@link org.evosuite.testcase.TestCase} object.
     */
    TestCase clone();

    /**
     * Determine the set of classes that are accessed by the test case
     *
     * @return Set of accessed classes
     */
    Set<Class<?>> getAccessedClasses();

    /**
     * Retrieve an object containing information about what environment components this test interacted with
     *
     * @return a {@link java.util.List} object.
     */
    AccessedEnvironment getAccessedEnvironment();

    /**
     * Get all assertions that exist for this test case
     *
     * @return List of assertions
     * <p>
     *         TODO: Also return ExceptionAssertion?
     */
    List<Assertion> getAssertions();

    /**
     * Retrieve all violations observed during test execution
     *
     * @return
     */
    Set<ContractViolation> getContractViolations();


    /**
     * Retrieve all coverage goals covered by this test
     *
     * @return a {@link java.util.Set} object.
     */
    Set<TestFitnessFunction> getCoveredGoals();

    /**
     * <p>getDeclaredExceptions</p>
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Class<?>> getDeclaredExceptions();

    /**
     * Determine the set of variables that var depends on
     *
     * @param var Variable to check for
     * @return Set of dependency variables
     */
    Set<VariableReference> getDependencies(VariableReference var);

    /**
     * Get the last object of the defined type
     *
     * @param type
     * @return
     * @throws ConstructionFailedException
     */
    VariableReference getLastObject(Type type)
            throws ConstructionFailedException;

    /**
     * Get the last object of the defined type
     *
     * @param type
     * @return
     * @throws ConstructionFailedException
     */
    VariableReference getLastObject(Type type, int position)
            throws ConstructionFailedException;


    /**
     * Get actual object represented by a variable for a given execution scope
     *
     * @param reference Variable
     * @param scope     Excution scope
     * @return Object in scope
     */
    Object getObject(VariableReference reference, Scope scope);

    /**
     * Get all objects up to the given position.
     *
     * @param position a int.
     * @return a {@link java.util.List} object.
     */
    List<VariableReference> getObjects(int position);

    /**
     * Get all objects up to position satisfying constraint
     *
     * @param type     a {@link java.lang.reflect.Type} object.
     * @param position a int.
     * @return a {@link java.util.List} object.
     */
    List<VariableReference> getObjects(Type type, int position);

    /**
     * Get a random object matching type
     *
     * @param type     a {@link java.lang.reflect.Type} object.
     * @param position Upper bound in test case up to which objects are considered
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @throws org.evosuite.ga.ConstructionFailedException if no such object exists
     */
    VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position)
            throws ConstructionFailedException;

    /**
     * Get a random object matching type
     *
     * @param type     a {@link java.lang.reflect.Type} object.
     * @param position Upper bound in test case up to which objects are considered
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @throws org.evosuite.ga.ConstructionFailedException if no such object exists
     */
    VariableReference getRandomNonNullObject(Type type, int position)
            throws ConstructionFailedException;

    /**
     * Get a random object matching type
     *
     * @return Random object
     * @throws ConstructionFailedException if any.
     */
    VariableReference getRandomObject();

    /**
     * Get a random object matching type
     *
     * @param position Upper bound in test case up to which objects are considered
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @throws ConstructionFailedException if no such object exists
     */
    VariableReference getRandomObject(int position);

    /**
     * Get a random object matching type
     *
     * @param type Class we are looking for
     * @return Random object
     * @throws org.evosuite.ga.ConstructionFailedException if any.
     */
    VariableReference getRandomObject(Type type)
            throws ConstructionFailedException;

    /**
     * Get a random object matching type
     *
     * @param type     a {@link java.lang.reflect.Type} object.
     * @param position Upper bound in test case up to which objects are considered
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @throws org.evosuite.ga.ConstructionFailedException if no such object exists
     */
    VariableReference getRandomObject(Type type, int position)
            throws ConstructionFailedException;

    /**
     * Determine the set of variables that depend on var
     *
     * @param var Variable to check for
     * @return Set of dependent variables
     */
    Set<VariableReference> getReferences(VariableReference var);

    /**
     * Get return value (variable) of statement at position
     *
     * @param position a int.
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    VariableReference getReturnValue(int position);

    /**
     * Access statement by index
     *
     * @param position Index of statement
     * @return Statement at position
     */
    Statement getStatement(int position);

    /**
     * Check if there is a statement at the given position.
     *
     * @param position Index of statement
     * @return Whether or not there is a statement at the given position.
     */
    boolean hasStatement(int position);

    /**
     * Check if there are any assertions
     *
     * @return True if there are assertions
     */
    boolean hasAssertions();

    /**
     * <p>hasCastableObject</p>
     *
     * @param type a {@link java.lang.reflect.Type} object.
     * @return a boolean.
     */
    boolean hasCastableObject(Type type);

    /**
     * Check if the test case has an object of a given class
     *
     * @param type     Type to look for
     * @param position Upper bound up to which the test is checked
     * @return True if there is something usable
     */
    boolean hasObject(Type type, int position);

    /**
     * Check if var is referenced after its definition
     *
     * @param var Variable to check for
     * @return True if there is a use of var
     */
    boolean hasReferences(VariableReference var);


    /**
     * Check if all methods/fields accessed are accessible also for the current SUT
     *
     * @return
     */
    boolean isAccessible();

    /**
     * <p>isEmpty</p>
     *
     * @return true if size()==0
     */
    boolean isEmpty();

    boolean isFailing();

    void setFailing();

    /**
     * Check if the current test case does cover the given goal.
     *
     * @param goal
     * @return
     */
    boolean isGoalCovered(TestFitnessFunction goal);

    /**
     * Check if this test case is a prefix of t
     *
     * <p>
     * A test case {@code A} is a prefix of a test case {@code B} if
     * and only if the first {@code length(A)} statements of {@code B} are
     * equal to ones of {@code A}, in the same order.
     * In other words, {@code B} can be seen as an extension of {@code A}.
     *
     * @param t Test case to check against
     * @return True if this test is a prefix of t
     */
    boolean isPrefix(TestCase t);

    /**
     * A test can be unstable if its assertions fail, eg due to non-determinism,
     * non-properly handled static variables and side effects on environment, etc
     *
     * @return
     */
    boolean isUnstable();

    /**
     * Check if test case is valid (executable)
     *
     * @return a boolean.
     */
    boolean isValid();

    /**
     * Remove statement at position and fix variable references
     *
     * @param position a int.
     */
    void remove(int position);

    /**
     * Remove assertion from test case
     */
    void removeAssertion(Assertion assertion);

    /**
     * Remove all assertions from test case
     */
    void removeAssertions();

    /**
     * Replace a VariableReference with another one
     *
     * @param var1 The old variable
     * @param var2 The new variable
     */
    void replace(VariableReference var1, VariableReference var2);


    /**
     * Set new statement at position
     *
     * @param statement New statement
     * @param position  Position at which to add
     * @return Return value of statement. Notice that the test might choose to
     * modify the statement you inserted. You should use the returned
     * variable reference and not use references
     */
    VariableReference setStatement(Statement statement, int position);

    /**
     * Define whether this test case is unstable or not
     *
     * @param unstable
     */
    void setUnstable(boolean unstable);

    /**
     * <p>size</p>
     *
     * @return Number of statements
     */
    int size();

    /**
     * Get number of statements plus the number of assertions
     *
     * @return Number of statements plus number of assertions
     */
    int sizeWithAssertions();

    /**
     * Get Java code representation of the test case
     *
     * @return Code as string
     */
    String toCode();

    /**
     * Get Java code representation of the test case
     *
     * @param exceptions a {@link java.util.Map} object.
     * @return Code as string
     */
    String toCode(Map<Integer, Throwable> exceptions);

}
