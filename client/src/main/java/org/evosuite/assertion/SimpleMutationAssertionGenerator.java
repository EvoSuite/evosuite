/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class SimpleMutationAssertionGenerator extends MutationAssertionGenerator {

    private final static Logger logger = LoggerFactory.getLogger(SimpleMutationAssertionGenerator.class);


    @Override
    public void addAssertions(TestSuiteChromosome suite) {

        setupClassLoader(suite);

        if (!Properties.hasTargetClassBeenLoaded()) {
            // Need to load class explicitly since it was re-instrumented
            Properties.getTargetClassAndDontInitialise();
            if (!Properties.hasTargetClassBeenLoaded()) {
                logger.warn("Could not initialize SUT before Assertion generation");
            }
        }

        Set<Integer> tkilled = new HashSet<>();
        int numTest = 0;
        boolean timeIsShort = false;

        for (TestCase test : suite.getTests()) {
            if (!TimeController.getInstance().isThereStillTimeInThisPhase()) {
                logger.warn("Reached maximum time to generate assertions, aborting assertion generation");
                break;
            }

            // If at 50% of the time we have only done X% of the tests, then don't minimise
            if (!timeIsShort && TimeController.getInstance().getPhasePercentage() > Properties.ASSERTION_MINIMIZATION_FALLBACK_TIME) {
                if (numTest < Properties.ASSERTION_MINIMIZATION_FALLBACK * suite.size()) {
                    logger.warn("Assertion minimization is taking too long ({}% of time used, but only {}/{} tests minimized), falling back to using all assertions", TimeController.getInstance().getPhasePercentage(), numTest, suite.size());
                    timeIsShort = true;
                }
            }

            if (timeIsShort) {
                CompleteAssertionGenerator generator = new CompleteAssertionGenerator();
                generator.addAssertions(test);
                numTest++;
            } else {
                // Set<Integer> killed = new HashSet<Integer>();
                addAssertions(test, tkilled);
                //progressMonitor.updateStatus((100 * numTest++) / tests.size());
                ClientState state = ClientState.ASSERTION_GENERATION;
                ClientStateInformation information = new ClientStateInformation(state);
                information.setProgress((100 * numTest++) / suite.size());
                ClientServices.getInstance().getClientNode().changeState(state, information);
            }
        }

        calculateMutationScore(tkilled);
        restoreCriterion(suite);
    }


    /**
     * Generate assertions to kill all the mutants defined in the pool
     *
     * @param test   a {@link org.evosuite.testcase.TestCase} object.
     * @param killed a {@link java.util.Set} object.
     */
    private void addAssertions(TestCase test, Set<Integer> killed) {
        addAssertions(test, killed, mutants);
        filterRedundantNonnullAssertions(test);
    }

    /**
     * Add assertions to current test set for given set of mutants
     *
     * @param test    a {@link org.evosuite.testcase.TestCase} object.
     * @param killed  a {@link java.util.Set} object.
     * @param mutants a {@link java.util.Map} object.
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

        Map<Mutation, List<OutputTrace<?>>> mutationTraces = new HashMap<>();
        List<Mutation> executedMutants = new ArrayList<>();

        for (Integer mutationId : origResult.getTrace().getTouchedMutants()) {
            if (!mutants.containsKey(mutationId)) {
                //logger.warn("Mutation ID unknown: " + mutationId);
                //logger.warn(mutants.keySet().toString());
            } else
                executedMutants.add(mutants.get(mutationId));
        }

        Randomness.shuffle(executedMutants);
        logger.debug("Executed mutants: " + origResult.getTrace().getTouchedMutants());

        int numExecutedMutants = 0;
        for (Mutation m : executedMutants) {

            numExecutedMutants++;
            if (!TimeController.getInstance().isThereStillTimeInThisPhase()) {
                logger.info("Reached maximum time to generate assertions!");
                break;
            }

            assert (m != null);
            if (MutationTimeoutStoppingCondition.isDisabled(m)) {
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

            logger.debug("Running test on mutation {}", m.getMutationName());
            ExecutionResult mutantResult = runTest(test, m);

            int numKilled = 0;
            for (Class<?> observerClass : observerClasses) {
                if (mutantResult.getTrace(observerClass) == null
                        || origResult.getTrace(observerClass) == null)
                    continue;
                numKilled += origResult.getTrace(observerClass).getAssertions(test,
                        mutantResult.getTrace(observerClass));
            }

            List<OutputTrace<?>> traces = new ArrayList<>(
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

        List<Assertion> assertions = test.getAssertions();
        logger.info("Got " + assertions.size() + " assertions");
        Map<Integer, Set<Integer>> killMap = new HashMap<>();
        int num = 0;
        for (Assertion assertion : assertions) {
            Set<Integer> killedMutations = new HashSet<>();
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

        int killedBefore = getNumKilledMutants(test, mutationTraces, executedMutants);

        logger.debug("Need to kill mutants: " + killedBefore);
        logger.debug(killMap.toString());
        minimize(test, executedMutants, assertions, killMap);

        int killedAfter = getNumKilledMutants(test, mutationTraces, executedMutants);

        int s2 = killed.size() - s1;
        assert (killedBefore == killedAfter) : "Mutants killed before / after / should be: "
                + killedBefore + "/" + killedAfter + "/" + s2 + ": " + test.toCode();
        logger.info("Mutants killed before / after / should be: " + killedBefore + "/"
                + killedAfter + "/" + s2);

        logger.info("Assertions in this test: " + test.getAssertions().size());
        //TestCase clone = test.clone();

        if (primitiveWithoutAssertion(test.getStatement(test.size() - 1))) {
            logger.info("Last statement has primitive return value but no assertions: " + test.toCode());
            for (Assertion assertion : assertions) {
                if (assertion instanceof PrimitiveAssertion) {
                    if (assertion.getStatement().equals(test.getStatement(test.size() - 1))) {
                        logger.debug("Adding a primitive assertion " + assertion);
                        test.getStatement(test.size() - 1).addAssertion(assertion);
                        break;
                    }
                }
            }
            filterInspectorPrimitiveDuplication(test.getStatement(test.size() - 1));
        }

        // IF there are no mutant killing assertions on the last statement, still assert something
        if (test.getStatement(test.size() - 1).getAssertions().isEmpty()
                || justNullAssertion(test.getStatement(test.size() - 1))) {
            logger.info("Last statement has no assertions: " + test.toCode());
            logger.info("Assertions to choose from: " + assertions.size());

            if (test.getStatement(test.size() - 1).getAssertions().isEmpty()) {
                logger.debug("Last statement: "
                        + test.getStatement(test.size() - 1).getCode());
            }
            if (origResult.isThereAnExceptionAtPosition(test.size() - 1))
                logger.debug("Exception on last statement!");

            if (justNullAssertion(test.getStatement(test.size() - 1)))
                logger.debug("Just null assertions on last statement: " + test.toCode());

            boolean haveAssertion = false;
            for (Assertion assertion : assertions) {
                if (assertion instanceof PrimitiveAssertion) {
                    if (assertion.getStatement().equals(test.getStatement(test.size() - 1))) {
                        logger.debug("Adding a primitive assertion " + assertion);
                        test.getStatement(test.size() - 1).addAssertion(assertion);
                        haveAssertion = true;
                        break;
                    }
                }
            }
            if (!haveAssertion) {
                logger.info("Could not find a primitive assertion, continuing search");

                for (Assertion assertion : assertions) {
                    if (assertion instanceof NullAssertion)
                        continue;

                    if (assertion.getStatement().equals(test.getStatement(test.size() - 1))) {
                        logger.info("Adding an assertion: " + assertion);
                        test.getStatement(test.size() - 1).addAssertion(assertion);
                        haveAssertion = true;
                        break;
                    }
                }
            }

            //if (!test.hasAssertions()) {
            if (!haveAssertion) {
                logger.info("After second round we still have no assertion");
                Method inspectorMethod = null;
                if (test.getStatement(test.size() - 1) instanceof MethodStatement) {
                    MethodStatement methodStatement = (MethodStatement) test.getStatement(test.size() - 1);
                    Method method = methodStatement.getMethod().getMethod();
                    if (method.getParameterTypes().length == 0) {
                        if (method.getReturnType().isPrimitive()
                                && !method.getReturnType().equals(void.class)) {
                            inspectorMethod = method;
                        }
                    }
                }
                for (OutputTrace<?> trace : origResult.getTraces()) {
                    trace.getAllAssertions(test);
                }

                Set<Assertion> target = new HashSet<>(
                        test.getStatement(test.size() - 1).getAssertions());
                logger.debug("Found assertions: " + target.size());

                test.removeAssertions();
                //test.addAssertions(clone);
                VariableReference targetVar = test.getStatement(test.size() - 1).getReturnValue();
                if (!targetVar.isVoid()) {
                    logger.debug("Return value is non void: " + targetVar.getClassName());

                    int maxAssertions = 1;
                    int numAssertions = 0;
                    for (Assertion ass : target) {
                        if (ass.getReferencedVariables().contains(targetVar)
                                && !(ass instanceof NullAssertion)) {

                            if (ass instanceof InspectorAssertion) {
                                if (((InspectorAssertion) ass).inspector.getMethod().equals(inspectorMethod)) {
                                    continue;
                                }
                            }

                            test.getStatement(test.size() - 1).addAssertion(ass);
                            logger.debug("Adding assertion " + ass.getCode());
                            if (++numAssertions >= maxAssertions)
                                break;
                        } else {
                            logger.debug("Assertion does not contain target: "
                                    + ass.getCode());
                        }
                    }
                    if (numAssertions == 0) {
                        for (Assertion ass : target) {
                            if (ass.getReferencedVariables().contains(targetVar)) {

                                test.getStatement(test.size() - 1).addAssertion(ass);
                                logger.debug("Adding assertion " + ass.getCode());
                                if (++numAssertions >= maxAssertions)
                                    break;
                            } else {
                                logger.debug("Assertion does not contain target: "
                                        + ass.getCode());
                            }
                        }
                    }
                } else {
                    logger.debug("Return value is void");

                    Set<VariableReference> targetVars = test.getStatement(test.size() - 1).getVariableReferences();
                    int maxAssertions = 1;
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
                logger.info("1. Done with assertions");

            }
            logger.info("2. Done with assertions");
            filterInspectorPrimitiveDuplication(test.getStatement(test.size() - 1));
        }

        if (!origResult.noThrownExceptions()) {
            if (!test.getStatement(test.size() - 1).getAssertions().isEmpty()) {
                logger.debug("Removing assertions after exception");
                test.getStatement(test.size() - 1).removeAssertions();
            }
        }

    }

    /**
     * Return a minimal subset of the assertions that covers all killable
     * mutants
     *
     * @param test       The test case that should be executed
     * @param mutants    The list of mutants of the unit
     * @param assertions All assertions that can be generated for the test case
     * @param killMap    Mapping of assertion to mutant ids that are killed by the
     *                   assertion
     */
    private void minimize(TestCase test, List<Mutation> mutants,
                          final List<Assertion> assertions, Map<Integer, Set<Integer>> killMap) {

        class Pair implements Comparable<Object> {
            final Integer assertion;
            final Integer num_killed;

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
        Set<Integer> to_kill = new HashSet<>();
        for (Entry<Integer, Set<Integer>> entry : killMap.entrySet()) {
            to_kill.addAll(entry.getValue());
        }
        logger.debug("Need to kill mutants: " + to_kill.size());

        Set<Integer> killed = new HashSet<>();
        Set<Assertion> result = new HashSet<>();

        boolean done = false;
        while (!done) {
            // logger.info("Have to kill "+to_kill.size());
            List<Pair> a = new ArrayList<>();
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
