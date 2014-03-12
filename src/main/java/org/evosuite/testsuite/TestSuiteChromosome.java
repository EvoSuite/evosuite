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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.localsearch.LocalSearchObjective;
import org.evosuite.localsearch.TestSuiteLocalSearch;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

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

	private static final long serialVersionUID = 88380759969800800L;

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

	
	public void clearMutationHistory() {
		for(TestChromosome test : tests) {
			test.getMutationHistory().clear();
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
	 * {@inheritDoc}
	 * 
	 * Create a deep copy of this test suite
	 */
	@Override
	public TestSuiteChromosome clone() {
		return new TestSuiteChromosome(this);
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
	
	public boolean isUnmodifiable(TestChromosome test) {
		return unmodifiableTests.contains(test);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean localSearch(LocalSearchObjective<? extends Chromosome> objective) {
		TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.getLocalSearch();
		return localSearch.doSearch(this, (LocalSearchObjective<TestSuiteChromosome>) objective);
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
		int i = 0;
		for (TestChromosome test : tests) {
			result += "Test "+i+": \n";
			i++;
			result += test.getTestCase().toCode() + "\n";
		}
		return result;
	}
}
