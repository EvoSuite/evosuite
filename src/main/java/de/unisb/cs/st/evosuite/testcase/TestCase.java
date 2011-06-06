/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.assertion.Assertion;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;

/**
 * 
 * A test case is a list of statements
 * 
 * @author Sebastian Steenbuck
 *
 */
public interface TestCase extends Iterable<StatementInterface>, Cloneable {

	/**
	 * 
	 * @return Number of statements
	 */
	public int size();

	/**
	 * 
	 * @return true if size()==0
	 */
	public boolean isEmpty();

	/**
	 * Remove all statements after a given position
	 * 
	 * @param length
	 *            Length of the test case after chopping
	 */
	public void chop(int length);

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
	 */
	public String toCode(Map<Integer, Throwable> exceptions);

	/**
	 * Get all objects up to position satisfying constraint
	 * 
	 * @param type
	 * @param position
	 * @return
	 */
	public List<VariableReference> getObjects(Type type, int position);

	/**
	 * Get all objects up to position satisfying constraint
	 * 
	 * @param type
	 * @param position
	 * @param constraint
	 * @return
	 */
	public List<VariableReference> getObjects(int position);

	/**
	 * Get a random object matching type
	 * 
	 * @param type
	 *            Class we are looking for
	 * @return Random object
	 * @throws ConstructionFailedException
	 */
	public VariableReference getRandomObject();

	/**
	 * Get a random object matching type
	 * 
	 * @param type
	 * @param position
	 *            Upper bound in test case up to which objects are considered
	 * @return
	 * @throws ConstructionFailedException
	 *             if no such object exists
	 */
	public VariableReference getRandomObject(int position);

	/**
	 * Get a random object matching type
	 * 
	 * @param type
	 *            Class we are looking for
	 * @return Random object
	 * @throws ConstructionFailedException
	 */
	public VariableReference getRandomObject(Type type)
			throws ConstructionFailedException;

	/**
	 * Get a random object matching type
	 * 
	 * @param type
	 * @param position
	 *            Upper bound in test case up to which objects are considered
	 * @return
	 * @throws ConstructionFailedException
	 *             if no such object exists
	 */
	public VariableReference getRandomObject(Type type, int position)
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
	 * Set new statement at position
	 * 
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement. Notice that the test might choose to modify the statement you inserted. You should use the returned variable reference and not use references 
	 */
	public VariableReference setStatement(StatementInterface statement, int position);

	/**
	 * Add new statement at position and fix following variable references
	 * 
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement. Notice that the test might choose to modify the statement you inserted. You should use the returned variable reference and not use references 
	 */
	public VariableReference addStatement(StatementInterface statement, int position);

	/**
	 * Append new statement at end of test case
	 * 
	 * @param statement
	 *            New statement
	 * @return VariableReference of return value
	 */
	public VariableReference addStatement(StatementInterface statement);

	/**
	 * Get return value (variable) of statement at position
	 * 
	 * @param position
	 * @return
	 */
	public VariableReference getReturnValue(int position);

	/**
	 * Check if var is referenced after its definition
	 * 
	 * @param var
	 *            Variable to check for
	 * @return True if there is a use of var
	 */
	public boolean hasReferences(VariableReference var);

	public List<VariableReference> getReferences(VariableReference var);

	/**
	 * Remove statement at position and fix variable references
	 * 
	 * @param position
	 */
	public void remove(int position);

	/**
	 * Access statement by index
	 * 
	 * @param position
	 *            Index of statement
	 * @return Statement at position
	 */
	public StatementInterface getStatement(int position);

	/**
	 * Check if this test case is a prefix of t
	 * 
	 * @param t
	 *            Test case to check against
	 * @return True if this test is a prefix of t
	 */
	public boolean isPrefix(TestCase t);

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

	public boolean hasCastableObject(Type type);

	/**
	 * Determine the set of classes that are accessed by the test case
	 * 
	 * @return Set of accessed classes
	 */
	public Set<Class<?>> getAccessedClasses();

	/**
	 * Copy all the assertions from other test case
	 * 
	 * @param other
	 *            The other test case
	 * 
	 */
	public void addAssertions(TestCase other);

	/**
	 * Check if there are any assertions
	 * 
	 * @return True if there are assertions
	 */
	public boolean hasAssertions();

	/**
	 * Get all assertions that exist for this test case
	 * 
	 * @return List of assertions
	 * 
	 *         TODO: Also return ExceptionAssertion?
	 */
	public List<Assertion> getAssertions();

	/**
	 * Remove all assertions from test case
	 */
	public void removeAssertions();

	/**
	 * Check if test case is valid (executable)
	 * 
	 * @return
	 */
	public boolean isValid();

	public Set<Class<?>> getDeclaredExceptions();

	public boolean hasCalls();

	public void addCoveredGoal(TestFitnessFunction goal);

	public Set<TestFitnessFunction> getCoveredGoals();
	
	public TestCase clone();

}