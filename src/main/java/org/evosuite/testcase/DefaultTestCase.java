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
package org.evosuite.testcase;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.assertion.Assertion;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testsuite.TestCallStatement;
import org.evosuite.utils.ListenableList;
import org.evosuite.utils.Listener;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test case is a list of statements
 *
 * @author Gordon Fraser
 */
public class DefaultTestCase implements TestCase, Serializable {

	private static final long serialVersionUID = -689512549778944250L;

	private static Logger logger = LoggerFactory.getLogger(DefaultTestCase.class);

	private List<String> accessedFiles = new ArrayList<String>();

	/** The statements */
	protected final ListenableList<StatementInterface> statements;

	// a list of all goals this test covers
	private final HashSet<TestFitnessFunction> coveredGoals = new HashSet<TestFitnessFunction>();

	/** {@inheritDoc} */
	@Override
	public void addStatements(List<? extends StatementInterface> statements) {
		this.statements.addAll(statements);
	}

	/**
	 * Constructor
	 */
	public DefaultTestCase() {
		statements = new ListenableList<StatementInterface>(
		        new ArrayList<StatementInterface>());
	}

	/**
	 * Convenience constructor
	 *
	 * @param statements a {@link java.util.List} object.
	 */
	public DefaultTestCase(List<StatementInterface> statements) {
		if (statements instanceof ListenableList) {
			this.statements = (ListenableList<StatementInterface>) statements;
		} else {
			this.statements = new ListenableList<StatementInterface>(statements);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#size()
	 */
	/** {@inheritDoc} */
	@Override
	public int size() {
		return statements.size();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#isEmpty()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return statements.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#chop(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void chop(int length) {
		while (statements.size() > length) {
			statements.remove(length);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#toCode()
	 */
	/** {@inheritDoc} */
	@Override
	public String toCode() {
		TestCodeVisitor visitor = new TestCodeVisitor();
		accept(visitor);
		return visitor.getCode();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#toCode(java.util.Map)
	 */
	/** {@inheritDoc} */
	@Override
	public String toCode(Map<Integer, Throwable> exceptions) {
		TestCodeVisitor visitor = new TestCodeVisitor();
		visitor.setExceptions(exceptions);
		accept(visitor);
		return visitor.getCode();
	}

	private void addFields(List<VariableReference> variables, VariableReference var,
	        Type type) {

		if (!var.isPrimitive() && !(var instanceof NullReference)) {
			// add fields of this object to list
			for (Field field : StaticTestCluster.getAccessibleFields(var.getVariableClass())) {
				Type fieldType = field.getType();
				try {
					fieldType = field.getGenericType();
				} catch (java.lang.reflect.GenericSignatureFormatError e) {
					// Ignore
					fieldType = field.getType();
				}
				FieldReference f = new FieldReference(this, field, fieldType, var);
				if (f.getDepth() <= 2) {
					if (type != null) {
						if (f.isAssignableTo(type) && !variables.contains(f)) {
							variables.add(f);
						}
					} else if (!variables.contains(f)) {
						variables.add(f);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getObjects(java.lang.reflect.Type, int)
	 */
	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getObjects(Type type, int position) {
		List<VariableReference> variables = new LinkedList<VariableReference>();

		for (int i = 0; i < position && i < size(); i++) {
			VariableReference value = statements.get(i).getReturnValue();
			if (value == null)
				continue;
			if (value instanceof ArrayReference) {
				if (value.isAssignableTo(type)) {
					variables.add(value);
				} else if (GenericClass.isAssignable(type, value.getComponentType())) {
					/*
					logger.info("Found compatible array for " + type + ": "
					        + value.getSimpleClassName() + " " + value.getName() + " - "
					        + value.getVariableClass());
					        */
					for (int index = 0; index < ((ArrayReference) value).getArrayLength(); index++) {
						//logger.info("Adding array index " + index + " to array "
						//       + value.getSimpleClassName() + " " + value.getName());
						variables.add(new ArrayIndex(this, (ArrayReference) value, index));
					}
				}
			} else if (value instanceof ArrayIndex) {
				// Don't need to add this because array indices are created for array statement
			} else if (value.isAssignableTo(type)) {
				variables.add(value);
			} else {
				addFields(variables, value, type);
			}
		}

		return variables;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getObjects(int)
	 */
	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getObjects(int position) {
		List<VariableReference> variables = new LinkedList<VariableReference>();

		for (int i = 0; i < position && i < statements.size(); i++) {
			VariableReference value = statements.get(i).getReturnValue();

			if (value == null)
				continue;
			// TODO: Need to support arrays that were not self-created
			if (value instanceof ArrayReference) { // &&
				for (int index = 0; index < ((ArrayReference) value).getArrayLength(); index++) {
					variables.add(new ArrayIndex(this, (ArrayReference) value, index));
				}
			} else if (!(value instanceof ArrayIndex)) {
				variables.add(value);
				addFields(variables, value, null);
			}
			// logger.trace(statements.get(i).retval.getSimpleClassName());
		}

		return variables;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getRandomObject()
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject() {
		return getRandomObject(statements.size());
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getRandomObject(int)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject(int position) {
		List<VariableReference> variables = getObjects(position);
		if (variables.isEmpty())
			return null;

		return Randomness.choice(variables);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getRandomObject(java.lang.reflect.Type)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject(Type type)
	        throws ConstructionFailedException {
		return getRandomObject(type, statements.size());
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getRandomObject(java.lang.reflect.Type, int)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomObject(Type type, int position)
	        throws ConstructionFailedException {
		assert (type != null);
		List<VariableReference> variables = getObjects(type, position);
		if (variables.isEmpty())
			throw new ConstructionFailedException("Found no variables of type " + type
			        + " at position " + position);

		return Randomness.choice(variables);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getRandomObject(java.lang.reflect.Type, int)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getRandomNonNullObject(Type type, int position)
	        throws ConstructionFailedException {
		assert (type != null);
		List<VariableReference> variables = getObjects(type, position);
		Iterator<VariableReference> iterator = variables.iterator();
		while (iterator.hasNext()) {
			if (iterator.next() instanceof NullReference)
				iterator.remove();
		}
		if (variables.isEmpty())
			throw new ConstructionFailedException("Found no variables of type " + type
			        + " at position " + position);

		return Randomness.choice(variables);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getObject(org.evosuite.testcase.VariableReference, org.evosuite.testcase.Scope)
	 */
	/** {@inheritDoc} */
	@Override
	public Object getObject(VariableReference reference, Scope scope) {
		try {
			return reference.getObject(scope);
		} catch (CodeUnderTestException e) {
			throw new AssertionError("This case isn't handled yet");
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#setStatement(org.evosuite.testcase.Statement, int)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference setStatement(StatementInterface statement, int position) {
		statements.set(position, statement);
		assert (isValid());
		return statement.getReturnValue(); // TODO:
		                                   // -1?
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#addStatement(org.evosuite.testcase.Statement, int)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference addStatement(StatementInterface statement, int position) {
		statements.add(position, statement);
		assert (isValid());
		return statement.getReturnValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#addStatement(org.evosuite.testcase.Statement)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference addStatement(StatementInterface statement) {
		statements.add(statement);
		try {
			assert (isValid());
		} catch (AssertionError e) {
			logger.info("Is not valid: ");
			for (StatementInterface s : statements) {
				try {
					logger.info(s.getCode());
				} catch (AssertionError e2) {
					logger.info("Found error in: " + s);
					if (s instanceof MethodStatement) {
						MethodStatement ms = (MethodStatement) s;
						if (!ms.isStatic()) {
							logger.info("Callee: ");
							logger.info(ms.callee.toString());
						}
						int num = 0;
						for (VariableReference v : ms.parameters) {
							logger.info("Parameter " + num);
							logger.info(v.getVariableClass().toString());
							logger.info(v.getClass().toString());
							logger.info(v.toString());
						}
					}
				}
			}
			assert (false);
		}
		return statement.getReturnValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getReturnValue(int)
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getReturnValue(int position) {
		return statements.get(position).getReturnValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#hasReferences(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasReferences(VariableReference var) {
		if (var == null || var.getStPosition() == -1)
			return false;

		for (int i = var.getStPosition() + 1; i < statements.size(); i++) {
			if (statements.get(i).references(var))
				return true;
		}
		for (Assertion assertion : statements.get(var.getStPosition()).getAssertions()) {
			if (assertion.getReferencedVariables().contains(var))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getReferences(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getReferences(VariableReference var) {
		Set<VariableReference> references = new HashSet<VariableReference>();

		if (var == null || var.getStPosition() == -1)
			return references;

		// references.add(var);

		for (int i = var.getStPosition() + 1; i < statements.size(); i++) {
			Set<VariableReference> temp = new HashSet<VariableReference>();
			if (statements.get(i).references(var))
				temp.add(statements.get(i).getReturnValue());
			for (VariableReference v : references) {
				if (statements.get(i).references(v))
					temp.add(statements.get(i).getReturnValue());
			}
			references.addAll(temp);
		}

		return references;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getDependencies(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getDependencies(VariableReference var) {
		Set<VariableReference> dependencies = new HashSet<VariableReference>();

		if (var == null || var.getStPosition() == -1)
			return dependencies;

		Set<StatementInterface> dependentStatements = new HashSet<StatementInterface>();
		dependentStatements.add(statements.get(var.getStPosition()));

		for (int i = var.getStPosition(); i >= 0; i--) {
			Set<StatementInterface> newStatements = new HashSet<StatementInterface>();
			for (StatementInterface s : dependentStatements) {
				if (s.references(statements.get(i).getReturnValue())) {
					newStatements.add(statements.get(i));
					dependencies.add(statements.get(i).getReturnValue());
					break;
				}
			}
			dependentStatements.addAll(newStatements);
		}

		return dependencies;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#remove(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void remove(int position) {
		logger.debug("Removing statement " + position);
		if (position >= size()) {
			return;
		}
		statements.remove(position);
		assert (isValid());
		// for(Statement s : statements) {
		// for(Asss.assertions)
		// }
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getStatement(int)
	 */
	/** {@inheritDoc} */
	@Override
	public StatementInterface getStatement(int position) {
		return statements.get(position);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#isPrefix(org.evosuite.testcase.DefaultTestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPrefix(TestCase t) {
		if (statements.size() > t.size())
			return false;

		for (int i = 0; i < statements.size(); i++) {
			if (!statements.get(i).same(t.getStatement(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Equality check
	 */
	// public boolean equals(TestCase t) {
	// return statements.size() == t.statements.size() && isPrefix(t);
	// }
	@Override
	public int hashCode() {
		return statements.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultTestCase other = (DefaultTestCase) obj;

		if (statements == null) {
			if (other.statements != null)
				return false;
		} else {
			if (statements.size() != other.statements.size())
				return false;
			// if (!statements.equals(other.statements))
			for (int i = 0; i < statements.size(); i++) {
				if (!statements.get(i).equals(other.statements.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#hasObject(java.lang.reflect.Type, int)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasObject(Type type, int position) {
		for (int i = 0; i < position; i++) {
			StatementInterface st = statements.get(i);
			if (st.getReturnValue() == null)
				continue; // Nop
			if (st.getReturnValue().isAssignableTo(type)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#hasCastableObject(java.lang.reflect.Type)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasCastableObject(Type type) {
		for (StatementInterface st : statements) {
			if (st.getReturnValue().isAssignableFrom(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a copy of the test case
	 */
	@Override
	public DefaultTestCase clone() {
		DefaultTestCase t = new DefaultTestCase();
		for (StatementInterface s : statements) {
			StatementInterface copy = s.clone(t);
			t.statements.add(copy);
			copy.setRetval(s.getReturnValue().clone(t));
			copy.setAssertions(s.copyAssertions(t, 0));
		}
		t.coveredGoals.addAll(coveredGoals);
		t.accessedFiles.addAll(accessedFiles);
		//t.exception_statement = exception_statement;
		//t.exceptionThrown = exceptionThrown;
		return t;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getAccessedClasses()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Class<?>> getAccessedClasses() {
		Set<Class<?>> accessed_classes = new HashSet<Class<?>>();
		for (StatementInterface s : statements) {
			for (VariableReference var : s.getVariableReferences()) {
				if (var != null && !var.isPrimitive()) {
					Class<?> clazz = var.getVariableClass();
					while (clazz.isMemberClass()) {
						//accessed_classes.add(clazz);
						clazz = clazz.getEnclosingClass();
					}
					while (clazz.isArray())
						clazz = clazz.getComponentType();
					accessed_classes.add(clazz);
				}
			}
			if (s instanceof MethodStatement) {
				MethodStatement ms = (MethodStatement) s;
				accessed_classes.addAll(Arrays.asList(ms.getMethod().getExceptionTypes()));
				accessed_classes.add(ms.getMethod().getDeclaringClass());
				accessed_classes.add(ms.getMethod().getReturnType());
				accessed_classes.addAll(Arrays.asList(ms.getMethod().getParameterTypes()));
			} else if (s instanceof FieldStatement) {
				FieldStatement fs = (FieldStatement) s;
				accessed_classes.add(fs.getField().getDeclaringClass());
				accessed_classes.add(fs.getField().getType());
			} else if (s instanceof ConstructorStatement) {
				ConstructorStatement cs = (ConstructorStatement) s;
				accessed_classes.add(cs.getConstructor().getDeclaringClass());
				accessed_classes.addAll(Arrays.asList(cs.getConstructor().getExceptionTypes()));
				accessed_classes.addAll(Arrays.asList(cs.getConstructor().getParameterTypes()));
			}
		}
		return accessed_classes;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#addAssertions(org.evosuite.testcase.DefaultTestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public void addAssertions(TestCase other) {
		for (int i = 0; i < statements.size() && i < other.size(); i++) {
			for (Assertion a : other.getStatement(i).getAssertions()) {
				if (!statements.get(i).getAssertions().contains(a))
					if (a != null)
						statements.get(i).getAssertions().add(a.clone(this));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#hasAssertions()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasAssertions() {
		for (StatementInterface s : statements) {
			if (s.hasAssertions())
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getAssertions()
	 */
	/** {@inheritDoc} */
	@Override
	public List<Assertion> getAssertions() {
		List<Assertion> assertions = new ArrayList<Assertion>();
		for (StatementInterface s : statements) {
			assertions.addAll(s.getAssertions());
		}
		return assertions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#removeAssertions()
	 */
	/** {@inheritDoc} */
	@Override
	public void removeAssertions() {
		for (StatementInterface s : statements) {
			s.removeAssertions();
		}

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#isValid()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isValid() {
		for (StatementInterface s : statements) {
			assert (s.isValid());
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getDeclaredExceptions()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		Set<Class<?>> exceptions = new HashSet<Class<?>>();
		for (StatementInterface statement : statements) {
			exceptions.addAll(statement.getDeclaredExceptions());
		}
		return exceptions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#hasCalls()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasCalls() {
		for (StatementInterface s : statements) {
			if (s instanceof TestCallStatement) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#addCoveredGoal(org.evosuite.testcase.TestFitnessFunction)
	 */
	/** {@inheritDoc} */
	@Override
	public void addCoveredGoal(TestFitnessFunction goal) {
		coveredGoals.add(goal);
		// TODO: somehow adds the same goal more than once (fitnessfunction.equals()?)
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getCoveredGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<TestFitnessFunction> getCoveredGoals() {
		return coveredGoals;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#clearCoveredGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public void clearCoveredGoals() {
		coveredGoals.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	/** {@inheritDoc} */
	@Override
	public Iterator<StatementInterface> iterator() {
		return statements.iterator();
	}

	/** {@inheritDoc} */
	@Override
	public void addListener(Listener<Void> listener) {
		statements.addListener(listener);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteListener(Listener<Void> listener) {
		statements.deleteListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		for (StatementInterface statement : statements) {
			statement.replace(var1, var2);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#accept(org.evosuite.testcase.TestVisitor)
	 */
	/** {@inheritDoc} */
	@Override
	public void accept(TestVisitor visitor) {
		visitor.visitTestCase(this);

		Iterator<StatementInterface> iterator = statements.iterator();
		while (iterator.hasNext()) {
			StatementInterface statement = iterator.next();
			logger.trace("Visiting statement " + statement.getCode());
			if (statement instanceof PrimitiveStatement<?>)
				visitor.visitPrimitiveStatement((PrimitiveStatement<?>) statement);
			else if (statement instanceof FieldStatement)
				visitor.visitFieldStatement((FieldStatement) statement);
			else if (statement instanceof ConstructorStatement)
				visitor.visitConstructorStatement((ConstructorStatement) statement);
			else if (statement instanceof MethodStatement)
				visitor.visitMethodStatement((MethodStatement) statement);
			else if (statement instanceof AssignmentStatement)
				visitor.visitAssignmentStatement((AssignmentStatement) statement);
			else if (statement instanceof ArrayStatement)
				visitor.visitArrayStatement((ArrayStatement) statement);
			else if (statement instanceof NullStatement)
				visitor.visitNullStatement((NullStatement) statement);
			else if (statement instanceof PrimitiveExpression)
				visitor.visitPrimitiveExpression((PrimitiveExpression) statement);
			else
				throw new RuntimeException("Unknown statement type: " + statement);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return toCode();
	}

	/**
	 * <p>changeClassLoader</p>
	 *
	 * @param loader a {@link java.lang.ClassLoader} object.
	 */
	public void changeClassLoader(ClassLoader loader) {
		for (StatementInterface s : statements) {
			s.changeClassLoader(loader);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#getAccessedFiles()
	 */
	/** {@inheritDoc} */
	@Override
	public List<String> getAccessedFiles() {
		return accessedFiles;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCase#setAccessedFiles(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public void setAccessedFiles(List<String> files) {
		accessedFiles = files;
	}
	/*
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		coveredGoals = new HashSet<TestFitnessFunction>();
	}
	*/
}
