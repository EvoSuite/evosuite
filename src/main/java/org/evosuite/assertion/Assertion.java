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
package org.evosuite.assertion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class of an executable code assertion
 * 
 * @author Gordon Fraser
 */
public abstract class Assertion implements Serializable {

	private static final long serialVersionUID = 1617423211706717599L;

	/** Variable on which the assertion is made */
	protected VariableReference source;

	/** Expected value of variable */
	protected Object value;

	/** Statement to which the assertion is added */
	protected StatementInterface statement;

	protected transient Set<Mutation> killedMutants = new LinkedHashSet<Mutation>();

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(Assertion.class);

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
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
		Assertion other = (Assertion) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public void addKilledMutation(Mutation m) {
		killedMutants.add(m);
	}

	public Set<Mutation> getKilledMutations() {
		return killedMutants;
	}

	/**
	 * Setter for statement to which assertion is added
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 */
	public void setStatement(StatementInterface statement) {
		this.statement = statement;
	}

	/**
	 * Getter for statement to which assertion is added
	 * 
	 * @return a {@link org.evosuite.testcase.StatementInterface} object.
	 */
	public StatementInterface getStatement() {
		return statement;
	}

	/**
	 * Getter for source variable
	 * 
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getSource() {
		return source;
	}
	
	public void setSource(VariableReference var) {
		source = var;
	}

	/**
	 * Getter for value object
	 * 
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * This method returns the Java Code
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String getCode();

	/**
	 * {@inheritDoc}
	 * 
	 * Return a copy of the assertion
	 */
	@Override
	public final Assertion clone() {
		throw new UnsupportedOperationException("Use Assertion.clone(TestCase)");
	}

	/**
	 * Return a copy of the assertion, which is valid in newTestCase
	 * 
	 * @param newTestCase
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link org.evosuite.assertion.Assertion} object.
	 */
	public Assertion clone(TestCase newTestCase) {
		return copy(newTestCase, 0);
	}

	/**
	 * Return a copy of the assertion, which is valid in newTestCase
	 * 
	 * @param newTestCase
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param offset
	 *            a int.
	 * @return a {@link org.evosuite.assertion.Assertion} object.
	 */
	public abstract Assertion copy(TestCase newTestCase, int offset);

	/**
	 * Determine if assertion holds in current scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 * @return a boolean.
	 */
	public abstract boolean evaluate(Scope scope);

	/**
	 * Return all the variables that are part of this assertion
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<VariableReference> getReferencedVariables() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(source);
		return vars;
	}

	/**
	 * Self-check
	 * 
	 * @return a boolean.
	 */
	public boolean isValid() {
		return source != null && value != null;
	}

	public void changeClassLoader(ClassLoader loader) {
		// No-op by default?
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
    IOException {
		ois.defaultReadObject();

		killedMutants = new LinkedHashSet<Mutation>();
	}

}
