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
 * 
 * @author Gordon Fraser
 */
package org.evosuite.testcase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.evosuite.assertion.OutputTrace;
import org.evosuite.coverage.mutation.Mutation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionResult implements Cloneable {

	private static final Logger logger = LoggerFactory.getLogger(ExecutionResult.class);

	/** Test case that produced this execution result */
	public TestCase test;

	/** Mutation that was active during execution */
	public Mutation mutation;

	/** Map statement number to raised exception */
	protected Map<Integer, Throwable> exceptions = new HashMap<Integer, Throwable>();

	/** Record for each exception if it was explicitly thrown */
	public Map<Integer, Boolean> explicitExceptions = new HashMap<Integer, Boolean>();

	/** Trace recorded during execution */
	protected ExecutionTrace trace;

	/** Output traces produced by observers */
	protected final Map<Class<?>, OutputTrace<?>> traces = new HashMap<Class<?>, OutputTrace<?>>();

	// experiment .. tried to remember intermediately calculated ControlFlowDistances .. no real speed up
	//	public Map<Branch, ControlFlowDistance> intermediateDistances;

	/**
	 * Default constructor when executing without mutation
	 * 
	 * @param t
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public ExecutionResult(TestCase t) {
		trace = null;
		mutation = null;
		test = t;
	}

	/**
	 * <p>
	 * setThrownExceptions
	 * </p>
	 * 
	 * @param data
	 *            a {@link java.util.Map} object.
	 */
	public void setThrownExceptions(Map<Integer, Throwable> data) {
		exceptions.clear();
		for (Integer position : data.keySet()) {
			reportNewThrownException(position, data.get(position));
		}
	}

	/**
	 * <p>
	 * getFirstPositionOfThrownException
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getFirstPositionOfThrownException() {
		Integer min = null;
		for (Integer position : exceptions.keySet()) {
			if (min == null || position < min) {
				min = position;
			}
		}
		return min;
	}

	/**
	 * <p>
	 * reportNewThrownException
	 * </p>
	 * 
	 * @param position
	 *            a {@link java.lang.Integer} object.
	 * @param t
	 *            a {@link java.lang.Throwable} object.
	 */
	public void reportNewThrownException(Integer position, Throwable t) {
		exceptions.put(position, t);
	}

	/**
	 * <p>
	 * getPositionsWhereExceptionsWereThrown
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Integer> getPositionsWhereExceptionsWereThrown() {
		return exceptions.keySet();
	}

	/**
	 * <p>
	 * getAllThrownExceptions
	 * </p>
	 * 
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Throwable> getAllThrownExceptions() {
		return exceptions.values();
	}

	/**
	 * <p>
	 * isThereAnExceptionAtPosition
	 * </p>
	 * 
	 * @param position
	 *            a {@link java.lang.Integer} object.
	 * @return a boolean.
	 */
	public boolean isThereAnExceptionAtPosition(Integer position) {
		return exceptions.containsKey(position);
	}

	/**
	 * <p>
	 * noThrownExceptions
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean noThrownExceptions() {
		return exceptions.isEmpty();
	}

	/**
	 * <p>
	 * getExceptionThrownAtPosition
	 * </p>
	 * 
	 * @param position
	 *            a {@link java.lang.Integer} object.
	 * @return a {@link java.lang.Throwable} object.
	 */
	public Throwable getExceptionThrownAtPosition(Integer position) {
		return exceptions.get(position);
	}

	/**
	 * <p>
	 * getNumberOfThrownExceptions
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getNumberOfThrownExceptions() {
		return exceptions.size();
	}

	/**
	 * shouldn't be used
	 * 
	 * @return a {@link java.util.Map} object.
	 */
	@Deprecated
	public Map<Integer, Throwable> exposeExceptionMapping() {
		return exceptions;
	}

	/**
	 * 
	 * @return Mapping of statement indexes and thrown exceptions.
	 */
	public Map<Integer, Throwable> getCopyOfExceptionMapping() {
		Map<Integer, Throwable> copy = new HashMap<Integer, Throwable>();
		copy.putAll(exceptions);
		return copy;
	}

	/**
	 * Constructor when executing with mutation
	 * 
	 * @param t
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param m
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public ExecutionResult(TestCase t, Mutation m) {
		trace = null;
		mutation = m;
		test = t;
	}

	/**
	 * Accessor to the execution trace
	 * 
	 * @return a {@link org.evosuite.testcase.ExecutionTrace} object.
	 */
	public ExecutionTrace getTrace() {
		return trace;
	}

	/**
	 * Set execution trace to different value
	 * 
	 * @param trace
	 *            a {@link org.evosuite.testcase.ExecutionTrace} object.
	 */
	public void setTrace(ExecutionTrace trace) {
		assert (trace != null);
		this.trace = trace;
	}

	/**
	 * Store a new output trace
	 * 
	 * @param trace
	 *            a {@link org.evosuite.assertion.OutputTrace} object.
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 */
	public void setTrace(OutputTrace<?> trace, Class<?> clazz) {
		traces.put(clazz, trace);
	}

	/**
	 * Accessor for output trace produced by an observer of a particular class
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @return a {@link org.evosuite.assertion.OutputTrace} object.
	 */
	public OutputTrace<?> getTrace(Class<?> clazz) {
		return traces.get(clazz);
	}

	/**
	 * Accessor for the output traces produced by observers
	 * 
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<OutputTrace<?>> getTraces() {
		return traces.values();
	}

	/**
	 * Was the reason for termination a timeout?
	 * 
	 * @return a boolean.
	 */
	public boolean hasTimeout() {
		if (test == null)
			return false;

		int size = test.size();
		if (exceptions.containsKey(size)) {
			if (exceptions.get(size) instanceof TestCaseExecutor.TimeoutExceeded) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Does the test contain an exception caused in the test itself?
	 * 
	 * @return a boolean.
	 */
	public boolean hasTestException() {
		if (test == null)
			return false;

		for (Throwable t : exceptions.values()) {
			if (t instanceof CodeUnderTestException)
				return true;
		}

		return false;
	}

	/**
	 * Is there an undeclared exception in the trace?
	 * 
	 * @return a boolean.
	 */
	public boolean hasUndeclaredException() {
		if (test == null)
			return false;

		for (Integer i : exceptions.keySet()) {
			Throwable t = exceptions.get(i);
			if (!test.getStatement(i).getDeclaredExceptions().contains(t.getClass()))
				return true;
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult clone() {
		ExecutionResult copy = new ExecutionResult(test, mutation);
		copy.exceptions.putAll(exceptions);
		copy.trace = trace.lazyClone();
		copy.explicitExceptions.putAll(explicitExceptions);
		for (Class<?> clazz : traces.keySet()) {
			copy.traces.put(clazz, traces.get(clazz).clone());
		}

		return copy;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String result = "";
		result += "Trace:";
		result += trace;
		return result;
	}
}
