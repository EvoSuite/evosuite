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
package org.evosuite.testsuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.DSEBudgetType;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.LocalSearchBudget;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.TestMutationHistoryEntry;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.Randomness;

/**
 * <p>
 * TestSuiteChromosome class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestSuiteChromosome extends AbstractTestSuiteChromosome<TestChromosome> {

	/** Secondary objectives used during ranking */
	private static final List<SecondaryObjective> secondaryObjectives = new ArrayList<SecondaryObjective>();

	/**
	 * <p>
	 * Constructor for TestSuiteChromosome.
	 * </p>
	 */
	public TestSuiteChromosome() {
		super();
	}

	/**
	 * <p>
	 * Constructor for TestSuiteChromosome.
	 * </p>
	 * 
	 * @param testChromosomeFactory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public TestSuiteChromosome(ChromosomeFactory<TestChromosome> testChromosomeFactory) {
		super(testChromosomeFactory);
	}

	/**
	 * <p>
	 * Constructor for TestSuiteChromosome.
	 * </p>
	 * 
	 * @param source
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	protected TestSuiteChromosome(TestSuiteChromosome source) {
		super(source);
	}

	private static final long serialVersionUID = 88380759969800800L;

	/**
	 * {@inheritDoc}
	 * 
	 * Create a deep copy of this test suite
	 */
	@Override
	public TestSuiteChromosome clone() {
		return new TestSuiteChromosome(this);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Apply mutation on test suite level
	 */
	@Override
	public void mutate() {
		for (int i = 0; i < Properties.NUMBER_OF_MUTATIONS; i++) {
			super.mutate();
		}
		handleTestCallStatements();
	}

	/**
	 * <p>
	 * handleTestCallStatements
	 * </p>
	 */
	protected void handleTestCallStatements() {
		Iterator<TestChromosome> it = tests.iterator();

		int num = 0;
		while (it.hasNext()) {
			ExecutableChromosome t = it.next();
			if (t.size() == 0) {
				it.remove();
				unmodifiableTests.remove(t);
				for (TestChromosome test : tests) {
					for (StatementInterface s : test.getTestCase()) {
						if (s instanceof TestCallStatement) {
							TestCallStatement call = (TestCallStatement) s;
							if (call.getTestNum() > num) {
								call.setTestNum(call.getTestNum() - 1);
							}
						}
					}
				}
			} else {
				num++;
			}
		}
	}

	@Override
	public void localSearch(LocalSearchObjective<? extends Chromosome> objective) {
		/*
		 * When we apply local search, due to budget constraints we might not be able
		 * to evaluate all the test cases in a test suite. When we apply LS several times on
		 * same individual in different generations, to avoid having always the same test cases searched for and
		 * others skipped, then we shuffle the test cases, so each time the order is different 
		 */
		Randomness.shuffle(tests);

		ensureDoubleExecution((TestSuiteFitnessFunction) objective.getFitnessFunction());

		double fitnessBefore = getFitness();
		for (int i = 0; i < tests.size(); i++) {
			TestChromosome test = tests.get(i);
			if (unmodifiableTests.contains(test)) {
				continue;
			}

			logger.debug("Local search on test " + i);
			TestSuiteLocalSearchObjective testObjective = new TestSuiteLocalSearchObjective(
			        (TestSuiteFitnessFunction) objective.getFitnessFunction(), this, i);

			if (LocalSearchBudget.isFinished()) {
				logger.debug("Local search budget used up");
				break;
			}
			logger.debug("Local search budget not yet used up");

			test.localSearch(testObjective);
		}

		LocalSearchBudget.individualImproved(this);
		assert (fitnessBefore >= getFitness()); //FIXME doesn't it assume minimization?
	}

	@Override
	public void applyAdaptiveLocalSearch(LocalSearchObjective<? extends Chromosome> objective) {

		if (!hasFitnessChanged()) {
			logger.info("Fitness has not changed, so not applying local search");
			return;
		}
		logger.info("Fitness has changed, applying local search with fitness "
		        + getFitness());

		// Apply standard DSE on entire suite?
		if(Properties.ADAPTIVE_LOCAL_SEARCH_DSE) {
			boolean hasRelevantTests = false;
			for(TestChromosome test : tests) {
				if(test.hasRelevantMutations()) {
					hasRelevantTests = true;
					break;
				}
			}
			if(hasRelevantTests) {
				if(Randomness.nextDouble() < Properties.DSE_ADAPTIVE_PROBABILITY) {
					TestSuiteDSE dse = new TestSuiteDSE(
							(TestSuiteFitnessFunction) objective.getFitnessFunction());
					dse.applyDSE(this);
				}
			}
			return;
		}
		
		List<TestChromosome> candidates = new ArrayList<TestChromosome>();
		for(TestChromosome test : tests) {
			logger.info("Checking test with history entries: "+test.getMutationHistory().size()+": "+test.getMutationHistory());
			if(test.hasRelevantMutations()) {
				TestCaseExpander expander = new TestCaseExpander();
				TestChromosome clone = new TestChromosome();
				clone.setTestCase(expander.expandTestCase(test.getTestCase()));
				for (TestMutationHistoryEntry mutation : test.getMutationHistory()) {
					if(mutation.getMutationType() == TestMutationHistoryEntry.TestMutation.DELETION) {
						clone.getMutationHistory().addMutationEntry(mutation.clone(clone.getTestCase()));
					} else {
						StatementInterface s1 = mutation.getStatement();
						if(expander.variableMapping.containsKey(s1.getPosition())) {
							for(VariableReference var : expander.variableMapping.get(s1.getPosition())) {
								clone.getMutationHistory().addMutationEntry(new TestMutationHistoryEntry(mutation.getMutationType(), clone.getTestCase().getStatement(var.getStPosition())));
							}
						} else {
							clone.getMutationHistory().addMutationEntry(new TestMutationHistoryEntry(mutation.getMutationType(), clone.getTestCase().getStatement(s1.getPosition())));
						}
					}
				}
				logger.info("Mutation history before expansion: "+test.getMutationHistory().size()+", after: "+clone.getMutationHistory().size());
				candidates.add(clone);
			}
		}

		for(TestChromosome clone : candidates) {
		    double oldFitness = getFitness();
		    addTest(clone);
		    TestSuiteLocalSearchObjective testObjective = new TestSuiteLocalSearchObjective((TestSuiteFitnessFunction) objective.getFitnessFunction(), this, tests.size() - 1);
		    logger.info("Applying DSE to test: " + clone.getTestCase().toCode());
		    clone.applyAdaptiveLocalSearch(testObjective);
		    if(getFitness() >= oldFitness) {
		    	logger.info("Removing new test from suite again as local search was not successful");
		    	tests.remove(clone);
		    }
		    if(Properties.DSE_KEEP_ALL_TESTS) {
		    	logger.info("Adding partial solutions: "+testObjective.getPartialSolutions().size()+" at fitness "+getFitness());
		    	boolean added = false;
		    	for(TestChromosome partialSolution : testObjective.getPartialSolutions()) {
		    		addTest(partialSolution);
		    		added = true;
		    	}
		    	if(added) {
		    		// Recalculate test suite with new tests
		    		testObjective.getFitnessFunction().getFitness(this);
			    	logger.info("After adding partial solutions fitness = "+getFitness());
		    	}
		    }
		    
		    // copies.add(clone);
		}

		// apply local search to all tests where a mutation was applied
		/*
				int numTest = 0;
				for (TestChromosome test : tests) {
					if (LocalSearchBudget.isFinished()) {
						logger.debug("Local search budget used up");
						break;
					}

					TestSuiteLocalSearchObjective testObjective = new TestSuiteLocalSearchObjective(
					        (TestSuiteFitnessFunction) objective.getFitnessFunction(), this,
					        numTest);
					numTest++;

					logger.debug("Checking local search on individual test");
					test.hasRelevantMutations()
					test.applyAdaptiveLocalSearch(testObjective);
				}
		*/
	}

	/**
	 * Ensure that all branches are executed twice
	 */
	private void ensureDoubleExecution(TestSuiteFitnessFunction objective) {

		Set<TestChromosome> duplicates = new HashSet<TestChromosome>();

		Map<Integer, Integer> covered = new HashMap<Integer, Integer>();
		Map<Integer, TestChromosome> testMap = new HashMap<Integer, TestChromosome>();
		for (TestChromosome test : getTestChromosomes()) {

			// Make sure we have an execution result
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				ExecutionResult result = test.executeForFitnessFunction(objective);
				test.setLastExecutionResult(result); // .clone();
				test.setChanged(false);
			}

			for (Entry<Integer, Integer> entry : test.getLastExecutionResult().getTrace().getPredicateExecutionCount().entrySet()) {
				if (!covered.containsKey(entry.getKey())) {
					covered.put(entry.getKey(), 0);
				}
				covered.put(entry.getKey(),
				            covered.get(entry.getKey()) + entry.getValue());
				testMap.put(entry.getKey(), test);
			}
		}

		for(Entry<Integer, Integer> entry : covered.entrySet()) {
			int branchId = entry.getKey();
			int count = entry.getValue();
			if (count == 1) {
				TestChromosome duplicate = (TestChromosome) testMap.get(branchId).clone();
				ExecutionResult result = duplicate.executeForFitnessFunction(objective);
				duplicate.setLastExecutionResult(result); // .clone();
				duplicate.setChanged(false);
				duplicates.add(duplicate);
			}
		}

		if (!duplicates.isEmpty()) {
			logger.info("Adding " + duplicates.size()
			        + " tests to cover branches sufficiently");
			for (TestChromosome test : duplicates) {
				addTest(test);
			}
			setChanged(true);
			objective.getFitness(this);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Determine relative ordering of this chromosome to another chromosome If
	 * fitness is equal, the shorter chromosome comes first
	 */
	/*
	 * public int compareTo(Chromosome o) { if(RANK_LENGTH && getFitness() ==
	 * o.getFitness()) { return (int) Math.signum((length() -
	 * ((TestSuiteChromosome)o).length())); } else return (int)
	 * Math.signum(getFitness() - o.getFitness()); }
	 */
	@Override
	public String toString() {
		String result = "TestSuite: " + tests.size() + "\n";
		for (TestChromosome test : tests) {
			result += test.getTestCase().toCode() + "\n";
		}
		return result;
	}

	/**
	 * <p>
	 * getTests
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<TestCase> getTests() {
		List<TestCase> testcases = new ArrayList<TestCase>();
		for (TestChromosome test : tests) {
			testcases.add(test.getTestCase());
		}
		return testcases;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyDSE(GeneticAlgorithm<?> ga) {
		TestSuiteDSE dse = new TestSuiteDSE(
		        (TestSuiteFitnessFunction) ga.getFitnessFunction());
		return dse.applyDSE(this);
	}

	/**
	 * <p>
	 * getCoveredGoals
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<TestFitnessFunction> getCoveredGoals() {
		Set<TestFitnessFunction> goals = new HashSet<TestFitnessFunction>();
		for (TestChromosome test : tests) {
			goals.addAll(test.getTestCase().getCoveredGoals());
		}
		return goals;
	}

	/**
	 * Add a test to a test suite
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestChromosome addTest(TestCase test) {
		TestChromosome c = new TestChromosome();
		c.setTestCase(test);
		addTest(c);

		return c;
	}

	/**
	 * For manual algorithm
	 * 
	 * @param testCase
	 *            to remove
	 */
	public void deleteTest(TestCase testCase) {
		if (testCase != null) {
			for (int i = 0; i < tests.size(); i++) {
				if (tests.get(i).getTestCase().equals((testCase))) {
					tests.remove(i);
					unmodifiableTests.remove(i);
				}
			}
		}
	}

	/**
	 * Remove all tests
	 */
	public void clearTests() {
		tests.clear();
		unmodifiableTests.clear();
	}

	/**
	 * <p>
	 * restoreTests
	 * </p>
	 * 
	 * @param backup
	 *            a {@link java.util.ArrayList} object.
	 */
	public void restoreTests(ArrayList<TestCase> backup) {
		tests.clear();
		unmodifiableTests.clear();
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		for (TestCase testCase : backup) {
			addTest(testCase);
			executor.execute(testCase);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareSecondaryObjective(Chromosome o) {
		int objective = 0;
		int c = 0;

		while (c == 0 && objective < secondaryObjectives.size()) {
			SecondaryObjective so = secondaryObjectives.get(objective++);
			if (so == null)
				break;
			c = so.compareChromosomes(this, o);
		}
		//logger.debug("Comparison: " + fitness + "/" + size() + " vs " + o.fitness + "/"
		//        + o.size() + " = " + c);
		return c;
	}

	/**
	 * Add an additional secondary objective to the end of the list of
	 * objectives
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void addSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.add(objective);
	}

	/**
	 * Remove secondary objective from list, if it is there
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void removeSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.remove(objective);
	}
	
	public void clearMutationHistory() {
		for(TestChromosome test : tests) {
			test.getMutationHistory().clear();
		}
	}
}
