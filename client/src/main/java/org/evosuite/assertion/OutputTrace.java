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

import java.util.HashMap;
import java.util.Map;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class of execution traces
 * 
 * @author Gordon Fraser
 */
public class OutputTrace<T extends OutputTraceEntry> implements Cloneable {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(OutputTrace.class);

	/** One entry per statement and per variable */
	protected Map<Integer, Map<VariableReference, T>> trace = new HashMap<Integer, Map<VariableReference, T>>();

	/**
	 * Insert a new entry into the trace
	 * 
	 * @param position
	 *            a int.
	 * @param entry
	 *            a T object.
	 * @param var
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @param <T>
	 *            a T object.
	 */
	public synchronized void addEntry(int position, VariableReference var, T entry) {
		if (!trace.containsKey(position))
			trace.put(position, new HashMap<VariableReference, T>());

		trace.get(position).put(var, entry);
	}

	/**
	 * Get the current entry at the given position
	 * 
	 * @param position
	 *            a int.
	 * @param var
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @return a T object.
	 */
	public synchronized T getEntry(int position, VariableReference var) {
		if (!trace.containsKey(position)) {
			trace.put(position, new HashMap<VariableReference, T>());
			return null;
		}

		if (!trace.get(position).containsKey(var))
			return null;

		return trace.get(position).get(var);
	}

	/**
	 * Get the current entry at the given position
	 * 
	 * @param position
	 *            a int.
	 * @param var
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @return a boolean.
	 */
	public boolean containsEntry(int position, VariableReference var) {
		if (!trace.containsKey(position)) {
			trace.put(position, new HashMap<VariableReference, T>());
			return false;
		}

		if (!trace.get(position).containsKey(var))
			return false;

		return true;
	}

	/**
	 * Binary decision whether the two traces differ in any way
	 * 
	 * @param other
	 *            a {@link org.evosuite.assertion.OutputTrace} object.
	 * @return a boolean.
	 */
	public boolean differs(OutputTrace<?> other) {
		for (Integer statement : trace.keySet()) {
			if (other.trace.containsKey(statement)) {
				for (VariableReference var : trace.get(statement).keySet()) {
					if (trace.get(statement).get(var).differs(other.trace.get(statement).get(var)))
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Count the number of differences between two traces
	 * 
	 * @param other
	 *            a {@link org.evosuite.assertion.OutputTrace} object.
	 * @return a int.
	 */
	public int numDiffer(OutputTrace<?> other) {
		int num = 0;

		for (Integer statement : trace.keySet()) {
			if (other.trace.containsKey(statement)) {
				for (VariableReference var : trace.get(statement).keySet()) {
					if (trace.get(statement).get(var).differs(other.trace.get(statement).get(var)))
						num++;
				}
			}
		}

		return num;
	}

	/**
	 * Get all assertions based on trace differences
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param other
	 *            a {@link org.evosuite.assertion.OutputTrace} object.
	 * @return a int.
	 */
	public int getAssertions(TestCase test, OutputTrace<?> other) {
		int num = 0;

		for (Integer statement : trace.keySet()) {
			if (other.trace.containsKey(statement)) {
				for (VariableReference var : trace.get(statement).keySet()) {
					for (Assertion assertion : trace.get(statement).get(var).getAssertions(other.trace.get(statement).get(var))) {
						assert (assertion.isValid()) : "Invalid assertion: "
						        + assertion.getCode() + ", " + assertion.value;
						test.getStatement(statement).addAssertion(assertion);
						num++;
					}
				}
			}
		}

		return num;
	}

	/**
	 * Get all possible assertions
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a int.
	 */
	public int getAllAssertions(TestCase test) {
		int num = 0;

		for (Integer statement : trace.keySet()) {
			for (VariableReference var : trace.get(statement).keySet()) {
				for (Assertion assertion : trace.get(statement).get(var).getAssertions()) {
					assert (assertion.isValid()) : "Invalid assertion: "
					        + assertion.getCode() + ", " + assertion.value;
					test.getStatement(statement).addAssertion(assertion);
					num++;
				}
			}
		}

		return num;
	}

	/**
	 * Get all possible assertions
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a int.
	 */
	public int getAllAssertions(TestCase test, int statement) {
		int num = 0;

		if (!trace.containsKey(statement))
			return 0;

		for (VariableReference var : trace.get(statement).keySet()) {
			for (Assertion assertion : trace.get(statement).get(var).getAssertions()) {
				assert (assertion.isValid()) : "Invalid assertion: "
				        + assertion.getCode() + ", " + assertion.value;
				test.getStatement(statement).addAssertion(assertion);
				num++;
			}
		}

		return num;
	}

	/**
	 * Check if this trace makes the assertion fail
	 * 
	 * @param assertion
	 *            a {@link org.evosuite.assertion.Assertion} object.
	 * @return a boolean.
	 */
	public boolean isDetectedBy(Assertion assertion) {
		assert (assertion.isValid());

		for (Integer statement : trace.keySet()) {
			for (VariableReference var : trace.get(statement).keySet()) {
				if (trace.get(statement).get(var).isDetectedBy(assertion))
					return true;
			}
		}

		return false;
	}

	/**
	 * Reset the trace
	 */
	public synchronized void clear() {
		trace.clear();
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized OutputTrace<T> clone() {
		OutputTrace<T> copy = new OutputTrace<T>();
		for (Integer position : trace.keySet()) {
			copy.trace.put(position, new HashMap<VariableReference, T>());
			for (VariableReference var : trace.get(position).keySet()) {
				copy.trace.get(position).put(var,
				                             (T) trace.get(position).get(var).cloneEntry());
			}
		}
		return copy;
	}
}
