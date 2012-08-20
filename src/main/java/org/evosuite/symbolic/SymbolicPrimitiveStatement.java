/**
 * 
 */
package org.evosuite.symbolic;

import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.assertion.Assertion;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.VariableReference;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * @author Gordon Fraser
 * 
 */
public class SymbolicPrimitiveStatement implements StatementInterface {

	private final PrimitiveStatement<?> concreteStatement;

	public SymbolicPrimitiveStatement(PrimitiveStatement<?> statement) {
		this.concreteStatement = statement;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#references(org.evosuite.testcase.VariableReference)
	 */
	@Override
	public boolean references(VariableReference var) {
		return concreteStatement.references(var);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		concreteStatement.replace(var1, var2);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#execute(org.evosuite.testcase.Scope, java.io.PrintStream)
	 */
	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {

		// TODO: Add symbolic stuff to set up DSC 

		return concreteStatement.execute(scope, out);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#isValid()
	 */
	@Override
	public boolean isValid() {
		// FIXXME
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getBytecode(org.objectweb.asm.commons.GeneratorAdapter, java.util.Map, java.lang.Throwable)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {
		concreteStatement.getBytecode(mg, locals, exception);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getReturnType()
	 */
	@Override
	public Type getReturnType() {
		return concreteStatement.getReturnType();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getReturnClass()
	 */
	@Override
	public Class<?> getReturnClass() {
		return concreteStatement.getReturnClass();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getCode()
	 */
	@Override
	public String getCode() {
		return concreteStatement.getCode();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getCode(java.lang.Throwable)
	 */
	@Override
	public String getCode(Throwable exception) {
		return concreteStatement.getCode(exception);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getReturnValue()
	 */
	@Override
	public VariableReference getReturnValue() {
		return concreteStatement.getReturnValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getVariableReferences()
	 */
	@Override
	public Set<VariableReference> getVariableReferences() {
		return concreteStatement.getVariableReferences();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getUniqueVariableReferences()
	 */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return concreteStatement.getUniqueVariableReferences();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#clone(org.evosuite.testcase.TestCase)
	 */
	@Override
	public StatementInterface clone(TestCase newTestCase) {
		return new SymbolicPrimitiveStatement(
		        (PrimitiveStatement<?>) concreteStatement.clone(newTestCase));
	}

	/** {@inheritDoc} */
	@Override
	public final StatementInterface clone() {
		throw new UnsupportedOperationException("Use statementInterface.clone(TestCase)");
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#copy(org.evosuite.testcase.TestCase, int)
	 */
	@Override
	public StatementInterface copy(TestCase newTestCase, int offset) {
		return new SymbolicPrimitiveStatement(
		        (PrimitiveStatement<?>) concreteStatement.copy(newTestCase, offset));
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#copyAssertions(org.evosuite.testcase.TestCase, int)
	 */
	@Override
	public Set<Assertion> copyAssertions(TestCase newTestCase, int offset) {
		return concreteStatement.copyAssertions(newTestCase, offset);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#hasAssertions()
	 */
	@Override
	public boolean hasAssertions() {
		return concreteStatement.hasAssertions();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#addAssertion(org.evosuite.assertion.Assertion)
	 */
	@Override
	public void addAssertion(Assertion assertion) {
		concreteStatement.addAssertion(assertion);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#setAssertions(java.util.Set)
	 */
	@Override
	public void setAssertions(Set<Assertion> assertions) {
		concreteStatement.setAssertions(assertions);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getAssertionCode()
	 */
	@Override
	public String getAssertionCode() {
		return concreteStatement.getAssertionCode();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#removeAssertions()
	 */
	@Override
	public void removeAssertions() {
		concreteStatement.removeAssertions();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#removeAssertion(org.evosuite.assertion.Assertion)
	 */
	@Override
	public void removeAssertion(Assertion assertion) {
		concreteStatement.removeAssertion(assertion);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getAssertions()
	 */
	@Override
	public Set<Assertion> getAssertions() {
		return concreteStatement.getAssertions();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getDeclaredExceptions()
	 */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		return concreteStatement.getDeclaredExceptions();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getPosition()
	 */
	@Override
	public int getPosition() {
		return concreteStatement.getPosition();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#same(org.evosuite.testcase.StatementInterface)
	 */
	@Override
	public boolean same(StatementInterface s) {
		return concreteStatement.same(s);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#isDeclaredException(java.lang.Throwable)
	 */
	@Override
	public boolean isDeclaredException(Throwable t) {
		return concreteStatement.isDeclaredException(t);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.AbstractTestFactory)
	 */
	@Override
	public boolean mutate(TestCase test, TestFactory factory) {
		return concreteStatement.mutate(test, factory);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#setRetval(org.evosuite.testcase.VariableReference)
	 */
	@Override
	public void setRetval(VariableReference newRetVal) {
		concreteStatement.setRetval(newRetVal);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getAccessibleObject()
	 */
	@Override
	public AccessibleObject getAccessibleObject() {
		return concreteStatement.getAccessibleObject();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#isAssignmentStatement()
	 */
	@Override
	public boolean isAssignmentStatement() {
		return concreteStatement.isAssignmentStatement();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		concreteStatement.changeClassLoader(loader);
	}

}
