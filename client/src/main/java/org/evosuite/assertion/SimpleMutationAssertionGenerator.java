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
package org.evosuite.assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMutationAssertionGenerator extends MutationAssertionGenerator {

	private final static Logger logger = LoggerFactory.getLogger(SimpleMutationAssertionGenerator.class);

	@Override
	public void addAssertions(TestSuiteChromosome suite) {
		
		setupClassLoader(suite);
		
		Set<Integer> tkilled = new HashSet<>();
		int numTest = 0;
		for (TestCase test : suite.getTests()) {
			if (! TimeController.getInstance().isThereStillTimeInThisPhase()) {
				logger.info("Reached maximum time to generate assertions!");
				break;
			}
			// Set<Integer> killed = new HashSet<Integer>();
			addAssertions(test, tkilled);
			//progressMonitor.updateStatus((100 * numTest++) / tests.size());
			ClientState state = ClientState.ASSERTION_GENERATION;
			ClientStateInformation information = new ClientStateInformation(state);
			information.setProgress((100 * numTest++) / suite.size());
			ClientServices.getInstance().getClientNode().changeState(state, information);
		}	
		
		calculateMutationScore(tkilled);
		restoreCriterion(suite);
	}

	
	/**
	 * Generate assertions to kill all the mutants defined in the pool
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param killed
	 *            a {@link java.util.Set} object.
	 */
	private void addAssertions(TestCase test, Set<Integer> killed) {
		addAssertions(test, killed, mutants);
		filterRedundantNonnullAssertions(test);
	}

	/**
	 * Add assertions to current test set for given set of mutants
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param killed
	 *            a {@link java.util.Set} object.
	 * @param mutants
	 *            a {@link java.util.Map} object.
	 */
	private void addAssertions(TestCase test, Set<Integer> killed,
	        Map<Integer, Mutation> mutants) {

		if (test.isEmpty())
			return;

		logger.debug("Generating assertions");
		int s1 = killed.size();

		logger.debug("Running on original");
		ExecutionResult origResult = runTest(test);

		if (origResult.hasTimeout() || origResult.hasTestException()) {
			logger.debug("Skipping test, as it has timeouts or exceptions");
			return;
		}

		List<Mutation> executedMutants = new ArrayList<Mutation>();
		for (Integer mutationId : origResult.getTrace().getTouchedMutants()) {
			if (mutants.containsKey(mutationId)) {
                executedMutants.add(mutants.get(mutationId));
            }
		}

		Randomness.shuffle(executedMutants);
		logger.debug("Executed mutants: "+origResult.getTrace().getTouchedMutants());

        // Store all assertions in case we need to add some non-killing ones later
        List<Assertion> allAssertions = getAllAssertions(origResult);
        test.removeAssertions(); // But now start with a test without assertions

        // Generate traces from execution on selected mutants
        Map<Mutation, List<OutputTrace<?>>> mutationTraces = executeOnMutantsAndAddAssertions(executedMutants, origResult, killed);

        // All assertions that kill mutants
		List<Assertion> mutationAssertions = test.getAssertions();
		logger.info("Got " + mutationAssertions.size() + " assertions");

        // Map all assertions to the mutants they kill in preparation for minimisation
		Map<Integer, Set<Integer>> killMap = getKillMap(mutationAssertions, executedMutants, mutationTraces);

		int killedBefore = getNumKilledMutants(test, mutationTraces, executedMutants);
		logger.debug("Need to kill mutants: " + killedBefore);

        // Do the actual minimisation of assertions
		minimize(test, executedMutants, mutationAssertions, killMap);

		int killedAfter = getNumKilledMutants(test, mutationTraces, executedMutants);

		int s2 = killed.size() - s1;
		assert (killedBefore == killedAfter) : "Mutants killed before / after / should be: "
		        + killedBefore + "/" + killedAfter + "/" + s2 + ": " + test.toCode();
		logger.info("Mutants killed before / after / should be: " + killedBefore + "/"
		        + killedAfter + "/" + s2);

		logger.info("Assertions in total    : " + allAssertions.size());
        logger.info("Assertions killing mutants : " + mutationAssertions.size());
        logger.info("Assertions in this test: " + test.getAssertions().size());

        // We always want assertions at the end, even if they don't help killing mutants
        ensureAssertionsOnLastStatement(test, origResult, mutationAssertions);
        ensureAssertionsOnLastStatement(test, origResult, allAssertions);

        // Remove inspectors that do the same as the last method statement
        filterInspectorPrimitiveDuplication(test.getStatement(test.size() - 1));

        // We always want at least one assertion on non-failing tests
        assert(!origResult.noThrownExceptions() || !test.getStatement(test.size() - 1).getAssertions().isEmpty()) : "No assertion in test: "+test.toCode();
	}

    private List<Assertion> getAllAssertions(ExecutionResult result) {
        TestCase test = result.test;
        for (OutputTrace<?> trace : result.getTraces()) {
            trace.getAllAssertions(test);
        }
        return test.getAssertions();
    }

    private Map<Mutation, List<OutputTrace<?>>> executeOnMutantsAndAddAssertions(List<Mutation> executedMutants, ExecutionResult origResult, Set<Integer> killed) {
        int numExecutedMutants = 0;
        TestCase test = origResult.test;
        Map<Mutation, List<OutputTrace<?>>> mutationTraces = new HashMap<Mutation, List<OutputTrace<?>>>();

        for (Mutation m : executedMutants) {

            numExecutedMutants++;
            if (! TimeController.getInstance().isThereStillTimeInThisPhase()) {
                logger.info("Reached maximum time to generate assertions!");
                break;
            }

            assert (m != null);
            if(MutationTimeoutStoppingCondition.isDisabled(m)) {
                killed.add(m.getId());
                continue;
            }
            if (timedOutMutations.containsKey(m)) {
                if (timedOutMutations.get(m) >= Properties.MUTATION_TIMEOUTS) {
                    logger.debug("Skipping timed out mutant");
                    killed.add(m.getId());
                    continue;
                }
            }
            if (exceptionMutations.containsKey(m)) {
                if (exceptionMutations.get(m) >= Properties.MUTATION_TIMEOUTS) {
                    logger.debug("Skipping mutant with exceptions");
                    killed.add(m.getId());
                    continue;
                }
            }
            if (Properties.MAX_MUTANTS_PER_TEST > 0
                    && numExecutedMutants > Properties.MAX_MUTANTS_PER_TEST)
                break;

			/*
			if (killed.contains(m.getId())) {
				logger.info("Skipping dead mutant");
				continue;
			}
			*/

            logger.debug("Running test on mutation {}",  m.getMutationName());
            ExecutionResult mutantResult = runTest(test, m);

            int numKilled = 0;
            for (Class<?> observerClass : observerClasses) {
                if (mutantResult.getTrace(observerClass) == null
                        || origResult.getTrace(observerClass) == null)
                    continue;
                numKilled += origResult.getTrace(observerClass).getAssertions(test,
                        mutantResult.getTrace(observerClass));
            }

            List<OutputTrace<?>> traces = new ArrayList<OutputTrace<?>>(
                    mutantResult.getTraces());
            mutationTraces.put(m, traces);

            if (mutantResult.hasTimeout()) {
                logger.debug("Increasing timeout count!");
                if (!timedOutMutations.containsKey(m)) {
                    timedOutMutations.put(m, 1);
                } else {
                    timedOutMutations.put(m, timedOutMutations.get(m) + 1);
                }
                MutationTimeoutStoppingCondition.timeOut(m);

            } else if (!mutantResult.noThrownExceptions()
                    && origResult.noThrownExceptions()) {
                logger.debug("Increasing exception count.");
                if (!exceptionMutations.containsKey(m)) {
                    exceptionMutations.put(m, 1);
                } else {
                    exceptionMutations.put(m, exceptionMutations.get(m) + 1);
                }
                MutationTimeoutStoppingCondition.raisedException(m);
            }

            if (numKilled > 0
                    || mutantResult.hasTimeout()
                    || (!mutantResult.noThrownExceptions() && origResult.noThrownExceptions())) {
                killed.add(m.getId());
            }
        }
        return mutationTraces;
    }

    private Map<Integer, Set<Integer>> getKillMap(List<Assertion> mutationAssertions, List<Mutation> executedMutants, Map<Mutation, List<OutputTrace<?>>> mutationTraces) {
        Map<Integer, Set<Integer>> killMap = new HashMap<Integer, Set<Integer>>();
        int num = 0;
        for (Assertion assertion : mutationAssertions) {
            Set<Integer> killedMutations = new HashSet<Integer>();
            for (Mutation m : executedMutants) {

                boolean isKilled = false;
                if (mutationTraces.containsKey(m)) {
                    for (OutputTrace<?> trace : mutationTraces.get(m)) {
                        if (trace.isDetectedBy(assertion)) {
                            isKilled = true;
                            break;
                        }
                    }
                }
                if (isKilled) {
                    killedMutations.add(m.getId());
                    assertion.addKilledMutation(m);
                }
            }
            killMap.put(num, killedMutations);
            //logger.info("Assertion " + num + " kills mutants " + killedMutations);
            num++;
        }
        return killMap;
    }

    private void ensureAssertionsOnLastStatement(TestCase test, ExecutionResult origResult, List<Assertion> assertions) {
        // No need to add assertion of the test ends with an exception
        if(!origResult.noThrownExceptions()) {
            if (!test.getStatement(test.size() - 1).getAssertions().isEmpty()) {
                logger.debug("Removing assertions after exception");
                test.getStatement(test.size() - 1).removeAssertions();
            }
            return;
        }

        // If the last statement returns something non-void, attempt to assert on it
        Statement lastStatement = test.getStatement(test.size() - 1);
        if(!lastStatement.getReturnValue().isVoid()) {
            boolean hasAssertion = hasAssertionOnReturnValue(lastStatement);
            if(!hasAssertion) { // Assert return value directly
                logger.debug("Trying to add assertion to last return value");
                hasAssertion = attemptToAddAnyNonNullAssertionToReturnValue(lastStatement, assertions);
            }
            if(!hasAssertion) { // Allow null assertion on return value
                logger.debug("Trying to add assertion to last return value, allowing null");
                attemptToAddAnyAssertionToReturnValue(lastStatement, assertions);
            }
        }

        // If we do not have an assertion at the end, select one
        boolean hasAssertion = !lastStatement.getAssertions().isEmpty();
        if(!hasAssertion) { // Any non-null assertion at the end would be good at this point
            logger.debug("Trying to add assertion to last statement");
            hasAssertion = attemptToAddAnyNonNullAssertionToStatement(lastStatement, assertions);
        }
        if(!hasAssertion) { // Any assertion at the end would be good at this point
            logger.debug("Trying to add assertion to last statement, allowing null");
            attemptToAddAnyAssertionToStatement(lastStatement, assertions);
        }
    }

    private boolean attemptToAddAnyNonNullAssertionToReturnValue(Statement statement, List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            if (assertion instanceof NullAssertion)
                continue;

            if (assertion.getStatement().equals(statement) && assertion.getSource().equals(statement.getReturnValue())) {
                logger.info("Adding an assertion: " + assertion);
                statement.addAssertion(assertion);
                return true;
            }
        }
        return false;
    }

    private boolean attemptToAddAnyAssertionToReturnValue(Statement statement, List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            logger.debug("Considering "+assertion+": "+(assertion.getStatement().equals(statement)) +", "+(assertion.getSource().equals(statement.getReturnValue())));
            if (assertion.getStatement().equals(statement) && assertion.getSource().equals(statement.getReturnValue())) {
                logger.info("Adding an assertion: " + assertion);
                statement.addAssertion(assertion);
                return true;
            }
        }
        return false;
    }

    private boolean attemptToAddAnyNonNullAssertionToStatement(Statement statement, List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            if (assertion instanceof NullAssertion)
                continue;

            if (assertion.getStatement().equals(statement)) {
                logger.info("Adding an assertion: " + assertion);
                statement.addAssertion(assertion);
                return true;
            }
        }
        return false;
    }

    private boolean attemptToAddAnyAssertionToStatement(Statement statement, List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            if (assertion.getStatement().equals(statement)) {
                logger.info("Adding an assertion: " + assertion);
                statement.addAssertion(assertion);
                return true;
            }
        }
        return false;
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
	 * @param killMap
	 *            Mapping of assertion to mutant ids that are killed by the
	 *            assertion
	 */
	private void minimize(TestCase test, List<Mutation> mutants,
	        final List<Assertion> assertions, Map<Integer, Set<Integer>> killMap) {

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
				if (num_killed.equals(other.num_killed)) {
					Assertion first = assertions.get(assertion);
					Assertion second = assertions.get(other.assertion);
					if (first instanceof PrimitiveAssertion) {
						return 1;
					} else if (second instanceof PrimitiveAssertion) {
						return -1;
					} else {
						return assertion.compareTo(other.assertion);
					}
				}
				// return assertion.compareTo(other.assertion);
				//				return other.assertion.compareTo(assertion);
				else
					return num_killed.compareTo(other.num_killed);
			}
		}
		Set<Integer> to_kill = new HashSet<Integer>();
		for (Entry<Integer, Set<Integer>> entry : killMap.entrySet()) {
			to_kill.addAll(entry.getValue());
		}
		logger.debug("Need to kill mutants: " + to_kill.size());

		Set<Integer> killed = new HashSet<Integer>();
		Set<Assertion> result = new HashSet<Assertion>();

		boolean done = false;
		while (!done) {
			// logger.info("Have to kill "+to_kill.size());
			List<Pair> a = new ArrayList<Pair>();
			for (Entry<Integer, Set<Integer>> entry : killMap.entrySet()) {
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
				for (Integer m : killMap.get(best.assertion)) {
					// logger.info("Killed "+m);
					killed.add(m);
					to_kill.remove(m);
				}
			}
		}
		logger.debug("Killed mutants: " + killed.size());

		// sort by number of assertions killed
		// pick assertion that kills most
		// remove all mutations that are already killed
		logger.debug("Minimized assertions from " + assertions.size() + " to "
		        + result.size());

		if (!result.isEmpty()) {
			test.removeAssertions();

			for (Assertion assertion : result) {
				assertion.getStatement().addAssertion(assertion);
			}
		} else {
			logger.debug("Not removing assertions because no new assertions were found");
		}

	}
}
