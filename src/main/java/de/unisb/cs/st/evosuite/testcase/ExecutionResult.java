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

import de.unisb.cs.st.evosuite.assertion.OutputTrace;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;

public class ExecutionResult {
	public TestCase test;

	public Mutation mutation;

	/** Map statement number to raised exception */
	public Map<Integer, Throwable> exceptions = new HashMap<Integer, Throwable>();

	private ExecutionTrace trace;
	private final Map<Class<?>, OutputTrace<?>> traces = new HashMap<Class<?>, OutputTrace<?>>();

	// experiment .. tried to remember intermediately calculated ControlFlowDistances .. no real speed up
	//	public Map<Branch, ControlFlowDistance> intermediateDistances;

	public ExecutionResult(TestCase t) {
		trace = null;
		mutation = null;
		test = t;
	}

	public ExecutionResult(TestCase t, Mutation m) {
		trace = null;
		mutation = m;
		test = t;
	}

	public ExecutionTrace getTrace() {
		return trace;
	}

	public void setTrace(ExecutionTrace trace) {
		assert (trace != null);
		this.trace = trace;
	}

	public void setTrace(OutputTrace<?> trace, Class<?> clazz) {
		traces.put(clazz, trace);
	}

	public OutputTrace<?> getTrace(Class<?> clazz) {
		return traces.get(clazz);
	}

	public Collection<OutputTrace<?>> getTraces() {
		return traces.values();
	}

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

	// Killed mutants
	// List<Mutation> dead = new ArrayList<Mutation>();

	// Live mutants
	// List<Mutation> live = new ArrayList<Mutation>();

	// Objects with mutants
	// List<Mutation> have_object = new ArrayList<Mutation>();

	// Mutated methods called
	// List<Mutation> have_methodcall = new ArrayList<Mutation>();

	// Mutations touched
	// List<Mutation> touched = new ArrayList<Mutation>();

	// Map<Mutation, List<ExecutionTracer.TraceEntry> > mutant_traces = new
	// HashMap<Mutation, List<TraceEntry> >();

	// Map<Long, Double> distance = new HashMap<Long, Double>();
	// Map<Long, Integer> levenshtein = new HashMap<Long, Integer>();

	// Fitness(M) = A * have_object(M) + B * have_methodcall(M) + C * touched(M)
	// + D * impact(M)

	// impact(M) = Sum(E * distance_method * 3)

	// int num_mutants;
	// double average_length = 0.0;
	// double max_length = 0.0;

	// int getNumKilled() {
	// return dead.size();
	// }
}
