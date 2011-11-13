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

package de.unisb.cs.st.evosuite.assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationObserver;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * This class executes a test case on a unit and all mutants and infers
 * assertions from the resulting traces.
 * 
 * @author Gordon Fraser
 * 
 */
public class MutationAssertionGenerator extends AssertionGenerator {

	private final Map<Integer, Mutation> mutants = new HashMap<Integer, Mutation>();

	private final static Logger logger = LoggerFactory.getLogger(MutationAssertionGenerator.class);

	private final Set<Integer> killed_ALL = new HashSet<Integer>();

	private static PrimitiveTraceObserver primitive_observer = new PrimitiveTraceObserver();
	private static ComparisonTraceObserver comparison_observer = new ComparisonTraceObserver();
	private static InspectorTraceObserver inspector_observer = new InspectorTraceObserver();
	private static PrimitiveFieldTraceObserver field_observer = new PrimitiveFieldTraceObserver();
	private static NullTraceObserver null_observer = new NullTraceObserver();

	private final static Map<Mutation, Integer> timedOutMutations = new HashMap<Mutation, Integer>();

	protected static Class<?>[] observerClasses = { PrimitiveTraceEntry.class,
	        ComparisonTraceEntry.class, InspectorTraceEntry.class,
	        PrimitiveFieldTraceEntry.class, NullTraceEntry.class };

	/**
	 * Default constructor
	 */
	public MutationAssertionGenerator() {
		for (Mutation m : MutationPool.getMutants()) {
			mutants.put(m.getId(), m);
		}
		executor.newObservers();
		executor.addObserver(primitive_observer);
		executor.addObserver(comparison_observer);
		executor.addObserver(inspector_observer);
		executor.addObserver(field_observer);
		executor.addObserver(null_observer);
	}

	public int numMutants() {
		return MutationPool.getMutantCounter();
	}

	/**
	 * Execute a test case on the original unit
	 * 
	 * @param test
	 *            The test case that should be executed
	 */
	@Override
	protected ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	/**
	 * Execute a test case on a mutant
	 * 
	 * @param test
	 *            The test case that should be executed
	 * @param mutant
	 *            The mutant on which the test case shall be executed
	 */
	private ExecutionResult runTest(TestCase test, Mutation mutant) {
		ExecutionResult result = new ExecutionResult(test, mutant);
		//resetObservers();
		comparison_observer.clear();
		primitive_observer.clear();
		inspector_observer.clear();
		field_observer.clear();
		null_observer.clear();
		try {
			logger.debug("Executing test");
			MutationObserver.activateMutation(mutant);
			result = executor.execute(test);
			MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);

			result.setTrace(comparison_observer.getTrace(), ComparisonTraceEntry.class);
			result.setTrace(primitive_observer.getTrace(), PrimitiveTraceEntry.class);
			result.setTrace(inspector_observer.getTrace(), InspectorTraceEntry.class);
			result.setTrace(field_observer.getTrace(), PrimitiveFieldTraceEntry.class);
			result.setTrace(null_observer.getTrace(), NullTraceEntry.class);

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			//System.exit(1);
		}

		return result;
	}

	/**
	 * Return a minimal subset of the assertions that covers all killable
	 * mutants
	 * 
	 * @param test
	 *            The test case that should be executed
	 * @param mutants
	 *            The list of mutants of the unit
	 * @param assertions
	 *            All assertions that can be generated for the test case
	 * @param kill_map
	 *            Mapping of assertion to mutant ids that are killed by the
	 *            assertion
	 */
	private void minimize(TestCase test, List<Mutation> mutants,
	        List<Assertion> assertions, Map<Integer, Set<Integer>> kill_map) {

		class Pair implements Comparable<Object> {
			Integer assertion;
			Integer num_killed;

			public Pair(int a, int k) {
				assertion = a;
				num_killed = k;
			}

			@Override
			public int compareTo(Object o) {
				Pair other = (Pair) o;
				return num_killed.compareTo(other.num_killed);
			}
		}
		Set<Integer> to_kill = new HashSet<Integer>();
		for (Entry<Integer, Set<Integer>> entry : kill_map.entrySet()) {
			to_kill.addAll(entry.getValue());
		}

		Set<Integer> killed = new HashSet<Integer>();
		Set<Assertion> result = new HashSet<Assertion>();

		boolean done = false;
		while (!done) {
			// logger.info("Have to kill "+to_kill.size());
			List<Pair> a = new ArrayList<Pair>();
			for (Entry<Integer, Set<Integer>> entry : kill_map.entrySet()) {
				int num = 0;
				for (Integer m : entry.getValue()) {
					if (!killed.contains(m))
						num++;
				}
				if (num > 0) {
					a.add(new Pair(entry.getKey(), num));
				}
			}
			if (a.isEmpty())
				done = true;
			else {
				Pair best = Collections.max(a);
				// logger.info("Chosen "+best.assertion);
				result.add(assertions.get(best.assertion));
				for (Integer m : kill_map.get(best.assertion)) {
					// logger.info("Killed "+m);
					killed.add(m);
					to_kill.remove(m);
				}
			}
		}

		// sort by number of assertions killed
		// pick assertion that kills most
		// remove all mutations that are already killed
		logger.debug("Minimized assertions from " + assertions.size() + " to "
		        + result.size());

		/*
		for (StatementInterface s : test) {
			Set<Assertion> reducedAssertions = new HashSet<Assertion>();
			for (Assertion assertion : s.getAssertions()) {
				for (Assertion other : result)
					if (other == assertion)
						reducedAssertions.add(assertion);
			}
			s.setAssertions(reducedAssertions);
		}
		*/

		/*
		for (Assertion assertion : assertions) {
			int statement = assertion.getSource().getStPosition();
			test.getStatement(statement).addAssertion(assertion);
		}
		*/

		if (!result.isEmpty()) {
			test.removeAssertions();

			for (Assertion assertion : result) {
				int statement = assertion.getSource().getStPosition();
				assertion.getStatement().addAssertion(assertion);
				//test.getStatement(statement).addAssertion(assertion);

			}
		} else {
			logger.info("Not removing assertions because no new assertions were found");
		}

	}

	public void addAssertions(TestCase test, Mutation mutant) {
		ExecutionResult orig_result = runTest(test);
		ExecutionResult mutant_result = runTest(test, mutant);

		for (Class<?> observerClass : observerClasses) {
			orig_result.getTrace(observerClass).getAssertions(test,
			                                                  mutant_result.getTrace(observerClass));
		}

		logger.debug("Generated " + test.getAssertions().size() + " assertions");
	}

	/**
	 * Generate assertions to kill all the mutants defined in the pool
	 * 
	 * @param test
	 * @param killed
	 */
	public void addAssertions(TestCase test, Set<Integer> killed) {
		addAssertions(test, killed, mutants);
	}

	/**
	 * Add assertions to current test set for given set of mutants
	 * 
	 * @param test
	 * @param killed
	 * @param mutants
	 */
	public void addAssertions(TestCase test, Set<Integer> killed,
	        Map<Integer, Mutation> mutants) {

		logger.info("Generating assertions");

		int s1 = killed.size();

		logger.info("Running on original");
		ExecutionResult orig_result = runTest(test);

		if (orig_result.hasTimeout()) {
			logger.info("Skipping test, as it has timeouts");
			return;
		}

		Map<Mutation, List<OutputTrace<?>>> mutation_traces = new HashMap<Mutation, List<OutputTrace<?>>>();
		List<Mutation> executedMutants = new ArrayList<Mutation>();

		for (Integer mutationId : orig_result.getTrace().touchedMutants) {
			executedMutants.add(mutants.get(mutationId));
		}
		logger.info("Running test " + test.hashCode() + " on " + executedMutants.size()
		        + "/" + mutants.size() + " mutants");

		for (Mutation m : executedMutants) {

			if (timedOutMutations.containsKey(m)) {
				logger.info("CURRENT MUTANT TIMEOUTS: " + timedOutMutations.get(m));
				if (timedOutMutations.get(m) >= Properties.MUTATION_TIMEOUTS) {
					logger.info("Skipping timed out mutant");
					continue;
				}
			}

			/*
			if (killed.contains(m.getId())) {
				logger.info("Skipping dead mutant");
				continue;
			}
			*/

			logger.info("Running test " + test.hashCode() + " on mutation "
			        + m.getMutationName());
			ExecutionResult mutant_result = runTest(test, m);

			int numKilled = 0;
			for (Class<?> observerClass : observerClasses) {
				if (mutant_result.getTrace(observerClass) == null
				        || orig_result.getTrace(observerClass) == null)
					continue;
				numKilled += orig_result.getTrace(observerClass).getAssertions(test,
				                                                               mutant_result.getTrace(observerClass));
			}

			List<OutputTrace<?>> traces = new ArrayList<OutputTrace<?>>(
			        mutant_result.getTraces());
			mutation_traces.put(m, traces);

			if (mutant_result.hasTimeout()) {
				if (!timedOutMutations.containsKey(m)) {
					timedOutMutations.put(m, 1);
				} else {
					timedOutMutations.put(m, timedOutMutations.get(m) + 1);
				}
				logger.info("SET TIMEOUTS TO: " + timedOutMutations.get(m));
			}

			if (numKilled > 0 || mutant_result.hasTimeout()) {
				killed.add(m.getId());
			}
		}

		Set<Integer> killed_before = new HashSet<Integer>();
		List<Assertion> assertions = test.getAssertions();
		logger.info("Got " + assertions.size() + " assertions");
		Map<Integer, Set<Integer>> kill_map = new HashMap<Integer, Set<Integer>>();
		int num = 0;
		for (Assertion assertion : assertions) {
			Set<Integer> killed_mutations = new HashSet<Integer>();
			for (Mutation m : executedMutants) {

				boolean is_killed = false;
				if (mutation_traces.containsKey(m)) {
					for (OutputTrace<?> trace : mutation_traces.get(m)) {
						is_killed = trace.isDetectedBy(assertion);
						if (is_killed) {
							killed_ALL.add(m.getId());
							killed_before.add(m.getId());
							break;
						}
					}
				} else {
					is_killed = true;
				}
				if (is_killed) {
					// logger.info("Found assertion for mutation: "+m.getId());
					killed_mutations.add(m.getId());
					// statistics.setAsserted(m);
					// } else {
					// logger.info("Found no assertions for mutation: "+m.getId());
				}
			}
			kill_map.put(num, killed_mutations);
			num++;
		}
		minimize(test, executedMutants, assertions, kill_map);

		Set<Integer> killed_after = new HashSet<Integer>();
		assertions = test.getAssertions();
		for (Assertion assertion : assertions) {
			for (Mutation m : executedMutants) {

				boolean is_killed = false;
				if (mutation_traces.containsKey(m)) {
					for (OutputTrace<?> trace : mutation_traces.get(m)) {
						is_killed = trace.isDetectedBy(assertion);
						if (is_killed) {
							killed_after.add(m.getId());
							break;
						}
					}
				} else {
					is_killed = true;
				}
			}
		}
		int s2 = killed.size() - s1;
		logger.debug("Mutants killed before / after / should be: " + killed_before.size()
		        + "/" + killed_after.size() + "/" + s2);

		TestCase clone = test.clone();

		// IF there are no mutant killing assertions on the last statement, still assert something
		if (test.getStatement(test.size() - 1).getAssertions().isEmpty()
		        || justNullAssertion(test.getStatement(test.size() - 1))) {
			logger.info("No assertions on last statement: " + test.toCode());
			for (OutputTrace<?> trace : orig_result.getTraces()) {
				trace.getAllAssertions(test);
			}

			Set<Assertion> target = new HashSet<Assertion>(
			        test.getStatement(test.size() - 1).getAssertions());
			logger.info("Found assertions: " + target.size());

			test.removeAssertions();
			test.addAssertions(clone);
			VariableReference targetVar = test.getStatement(test.size() - 1).getReturnValue();
			if (!targetVar.isVoid()) {
				int maxAssertions = 3;
				int numAssertions = 0;
				for (Assertion ass : target) {
					if (ass.getReferencedVariables().contains(targetVar)
					        && !(ass instanceof NullAssertion)) {

						test.getStatement(test.size() - 1).addAssertion(ass);
						if (++numAssertions >= maxAssertions)
							break;
					}
				}
			} else {
				Set<VariableReference> targetVars = test.getStatement(test.size() - 1).getVariableReferences();
				int maxAssertions = 2;
				int numAssertions = 0;
				for (Assertion ass : target) {
					Set<VariableReference> vars = ass.getReferencedVariables();
					vars.retainAll(targetVars);
					if (!vars.isEmpty()) {

						test.getStatement(test.size() - 1).addAssertion(ass);
						if (++numAssertions >= maxAssertions)
							break;
					}
				}

			}
		}

	}

	private boolean justNullAssertion(StatementInterface statement) {
		Set<Assertion> assertions = statement.getAssertions();
		if (assertions.isEmpty())
			return false;
		else {
			Iterator<Assertion> iterator = assertions.iterator();
			VariableReference ret = statement.getReturnValue();
			boolean just = true;
			while (iterator.hasNext()) {
				Assertion ass = iterator.next();
				if (!(ass instanceof NullAssertion)) {
					if (ass.getReferencedVariables().contains(ret)) {
						just = false;
						break;
					}
				}
			}

			return just;
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.AssertionGenerator#addAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void addAssertions(TestCase test) {
		// TODO Auto-generated method stub

	}

}
