package de.unisb.cs.st.evosuite.junit;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.assertion.Assertion;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.TestVisitor;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.evosuite.utils.Listener;

/**
 * This is a wrapper for a {@link TestCase} that allows
 * {@link StatementInterface}s to reference this test case and still exchange
 * the wrapped test case.
 * 
 * @author roessler
 * 
 */
public class DelegatingTestCase implements TestCase {

	private static final long serialVersionUID = 1L;

	private TestCase delegate;
	private final Set<Listener<Void>> listeners = new HashSet<Listener<Void>>();

	@Override
	public void accept(TestVisitor visitor) {
		delegate.accept(visitor);
	}

	@Override
	public void addAssertions(TestCase other) {
		delegate.addAssertions(other);
	}

	@Override
	public void addCoveredGoal(TestFitnessFunction goal) {
		delegate.addCoveredGoal(goal);
	}

	@Override
	public void addListener(Listener<Void> listener) {
		listeners.add(listener);
	}

	@Override
	public VariableReference addStatement(StatementInterface statement) {
		return delegate.addStatement(statement);
	}

	@Override
	public VariableReference addStatement(StatementInterface statement, int position) {
		return delegate.addStatement(statement, position);
	}

	@Override
	public void addStatements(List<? extends StatementInterface> statements) {
		delegate.addStatements(statements);
	}

	@Override
	public void chop(int length) {
		delegate.chop(length);
	}

	@Override
	public void clearCoveredGoals() {
		delegate.clearCoveredGoals();
	}

	@Override
	public TestCase clone() {
		return delegate.clone();
	}

	@Override
	public void deleteListener(Listener<Void> listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean equals(Object other) {
		if (delegate != null) {
			return delegate.equals(other);
		}
		return super.equals(other);
	}

	@Override
	public Set<Class<?>> getAccessedClasses() {
		return delegate.getAccessedClasses();
	}

	@Override
	public List<String> getAccessedFiles() {
		return delegate.getAccessedFiles();
	}

	@Override
	public List<Assertion> getAssertions() {
		return delegate.getAssertions();
	}

	@Override
	public Set<TestFitnessFunction> getCoveredGoals() {
		return delegate.getCoveredGoals();
	}

	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		return delegate.getDeclaredExceptions();
	}

	public TestCase getDelegate() {
		return delegate;
	}

	@Override
	public Set<VariableReference> getDependencies(VariableReference var) {
		return delegate.getDependencies(var);
	}

	@Override
	public Object getObject(VariableReference reference, Scope scope) {
		return delegate.getObject(reference, scope);
	}

	@Override
	public List<VariableReference> getObjects(int position) {
		return delegate.getObjects(position);
	}

	@Override
	public List<VariableReference> getObjects(Type type, int position) {
		return delegate.getObjects(type, position);
	}

	@Override
	public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {
		return delegate.getRandomNonNullObject(type, position);
	}

	@Override
	public VariableReference getRandomObject() {
		return delegate.getRandomObject();
	}

	@Override
	public VariableReference getRandomObject(int position) {
		return delegate.getRandomObject(position);
	}

	@Override
	public VariableReference getRandomObject(Type type) throws ConstructionFailedException {
		return delegate.getRandomObject(type);
	}

	@Override
	public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {
		return delegate.getRandomObject(type, position);
	}

	@Override
	public Set<VariableReference> getReferences(VariableReference var) {
		return delegate.getReferences(var);
	}

	@Override
	public VariableReference getReturnValue(int position) {
		return delegate.getReturnValue(position);
	}

	@Override
	public StatementInterface getStatement(int position) {
		return delegate.getStatement(position);
	}

	@Override
	public boolean hasAssertions() {
		return delegate.hasAssertions();
	}

	@Override
	public boolean hasCalls() {
		return delegate.hasCalls();
	}

	@Override
	public boolean hasCastableObject(Type type) {
		return delegate.hasCastableObject(type);
	}

	@Override
	public int hashCode() {
		if (delegate != null) {
			return delegate.hashCode();
		}
		return super.hashCode();
	}

	@Override
	public boolean hasObject(Type type, int position) {
		return delegate.hasObject(type, position);
	}

	@Override
	public boolean hasReferences(VariableReference var) {
		return delegate.hasReferences(var);
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean isFinished() {
		return delegate != null;
	}

	@Override
	public boolean isPrefix(TestCase t) {
		return delegate.isPrefix(t);
	}

	@Override
	public boolean isValid() {
		return delegate.isValid();
	}

	@Override
	public Iterator<StatementInterface> iterator() {
		return delegate.iterator();
	}

	@Override
	public void remove(int position) {
		delegate.remove(position);
	}

	@Override
	public void removeAssertions() {
		delegate.removeAssertions();
	}

	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		delegate.replace(var1, var2);
	}

	@Override
	public void setAccessedFiles(List<String> files) {
		delegate.setAccessedFiles(files);
	}

	public void setDelegate(TestCase delegate) {
		assert this.delegate == null;
		this.delegate = delegate;
		for (Listener<Void> listener : listeners) {
			delegate.addListener(listener);
		}
	}

	@Override
	public VariableReference setStatement(StatementInterface statement, int position) {
		return delegate.setStatement(statement, position);
	}

	@Override
	public int size() {
		if (delegate == null) {
			return 0;
		}
		return delegate.size();
	}

	@Override
	public String toCode() {
		return delegate.toCode();
	}

	@Override
	public String toCode(Map<Integer, Throwable> exceptions) {
		return delegate.toCode(exceptions);
	}

	@Override
	public String toString() {
		if (delegate != null) {
			return delegate.toString();
		}
		return super.toString();
	}

	@Override
	public int hashCode() {
		if (delegate != null) {
			return delegate.hashCode();
		}
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (delegate != null) {
			return delegate.equals(other);
		}
		return super.equals(other);
	}
}
