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
package de.unisb.cs.st.evosuite.testsuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.LocalSearchBudget;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteChromosome extends AbstractTestSuiteChromosome<TestChromosome> {

	/** Secondary objectives used during ranking */
	private static final List<SecondaryObjective> secondaryObjectives = new ArrayList<SecondaryObjective>();

	public TestSuiteChromosome(ChromosomeFactory<TestChromosome> testChromosomeFactory) {
		super(testChromosomeFactory);
	}

	protected TestSuiteChromosome(TestSuiteChromosome source) {
		super(source);
	}

	private static final long serialVersionUID = 88380759969800800L;

	public void addTest(TestCase test) {
		TestChromosome c = new TestChromosome();
		c.setTestCase(test);
		addTest(c);
	}

	/**
	 * Create a deep copy of this test suite
	 */
	@Override
	public TestSuiteChromosome clone() {
		return new TestSuiteChromosome(this);
	}

	/**
	 * Apply mutation on test suite level
	 */
	@Override
	public void mutate() {
		for (int i = 0; i < Properties.NUMBER_OF_MUTATIONS; i++) {
			super.mutate();
		}
		handleTestCallStatements();
	}

	protected void handleTestCallStatements() {
		Iterator<TestChromosome> it = tests.iterator();
		Iterator<Boolean> uit = unmodifiableTests.iterator();

		int num = 0;
		while (it.hasNext()) {
			ExecutableChromosome t = it.next();
			uit.next();
			if (t.size() == 0) {
				it.remove();
				uit.remove();
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

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#localSearch()
	 */
	@Override
	public void localSearch(LocalSearchObjective objective) {

		ensureDoubleExecution();
		LocalSearchBudget.localSearchStarted();

		double fitnessBefore = getFitness();
		for (int i = 0; i < tests.size(); i++) {
			if (unmodifiableTests.get(i))
				continue;
			TestSuiteLocalSearchObjective testObjective = new TestSuiteLocalSearchObjective(
			        (TestSuiteFitnessFunction) objective.getFitnessFunction(), this, i);
			if (LocalSearchBudget.isFinished()) {
				logger.debug("Local search budget used up");
				break;
			}
			logger.debug("Local search budget not yet used up");

			tests.get(i).localSearch(testObjective);
		}

		assert (fitnessBefore >= getFitness());
	}

	/**
	 * Ensure that all branches are executed twice
	 */
	private void ensureDoubleExecution() {

		Set<TestChromosome> duplicates = new HashSet<TestChromosome>();

		Map<Integer, Integer> covered = new HashMap<Integer, Integer>();
		Map<Integer, TestChromosome> testMap = new HashMap<Integer, TestChromosome>();
		for (TestChromosome test : getTestChromosomes()) {
			for (Entry<Integer, Integer> entry : test.getLastExecutionResult().getTrace().covered_predicates.entrySet()) {
				if (!covered.containsKey(entry.getKey())) {
					covered.put(entry.getKey(), 0);
				}
				covered.put(entry.getKey(),
				            covered.get(entry.getKey()) + entry.getValue());
				testMap.put(entry.getKey(), test);
			}
		}

		for (Integer branchId : covered.keySet()) {
			int count = covered.get(branchId);
			if (count == 1) {
				duplicates.add((TestChromosome) testMap.get(branchId).clone());
			}
		}

		if (!duplicates.isEmpty()) {
			logger.info("Adding " + duplicates.size()
			        + " tests to cover branches sufficiently");
			for (TestChromosome test : duplicates) {
				addTest(test);
			}
		}
	}

	/**
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

	public List<TestCase> getTests() {
		List<TestCase> testcases = new ArrayList<TestCase>();
		for (TestChromosome test : tests) {
			testcases.add(test.getTestCase());
		}
		return testcases;
	}

	@Override
	public void applyDSE() {
		TestSuiteDSE dse = new TestSuiteDSE();
		dse.applyDSE(this);
	}

	public Set<TestFitnessFunction> getCoveredGoals() {
		Set<TestFitnessFunction> goals = new HashSet<TestFitnessFunction>();
		for (TestChromosome test : tests) {
			goals.addAll(test.getTestCase().getCoveredGoals());
		}
		return goals;
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
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#compareSecondaryObjective(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
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
	 */
	public static void addSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.add(objective);
	}

	/**
	 * Remove secondary objective from list, if it is there
	 * 
	 * @param objective
	 */
	public static void removeSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.remove(objective);
	}
}
