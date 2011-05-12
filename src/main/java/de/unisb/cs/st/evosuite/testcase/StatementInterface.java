/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.assertion.Assertion;

/**
 * @author Sebastian Steenbuck
 *
 */
public interface StatementInterface {

	/**
	 * Adjust all variables up to position by delta
	 * 
	 * @param position
	 * @param delta
	 */
	public void adjustVariableReferences(int position, int delta);

	/**
	 * Check if the statement makes use of var
	 * 
	 * @param var
	 *            Variable we are checking for
	 * @return True if var is referenced
	 */
	public boolean references(VariableReference var);

	public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException;

	/**
	 * Get Java representation of statement
	 * 
	 * @return
	 */
	public String getCode();

	/**
	 * Get Java representation of statement
	 * 
	 * @return
	 */
	public String getCode(Throwable exception);

	/**
	 * Generate bytecode by calling method generator
	 * 
	 * @param mg
	 */
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
			Throwable exception);

	/**
	 * 
	 * @return Generic type of return value
	 */
	public Type getReturnType();

	/**
	 * 
	 * @return Raw class of return value
	 */
	public Class<?> getReturnClass();

	/**
	 * Equality check
	 * 
	 * @param s
	 *            Other statement
	 * @return True if equals
	 */
	public boolean equals(Object s);

	/**
	 * Generate hash code
	 */
	public int hashCode();

	/**
	 * @return Variable representing return value
	 */
	public VariableReference getReturnValue();

	public Set<VariableReference> getVariableReferences();

	public List<VariableReference> getUniqueVariableReferences();

	/**
	 * 
	 * @param newTestCase the testcase in which this statement will be inserted
	 * @return
	 */
	public StatementInterface clone(TestCase newTestCase);
	
	/**
	 * Create deep copy of statement
	 */
	public StatementInterface clone();

	/**
	 * Check if there are assertions
	 * 
	 * @return True if there are assertions
	 */
	public boolean hasAssertions();

	/**
	 * Add a new assertion to statement
	 * 
	 * @param assertion
	 *            Assertion to be added
	 */
	public void addAssertion(Assertion assertion);

	/**
	 * Get Java code representation of assertions
	 * 
	 * @return String representing all assertions attached to this statement
	 */
	public String getAssertionCode();

	/**
	 * Delete all assertions attached to this statement
	 */
	public void removeAssertions();

	/**
	 * Delete assertion attached to this statement
	 */
	public void removeAssertion(Assertion assertion);

	/**
	 * Return list of assertions
	 */
	public Set<Assertion> getAssertions();

	public Set<Class<?>> getDeclaredExceptions();

	public int getPosition();

}