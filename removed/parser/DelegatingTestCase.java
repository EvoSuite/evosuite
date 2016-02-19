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
package org.evosuite.junit;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.assertion.Assertion;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.TestVisitor;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.Listener;


/**
 * This is a wrapper for a {@link TestCase} that allows
 * {@link StatementInterface}s to reference this test case and still exchange
 * the wrapped test case.
 *
 * @author roessler
 */
public class DelegatingTestCase implements TestCase {

	private static final long serialVersionUID = 1L;

	private TestCase delegate;
	private final Set<Listener<Void>> listeners = new HashSet<Listener<Void>>();

	/** {@inheritDoc} */
	@Override
	public void accept(TestVisitor visitor) {
		delegate.accept(visitor);
	}

	/** {@inheritDoc} */
	@Override
	public void addAssertions(TestCase other) {
		delegate.addAssertions(other);
	}

	/** {@inheritDoc} */
	@Override
	public void addCoveredGoal(TestFitnessFunction goal) {
		delegate.addCoveredGoal(goal);
	}

	/** {@inheritDoc} */
	@Override
	public void addListener(Listener<Void> listener) {
		listeners.add(listener);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference addStatement(StatementInterface statement) {
		return delegate.addStatement(statement);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference addStatement(StatementInterface statement, int position) {
		return delegate.addStatement(statement, position);
	}

	/** {@inheritDoc} */
	@Override
	public void addStatements(List<? extends StatementInterface> statements) {
		delegate.addStatements(statements);
	}

	/** {@inheritDoc} */
	@Override
	public void chop(int length) {
		delegate.chop(length);
	}

	/** {@inheritDoc} */
	@Override
	public void clearCoveredGoals() {
		delegate.clearCoveredGoals();
	}

	/** {@inheritDoc} */
	@Override
	public TestCase clone() {
		return delegate.clone();
	}

	/** {@inheritDoc} */
	@Override
	public void deleteListener(Listener<Void> listener) {
		listeners.remove(listener);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object other) {
		if (delegate != null) {
			return delegate.equals(other);
		}
		return super.equals(other);
	}
	
	/** {@inheritDoc} */
	@Override
	public Set<Class<?>> getAccessedClasses() {
		return delegate.getAccessedClasses();
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getAccessedFiles() {
		return delegate.getAccessedFiles();
	}

	/** {@inheritDoc} */
	@Override
	public List<Assertion> getAssertions() {
		return delegate.getAssertions();
	}

	/** {@inheritDoc} */
	@Override
	public Set<TestFitnessFunction> getCoveredGoals() {
		return delegate.getCoveredGoals();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		return delegate.getDeclaredExceptions();
	}

	/**
	 * <p>Getter for the field <code>delegate</code>.</p>
	 *
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase getDelegate() {
		return delegate;
	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getDependencies(VariableReference var) {
		return delegate.getDependencies(var);
	}

	/** {@inheritDoc} */
	@Override
	public Object getObject(VariableReference reference, Scope scope) {
		return delegate.getObject(reference, scope);
	}

	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getObjects(int position) {
		return delegate.getObjects(position);
	}

	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getObjects(Type type, int position) {
		return delegate.getObjects(type, position);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {
		return delegate.getRandomNonNullObject(type, position);
	}
	

	@Override
	public VariableReference getRandomNonNullNonPrimitiveObject(Type type,
			int position) throws ConstructionFailedException {
		return delegate.getRandomNonNullNonPrimitiveObject(type, position);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject() {
		return delegate.getRandomObject();
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject(int position) {
		return delegate.getRandomObject(position);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject(Type type) throws ConstructionFailedException {
		return delegate.getRandomObject(type);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {
		return delegate.getRandomObject(type, position);
	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getReferences(VariableReference var) {
		return delegate.getReferences(var);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference getReturnValue(int position) {
		return delegate.getReturnValue(position);
	}

	/** {@inheritDoc} */
	@Override
	public StatementInterface getStatement(int position) {
		return delegate.getStatement(position);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAssertions() {
		return delegate.hasAssertions();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasCalls() {
		return delegate.hasCalls();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasCastableObject(Type type) {
		return delegate.hasCastableObject(type);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (delegate != null) {
			return delegate.hashCode();
		}
		return super.hashCode();
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean hasObject(Type type, int position) {
		return delegate.hasObject(type, position);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasReferences(VariableReference var) {
		return delegate.hasReferences(var);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * <p>isFinished</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFinished() {
		return delegate != null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPrefix(TestCase t) {
		return delegate.isPrefix(t);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isValid() {
		return delegate.isValid();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<StatementInterface> iterator() {
		return delegate.iterator();
	}

	/** {@inheritDoc} */
	@Override
	public void remove(int position) {
		delegate.remove(position);
	}

	/** {@inheritDoc} */
	@Override
	public void removeAssertions() {
		delegate.removeAssertions();
	}
	
	@Override
	public void removeAssertion(Assertion assertion) {
		delegate.removeAssertion(assertion);
	}

	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		delegate.replace(var1, var2);
	}

	/** {@inheritDoc} */
	@Override
	public void setAccessedFiles(List<String> files) {
		delegate.setAccessedFiles(files);
	}

	/**
	 * <p>Setter for the field <code>delegate</code>.</p>
	 *
	 * @param delegate a {@link org.evosuite.testcase.TestCase} object.
	 */
	public void setDelegate(TestCase delegate) {
		assert this.delegate == null;
		this.delegate = delegate;
		for (Listener<Void> listener : listeners) {
			delegate.addListener(listener);
		}
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference setStatement(StatementInterface statement, int position) {
		return delegate.setStatement(statement, position);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		if (delegate == null) {
			return 0;
		}
		return delegate.size();
	}

	/** {@inheritDoc} */
	@Override
	public String toCode() {
		return delegate.toCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toCode(Map<Integer, Throwable> exceptions) {
		return delegate.toCode(exceptions);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (delegate != null) {
			return delegate.toString();
		}
		return super.toString();
	}

	

}
