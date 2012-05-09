/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.assertion.OutputTrace;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;

public class ExecutionResult {

	private static final Logger logger = LoggerFactory.getLogger(ExecutionResult.class);
	
	/** Test case that produced this execution result */
	public TestCase test;

	/** Mutation that was active during execution */
	public Mutation mutation;

	/** Map statement number to raised exception */
	private Map<Integer, Throwable> exceptions = new HashMap<Integer, Throwable>();

	/** Record for each exception if it was explicitly thrown */
	public Map<Integer, Boolean> explicitExceptions = new HashMap<Integer, Boolean>();

	/** Trace recorded during execution */
	private ExecutionTrace trace;

	/** Output traces produced by observers */
	private final Map<Class<?>, OutputTrace<?>> traces = new HashMap<Class<?>, OutputTrace<?>>();

	// experiment .. tried to remember intermediately calculated ControlFlowDistances .. no real speed up
	//	public Map<Branch, ControlFlowDistance> intermediateDistances;

	/**
	 * Default constructor when executing without mutation
	 * 
	 * @param t
	 */
	public ExecutionResult(TestCase t) {
		trace = null;
		mutation = null;
		test = t;
	}

	public void setThrownExceptions(Map<Integer, Throwable> data){
		exceptions.clear();
		for(Integer position : data.keySet()){
			reportNewThrownException(position, data.get(position));
		}
	}
	
	public Integer getFirstPositionOfThrownException(){
		Integer min = null;
		for(Integer position : exceptions.keySet()){
			if(min==null || position < min){
				min = position;
			}
		}
		return min;
	}
	
	public void reportNewThrownException(Integer position, Throwable t){
		exceptions.put(position, t);
	}
	
	public Set<Integer> getPositionsWhereExceptionsWereThrown(){
		return exceptions.keySet();
	}
	
	public Collection<Throwable> getAllThrownExceptions(){
		return exceptions.values();
	}
	
	public boolean isThereAnExceptionAtPosition(Integer position){
		return exceptions.containsKey(position);
	}
	
	public boolean noThrownExceptions(){
		return exceptions.isEmpty();
	}
	
	public Throwable getExceptionThrownAtPosition(Integer position){
		return exceptions.get(position);
	}
	
	public int getNumberOfThrownExceptions(){
		return exceptions.size();
	}
	
	/**
	 * 	 shouldn't be used 
	 */
	@Deprecated
	public Map<Integer, Throwable> exposeExceptionMapping(){
		return exceptions;
	}
	
	
	/**
	 * Constructor when executing with mutation
	 * 
	 * @param t
	 * @param m
	 */
	public ExecutionResult(TestCase t, Mutation m) {
		trace = null;
		mutation = m;
		test = t;
	}

	/**
	 * Accessor to the execution trace
	 * 
	 * @return
	 */
	public ExecutionTrace getTrace() {
		return trace;
	}

	/**
	 * Set execution trace to different value
	 * 
	 * @param trace
	 */
	public void setTrace(ExecutionTrace trace) {
		assert (trace != null);
		this.trace = trace;
	}

	/**
	 * Store a new output trace
	 * 
	 * @param trace
	 * @param clazz
	 */
	public void setTrace(OutputTrace<?> trace, Class<?> clazz) {
		traces.put(clazz, trace);
	}

	/**
	 * Accessor for output trace produced by an observer of a particular class
	 * 
	 * @param clazz
	 * @return
	 */
	public OutputTrace<?> getTrace(Class<?> clazz) {
		return traces.get(clazz);
	}

	/**
	 * Accessor for the output traces produced by observers
	 * 
	 * @return
	 */
	public Collection<OutputTrace<?>> getTraces() {
		return traces.values();
	}

	/**
	 * Was the reason for termination a timeout?
	 * 
	 * @return
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
	 * Is there an undeclared exception in the trace?
	 * 
	 * @return
	 */
	public boolean hasUndeclaredException() {
		if (test == null)
			return false;

		for (Integer i : exceptions.keySet()) {
			Throwable t = exceptions.get(i);
			if (!test.getStatement(i).getDeclaredExceptions().contains(t))
				return true;
		}

		return false;
	}

	@Override
	public ExecutionResult clone() {
		ExecutionResult copy = new ExecutionResult(test, mutation);
		copy.exceptions.putAll(exceptions);
		copy.trace = trace.clone();
		for (Class<?> clazz : traces.keySet()) {
			copy.traces.put(clazz, traces.get(clazz).clone());
		}

		return copy;
	}

	@Override
	public String toString() {
		String result = "";
		result += "Trace:";
		result += trace;
		return result;
	}
}
