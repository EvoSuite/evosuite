package org.evosuite.assertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.StructuredTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;

public class StructuredAssertionGenerator extends AssertionGenerator {

	private static PrimitiveTraceObserver primitiveObserver = new PrimitiveTraceObserver();
	private static ComparisonTraceObserver comparisonObserver = new ComparisonTraceObserver();
	private static SameTraceObserver sameObserver = new SameTraceObserver();
	private static InspectorTraceObserver inspectorObserver = new InspectorTraceObserver();
	private static PrimitiveFieldTraceObserver fieldObserver = new PrimitiveFieldTraceObserver();
	private static NullTraceObserver nullObserver = new NullTraceObserver();

	private final static Map<Mutation, Integer> timedOutMutations = new HashMap<Mutation, Integer>();

	/** Constant <code>observerClasses</code> */
	protected static Class<?>[] observerClasses = { PrimitiveTraceEntry.class,
	        ComparisonTraceEntry.class, SameTraceEntry.class, InspectorTraceEntry.class,
	        PrimitiveFieldTraceEntry.class, NullTraceEntry.class };

	/**
	 * Default constructor
	 */
	public StructuredAssertionGenerator() {
		TestCaseExecutor.getInstance().newObservers();
		TestCaseExecutor.getInstance().addObserver(primitiveObserver);
		TestCaseExecutor.getInstance().addObserver(comparisonObserver);
		TestCaseExecutor.getInstance().addObserver(sameObserver);
		TestCaseExecutor.getInstance().addObserver(inspectorObserver);
		TestCaseExecutor.getInstance().addObserver(fieldObserver);
		TestCaseExecutor.getInstance().addObserver(nullObserver);
	}

	@Override
	public void addAssertions(TestSuiteChromosome suite) {
		long startTime = System.currentTimeMillis();
		
		int numTest = 0;
		for (TestCase test : suite.getTests()) {
			long currentTime = System.currentTimeMillis() / 1000;
			if (currentTime - startTime > Properties.ASSERTION_TIMEOUT) {
				logger.info("Reached maximum time to generate assertions!");
				break;
			}
			// Set<Integer> killed = new HashSet<Integer>();
			addAssertions(test);
			ClientState state = ClientState.ASSERTION_GENERATION;
			ClientStateInformation information = new ClientStateInformation(
			        state);
			information.setProgress((100 * numTest++) / suite.size());
			ClientServices.getInstance().getClientNode().changeState(state,
			                                                         information);

			// progressMonitor.updateStatus((100 * numTest++) / tests.size());
			// tkilled.addAll(killed);
		}	
	}
	
	@Override
	public void addAssertions(TestCase test) {
		if (!(test instanceof StructuredTestCase))
			throw new IllegalArgumentException("Expecting StructuredTestCase");

		StructuredTestCase structuredTest = (StructuredTestCase) test;

		Set<String> targetMethods = structuredTest.getTargetMethods();

		List<Mutation> mutants = MutationPool.getMutants();

		ExecutionResult origResult = runTest(test);
		Map<Mutation, ExecutionResult> mutationResults = new HashMap<Mutation, ExecutionResult>();

		// execute on all mutants in the target method that were touched
		for (Mutation mutant : mutants) {
			if (!origResult.getTrace().wasMutationTouched(mutant.getId()))
				continue;
			if (!targetMethods.contains(mutant.getMethodName())) {
				continue;
			}

			ExecutionResult mutationResult = runTest(test, mutant);
			mutationResults.put(mutant, mutationResult);
		}

		addAssertions(structuredTest, origResult, mutationResults);
	}

	private void minimizeAssertions(StructuredTestCase test, ExecutionResult origResult,
	        Map<Mutation, ExecutionResult> mutationResults) {
		Set<Integer> killedMutants = new HashSet<Integer>();

		for (int position = test.size() - 1; position >= test.getFirstExerciseStatement(); position--) {
			StatementInterface statement = test.getStatement(position);
			if (!statement.hasAssertions())
				continue;

			List<Assertion> assertions = new ArrayList<Assertion>(
			        statement.getAssertions());
			Map<Integer, Set<Integer>> killMap = getKillMap(assertions, mutationResults);
			int num = 0;

			// This is to make sure we prefer assertions on return values.
			// TODO: Refactor
			for (Assertion assertion : assertions) {
				if (assertion instanceof PrimitiveAssertion) {
					boolean killsNew = false;
					for (Integer mutationId : killMap.get(num)) {
						if (!killedMutants.contains(mutationId)) {
							killsNew = true;
							break;
						}
					}
					if (!killsNew) {
						statement.removeAssertion(assertion);
					} else {
						killedMutants.addAll(killMap.get(num));
					}

				}
				num++;
			}

			for (int i = 0; i < assertions.size(); i++) {
				if (!killMap.containsKey(i)) {
					statement.removeAssertion(assertions.get(i));
					continue;
				}
				Assertion assertion = assertions.get(i);
				boolean killsNew = false;
				for (Integer mutationId : killMap.get(i)) {
					if (!killedMutants.contains(mutationId)) {
						killsNew = true;
						break;
					}
				}
				if (!killsNew) {
					statement.removeAssertion(assertion);
				} else {
					killedMutants.addAll(killMap.get(i));
				}
			}

			// If we have no assertions, then add...something?
			if (!statement.hasAssertions()) {
				boolean addedPrimitive = false;
				for (Assertion assertion : assertions) {
					if (assertion instanceof PrimitiveAssertion) {
						statement.addAssertion(assertion);
						addedPrimitive = true;
					}
				}
				if (!addedPrimitive)
					statement.addAssertion(Randomness.choice(assertions));
			}
		}
	}

	private Map<Integer, Set<Integer>> getKillMap(List<Assertion> assertions,
	        Map<Mutation, ExecutionResult> mutationResults) {
		Map<Integer, Set<Integer>> killMap = new HashMap<Integer, Set<Integer>>();

		int num = 0;
		for (Assertion assertion : assertions) {
			Set<Integer> killedMutations = new HashSet<Integer>();
			for (Mutation m : mutationResults.keySet()) {

				boolean isKilled = false;
				for (OutputTrace<?> trace : mutationResults.get(m).getTraces()) {
					if (trace.isDetectedBy(assertion)) {
						isKilled = true;
						break;
					}
				}
				if (isKilled) {
					killedMutations.add(m.getId());
					assertion.addKilledMutation(m);
				}
			}
			killMap.put(num, killedMutations);
			num++;
		}

		return killMap;
	}

	/**
	 * Add all assertions to the test case
	 * 
	 * @param test
	 * @param origResult
	 * @param mutantResult
	 * @return
	 */
	private int addAssertions(StructuredTestCase test, ExecutionResult origResult,
	        Map<Mutation, ExecutionResult> mutationResults) {
		int numKilled = 0;

		for (Class<?> observerClass : observerClasses) {
			if (origResult.getTrace(observerClass) == null)
				continue;

			for (int i = 0; i < test.size(); i++) {
				if (test.isExerciseStatement(i))
					origResult.getTrace(observerClass).getAllAssertions(test, i);
			}
		}

		minimizeAssertions(test, origResult, mutationResults);

		return numKilled;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Execute a test case on the original unit
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
		comparisonObserver.clear();
		sameObserver.clear();
		primitiveObserver.clear();
		inspectorObserver.clear();
		fieldObserver.clear();
		nullObserver.clear();
		try {
			logger.debug("Executing test");
			MutationObserver.activateMutation(mutant);
			result = TestCaseExecutor.getInstance().execute(test);
			MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);

			result.setTrace(comparisonObserver.getTrace(), ComparisonTraceEntry.class);
			result.setTrace(sameObserver.getTrace(), SameTraceEntry.class);
			result.setTrace(primitiveObserver.getTrace(), PrimitiveTraceEntry.class);
			result.setTrace(inspectorObserver.getTrace(), InspectorTraceEntry.class);
			result.setTrace(fieldObserver.getTrace(), PrimitiveFieldTraceEntry.class);
			result.setTrace(nullObserver.getTrace(), NullTraceEntry.class);

		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

}
