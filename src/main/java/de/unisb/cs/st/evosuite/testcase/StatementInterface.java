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
	 * Check if the statement makes use of var
	 * 
	 * @param var
	 *            Variable we are checking for
	 * @return True if var is referenced
	 */
	public boolean references(VariableReference var);

	/**
	 * Replace a VariableReference with another one
	 * 
	 * @param var1
	 *            The old variable
	 * @param var2
	 *            The new variable
	 */
	public void replace(VariableReference var1, VariableReference var2);

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
	 * Various consistency checks. This method might also return with an
	 * assertionError Functionality might depend on the status of
	 * enableAssertions in this JVM
	 * 
	 * @return
	 */
	public boolean isValid();

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
	@Override
	public boolean equals(Object s);

	/**
	 * Generate hash code
	 */
	@Override
	public int hashCode();

	/**
	 * @return Variable representing return value
	 */
	public VariableReference getReturnValue();

	public Set<VariableReference> getVariableReferences();

	public List<VariableReference> getUniqueVariableReferences();

	/**
	 * 
	 * @param newTestCase
	 *            the testcase in which this statement will be inserted
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

	/**
	 * Allows the comparing of Statements between TestCases. I.e. this is a more
	 * semantic comparison than the one done by equals. E.g. two Variable are
	 * equal if they are at the same position and they reference to objects of
	 * the same type.
	 * 
	 * @param s
	 * @return
	 */
	public boolean same(StatementInterface s);

	public boolean isValidException(Throwable t);

}