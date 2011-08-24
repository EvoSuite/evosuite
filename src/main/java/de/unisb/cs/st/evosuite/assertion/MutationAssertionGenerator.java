/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Javalanche is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * Javalanche. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.assertion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import de.unisb.cs.st.evosuite.testcase.OutputTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * This class executes a test case on a unit and all mutants and infers
 * assertions from the resulting traces.
 * 
 * @author Gordon Fraser
 * 
 */
public class MutationAssertionGenerator extends AssertionGenerator {

	private final List<Mutation> mutants;

	private final static Logger logger = LoggerFactory.getLogger(MutationAssertionGenerator.class);

	private final Map<TestCase, Map<Class<?>, Integer>> assertion_statistics_full = new HashMap<TestCase, Map<Class<?>, Integer>>();

	private final Map<TestCase, Map<Class<?>, Integer>> assertion_statistics_min = new HashMap<TestCase, Map<Class<?>, Integer>>();

	private final Map<TestCase, Map<Class<?>, Integer>> assertion_statistics_killed = new HashMap<TestCase, Map<Class<?>, Integer>>();

	private final Set<Integer> killed_ALL = new HashSet<Integer>();

	/**
	 * Default constructor
	 */
	public MutationAssertionGenerator() {
		mutants = MutationPool.getMutants();
	}

	public int numMutants() {
		return MutationPool.getMutantCounter();
	}

	public void resetObservers() {
		executor.newObservers();
		primitive_observer = new PrimitiveOutputTraceObserver();
		comparison_observer = new ComparisonTraceObserver();
		inspector_observer = new InspectorTraceObserver();
		field_observer = new PrimitiveFieldTraceObserver();
		null_observer = new NullOutputObserver();
		executor.addObserver(primitive_observer);
		executor.addObserver(comparison_observer);
		executor.addObserver(inspector_observer);
		executor.addObserver(field_observer);
		executor.addObserver(null_observer);
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
		resetObservers();
		try {
			logger.debug("Executing test");
			MutationObserver.activateMutation(mutant);

			result = executor.execute(test);

			MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			// result.touched.addAll(HOMObserver.getTouched());

			result.comparison_trace = comparison_observer.getTrace();
			result.primitive_trace = primitive_observer.getTrace();
			result.inspector_trace = inspector_observer.getTrace();
			result.field_trace = field_observer.getTrace();
			result.null_trace = null_observer.getTrace();

			// for(TestObserver observer : observers) {
			// observer.testResult(result);
			// }
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
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
		// First try to do this without string assertions
		boolean done = false;
		while (!done) {
			// logger.info("Have to kill "+to_kill.size());
			List<Pair> a = new ArrayList<Pair>();
			for (Entry<Integer, Set<Integer>> entry : kill_map.entrySet()) {
				// if(assertions.get(entry.getKey()) instanceof StringAssertion)
				// continue;
				// if(assertions.get(entry.getKey()) instanceof
				// ExceptionAssertion)
				// continue;
				int num = 0;
				for (Integer m : entry.getValue()) {
					if (!killed.contains(m))
						num++;
				}
				if (num > 0) {
					// logger.info("Assertion "+entry.getKey()+" kills "+num);
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

		done = to_kill.isEmpty();
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
					// logger.info("Assertion "+entry.getKey()+" kills "+num);
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
		int num_string_assertions = 0;
		int num_inspector_assertions = 0;
		int num_field_assertions = 0;
		int num_primitive_assertions = 0;
		int num_comparison_assertions = 0;
		int num_exception_assertions = 0;
		int num_null_assertions = 0;

		for (Assertion assertion : assertions) {
			int statement = assertion.getSource().getStPosition();
			test.getStatement(statement).addAssertion(assertion);
			if (assertion instanceof StringAssertion)
				num_string_assertions++;
			else if (assertion instanceof InspectorAssertion)
				num_inspector_assertions++;
			else if (assertion instanceof PrimitiveFieldAssertion)
				num_field_assertions++;
			else if (assertion instanceof PrimitiveAssertion)
				num_primitive_assertions++;
			else if (assertion instanceof EqualsAssertion)
				num_comparison_assertions++;
			else if (assertion instanceof CompareAssertion)
				num_comparison_assertions++;
			else if (assertion instanceof ExceptionAssertion)
				num_exception_assertions++;
			else if (assertion instanceof NullAssertion)
				num_null_assertions++;
			else
				logger.error("Found unknown assertion!");

		}
		// TODO: List exception assertion
		logger.debug("Assertions before minimization: " + test.getAssertions().size()
		        + "," + num_string_assertions + "," + num_inspector_assertions + ","
		        + num_field_assertions + "," + num_comparison_assertions + ","
		        + num_primitive_assertions + "," + num_exception_assertions + ","
		        + num_null_assertions);

		if (!result.isEmpty()) {
			test.removeAssertions();
			num_string_assertions = 0;
			num_inspector_assertions = 0;
			num_field_assertions = 0;
			num_primitive_assertions = 0;
			num_comparison_assertions = 0;
			num_exception_assertions = 0;
			num_null_assertions = 0;
			for (Assertion assertion : result) {
				int statement = assertion.getSource().getStPosition();
				test.getStatement(statement).addAssertion(assertion);
				if (assertion instanceof StringAssertion)
					num_string_assertions++;
				else if (assertion instanceof InspectorAssertion)
					num_inspector_assertions++;
				else if (assertion instanceof PrimitiveFieldAssertion)
					num_field_assertions++;
				else if (assertion instanceof PrimitiveAssertion)
					num_primitive_assertions++;
				else if (assertion instanceof EqualsAssertion)
					num_comparison_assertions++;
				else if (assertion instanceof CompareAssertion)
					num_comparison_assertions++;
				else if (assertion instanceof ExceptionAssertion)
					num_exception_assertions++;
				else if (assertion instanceof NullAssertion)
					num_null_assertions++;
				else
					logger.error("Found unknown assertion!");
			}
		} else {
			logger.info("Not removing assertions because no new assertions were found");
		}
		// TODO: List exception assertion
		logger.debug("Assertions after minimization: " + test.getAssertions().size()
		        + "," + num_string_assertions + "," + num_inspector_assertions + ","
		        + num_field_assertions + "," + num_comparison_assertions + ","
		        + num_primitive_assertions + "," + num_exception_assertions);
	}

	public void addAssertions(TestCase test, Mutation mutant) {
		ExecutionResult orig_result = runTest(test);
		ExecutionResult mutant_result = runTest(test, mutant);

		// orig_result.output_trace
		// .getAssertions(test, mutant_result.output_trace);
		orig_result.comparison_trace.getAssertions(test, mutant_result.comparison_trace);
		orig_result.primitive_trace.getAssertions(test, mutant_result.primitive_trace);
		orig_result.inspector_trace.getAssertions(test, mutant_result.inspector_trace);
		orig_result.field_trace.getAssertions(test, mutant_result.field_trace);
		orig_result.null_trace.getAssertions(test, mutant_result.null_trace);

		logger.debug("Generated " + test.getAssertions().size() + " assertions");
	}

	public void addAssertions(TestCase test, Set<Integer> killed) {
		addAssertions(test, killed, mutants);
	}

	public void addAssertions(TestCase test, Set<Integer> killed, List<Mutation> mutants) {

		logger.info("Generating assertions");

		@SuppressWarnings("unused")
		int ALL_assertions = 0;
		@SuppressWarnings("unused")
		int MIN_assertions = 0;

		assertion_statistics_full.put(test, new HashMap<Class<?>, Integer>());

		assertion_statistics_killed.put(test, new HashMap<Class<?>, Integer>());
		assertion_statistics_killed.get(test).put(StringAssertion.class, 0);
		assertion_statistics_killed.get(test).put(CompareAssertion.class, 0);
		assertion_statistics_killed.get(test).put(EqualsAssertion.class, 0);
		assertion_statistics_killed.get(test).put(InspectorAssertion.class, 0);
		assertion_statistics_killed.get(test).put(PrimitiveFieldAssertion.class, 0);
		assertion_statistics_killed.get(test).put(PrimitiveAssertion.class, 0);
		assertion_statistics_killed.get(test).put(NullAssertion.class, 0);

		assertion_statistics_min.put(test, new HashMap<Class<?>, Integer>());
		assertion_statistics_min.get(test).put(StringAssertion.class, 0);
		assertion_statistics_min.get(test).put(CompareAssertion.class, 0);
		assertion_statistics_min.get(test).put(EqualsAssertion.class, 0);
		assertion_statistics_min.get(test).put(InspectorAssertion.class, 0);
		assertion_statistics_min.get(test).put(PrimitiveFieldAssertion.class, 0);
		assertion_statistics_min.get(test).put(PrimitiveAssertion.class, 0);
		assertion_statistics_min.get(test).put(NullAssertion.class, 0);

		// MutationStatistics statistics = MutationStatistics.getInstance();
		int s1 = killed.size();
		// Get additional traces from observers directly:
		// Primitive trace - trace primitive values
		// equals trace - call equals on values of identical classes
		// compareto trace - call compareto on values of identical classes

		logger.debug("Running on original");
		ExecutionResult orig_result = runTest(test);

		Map<Mutation, List<OutputTrace>> mutation_traces = new HashMap<Mutation, List<OutputTrace>>();

		// TODO: We can do this much nicer
		// Run test case on all mutations
		for (Mutation m : mutants) {
			logger.debug("Running on mutation " + m.getId());
			// logger.info(m.toString());

			// logger.info("Cloning test: " + test.toCode());
			ExecutionResult mutant_result = runTest(test.clone(), m);
			// logger.info("Clone: " + mutant_result.test.toCode());
			List<OutputTrace> traces = new ArrayList<OutputTrace>();
			traces.add(mutant_result.comparison_trace);
			traces.add(mutant_result.primitive_trace);
			traces.add(mutant_result.inspector_trace);
			traces.add(mutant_result.field_trace);
			// traces.add(mutant_result.output_trace);
			traces.add(mutant_result.null_trace);
			// traces.add(mutant_exception_trace); // TODO!
			mutation_traces.put(m, traces);

			int num_killed = 0;

			int last_num = 0;
			// num_killed += orig_result.output_trace.getAssertions(test,
			// mutant_result.output_trace);
			// logger.info("String: Potentially "+orig_result.output_trace.numDiffer(mutant_result.output_trace));
			assertion_statistics_full.get(test).put(StringAssertion.class,
			                                        num_killed - last_num);
			if (num_killed > last_num) {
				logger.debug("Added " + num_killed + " string assertions for mutation: "
				        + m.getId());
				last_num = num_killed;
			}
			// logger.info("Comparison: Potentially "+orig_result.comparison_trace.numDiffer(mutant_result.comparison_trace));
			num_killed += orig_result.comparison_trace.getAssertions(test,
			                                                         mutant_result.comparison_trace);
			assertion_statistics_full.get(test).put(CompareAssertion.class,
			                                        num_killed - last_num);
			if (num_killed > last_num) {
				logger.debug("Added " + num_killed
				        + " comparison assertions for mutation: " + m.getId());
				last_num = num_killed;
			} else {
				// logger.info("Could add "+orig_result.comparison_trace.numDiffer(mutant_result.comparison_trace)+" comparison assertions for mutation: "+m.getId());
			}
			// logger.info("Primitive: Potentially "+orig_result.primitive_trace.numDiffer(mutant_result.primitive_trace));
			num_killed += orig_result.primitive_trace.getAssertions(test,
			                                                        mutant_result.primitive_trace);
			assertion_statistics_full.get(test).put(PrimitiveAssertion.class,
			                                        num_killed - last_num);
			if (num_killed > last_num) {
				logger.debug("Added " + num_killed
				        + " primitive assertions for mutation: " + m.getId());
				last_num = num_killed;
			}
			// logger.info("Inspector: Potentially "+orig_result.inspector_trace.numDiffer(mutant_result.inspector_trace));
			num_killed += orig_result.inspector_trace.getAssertions(test,
			                                                        mutant_result.inspector_trace);
			assertion_statistics_full.get(test).put(InspectorAssertion.class,
			                                        num_killed - last_num);
			if (num_killed > last_num) {
				logger.debug("Added " + num_killed
				        + " inspector assertions for mutation: " + m.getId());
				last_num = num_killed;
			} else {
				// logger.info("Could add "+orig_result.inspector_trace.numDiffer(mutant_result.inspector_trace)+" inspector assertions for mutation: "+m.getId());
			}
			// logger.info("String: Potentially "+orig_result.output_trace.numDiffer(mutant_result.output_trace));
			num_killed += orig_result.field_trace.getAssertions(test,
			                                                    mutant_result.field_trace);
			assertion_statistics_full.get(test).put(PrimitiveFieldAssertion.class,
			                                        num_killed - last_num);
			if (num_killed > last_num) {
				logger.debug("Added " + num_killed + " field assertions for mutation: "
				        + m.getId());
				last_num = num_killed;
			}

			num_killed += orig_result.null_trace.getAssertions(test,
			                                                   mutant_result.null_trace);
			assertion_statistics_full.get(test).put(NullAssertion.class,
			                                        num_killed - last_num);
			if (num_killed > last_num) {
				logger.debug("Added " + num_killed + " field assertions for mutation: "
				        + m.getId());
				last_num = num_killed;
			}

			if (num_killed > 0) {
				killed.add(m.getId());

				// logger.info("Setting mutant asserted "+m.getId());
				// statistics.setAsserted(m);
			}
		}

		Set<Integer> killed_before = new HashSet<Integer>();
		List<Assertion> assertions = test.getAssertions();
		assertion_statistics_full.get(test).put(StringAssertion.class, assertions.size());
		logger.info("Got " + assertions.size() + " assertions");
		Map<Integer, Set<Integer>> kill_map = new HashMap<Integer, Set<Integer>>();
		int num = 0;
		for (Assertion assertion : assertions) {
			Set<Integer> killed_mutations = new HashSet<Integer>();
			for (Mutation m : mutants) {

				boolean is_killed = false;
				for (OutputTrace trace : mutation_traces.get(m)) {
					is_killed = trace.isDetectedBy(assertion);
					if (is_killed) {
						killed_ALL.add(m.getId());
						killed_before.add(m.getId());
						break;
					}
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
		//minimize(test, mutants, assertions, kill_map);

		Set<Integer> killed_after = new HashSet<Integer>();
		assertions = test.getAssertions();
		for (Assertion assertion : assertions) {
			assertion_statistics_min.get(test).put(assertion.getClass(),
			                                       assertion_statistics_min.get(test).get(assertion.getClass()) + 1);

			for (Mutation m : mutants) {

				boolean is_killed = false;
				for (OutputTrace trace : mutation_traces.get(m)) {
					is_killed = trace.isDetectedBy(assertion);
					if (is_killed) {
						assertion_statistics_killed.get(test).put(assertion.getClass(),
						                                          assertion_statistics_killed.get(test).get(assertion.getClass()) + 1);
						killed_after.add(m.getId());
						break;
					}
				}
			}
		}
		int s2 = killed.size() - s1;
		logger.debug("Mutants killed before / after / should be: " + killed_before.size()
		        + "/" + killed_after.size() + "/" + s2);

		assertion_statistics_min.get(test).put(StringAssertion.class, assertions.size());

	}

	public boolean isKilled(Mutation mutation, TestCase test) {

		return false;
	}

	public void writeStatistics() {
		try {
			File f = new File(Properties.REPORT_DIR + "/statistics_assertions.csv");
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			if (f.length() == 0L) {
				out.write("Class,String,Comparison,Inspector,Primitive,Field,Null,MinString,MinComparison,MinInspector,MinPrimitive,MinField,MinNull,KilledString,KilledComparison,KilledInspector,KilledPrimitive,KilledField,KilledNull\n");
			}

			for (TestCase test : assertion_statistics_full.keySet()) {
				out.write(Properties.TARGET_CLASS + ",");
				out.write(assertion_statistics_full.get(test).get(StringAssertion.class)
				        + ",");
				out.write(assertion_statistics_full.get(test).get(CompareAssertion.class)
				        + ",");
				out.write(assertion_statistics_full.get(test).get(InspectorAssertion.class)
				        + ",");
				out.write(assertion_statistics_full.get(test).get(PrimitiveAssertion.class)
				        + ",");
				out.write(assertion_statistics_full.get(test).get(PrimitiveFieldAssertion.class)
				        + ",");
				out.write(assertion_statistics_full.get(test).get(NullAssertion.class)
				        + ",");

				out.write(assertion_statistics_min.get(test).get(StringAssertion.class)
				        + ",");
				out.write((assertion_statistics_min.get(test).get(CompareAssertion.class) + assertion_statistics_min.get(test).get(EqualsAssertion.class))
				        + ",");
				out.write(assertion_statistics_min.get(test).get(InspectorAssertion.class)
				        + ",");
				out.write(assertion_statistics_min.get(test).get(PrimitiveAssertion.class)
				        + ",");
				out.write(assertion_statistics_min.get(test).get(PrimitiveFieldAssertion.class)
				        + ",");
				out.write(assertion_statistics_min.get(test).get(NullAssertion.class)
				        + ",");

				out.write(assertion_statistics_killed.get(test).get(StringAssertion.class)
				        + ",");
				out.write((assertion_statistics_killed.get(test).get(CompareAssertion.class) + assertion_statistics_killed.get(test).get(EqualsAssertion.class))
				        + ",");
				out.write(assertion_statistics_killed.get(test).get(InspectorAssertion.class)
				        + ",");
				out.write(assertion_statistics_killed.get(test).get(PrimitiveAssertion.class)
				        + ",");
				out.write(assertion_statistics_killed.get(test).get(PrimitiveFieldAssertion.class)
				        + ",");
				out.write(assertion_statistics_killed.get(test).get(NullAssertion.class)
				        + ",");

				out.write(killed_ALL.size() + ","); // number of mutants killed
				                                    // out of target mutants
			}
			out.close();
			f = new File(Properties.REPORT_DIR + "/statistics_mutation.csv");
			out = new BufferedWriter(new FileWriter(f, true));
			if (f.length() == 0L) {
				out.write("Class,Total,NonVRO,VRO,Killed,KilledVRO,Score,ScoreVRO\n");
			}
			int num = numMutants();
			out.write(Properties.TARGET_CLASS + ",");
			out.write(mutants.size() + ","); // number of mutants killed out of
			                                 // target mutants
			out.write(num + ","); // number of mutants killed out of target
			                      // mutants
			out.write((mutants.size() - num) + ","); // number of mutants killed
			                                         // out of target mutants
			out.write(killed_ALL.size() + ","); // number of mutants killed out
			                                    // of target mutants
			if (mutants.size() > 0)
				out.write((1.0 * killed_ALL.size() / num) + ","); // number of
				                                                  // mutants
				                                                  // killed out
				                                                  // of VRO
				                                                  // mutants
			else
				out.write("1.0,");
			out.close();

		} catch (IOException e) {

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
